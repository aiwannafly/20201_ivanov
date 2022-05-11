package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import torrent.ByteOperations;
import torrent.Constants;

import java.io.*;
import java.net.Socket;
import java.util.List;

class DownloadHandler implements Runnable {
    private PrintWriter out;
    private InputStream in;
    private final Socket leechSocket;
    private final Torrent torrent;
    private FileOutputStream fileStream;
    private final BitTorrentClient client;

    public DownloadHandler(BitTorrentClient client, Socket leechSocket, Torrent torrentFile) {
        this.leechSocket = leechSocket;
        this.torrent = torrentFile;
        this.client = client;
        try {
            this.out = new PrintWriter(leechSocket.getOutputStream(), true);
            this.in = leechSocket.getInputStream();
            File receivedFile = new File(Constants.PATH + Constants.PREFIX + torrentFile.getName());
            this.fileStream = new FileOutputStream(receivedFile);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        // System.out.println("Start requesting...");
        List<String> pieces = torrent.getPieces();
        for (int i = 0; i < pieces.size(); i++) {
            int pieceLength;
            if (i == pieces.size() - 1) {
                pieceLength = torrent.getTotalSize().intValue() % torrent.getPieceLength().intValue();
            } else {
                pieceLength = torrent.getPieceLength().intValue();
            }
            requestPiece(i, 0, pieceLength);
            // System.out.println("Requested");
            boolean received = receivePiece();
            System.out.println("=== Received piece " + (i + 1));
            if (!received) {
                System.out.println("Failed to receive a piece");
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
        synchronized (client) {
            client.giveFileTask(() -> {
                try {
                    String data = message.substring(13);
                    byte[] bytes = ByteOperations.getBytesFromString(data);
                    fileStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }
}
