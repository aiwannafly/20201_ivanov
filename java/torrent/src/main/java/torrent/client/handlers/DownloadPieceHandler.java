package torrent.client.handlers;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.FileManager;
import torrent.client.util.ByteOperations;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class DownloadPieceHandler implements Runnable {
    private final Torrent torrentFile;
    private final FileManager fileManager;
    private final String fileName;
    private final int pieceIdx;
    private final int pieceLength;
    private final PrintWriter out;
    private final InputStream in;

    public DownloadPieceHandler(Torrent torrentFile, FileManager fileManager,
                                String fileName, int pieceIdx, int pieceLength,
                                PrintWriter out, InputStream in) {
        this.fileManager = fileManager;
        this.torrentFile = torrentFile;
        this.fileName = fileName;
        this.pieceIdx = pieceIdx;
        this.pieceLength = pieceLength;
        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {
        requestPiece(pieceIdx, 0, pieceLength);
        System.out.println("=== Requested                 " + (pieceIdx + 1) + " in " + Thread.currentThread().getId());
        System.out.flush();
        boolean received = receivePiece();
        if (!received) {
            System.out.println("=== Failed to receive a piece " + (pieceIdx + 1) + " in " + Thread.currentThread().getId());
            System.out.flush();
        } else {
            System.out.println("=== Received piece            " + (pieceIdx + 1) + " in " + Thread.currentThread().getId());
            System.out.flush();
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
            System.err.println("=== Bad length: " + message.length());
            return false;
        }
        System.err.println("Message length: " + message.length());
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
            System.err.println("=== Bad hash, r and o:");
            System.err.println(receivedHash);
            System.err.println(origHash);
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