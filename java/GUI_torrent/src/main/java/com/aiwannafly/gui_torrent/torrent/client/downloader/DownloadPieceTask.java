package com.aiwannafly.gui_torrent.torrent.client.downloader;

import com.aiwannafly.gui_torrent.torrent.client.exceptions.BadMessageException;
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
    private final DownloadManager.PeerInfo peerInfo;
    private final int peerId;
    private ArrayList<Integer> newAvailablePieces;

    public DownloadPieceTask(Torrent torrentFile, FileManager fileManager,
                             String fileName, int peerPort, int pieceIdx, int pieceLength,
                             DownloadManager.PeerInfo peerInfo) {
        this.fileManager = fileManager;
        this.peerId = peerPort;
        this.torrentFile = torrentFile;
        this.fileName = fileName;
        this.pieceIdx = pieceIdx;
        this.pieceLength = pieceLength;
        this.peerInfo = peerInfo;
    }

    @Override
    public ResponseInfo call() throws IOException, BadMessageException {
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
        peerInfo.out.print(message);
        peerInfo.out.flush();
    }

    private ResponseInfo.Status receivePiece() throws IOException, BadMessageException {
        Message.MessageInfo messageInfo = Message.getMessage(peerInfo.channel);
        if (messageInfo.type == Message.KEEP_ALIVE) {
            return ResponseInfo.Status.GOT_KEEP_ALIVE;
        }
        // piece: <len=0009+X><id=7><index><begin><block>
        String message = messageInfo.data;
        if (messageInfo.type == Message.HAVE) {
            int idx = ByteOperations.convertFromBytes(message.substring(0, 4));
            if (newAvailablePieces == null) {
                newAvailablePieces = new ArrayList<>();
            }
            newAvailablePieces.add(idx);
            // System.out.println("=== Received 'HAVE " + idx + "'");
            return ResponseInfo.Status.HAVE;
        }
        if (messageInfo.type != Message.PIECE) {
            return ResponseInfo.Status.LOST;
        }
        Message.Piece piece = messageInfo.piece;
        String origHash = torrentFile.getPieces().get(piece.idx);
        String receivedHash;
        try {
            receivedHash = getSha1(piece.data);
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
            offset = piece.idx * torrentFile.getPieceLength().intValue() + piece.begin;
            fileManager.writePiece(fileName, offset, piece.data);
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
