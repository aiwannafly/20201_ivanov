package com.aiwannafly.gui_torrent.torrent.client.downloader;

import com.aiwannafly.gui_torrent.torrent.ObservableList;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.Constants;
import com.aiwannafly.gui_torrent.torrent.client.file_manager.FileManager;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.BadMessageException;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.DifferentHandshakesException;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.NoSeedsException;
import com.aiwannafly.gui_torrent.torrent.client.messages.Message;
import com.aiwannafly.gui_torrent.torrent.client.util.BitTorrentHandshake;
import com.aiwannafly.gui_torrent.torrent.client.util.Handshake;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
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
    private final CompletionService<ResponseInfo> service;
    private final ArrayList<Integer> leftPieces;
    private final FileManager fileManager;
    private final String fileName;
    private final String peerId;
    private boolean submittedFirstTasks = false;
    private boolean closed = false;
    private KeepAliveHandler keepAliveHandler;
    private final Map<Integer, PeerInfo> peersInfo = new HashMap<>();
    private final ObservableList<Integer> myPieces;

    public enum Status {
        FINISHED, NOT_FINISHED
    }

    public static class Result {
        Status status;
        public String torrentFileName;
    }

    static class PeerInfo {
        InputStream in;
        PrintWriter out;
        Long lastKeepAliveTimeMillis;
        ArrayList<Integer> availablePieces;
        SocketChannel channel;
    }

    public DownloadManager(Torrent torrentFile, FileManager
            fileManager, String peerId, Map<Integer, ArrayList<Integer>> peersPieces,
                           ExecutorService leechPool, ObservableList<Integer> myPieces) throws NoSeedsException {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.torrentFile = torrentFile;
        this.myPieces = myPieces;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        this.leftPieces = new ArrayList<>();
        for (Integer peerPort : peersPieces.keySet()) {
            if (peersInfo.size() >= Constants.DOWNLOAD_MAX_THREADS_COUNT) {
                break;
            }
            try {
                establishConnection(peerPort, peersPieces.get(peerPort));
            } catch (DifferentHandshakesException e) {
                System.err.println("=== " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (peersInfo.isEmpty()) {
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
            keepAliveHandler = new KeepAliveHandler(peersInfo);
            keepAliveHandler.start();
        }
        Result downloadResult = new Result();
        downloadResult.torrentFileName = torrentFile.getName() + Constants.POSTFIX;
        downloadResult.status = DownloadManager.Status.NOT_FINISHED;
        Random random = new Random();
        if (!submittedFirstTasks) {
            for (Integer peerPort : peersInfo.keySet()) {
                requestRandomPiece(random, peerPort);
            }
        }
        submittedFirstTasks = true;
        if (peersInfo.size() == 0) {
            System.err.println("=== No seeds left. Downloading failed");
            shutdown();
            closed = true;
            downloadResult.status = DownloadManager.Status.FINISHED;
            return downloadResult;
        }
        try {
            Future<ResponseInfo> future = service.take();
            ResponseInfo result = future.get();
            int pieceIdx = result.pieceIdx;
            int peerPort = result.peerPort;
            if (!peersInfo.containsKey(peerPort)) {
                /* The connection was closed */
                if (result.status == ResponseInfo.Status.LOST) {
                    leftPieces.add(pieceIdx);
                }
            } else {
                if (result.newAvailablePieces != null) {
                    // System.out.println("Got some new pieces!");
                    peersInfo.get(peerPort).availablePieces.addAll(result.newAvailablePieces);
                }
                if (result.receivedKeepAlive) {
                    peersInfo.get(peerPort).lastKeepAliveTimeMillis = result.keepAliveTimeMillis;
                }
                if (result.status == ResponseInfo.Status.LOST) {
                    leftPieces.add(pieceIdx);
                }
                if (result.status == ResponseInfo.Status.RECEIVED) {
                    synchronized (myPieces) {
                        myPieces.add(pieceIdx);
                    }
                }
                if (leftPieces.size() > 0) {
                    requestRandomPiece(random, peerPort);
                } else {
                    shutdown();
                    closed = true;
                    downloadResult.status = DownloadManager.Status.FINISHED;
                    return downloadResult;
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
        for (Integer peerPort : peersInfo.keySet()) {
            try {
                if (peersInfo.get(peerPort).out != null) {
                    peersInfo.get(peerPort).out.close();
                }
                if (peersInfo.get(peerPort).in != null) {
                    peersInfo.get(peerPort).in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closed = true;
    }

    private void requestRandomPiece(Random random, int peerPort) {
        if (leftPieces.size() == 0) {
            return;
        }
        ArrayList<Integer> availablePieces = peersInfo.get(peerPort).availablePieces;
        ArrayList<Integer> interestingPieces = new ArrayList<>();
        for (Integer piece : availablePieces) {
            if (leftPieces.contains(piece)) {
                interestingPieces.add(piece);
            }
        }
        if (interestingPieces.size() == 0) {
            service.submit(new CollectPiecesInfoTask(peersInfo.get(peerPort), peerPort));
            System.out.println("=== Wait for info about new pieces from " + peerPort);
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
                peersInfo.get(peerPort).out, peersInfo.get(peerPort).in));
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

    private void establishConnection(int peerPort, ArrayList<Integer> availablePieces)
            throws IOException, DifferentHandshakesException {
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
            PeerInfo peerInfo = new PeerInfo();
            peerInfo.channel = currentPeerChannel;
            peerInfo.in = currentPeerChannel.socket().getInputStream();
            peerInfo.out = out;
            peerInfo.lastKeepAliveTimeMillis = System.currentTimeMillis();
            peerInfo.availablePieces = availablePieces;
            peersInfo.put(peerPort, peerInfo);
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
        Message.MessageInfo messageInfo;
        do {
            try {
                messageInfo = Message.getMessage(peerChannel);
            } catch (BadMessageException e) {
                e.printStackTrace();
                return;
            }
        } while (messageInfo.type == Message.KEEP_ALIVE);
        if (messageInfo.type != Message.BITFIELD) {
            System.err.println("Not bitfield");
            return;
        }
        int bitsInByte = 8;
        byte[] bytes = messageInfo.data.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            int bitIdx = i % bitsInByte;
            int byteIdx = i / bitsInByte;
            byte bit = (byte) (1 << bitIdx);
            if ((bytes[byteIdx] & bit) != 0) {
                if (!peersInfo.get(peerPort).availablePieces.contains(i)) {
                    peersInfo.get(peerPort).availablePieces.add(i);
                    System.out.print(i + " ");
                }
            }
        }
    }
}
