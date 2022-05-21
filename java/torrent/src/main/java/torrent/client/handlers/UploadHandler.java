package torrent.client.handlers;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.BitTorrentClient;
import torrent.client.FileManager;
import torrent.client.util.BitTorrentHandshake;
import torrent.client.util.ByteOperations;

import java.awt.datatransfer.FlavorEvent;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class UploadHandler implements Runnable {
    private final ServerSocketChannel serverSocketChannel;
    private final Torrent torrentFile;
    private final FileManager fileManager;
    private final String peerId;

    public UploadHandler(Torrent torrentFile, FileManager fileManager, String peerId,
                         ServerSocketChannel serverSocket) {
        this.torrentFile = torrentFile;
        this.fileManager = fileManager;
        this.peerId = peerId;
        this.serverSocketChannel = serverSocket;
    }

    @Override
    public void run() {
        Selector selector; // selector is open here
        try {
            selector = Selector.open();
            int ops = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, ops, null);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        while (true) {
            try {
                handleEvents(selector);
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void handleEvents(Selector selector) throws IOException {
        selector.select();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
        ByteBuffer lengthBuf = ByteBuffer.allocate(4);
        while (keysIterator.hasNext()) {
            SelectionKey myKey = keysIterator.next();
            if (myKey.isAcceptable()) {
                SocketChannel client = serverSocketChannel.accept();
                String myHandshake = new BitTorrentHandshake(torrentFile.getInfo_hash(), peerId).getMessage();
                ByteBuffer buf = ByteBuffer.allocate(myHandshake.length());
                client.read(buf);
                PrintWriter out = new PrintWriter(client.socket().getOutputStream(), true);
                out.print(myHandshake);
                out.flush();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                System.out.println("=== Connection accepted: " + client.getLocalAddress());
            } else if (myKey.isReadable()) {
                SocketChannel client = (SocketChannel) myKey.channel();
                StringBuilder messageBuilder = new StringBuilder();
                int read = client.read(lengthBuf);
                String result = new String(lengthBuf.array());
                messageBuilder.append(result);
                int messageLength = ByteOperations.convertFromBytes(messageBuilder.toString());
                // System.out.println("=== Length (from msg): " + messageLength);
                if (messageLength <= 0) {
                    myKey.cancel();
                    break;
                }
                if (messageLength > 50) {
                    System.err.println("Too long msg: " + messageLength);
                    keysIterator.remove();
                    return;
                }
                ByteBuffer messageBuf = ByteBuffer.allocate(messageLength);
                int count = client.read(messageBuf);
                result = new String(messageBuf.array());
                if (count != messageLength) {
                    System.err.println("Read just " + count + " / " + messageLength + " bytes.");
                }
                messageBuilder.append(result);
                String message = messageBuilder.toString();
                boolean handled = handleMessage(client, message);
                if (!handled) {
                    System.out.println("Failed to handle the message");
                }
            }
            keysIterator.remove();
        }

    }

    private boolean handleMessage(SocketChannel client, String message) throws IOException {
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        if (id == Constants.REQUEST_ID) {
            if (message.length() < 4 + 13) {
                System.err.println("=== Bad msg length");
                return false;
            }
            int idx = ByteOperations.convertFromBytes(message.substring(5, 9));
            int begin = ByteOperations.convertFromBytes(message.substring(9, 13));
            int length = ByteOperations.convertFromBytes(message.substring(13, 17));
            String reply = ByteOperations.convertIntoBytes(9 + length) + "7" +
                    ByteOperations.convertIntoBytes(idx) + ByteOperations.convertIntoBytes(begin);
            client.write(ByteBuffer.wrap(reply.getBytes(StandardCharsets.UTF_8)));
            int offset = idx * torrentFile.getPieceLength().intValue() + begin;
            byte[] piece = fileManager.readPiece(torrentFile.getName(), offset, length);
            client.write(ByteBuffer.wrap(piece));
            return true;
        }
        return false;
    }
}
