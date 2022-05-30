package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.exceptions.DifferentHandshakesException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.handlers.DownloadPieceHandler;
import torrent.client.util.BitTorrentHandshake;
import torrent.client.util.Handshake;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;

public class DownloadManager implements Callable<DownloadManager.Result> {
    private final Torrent torrentFile;
    private final int[] peerPorts;
    private final ExecutorService leechPool;
    private final CompletionService<DownloadPieceHandler.Result> service;
    private int workingPeersCount;
    private final ArrayList<Integer> leftPieces;
    private final Map<Integer, InputStream> ins = new HashMap<>();
    private final Map<Integer, PrintWriter> outs = new HashMap<>();
    private final Map<Integer, Long> lastKeepAliveTimes = new HashMap<>();
    private final Timer keepAliveSendTimer = new Timer();
    private final Timer keepAliveRecvTimer = new Timer();
    private final FileManager fileManager;
    private final String fileName;
    private final String peerId;
    private boolean stopped = false;
    private boolean submittedFirstTasks = false;
    private boolean closed = false;

    public enum Status {
        FINISHED, NOT_FINISHED
    }

    public static class Result {
        Status status;
        String torrentFileName;
    }

    public DownloadManager(Torrent torrentFile, FileManager
            fileManager, String peerId, int[] peerPorts, ArrayList<Integer> leftPieces) throws NoSeedsException {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.peerPorts = peerPorts;
        this.torrentFile = torrentFile;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        this.workingPeersCount = 0;
        this.leftPieces = leftPieces;
        for (int i = 0; i < peerPorts.length && i < Constants.DOWNLOAD_MAX_THREADS_COUNT; i++) {
            try {
                establishConnection(peerPorts[i]);
            } catch (DifferentHandshakesException e) {
                System.err.println("=== " + e.getMessage());
                continue;
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            this.workingPeersCount++;
        }
        if (0 == workingPeersCount) {
            throw new NoSeedsException("No seeds uploading file " + torrentFile.getName());
        }
        this.leechPool = Executors.newFixedThreadPool(Constants.DOWNLOAD_MAX_THREADS_COUNT);
        this.service = new ExecutorCompletionService<>(this.leechPool);
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            leftPieces.add(i);
        }
    }

