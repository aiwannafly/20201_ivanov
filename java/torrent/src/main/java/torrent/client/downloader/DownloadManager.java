package torrent.client.downloader;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.FileManager;
import torrent.client.exceptions.DifferentHandshakesException;
import torrent.client.exceptions.NoSeedsException;
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

public class DownloadManager {
    private final Torrent torrentFile;
    private final ExecutorService leechPool;
    private final CompletionService<DownloadPieceHandler.Result> service;
    private final ArrayList<Integer> leftPieces;
    private final Map<Integer, SeedInfo> seedsInfo = new HashMap<>();
    private final FileManager fileManager;
    private final String fileName;
    private final String peerId;
    private boolean stopped = false;
    private boolean submittedFirstTasks = false;
    private boolean closed = false;
    private KeepAliveHandler keepAliveHandler;

    public enum Status {
        FINISHED, NOT_FINISHED
    }

    public static class Result {
        Status status;
        public String torrentFileName;
    }

    static class SeedInfo {
        InputStream in;
        PrintWriter out;
        Long lastKeepAliveTimeMillis;
    }

    public DownloadManager(Torrent torrentFile, FileManager
            fileManager, String peerId, int[] peerPorts, ArrayList<Integer> leftPieces,
                           ExecutorService leechPool) throws NoSeedsException {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.torrentFile = torrentFile;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        int workingPeersCount = 0;
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
            workingPeersCount++;
        }
        if (0 == workingPeersCount) {
            throw new NoSeedsException("No seeds uploading file " + torrentFile.getName());
        }
        this.leechPool = leechPool;
        this.service = new ExecutorCompletionService<>(this.leechPool);
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            leftPieces.add(i);
        }
    }

    public Result downloadNextPiece() {
        if (!submittedFirstTasks) {
            keepAliveHandler = new KeepAliveHandler(seedsInfo);
            keepAliveHandler.start();
        }
        Result downloadResult = new Result();
        downloadResult.torrentFileName = torrentFile.getName() + Constants.POSTFIX;
        downloadResult.status = DownloadManager.Status.NOT_FINISHED;
        Random random = new Random();
        if (leftPieces.size() == 0) {
            System.out.println("=== File " + fileName + " was downloaded successfully!");
            shutdown();
            closed = true;
            downloadResult.status = DownloadManager.Status.FINISHED;
            return downloadResult;
        }
        if (stopped) {
            return downloadResult;
        }
        if (!submittedFirstTasks) {
            for (Integer peerPort : seedsInfo.keySet()) {
                requestRandomPiece(random, peerPort);
            }
        }
        submittedFirstTasks = true;
        if (seedsInfo.size() == 0) {
            System.err.println("=== No seeds left. Downloading failed");
            shutdown();
            closed = true;
            downloadResult.status = DownloadManager.Status.FINISHED;
            return downloadResult;
        }
        try {
            Future<DownloadPieceHandler.Result> future = service.take();
            DownloadPieceHandler.Result result = future.get();
            int pieceIdx = result.pieceId;
            int peerPort = result.peerPort;
            if (!seedsInfo.containsKey(peerPort)) {
                if (result.status == DownloadPieceHandler.Status.LOST) {
                    leftPieces.add(pieceIdx);
                }
            } else {
                if (result.receivedKeepAlive) {
                    seedsInfo.get(peerPort).lastKeepAliveTimeMillis = result.keepAliveTimeMillis;
                }
                if (result.status == DownloadPieceHandler.Status.LOST) {
                    leftPieces.add(pieceIdx);
                } else {
                    System.out.println("=== Received piece            " + (pieceIdx + 1));
                }
                if (leftPieces.size() > 0) {
                    requestRandomPiece(random, peerPort);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        downloadResult.status = DownloadManager.Status.NOT_FINISHED;
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
        keepAliveHandler.stop();
        leechPool.shutdown();
        try {
            boolean completed = leechPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("=== Execution was not completed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Integer peerPort : seedsInfo.keySet()) {
            try {
                if (seedsInfo.get(peerPort).out != null) {
                    seedsInfo.get(peerPort).out.close();
                }
                if (seedsInfo.get(peerPort).in != null) {
                    seedsInfo.get(peerPort).in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestRandomPiece(Random random, int peerPort) {
        int randomIdx = random.nextInt(leftPieces.size());
        int nextPieceIdx = leftPieces.remove(randomIdx);
        requestPiece(nextPieceIdx, peerPort);
    }

    private void requestPiece(int pieceIdx, int peerPort) {
        int pieceLength = getPieceLength(pieceIdx);
        service.submit(new DownloadPieceHandler(torrentFile, fileManager,
                fileName, peerPort, pieceIdx, pieceLength,
                seedsInfo.get(peerPort).out, seedsInfo.get(peerPort).in));
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
            SeedInfo seedInfo = new SeedInfo();
            seedInfo.in = currentPeerChannel.socket().getInputStream();
            seedInfo.out = out;
            seedInfo.lastKeepAliveTimeMillis = System.currentTimeMillis();
            seedsInfo.put(peerPort, seedInfo);
        } else {
            throw new DifferentHandshakesException("Handshakes are different, reject connection");
        }
    }
}
