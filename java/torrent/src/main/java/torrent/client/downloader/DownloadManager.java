package torrent.client.downloader;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.FileManager;
import torrent.client.exceptions.BadMessageException;
import torrent.client.exceptions.DifferentHandshakesException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.util.BitTorrentHandshake;
import torrent.client.util.ByteOperations;
import torrent.client.util.Handshake;
import torrent.client.util.MessageType;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class DownloadManager {
    private final Torrent torrentFile;
    private final ExecutorService leechPool;
    private final CompletionService<DownloadPieceTask.Result> service;
    private final ArrayList<Integer> leftPieces;
    private final Map<Integer, SeedInfo> seedsInfo = new HashMap<>();
    private final FileManager fileManager;
    private final String fileName;
    private final String peerId;
    private boolean submittedFirstTasks = false;
    private boolean closed = false;
    private KeepAliveHandler keepAliveHandler;
    private final Map<Integer, ArrayList<Integer>> peersPieces;
    private final ArrayList<Integer> myPieces;

    public enum Status {
        FINISHED, NOT_FINISHED
    }

    public static class Result {
        Status status;
        public String torrentFileName;
    }

    static class SeedInfo {
        InputStream in;
        PrintWriter out;
        Long lastKeepAliveTimeMillis;
    }

    public DownloadManager(Torrent torrentFile, FileManager
            fileManager, String peerId, Map<Integer, ArrayList<Integer>> peersPieces,
                           ExecutorService leechPool, ArrayList<Integer> myPieces) throws NoSeedsException {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.torrentFile = torrentFile;
        this.myPieces = myPieces;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        this.leftPieces = new ArrayList<>();
        this.peersPieces = peersPieces;
        for (Integer peerPort: peersPieces.keySet()) {
            if (seedsInfo.size() >= Constants.DOWNLOAD_MAX_THREADS_COUNT) {
                break;
            }
            try {
                establishConnection(peerPort);
            } catch (DifferentHandshakesException e) {
                System.err.println("=== " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (seedsInfo.isEmpty()) {
            throw new NoSeedsException("No seeds uploading file " + torrentFile.getName());
        }
        this.leechPool = leechPool;
        this.service = new ExecutorCompletionService<>(this.leechPool);
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            leftPieces.add(i);
        }
    }

    public Result downloadNextPiece() {
        if (!submittedFirstTasks) {
            keepAliveHandler = new KeepAliveHandler(seedsInfo);
            keepAliveHandler.start();
        }
        Result downloadResult = new Result();
        downloadResult.torrentFileName = torrentFile.getName() + Constants.POSTFIX;
        downloadResult.status = DownloadManager.Status.NOT_FINISHED;
        Random random = new Random();
        if (leftPieces.size() == 0) {
            System.out.println("=== File " + fileName + " was downloaded successfully!");
            shutdown();
            closed = true;
            downloadResult.status = DownloadManager.Status.FINISHED;
            return downloadResult;
        }
        if (!submittedFirstTasks) {
            for (Integer peerPort : seedsInfo.keySet()) {
                requestRandomPiece(random, peerPort);
            }
        }
        submittedFirstTasks = true;
        if (seedsInfo.size() == 0) {
            System.err.println("=== No seeds left. Downloading failed");
            shutdown();
            closed = true;
            downloadResult.status = DownloadManager.Status.FINISHED;
            return downloadResult;
        }
        try {
            Future<DownloadPieceTask.Result> future = service.take();
            DownloadPieceTask.Result result = future.get();
            int pieceIdx = result.pieceId;
            int peerPort = result.peerPort;
            if (!seedsInfo.containsKey(peerPort)) {
                if (result.status == DownloadPieceTask.Status.LOST) {
                    leftPieces.add(pieceIdx);
                }
            } else {
                if (result.newAvailablePieces != null) {
                    peersPieces.get(peerPort).addAll(result.newAvailablePieces);
                }
                if (result.receivedKeepAlive) {
                    seedsInfo.get(peerPort).lastKeepAliveTimeMillis = result.keepAliveTimeMillis;
                }
                if (result.status == DownloadPieceTask.Status.LOST) {
                    leftPieces.add(pieceIdx);
                } else {
                     System.out.println("=== Received piece " + (pieceIdx + 1) +
                             " from " + peerPort);
                     myPieces.add(pieceIdx);
                }
                if (leftPieces.size() > 0) {
                    requestRandomPiece(random, peerPort);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        downloadResult.status = DownloadManager.Status.NOT_FINISHED;
        return downloadResult;
    }

    public Torrent getTorrentFile() {
        return torrentFile;
    }

    public void shutdown() {
        if (closed) {
            return;
        }
        keepAliveHandler.stop();
        leechPool.shutdown();
        try {
            boolean completed = leechPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (!completed) {
                System.err.println("=== Execution was not completed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Integer peerPort : seedsInfo.keySet()) {
            try {
                if (seedsInfo.get(peerPort).out != null) {
                    seedsInfo.get(peerPort).out.close();
                }
                if (seedsInfo.get(peerPort).in != null) {
                    seedsInfo.get(peerPort).in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestRandomPiece(Random random, int peerPort) {
        ArrayList<Integer> availablePieces = peersPieces.get(peerPort);
        ArrayList<Integer> interestingPieces = new ArrayList<>();
        for (Integer piece: availablePieces) {
            if (leftPieces.contains(piece)) {
                interestingPieces.add(piece);
            }
        }
        if (interestingPieces.size() == 0) {
            System.err.println("=== Nothing to ask");
            return;
        }
        int randomIdx = random.nextInt(interestingPieces.size());
        int nextPieceIdx = interestingPieces.get(randomIdx);
        for (int i = 0; i < leftPieces.size(); i++) {
            if (leftPieces.get(i) == nextPieceIdx) {
                leftPieces.remove(i);
                break;
            }
        }
        requestPiece(nextPieceIdx, peerPort);
    }

    private void requestPiece(int pieceIdx, int peerPort) {
        int pieceLength = getPieceLength(pieceIdx);
        service.submit(new DownloadPieceTask(torrentFile, fileManager,
                fileName, peerPort, pieceIdx, pieceLength,
                seedsInfo.get(peerPort).out, seedsInfo.get(peerPort).in));
    }

    private int getPieceLength(int pieceIdx) {
        int pieceLength;
        if (pieceIdx == torrentFile.getPieces().size() - 1) {
            pieceLength = torrentFile.getTotalSize().intValue() % torrentFile.getPieceLength().intValue();
        } else {
            pieceLength = torrentFile.getPieceLength().intValue();
        }
        return pieceLength;
    }

    private void establishConnection(int peerPort) throws IOException, DifferentHandshakesException {
        SocketChannel currentPeerChannel = SocketChannel.open();
        InetSocketAddress address = new InetSocketAddress("localhost", peerPort);
        currentPeerChannel.connect(address);
        currentPeerChannel.configureBlocking(true);
        Handshake myHandshake = new BitTorrentHandshake(torrentFile.getInfo_hash(), peerId);
        PrintWriter out = new PrintWriter(currentPeerChannel.socket().getOutputStream(), true);
        out.print(myHandshake.getMessage());
        out.flush();
        ByteBuffer buf = ByteBuffer.allocate(myHandshake.getMessage().length());
        currentPeerChannel.read(buf);
        Handshake peerHandshake = new BitTorrentHandshake(new String(buf.array()));
        if (myHandshake.getInfoHash().equals(peerHandshake.getInfoHash())) {
            SeedInfo seedInfo = new SeedInfo();
            seedInfo.in = currentPeerChannel.socket().getInputStream();
            seedInfo.out = out;
            seedInfo.lastKeepAliveTimeMillis = System.currentTimeMillis();
            seedsInfo.put(peerPort, seedInfo);
            Selector selector = Selector.open();
            currentPeerChannel.configureBlocking(false);
            currentPeerChannel.register(selector, SelectionKey.OP_READ);
            int waitTime = 500;
            int returnValue = selector.select(waitTime);
            if (returnValue == 0) {
                return;
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keysIterator = selectedKeys.iterator();
            keysIterator.next().cancel();
            handleBitField(peerPort, currentPeerChannel);
            currentPeerChannel.configureBlocking(true);
        } else {
            throw new DifferentHandshakesException("Handshakes are different, reject connection");
        }
    }

    private void handleBitField(int peerPort, SocketChannel peerChannel) throws IOException {
        Message message;
        do {
            try {
                message = getMessage(peerChannel);
            } catch (BadMessageException e) {
                e.printStackTrace();
                return;
            }
        } while (message.type == MessageType.KEEP_ALIVE);
        if (message.type != MessageType.BITFIELD) {
            System.err.println("Not bitfield");
            return;
        }
        int bitsInByte = 8;
        byte[] bytes = message.data.getBytes(StandardCharsets.UTF_8);
        System.out.println("Pieces from bitfield: ");
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            int bitIdx = i % bitsInByte;
            int byteIdx = i / bitsInByte;
            byte bit = (byte) (1 << bitIdx);
            if ((bytes[byteIdx] & bit) != 0) {
                if (!peersPieces.get(peerPort).contains(i)) {
                    peersPieces.get(peerPort).add(i);
                    System.out.print(i + " ");
                }
            }
        }
        System.out.println();
    }

    private static class Message {
        int length;
        int type;
        String data;
    }

    private Message getMessage(SocketChannel client) throws IOException, BadMessageException {
        ByteBuffer lengthBuf = ByteBuffer.allocate(4);
        try {
            client.read(lengthBuf);
        } catch (SocketException e) {
            throw new BadMessageException(e.getMessage());
        }
        Message message = new Message();
        String lengthStr = new String(lengthBuf.array());
        message.length = ByteOperations.convertFromBytes(lengthStr);
        if (message.length == 0) {
            message.type = MessageType.KEEP_ALIVE;
            return message; // keep-alive
        }
        if (message.length < 0) {
            throw new BadMessageException("=== Bad length");
        }
        ByteBuffer messageBuf = ByteBuffer.allocate(message.length);
        int count = client.read(messageBuf);
        String data = new String(messageBuf.array());
        if (count != message.length) {
            System.err.println("Read just " + count + " / " + message.length + " bytes.");
        }
        message.type = Integer.parseInt(String.valueOf(data.charAt(0)));
        message.data = data.substring(1);
        return message;
    }
}
