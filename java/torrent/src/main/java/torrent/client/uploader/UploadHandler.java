package torrent.client.uploader;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.FileManager;
import torrent.client.exceptions.BadMessageException;
import torrent.client.exceptions.DifferentHandshakesException;
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
    private final Map<SocketChannel, LeechInfo> leechesInfo = new HashMap<>();
    private Selector selector;

    public static class LeechInfo {
        Long lastKeepAliveTime;
        ArrayList<Integer> sentPieces = new ArrayList<>();
        Queue<String> myReplies = new ArrayDeque<>();
    }

    public UploadHandler(Torrent torrentFile, FileManager fileManager, String peerId,
                         ServerSocketChannel serverSocket) {
        this.torrentFile = torrentFile;
        this.fileManager = fileManager;
        this.peerId = peerId;
        this.serverSocketChannel = serverSocket;
    }

    @Override
    public void run() {
        try {
            selector = Selector.open();
            int ops = serverSocketChannel.validOps();
            serverSocketChannel.register(selector, ops, null);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        KeepAliveHandler keepAliveHandler = new KeepAliveHandler(selector, leechesInfo);
        keepAliveHandler.start();
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
        keepAliveHandler.stop();
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleEvents(Selector selector) throws IOException {
        System.out.println("=== Wait on select...");
        selector.select();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
        while (keysIterator.hasNext()) {
            SelectionKey selectionKey = keysIterator.next();
            if (selectionKey.isAcceptable()) {
                SocketChannel client = serverSocketChannel.accept();
                try {
                    establishConnection(client);
                } catch (DifferentHandshakesException e) {
                    System.err.println(e.getMessage());
                }
                System.out.println("=== Connection accepted: " + client.getLocalAddress());
            }
            if (selectionKey.isReadable()) {
                SocketChannel client = (SocketChannel) selectionKey.channel();
                String message;
                try {
                    message = getMessage(client);
                } catch (BadMessageException e) {
                    System.out.println("=== Closed connection");
                    leechesInfo.remove(client);
                    selectionKey.cancel();
                    keysIterator.remove();
                    continue;
                }
                if (message.equals(Constants.KEEP_ALIVE_MESSAGE)) {
                    if (System.currentTimeMillis() - leechesInfo.get(client).lastKeepAliveTime < 1) {
                        System.out.println("=== Close connection");
                        leechesInfo.remove(client);
                        selectionKey.cancel();
                        keysIterator.remove();
                        continue;
                    }
                    System.out.println("=== Received keep-alive");
                    leechesInfo.get(client).lastKeepAliveTime = System.currentTimeMillis();
                    keysIterator.remove();
                    continue;
                }
                leechesInfo.get(client).myReplies.add(message);
                client.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            }
            if (selectionKey.isWritable()) {
                SocketChannel client = (SocketChannel) selectionKey.channel();
                client.register(selector, SelectionKey.OP_READ);
                if (leechesInfo.get(client).myReplies.isEmpty()) {
                    continue;
                }
                while (!leechesInfo.get(client).myReplies.isEmpty()) {
                    String message = leechesInfo.get(client).myReplies.remove();
                    try {
                        sendMessage(client, message);
                    } catch (BadMessageException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
            keysIterator.remove();
        }
    }

    private void establishConnection(SocketChannel client) throws DifferentHandshakesException,
            IOException {
        String myHandshake = new BitTorrentHandshake(torrentFile.getInfo_hash(), peerId).getMessage();
        ByteBuffer buf = ByteBuffer.allocate(myHandshake.length());
        client.read(buf);
        PrintWriter out = new PrintWriter(client.socket().getOutputStream(), true);
        out.print(myHandshake);
        out.flush();
        String leechHandshake = new String(buf.array());
        if (!myHandshake.equals(leechHandshake)) {
            throw new DifferentHandshakesException("=== Handshakes are different, reject connection");
        }
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        LeechInfo leechInfo = new LeechInfo();
        leechInfo.lastKeepAliveTime = 0L;
        leechesInfo.put(client, leechInfo);
    }

    private String getMessage(SocketChannel client) throws IOException, BadMessageException {
        ByteBuffer lengthBuf = ByteBuffer.allocate(4);
        try {
            client.read(lengthBuf);
        } catch (SocketException e) {
            throw new BadMessageException(e.getMessage());
        }
        String result = new String(lengthBuf.array());
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(result);
        int messageLength = ByteOperations.convertFromBytes(messageBuilder.toString());
        if (messageLength == 0) {
            return messageBuilder.toString(); // keep-alive
        }
        if (messageLength < 0) {
            throw new BadMessageException("=== Bad length");
        }
        ByteBuffer messageBuf = ByteBuffer.allocate(messageLength);
        int count = client.read(messageBuf);
        result = new String(messageBuf.array());
        if (count != messageLength) {
            System.err.println("Read just " + count + " / " + messageLength + " bytes.");
        }
        messageBuilder.append(result);
        return messageBuilder.toString();
    }

    private void sendMessage(SocketChannel client, String message) throws IOException,
            BadMessageException {
        int id = Integer.parseInt(String.valueOf(message.charAt(4)));
        if (id == MessageType.REQUEST) {
            if (message.length() < 4 + 13) {
                throw new BadMessageException("=== Bad length");
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
            leechesInfo.get(client).sentPieces.add(idx);
            return;
        }
        throw new BadMessageException("=== Unknown message type: " + id);
    }
}
