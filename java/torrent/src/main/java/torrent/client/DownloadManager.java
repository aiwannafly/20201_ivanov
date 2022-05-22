package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.handlers.DownloadPieceHandler;
import torrent.client.util.BitTorrentHandshake;
import torrent.client.util.ByteOperations;
import torrent.client.util.Handshake;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DownloadManager {
    private final Torrent torrentFile;
    private final int[] peerPorts;
    private final int peersCount;
    private final ExecutorService leechPool;
    private int workingPeersCount;
    private final ArrayList<Integer> leftPieces = new ArrayList<>();
    private final Map<Integer, InputStream> ins = new HashMap<>();
    private final Map<Integer, PrintWriter> outs = new HashMap<>();
    private final FileManager fileManager;
    private final String fileName;
    private int missRequestsCounter = 0;
    private final int MISS_LIMIT = 100;
    private final String peerId;

    public DownloadManager(Torrent torrentFile, FileManager
            fileManager, String peerId, int[] peerPorts) {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.peerPorts = peerPorts;
        this.peersCount = peerPorts.length;
        this.torrentFile = torrentFile;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        this.workingPeersCount = Math.min(peersCount, Constants.DOWNLOAD_MAX_THREADS_COUNT);
        this.leechPool = Executors.newFixedThreadPool(workingPeersCount);
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            leftPieces.add(i);
        }
        for (int i = 0; i < workingPeersCount; i++) {
            try {
                boolean established = establishConnection(peerPorts[i]);
                if (!established) {
                    System.err.println("=== Failed to establish connection with " + peerPorts[i]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void download() {
        Random random = new Random();
        int portIdx = 0;
        int piecesCount = torrentFile.getPieces().size();
        for (int i = 0; i < piecesCount; i++) {
            System.out.print(torrentFile.getPieces().get(i) + " ");
        }
        System.out.println();
        while (leftPieces.size() > 0) {
            int randomIdx = random.nextInt(leftPieces.size());
            int nextPieceIdx = leftPieces.remove(randomIdx);
            int pieceLength;
            if (nextPieceIdx == piecesCount - 1) {
                pieceLength = torrentFile.getTotalSize().intValue() % torrentFile.getPieceLength().intValue();
            } else {
                pieceLength = torrentFile.getPieceLength().intValue();
            }
            leechPool.execute(new DownloadPieceHandler(torrentFile, fileManager,
                    fileName, nextPieceIdx, pieceLength, outs.get(peerPorts[portIdx]), ins.get(peerPorts[portIdx])));
            portIdx++;
            if (portIdx == workingPeersCount) {
                portIdx = 0;
            }
        }
        // System.out.println("=== File " + fileName + " was downloaded successfully!");
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

    private boolean establishConnection(int peerPort) throws IOException {
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
            System.out.println("=== Successfully connected to " + peerPort);
            ins.put(peerPort, currentPeerChannel.socket().getInputStream());
            outs.put(peerPort, out);
            return true;
        } else {
            System.err.println("=== Handshakes are different, reject connection");
            return false;
        }
    }
}
