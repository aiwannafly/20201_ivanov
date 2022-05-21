package torrent.client.handlers;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.BitTorrentClient;
import torrent.client.util.ByteOperations;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionsHandler implements Runnable {
    private final BitTorrentClient client;
    private final ServerSocketChannel serverSocketChannel;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    public ConnectionsHandler(BitTorrentClient client, ServerSocketChannel serverSocket) {
        this.client = client;
        this.serverSocketChannel = serverSocket;
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open(); // selector is open here
            int ops = serverSocketChannel.validOps();
            SelectionKey selectionKey = serverSocketChannel.register(selector, ops, null);
            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
                ByteBuffer lengthBuf = ByteBuffer.allocate(4);
                while (keysIterator.hasNext()) {
                    SelectionKey myKey = keysIterator.next();
                    if (myKey.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        String myHandshake = this.client.getHandShakeMessage();
                        ByteBuffer buf = ByteBuffer.allocate(myHandshake.length());
                        client.read(buf);
                        System.out.println("=== Client HS: " + new String(buf.array()));
                        System.out.println("=== Server HS: " + myHandshake);
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
                            System.err.println("TOO LONG MSG: " + messageLength);
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
                        // System.out.println("=== Actual length: " + message.length());
                        // System.out.println("=== Message: " + message);
                        boolean handled = handleMessage(client, message);
                        if (!handled) {
                            System.out.println("Failed to handle the message");
                        }
                    }
                    keysIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean handleMessage(SocketChannel client, String message) throws IOException {
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        if (id == Constants.REQUEST_ID) {
            System.out.println("IS REQUEST");
            if (message.length() < 4 + 13) {
                System.out.println("BAD LENGTH");
                return false;
            }
            int idx = ByteOperations.convertFromBytes(message.substring(5, 9));
            int begin = ByteOperations.convertFromBytes(message.substring(9, 13));
            int length = ByteOperations.convertFromBytes(message.substring(13, 17));
            String reply = ByteOperations.convertIntoBytes(9 + length) + "7" +
                    ByteOperations.convertIntoBytes(idx) + ByteOperations.convertIntoBytes(begin);
            client.write(ByteBuffer.wrap(reply.getBytes(StandardCharsets.UTF_8)));
            Torrent torrent = this.client.getCurrentTorrentFile();
            int offset = idx * torrent.getPieceLength().intValue() + begin;
            byte[] piece = this.client.getFileManager().readPiece(torrent.getName(), offset, length);
            client.write(ByteBuffer.wrap(piece));
            return true;
        }
        return false;
    }

}
