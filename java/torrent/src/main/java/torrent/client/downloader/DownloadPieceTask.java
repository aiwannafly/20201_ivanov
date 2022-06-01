package torrent.client.downloader;

import be.christophedetroyer.torrent.Torrent;
import torrent.client.FileManager;
import torrent.client.util.ByteOperations;
import torrent.client.util.MessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

public class DownloadPieceTask implements Callable<ExchangeResult> {
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
    public ExchangeResult call() {
        System.out.println("Will request from " + peerId);
        ExchangeResult result = new ExchangeResult(ExchangeResult.Status.RECEIVED, peerId, pieceIdx);
        requestPiece(pieceIdx, 0, pieceLength);
        while (true) {
            ExchangeResult.Status received = receivePiece();
            if (received == ExchangeResult.Status.GOT_KEEP_ALIVE) {
                result.receivedKeepAlive = true;
                result.keepAliveTimeMillis = System.currentTimeMillis();
            } else if (received == ExchangeResult.Status.HAVE) {
                result.newAvailablePieces = this.newAvailablePieces;
            } else if (received == ExchangeResult.Status.LOST) {
                result.status = ExchangeResult.Status.LOST;
                return result;
            } else if (received == ExchangeResult.Status.RECEIVED) {
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

    private ExchangeResult.Status receivePiece() {
        StringBuilder messageBuilder = new StringBuilder();
        try {
            for (int i = 0; i < 4; i++) {
                messageBuilder.append((char) in.read());
            }
            int messageLength = ByteOperations.convertFromBytes(messageBuilder.toString());
            if (messageLength == 0) {
                return ExchangeResult.Status.GOT_KEEP_ALIVE;
            }
            for (int i = 0; i < messageLength; i++) {
                messageBuilder.append((char) in.read());
            }
        } catch (IOException e) {
            return ExchangeResult.Status.LOST;
        }
        String message = messageBuilder.toString();
        if (message.length() < 4 + 1 + 4) {
            System.err.println("=== Bad length: " + message.length());
            return ExchangeResult.Status.LOST;
        }
        // piece: <len=0009+X><id=7><index><begin><block>
        int len = ByteOperations.convertFromBytes(message.substring(0, 4));
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        if (id == MessageType.HAVE) {
            int idx = ByteOperations.convertFromBytes(message.substring(5, 9));
            if (newAvailablePieces == null) {
                newAvailablePieces = new ArrayList<>();
            }
            newAvailablePieces.add(idx);
            // System.out.println("=== Received 'HAVE " + idx + "'");
            return ExchangeResult.Status.HAVE;
        }
        if (id != MessageType.PIECE) {
            return ExchangeResult.Status.LOST;
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
            return ExchangeResult.Status.LOST;
        }
        if (!receivedHash.equals(origHash)) {
            System.err.println("=== Bad hash, r and o:");
            System.err.println(receivedHash);
            System.err.println(origHash);
            return ExchangeResult.Status.LOST;
        }
        try {
            int offset;
            offset = idx * torrentFile.getPieceLength().intValue() + begin;
            fileManager.writePiece(fileName, offset, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ExchangeResult.Status.RECEIVED;
    }

    private String getSha1(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(bytes);
        return String.format("%040x", new BigInteger(1, digest.digest())).toUpperCase(Locale.ROOT);
    }
}
