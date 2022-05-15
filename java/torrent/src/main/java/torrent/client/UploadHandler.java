package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import torrent.client.util.ByteOperations;
import torrent.Constants;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

class UploadHandler implements Runnable {
    private OutputStream out;
    private BufferedReader in;
    private final Socket seedSocket;
    private FileManager fileManager;
    private final String fileName;
    private final Torrent torrent;

    public UploadHandler(Socket seedSocket, Torrent torrent, FileManager fileManager) {
        this.seedSocket = seedSocket;
        this.torrent = torrent;
        this.fileName = torrent.getName();
        try {
            this.out = seedSocket.getOutputStream();
            this.in = new BufferedReader(new InputStreamReader(seedSocket.getInputStream()));
            this.fileManager = fileManager;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                // System.out.println("Waiting for new messages...");
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 0; i < 4; i++) {
                    messageBuilder.append((char ) in.read());
                }
                int messageLength = ByteOperations.convertFromBytes(messageBuilder.toString());
                // System.out.println("Length: " + messageLength);
                if (messageLength < 0) {
                    break;
                }
                char[] data = new char[messageLength];
                int count = in.read(data);
                if (count != messageLength) {
                    System.err.println("Read just " + count + " / " + messageLength + " bytes.");
                }
                messageBuilder.append(String.valueOf(data));
                String message = messageBuilder.toString();
                boolean handled = handleMessage(message);
                if (!handled) {
                    System.out.println("Failed to handle the message");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                seedSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean handleMessage(String message) throws IOException {
        // request: <len=0013><id=6><index><begin><length>
        // piece: <len=0009+X><id=7><index><begin><block>
        // int len = BinaryOperations.convertFromBytes(message.substring(0, 4));
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        if (id == Constants.REQUEST_ID) {
            if (message.length() < 4 + 13) {
                return false;
            }
            int idx = ByteOperations.convertFromBytes(message.substring(5, 9));
            int begin = ByteOperations.convertFromBytes(message.substring(9, 13));
            int length = ByteOperations.convertFromBytes(message.substring(13, 17));
            String reply = ByteOperations.convertIntoBytes(9 + length) + "7" +
                    ByteOperations.convertIntoBytes(idx) + ByteOperations.convertIntoBytes(begin);
            out.write(reply.getBytes(StandardCharsets.UTF_8));
            // System.out.println("Trying to read " + length + " bytes...");
            int offset = idx * torrent.getPieceLength().intValue() + begin;
            byte[] piece = fileManager.readPiece(fileName, offset, length);
//            byte[] piece = fileStream.readNBytes(length);
             // System.out.println("Read " + readBytes);
            out.write(piece);
            out.flush();
            return true;
        }
        return false;
    }
}
