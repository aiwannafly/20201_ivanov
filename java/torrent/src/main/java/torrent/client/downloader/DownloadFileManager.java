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

public class DownloadFileManager {
    private final Torrent torrentFile;
    private final ExecutorService leechPool;
    private final CompletionService<DownloadPieceTask.Result> service;
    private final ArrayList<Integer> leftPieces;
    private final Map<Integer, SeedInfo> seedsInfo = new HashMap<>();
    private final FileManager fileManager;
    private final String fileName;
    private final String peerId;
    private boolean submittedFirstTasks = false;
    private boolean closed = false;
    private KeepAliveHandler keepAliveHandler;
    private final Map<Integer, ArrayList<Integer>> peersPieces;

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

    public DownloadFileManager(Torrent torrentFile, FileManager
            fileManager, String peerId, Map<Integer, ArrayList<Integer>> peersPieces,
                               ExecutorService leechPool) throws NoSeedsException {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.torrentFile = torrentFile;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        this.leftPieces = new ArrayList<>();
        this.peersPieces = peersPieces;
        for (Integer peerPort: peersPieces.keySet()) {
            if (seedsInfo.size() >= Constants.DOWNLOAD_MAX_THREADS_COUNT) {
                break;
            }
            for (Integer piece: peersPieces.get(peerPort)) {
                System.out.print(piece + " ");
            }
            System.out.println();
            try {
                establishConnection(peerPort);
            } catch (DifferentHandshakesException e) {
                System.err.println("=== " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (seedsInfo.isEmpty()) {
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
        downloadResult.status = DownloadFileManager.Status.NOT_FINISHED;
        Random random = new Random();
        if (leftPieces.size() == 0) {
            System.out.println("=== File " + fileName + " was downloaded successfully!");
            shutdown();
            closed = true;
            downloadResult.status = DownloadFileManager.Status.FINISHED;
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
            downloadResult.status = DownloadFileManager.Status.FINISHED;
            return downloadResult;
        }
        try {
            Future<DownloadPieceTask.Result> future = service.take();
            DownloadPieceTask.Result result = future.get();
            int pieceIdx = result.pieceId;
            int peerPort = result.peerPort;
            if (!seedsInfo.containsKey(peerPort)) {
                if (result.status == DownloadPieceTask.Status.LOST) {
                    leftPieces.add(pieceIdx);
                }
            } else {
                if (result.receivedKeepAlive) {
                    seedsInfo.get(peerPort).lastKeepAliveTimeMillis = result.keepAliveTimeMillis;
                }
                if (result.status == DownloadPieceTask.Status.LOST) {
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
        downloadResult.status = DownloadFileManager.Status.NOT_FINISHED;
        return downloadResult;
    }

    public Torrent getTorrentFile() {
        return torrentFile;
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
        ArrayList<Integer> availablePieces = peersPieces.get(peerPort);
        ArrayList<Integer> interestingPieces = new ArrayList<>();
        for (Integer piece: availablePieces) {
            if (leftPieces.contains(piece)) {
                interestingPieces.add(piece);
            }
        }
        if (interestingPieces.size() == 0) {
            System.err.println("=== Nothing to ask");
            return;
        }
        int randomIdx = random.nextInt(interestingPieces.size());
        int nextPieceIdx = interestingPieces.get(randomIdx);
        for (int i = 0; i < leftPieces.size(); i++) {
            if (leftPieces.get(i) == nextPieceIdx) {
                leftPieces.remove(i);
                break;
            }
        }
        requestPiece(nextPieceIdx, peerPort);
    }

    private void requestPiece(int pieceIdx, int peerPort) {
        int pieceLength = getPieceLength(pieceIdx);
        service.submit(new DownloadPieceTask(torrentFile, fileManager,
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
