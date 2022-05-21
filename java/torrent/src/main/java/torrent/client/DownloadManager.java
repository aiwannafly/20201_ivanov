package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
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
    private final BitTorrentClient torrentClient;
    private final FileManager fileManager;
    private final String fileName;
    private int missRequestsCounter = 0;
    private final int MISS_LIMIT = 100;

    public DownloadManager(BitTorrentClient torrentClient, FileManager
            fileManager, Torrent torrentFile, int[] peerPorts) {
        this.torrentClient = torrentClient;
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
        while (leftPieces.size() > 0) {
            int randomIdx = random.nextInt(leftPieces.size());
            int nextPieceIdx = leftPieces.remove(randomIdx);
            int pieceLength;
            if (nextPieceIdx == piecesCount - 1) {
                pieceLength = torrentFile.getTotalSize().intValue() % torrentFile.getPieceLength().intValue();
            } else {
                pieceLength = torrentFile.getPieceLength().intValue();
            }
            leechPool.execute(() -> {
                requestPiece(nextPieceIdx, 0, pieceLength, outs.get(peerPorts[portIdx]));
                // System.out.println("Requested");
                boolean received = receivePiece(ins.get(peerPorts[portIdx]));
                if (!received) {
                    System.err.println("=== Failed to receive a piece " + (nextPieceIdx + 1));
                    missRequestsCounter++;
                    if (missRequestsCounter >= MISS_LIMIT) {
                        System.err.println("=== Failed to download");
                        return;
                    }
                    leftPieces.add(nextPieceIdx);
                } else {
                    System.out.println("=== Received piece " + (nextPieceIdx + 1));
                }
            });
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
        Handshake myHandshake = new BitTorrentHandshake(torrentClient.getHandShakeMessage());
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

    private void requestPiece(int index, int begin, int length, PrintWriter out) {
        String message = ByteOperations.convertIntoBytes(13) + "6" +
                ByteOperations.convertIntoBytes(index) + ByteOperations.convertIntoBytes(begin) +
                ByteOperations.convertIntoBytes(length);
        out.print(message);
        out.flush();
    }

    private boolean receivePiece(InputStream in) {
        StringBuilder messageBuilder = new StringBuilder();
        try {
            for (int i = 0; i < 4; i++) {
                messageBuilder.append((char) in.read());
            }
            int messageLength = ByteOperations.convertFromBytes(messageBuilder.toString());
            // System.out.println("Message length: " + messageLength);
            for (int i = 0; i < messageLength; i++) {
                messageBuilder.append((char) in.read());
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        String message = messageBuilder.toString();
        if (message.length() < 4 + 1 + 4 + 4) {
            return false;
        }
        // piece: <len=0009+X><id=7><index><begin><block>
        int len = ByteOperations.convertFromBytes(message.substring(0, 4));
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        // System.out.println("len: " + len);
        // System.out.println("id: " + id);
        if (id != Constants.PIECE_ID) {
            return false;
        }
        int idx = ByteOperations.convertFromBytes(message.substring(5, 9));
        int begin = ByteOperations.convertFromBytes(message.substring(9, 13));
        String data = message.substring(13);
        byte[] bytes = ByteOperations.getBytesFromString(data);
        String origHash = torrentFile.getPieces().get(idx);
        String receivedHash;
        try {
            receivedHash = getSha1(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
        if (!receivedHash.equals(origHash)) {
            return false;
        }
        try {
            int offset = idx * torrentFile.getPieceLength().intValue() + begin;
            fileManager.writePiece(fileName, offset, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private String getSha1(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(bytes);
        return String.format("%040x", new BigInteger(1, digest.digest())).toUpperCase(Locale.ROOT);
    }
}
