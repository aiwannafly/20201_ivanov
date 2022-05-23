package torrent.client.handlers;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.FileManager;
import torrent.client.util.BitTorrentHandshake;
import torrent.client.util.ByteOperations;
import torrent.client.util.MessageType;

import java.io.*;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UploadHandler implements Runnable {
    private final ServerSocketChannel serverSocketChannel;
    private final Torrent torrentFile;
    private final FileManager fileManager;
    private final String peerId;
    private final Timer keepAliveSendTimer;
    private final Timer keepAliveRecvTimer;
    private final Map<SocketChannel, Long> lastKeepAliveTimes = new HashMap<>();
    private Selector selector;
    private final ArrayList<SocketChannel> removalList = new ArrayList<>();

    public UploadHandler(Torrent torrentFile, FileManager fileManager, String peerId,
                         ServerSocketChannel serverSocket) {
        this.torrentFile = torrentFile;
        this.fileManager = fileManager;
        this.peerId = peerId;
        this.serverSocketChannel = serverSocket;
        this.keepAliveSendTimer = new Timer();
        this.keepAliveRecvTimer = new Timer();
    }

    @Override
    public void run() {
        // selector is open here
        try {
            selector = Selector.open();
            int ops = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, ops, null);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        TimerTask sendKeepALiveMsgs = new SendKeepAliveTask();
        TimerTask recvKeepAliveMsgs = new RecvKeepAliveTask();
        keepAliveSendTimer.schedule(sendKeepALiveMsgs, 0, 1000);
        keepAliveRecvTimer.schedule(recvKeepAliveMsgs, 0, 2000);
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
        keepAliveSendTimer.cancel();
        keepAliveRecvTimer.cancel();
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
                String leechHandshake = new String(buf.array());
                if (!myHandshake.equals(leechHandshake)) {
                    System.err.println("=== Handshakes are different, reject connection");
                    continue;
                }
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                lastKeepAliveTimes.put(client, System.currentTimeMillis());
                System.out.println("=== Connection accepted: " + client.getLocalAddress());
            } else if (myKey.isReadable()) {
                SocketChannel client = (SocketChannel) myKey.channel();
                StringBuilder messageBuilder = new StringBuilder();
                try {
                    client.read(lengthBuf);
                } catch (SocketException e) {
                    System.out.println("=== Closed connection");
                    lastKeepAliveTimes.remove(client);
                    myKey.cancel();
                    break;
                }
                String result = new String(lengthBuf.array());
                messageBuilder.append(result);
                int messageLength = ByteOperations.convertFromBytes(messageBuilder.toString());
                if (messageLength == 0) {
                    if (getTimeFromLastKeepAlive(client) > 0 &&
                        getTimeFromLastKeepAlive(client) < Constants.MIN_KEEP_ALIVE_INTERVAL) {
                        myKey.cancel(); // connection was closed from client side
                        lastKeepAliveTimes.remove(client);
                        break;
                    }
                    lastKeepAliveTimes.replace(client, System.currentTimeMillis());
                    continue;
                }
                if (messageLength < 0) {
                    lastKeepAliveTimes.remove(client);
                    myKey.cancel();
                    break;
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
                    System.err.println("=== Failed to handle the message");
                }
            }
            keysIterator.remove();
        }
    }

    private boolean handleMessage(SocketChannel client, String message) throws IOException {
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        if (id == MessageType.REQUEST) {
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

    private long getTimeFromLastKeepAlive(SocketChannel client) {
        if (0 == lastKeepAliveTimes.get(client)) {
            return 0;
        }
        return System.currentTimeMillis() - lastKeepAliveTimes.get(client);
    }

    private class SendKeepAliveTask extends TimerTask {
        @Override
        public void run() {
            for (SocketChannel client: lastKeepAliveTimes.keySet()) {
                String keepAliveMsg = "\0\0\0\0";
                try {
                    client.write(ByteBuffer.wrap(keepAliveMsg.getBytes(StandardCharsets.UTF_8)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class RecvKeepAliveTask extends TimerTask {
        @Override
        public void run() {
            removalList.clear();
            for (SocketChannel client: lastKeepAliveTimes.keySet()) {
                if (getTimeFromLastKeepAlive(client) > Constants.MAX_KEEP_ALIVE_INTERVAL) {
                    // close connection
                    removalList.add(client);
                }
            }
            if (removalList.isEmpty()) {
                return;
            }
            for (SocketChannel client: removalList) {
                lastKeepAliveTimes.remove(client);
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
            while (keysIterator.hasNext()) {
                SelectionKey myKey = keysIterator.next();
                if (removalList.contains((SocketChannel) myKey.channel())) {
                    myKey.cancel();
                }
                keysIterator.remove();
            }
        }
    }
}
