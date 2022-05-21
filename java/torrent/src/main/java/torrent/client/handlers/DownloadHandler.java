package torrent.client.handlers;

import be.christophedetroyer.torrent.Torrent;
import torrent.client.FileManager;
import torrent.client.util.ByteOperations;
import torrent.Constants;

import java.io.*;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.math.BigInteger;
import java.security.MessageDigest;


public class DownloadHandler implements Runnable {
    private PrintWriter out;
    private InputStream in;
    private final SocketChannel leechSocket;
    private final Torrent torrent;
    private FileManager fileManager;
    private final String fileName;
    private final ArrayList<Integer> leftPieces;
    private int missRequestsCounter = 0;
    private final int MISS_LIMIT = 100;

    public DownloadHandler(SocketChannel leechSocket, Torrent torrentFile,
                           FileManager fileManager, ArrayList<Integer> leftPieces) {
        this.leechSocket = leechSocket;
        this.torrent = torrentFile;
        this.leftPieces = leftPieces;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        try {
            this.out = new PrintWriter(leechSocket.socket().getOutputStream(), true);
            this.in = leechSocket.socket().getInputStream();
            this.fileManager = fileManager;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        // System.out.println("Start requesting...");
        int piecesCount = torrent.getPieces().size();
        Random random = new Random();
        while (true) {
            int nextPieceIdx;
            synchronized (leftPieces) {
                if (leftPieces.size() == 0) {
                    break;
                }
                int randomIdx = random.nextInt(leftPieces.size());
                nextPieceIdx = leftPieces.remove(randomIdx);
            }
            int pieceLength;
            if (nextPieceIdx == piecesCount - 1) {
                pieceLength = torrent.getTotalSize().intValue() % torrent.getPieceLength().intValue();
            } else {
                pieceLength = torrent.getPieceLength().intValue();
            }
            requestPiece(nextPieceIdx, 0, pieceLength);
            // System.out.println("Requested");
            boolean received = receivePiece();
            if (!received) {
                System.err.println("=== Failed to receive a piece " + (nextPieceIdx + 1));
                missRequestsCounter++;
                if (missRequestsCounter >= MISS_LIMIT) {
                    System.err.println("=== Failed to download");
                    break;
                }
                synchronized (leftPieces) {
                    leftPieces.add(nextPieceIdx);
                }
            } else {
                System.out.println("=== Received piece " + (nextPieceIdx + 1));
            }
        }
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            leechSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPiece(int index, int begin, int length) {
        String message = ByteOperations.convertIntoBytes(13) + "6" +
                ByteOperations.convertIntoBytes(index) + ByteOperations.convertIntoBytes(begin) +
                ByteOperations.convertIntoBytes(length);
        out.print(message);
        out.flush();
    }

    private boolean receivePiece() {
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
        String origHash = torrent.getPieces().get(idx);
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
            int offset = idx * torrent.getPieceLength().intValue() + begin;
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