    @Override
    public Result call() {
        TimerTask sendKeepALiveMsgs = new DownloadManager.SendKeepAliveTask();
        TimerTask recvKeepAliveMsgs = new DownloadManager.RecvKeepAliveTask();
        Result downloadResult = new Result();
        downloadResult.torrentFileName = torrentFile.getName() + Constants.POSTFIX;
        downloadResult.status = DownloadManager.Status.NOT_FINISHED;
        Random random = new Random();
        while (leftPieces.size() > 0) {
            if (stopped) {
                return downloadResult;
            }
            if (!submittedFirstTasks) {
                for (int portId = 0; portId < workingPeersCount; portId++) {
                    requestRandomPiece(random, portId);
                }
                keepAliveSendTimer.schedule(sendKeepALiveMsgs, 0, Constants.KEEP_ALIVE_SEND_INTERVAL);
                keepAliveRecvTimer.schedule(recvKeepAliveMsgs, 0, Constants.MAX_KEEP_ALIVE_INTERVAL);
            }
            submittedFirstTasks = true;
            try {
                Future<DownloadPieceHandler.Result> future = service.take();
                DownloadPieceHandler.Result result = future.get();
                int pieceIdx = result.pieceId;
                int portIdx = result.portIdx;
                if (result.receivedKeepAlive) {
                    lastKeepAliveTimes.replace(result.portIdx, result.keepAliveTimeMillis);
                }
                // System.out.println(result);
                if (result.status == DownloadPieceHandler.Status.LOST) {
                    // System.out.println("=== Failed to receive a piece " + (pieceIdx + 1));
                    requestPiece(pieceIdx, portIdx);
                    // System.out.println("=== Requested " + (pieceIdx + 1) + " again");
                } else {
                    System.out.println("=== Received piece            " + (pieceIdx + 1));
                    if (leftPieces.size() == 0) {
                        break;
                    }
                    requestRandomPiece(random, portIdx);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        shutdown();
        System.out.println("=== File " + fileName + " was downloaded successfully!");
        closed = true;
        downloadResult.status = DownloadManager.Status.FINISHED;
        return downloadResult;
    }

    public void stop() {
        stopped = true;
    }

    public void resume() {
        stopped = false;
    }

    public void shutdown() {
        if (closed) {
            return;
        }
        keepAliveSendTimer.cancel();
        keepAliveRecvTimer.cancel();
        leechPool.shutdown();
        try {
            boolean completed = leechPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("=== Execution was not completed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < workingPeersCount; i++) {
            try {
                if (outs.get(peerPorts[i]) != null) {
                    outs.get(peerPorts[i]).close();
                }
                if (ins.get(peerPorts[i]) != null) {
                    ins.get(peerPorts[i]).close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestRandomPiece(Random random, int portIdx) {
        int randomIdx = random.nextInt(leftPieces.size());
        int nextPieceIdx = leftPieces.remove(randomIdx);
        requestPiece(nextPieceIdx, portIdx);
    }

    private void requestPiece(int pieceIdx, int portIdx) {
        int pieceLength = getPieceLength(pieceIdx);
        service.submit(new DownloadPieceHandler(torrentFile, fileManager,
                fileName, portIdx, pieceIdx, pieceLength,
                outs.get(peerPorts[portIdx]), ins.get(peerPorts[portIdx])));
    }

    private int getPieceLength(int pieceIdx) {
        int pieceLength;
        if (pieceIdx == torrentFile.getPieces().size() - 1) {
            pieceLength = torrentFile.getTotalSize().intValue() % torrentFile.getPieceLength().intValue();
        } else {
            pieceLength = torrentFile.getPieceLength().intValue();
        }
        return pieceLength;
    }

    private void establishConnection(int peerPort) throws IOException, DifferentHandshakesException {
        SocketChannel currentPeerChannel = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress("localhost", peerPort);
        currentPeerChannel.connect(address);
        currentPeerChannel.configureBlocking(true);
        Handshake myHandshake = new BitTorrentHandshake(torrentFile.getInfo_hash(), peerId);
        PrintWriter out = new PrintWriter(currentPeerChannel.socket().getOutputStream(), true);
        out.print(myHandshake.getMessage());
        out.flush();
        ByteBuffer buf = ByteBuffer.allocate(myHandshake.getMessage().length());
        currentPeerChannel.read(buf);
        Handshake peerHandshake = new BitTorrentHandshake(new String(buf.array()));
        if (myHandshake.getInfoHash().equals(peerHandshake.getInfoHash())) {
            ins.put(peerPort, currentPeerChannel.socket().getInputStream());
            outs.put(peerPort, out);
            lastKeepAliveTimes.put(peerPort, System.currentTimeMillis());
        } else {
            throw new DifferentHandshakesException("Handshakes are different, reject connection");
        }
    }

    private class SendKeepAliveTask extends TimerTask {
        @Override
        public void run() {
//            System.out.println("Send to ...");
            for (PrintWriter out: outs.values()) {
                System.out.println("=== Send keep-alive");
                String keepAliveMsg = "\0\0\0\0";
                out.print(keepAliveMsg);
                out.flush();
            }
        }
    }

    private class RecvKeepAliveTask extends TimerTask {
        @Override
        public void run() {
//            System.out.println("Check from ...");
            for (Integer peerPort: lastKeepAliveTimes.keySet()) {
//                System.out.println("Someone");
                if (getTimeFromLastKeepAlive(peerPort) > Constants.MAX_KEEP_ALIVE_INTERVAL) {
                    System.out.println("=== Close connection");
                    // close connection
                    try {
                        ins.remove(peerPort).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outs.remove(peerPort).close();
                    lastKeepAliveTimes.remove(peerPort);
                }
            }
        }
    }

    private long getTimeFromLastKeepAlive(Integer peerPort) {
        if (0 == lastKeepAliveTimes.get(peerPort)) {
            return 0;
        }
        return System.currentTimeMillis() - lastKeepAliveTimes.get(peerPort);
    }
}
