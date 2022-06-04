package com.aiwannafly.gui_torrent.torrent.client.downloader;

import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.client.file_manager.FileManager;
import com.aiwannafly.gui_torrent.torrent.client.util.ByteOperations;
import com.aiwannafly.gui_torrent.torrent.client.messages.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

public class DownloadPieceTask implements Callable<ResponseInfo> {
    private final Torrent torrentFile;
    private final FileManager fileManager;
    private final String fileName;
    private final int pieceIdx;
    private final int pieceLength;
    private final PrintWriter out;
    private final InputStream in;
    private final int peerId;
    private ArrayList<Integer> newAvailablePieces;

    public DownloadPieceTask(Torrent torrentFile, FileManager fileManager,
                             String fileName, int peerId, int pieceIdx, int pieceLength,
                             PrintWriter out, InputStream in) {
        this.fileManager = fileManager;
        this.peerId = peerId;
        this.torrentFile = torrentFile;
        this.fileName = fileName;
        this.pieceIdx = pieceIdx;
        this.pieceLength = pieceLength;
        this.out = out;
        this.in = in;
    }

    @Override
    public ResponseInfo call() {
        ResponseInfo result = new ResponseInfo(ResponseInfo.Status.RECEIVED, peerId, pieceIdx);
        requestPiece(pieceIdx, 0, pieceLength);
        while (true) {
            ResponseInfo.Status received = receivePiece();
            if (received == ResponseInfo.Status.GOT_KEEP_ALIVE) {
                result.receivedKeepAlive = true;
                result.keepAliveTimeMillis = System.currentTimeMillis();
            } else if (received == ResponseInfo.Status.HAVE) {
                result.newAvailablePieces = this.newAvailablePieces;
            } else if (received == ResponseInfo.Status.LOST) {
                result.status = ResponseInfo.Status.LOST;
                return result;
            } else if (received == ResponseInfo.Status.RECEIVED) {
                break;
            }
        }
        return result;
    }

    private void requestPiece(int index, int begin, int length) {
        String message = ByteOperations.convertIntoBytes(13) + "6" +
                ByteOperations.convertIntoBytes(index) + ByteOperations.convertIntoBytes(begin) +
                ByteOperations.convertIntoBytes(length);
        out.print(message);
        out.flush();
    }

    private ResponseInfo.Status receivePiece() {
        StringBuilder messageBuilder = new StringBuilder();
        try {
            for (int i = 0; i < 4; i++) {
                messageBuilder.append((char) in.read());
            }
            int messageLength = ByteOperations.convertFromBytes(messageBuilder.toString());
            if (messageLength == 0) {
                return ResponseInfo.Status.GOT_KEEP_ALIVE;
            }
            for (int i = 0; i < messageLength; i++) {
                messageBuilder.append((char) in.read());
            }
        } catch (IOException e) {
            return ResponseInfo.Status.LOST;
        }
        String message = messageBuilder.toString();
        if (message.length() < 4 + 1 + 4) {
            System.err.println("=== Bad length: " + message.length());
            return ResponseInfo.Status.LOST;
        }
        // piece: <len=0009+X><id=7><index><begin><block>
        int len = ByteOperations.convertFromBytes(message.substring(0, 4));
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        if (id == Message.HAVE) {
            int idx = ByteOperations.convertFromBytes(message.substring(5, 9));
            if (newAvailablePieces == null) {
                newAvailablePieces = new ArrayList<>();
            }
            newAvailablePieces.add(idx);
            // System.out.println("=== Received 'HAVE " + idx + "'");
            return ResponseInfo.Status.HAVE;
        }
        if (id != Message.PIECE) {
            return ResponseInfo.Status.LOST;
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
            return ResponseInfo.Status.LOST;
        }
        if (!receivedHash.equals(origHash)) {
            System.err.println("=== Bad hash, r and o:");
            System.err.println(receivedHash);
            System.err.println(origHash);
            return ResponseInfo.Status.LOST;
        }
        try {
            int offset;
            offset = idx * torrentFile.getPieceLength().intValue() + begin;
            fileManager.writePiece(fileName, offset, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseInfo.Status.RECEIVED;
    }

    private String getSha1(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(bytes);
        return String.format("%040x", new BigInteger(1, digest.digest())).toUpperCase(Locale.ROOT);
    }
}