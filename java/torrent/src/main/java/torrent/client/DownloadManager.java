package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.exceptions.DifferentHandshakesException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.handlers.DownloadPieceHandler;
import torrent.client.util.BitTorrentHandshake;
import torrent.client.util.ByteOperations;
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
    private final int[] peerPorts;
    private final ExecutorService leechPool;
    private final CompletionService<DownloadPieceHandler.Result> service;
    private int workingPeersCount;
    private final ArrayList<Integer> leftPieces = new ArrayList<>();
    private final Map<Integer, InputStream> ins = new HashMap<>();
    private final Map<Integer, PrintWriter> outs = new HashMap<>();
    private final FileManager fileManager;
    private final String fileName;
    private final String peerId;

    public DownloadManager(Torrent torrentFile, FileManager
            fileManager, String peerId, int[] peerPorts) throws NoSeedsException {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.peerPorts = peerPorts;
        this.torrentFile = torrentFile;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        this.workingPeersCount = 0;
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
        this.leechPool = Executors.newFixedThreadPool(workingPeersCount);
        this.service = new ExecutorCompletionService<>(this.leechPool);
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            leftPieces.add(i);
        }
    }

    public void download() {
        Random random = new Random();
        boolean submittedFirstTasks = false;
        while (leftPieces.size() > 0) {
            if (!submittedFirstTasks) {
                for (int portId = 0; portId < workingPeersCount; portId++) {
                    requestRandomPiece(random, portId);
                }
            }
            submittedFirstTasks = true;
            try {
                Future<DownloadPieceHandler.Result> future = service.take();
                DownloadPieceHandler.Result result = future.get();
                int pieceIdx = result.pieceId;
                int portIdx = result.seedId;
                // System.out.println(result);
                if (result.status == DownloadPieceHandler.DownloadStatus.LOST) {
                    // System.out.println("=== Failed to receive a piece " + (pieceIdx + 1));
                    int pieceLength = getPieceLength(pieceIdx);
                    service.submit(new DownloadPieceHandler(torrentFile, fileManager,
                            fileName, portIdx, pieceIdx, pieceLength, outs.get(peerPorts[portIdx]),
                            ins.get(peerPorts[portIdx])));
                    // System.out.println("=== Requested " + (pieceIdx + 1) + " again");
                } else {
                    System.out.println("=== Received piece            " + (pieceIdx + 1));
                    requestRandomPiece(random, portIdx);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
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
        int pieceLength = getPieceLength(nextPieceIdx);
        // System.out.println("=== Requested                 " + (nextPieceIdx + 1));
        service.submit(new DownloadPieceHandler(torrentFile, fileManager,
                fileName, portIdx, nextPieceIdx, pieceLength, outs.get(peerPorts[portIdx]), ins.get(peerPorts[portIdx])));
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
        } else {
            throw new DifferentHandshakesException("Handshakes are different, reject connection");
        }
    }
}
