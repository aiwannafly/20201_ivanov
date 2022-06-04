package com.aiwannafly.gui_torrent.torrent.client.downloader;

import com.aiwannafly.gui_torrent.torrent.client.exceptions.*;
import com.aiwannafly.gui_torrent.torrent.client.tracker_communicator.TrackerCommunicator;
import com.aiwannafly.gui_torrent.torrent.client.util.ObservableList;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.client.Constants;
import com.aiwannafly.gui_torrent.torrent.client.file_manager.FileManager;
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
    private final CompletionService<ResponseInfo> service;
    private final ArrayList<Integer> leftPieces;
    private final FileManager fileManager;
    private final String fileName;
    private final String peerId;
    private boolean closed = false;
    private final KeepAliveHandler keepAliveHandler;
    private final Map<Integer, PeerInfo> peersInfo = new HashMap<>();
    private final ObservableList<Integer> myPieces;
    private final TrackerCommunicator trackerComm;
    private final Timer updateConnectionsTimer;

    public enum DownloadStatus {
        FINISHED, NOT_FINISHED, FAILED
    }

    public static class Result {
        DownloadStatus downloadStatus;
        public String torrentFileName;
    }

    enum PeerStatus {
        FREE, WORKING, INVALID
    }

    static class PeerInfo {
        InputStream in = null;
        PrintWriter out = null;
        Long lastKeepAliveTimeMillis = null;
        ArrayList<Integer> availablePieces = null;
        SocketChannel channel = null;
        PeerStatus peerStatus = PeerStatus.INVALID;
    }

    public DownloadManager(Torrent torrentFile, FileManager fileManager, String peerId,
                           ExecutorService leechPool,
                           ObservableList<Integer> myPieces, TrackerCommunicator trackerComm) throws NoSeedsException,
            ServerNotCorrespondsException, BadServerReplyException {
        this.peerId = peerId;
        this.fileManager = fileManager;
        this.trackerComm = trackerComm;
        this.torrentFile = torrentFile;
        this.myPieces = myPieces;
        this.fileName = Constants.PREFIX + torrentFile.getName();
        setConnections();
        this.updateConnectionsTimer = new Timer();
        TimerTask updateConnections = new TimerTask() {
            @Override
            public void run() {
                try {
                    setConnections();
                } catch (ServerNotCorrespondsException | NoSeedsException | BadServerReplyException e) {
                    e.printStackTrace();
                }
            }
        };
        long UPDATE_INTERV = 4 * 1000;
        updateConnectionsTimer.schedule(updateConnections, UPDATE_INTERV, UPDATE_INTERV);
        if (peersInfo.isEmpty()) {
            throw new NoSeedsException("No seeds uploading file " + torrentFile.getName());
        }
        this.service = new ExecutorCompletionService<>(leechPool);
        this.leftPieces = new ArrayList<>();
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            leftPieces.add(i);
        }
        keepAliveHandler = new KeepAliveHandler(peersInfo);
        keepAliveHandler.start();
    }

    public Result downloadNextPiece() {
        Result downloadResult = new Result();
        downloadResult.torrentFileName = torrentFile.getName() + Constants.POSTFIX;
        downloadResult.downloadStatus = DownloadStatus.NOT_FINISHED;
        Random random = new Random();
        for (Integer peerPort : peersInfo.keySet()) {
            if (peersInfo.get(peerPort).peerStatus == PeerStatus.FREE) {
                requestRandomPiece(random, peerPort);
            }
        }
        if (peersInfo.isEmpty()) {
            shutdown();
            closed = true;
            downloadResult.downloadStatus = DownloadStatus.FAILED;
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
                    // ("Got some new pieces!");
                    peersInfo.get(peerPort).availablePieces.addAll(result.newAvailablePieces);
                }
                if (result.receivedKeepAlive) {
                    peersInfo.get(peerPort).lastKeepAliveTimeMillis = result.keepAliveTimeMillis;
                }
                if (result.status == ResponseInfo.Status.LOST) {
                    leftPieces.add(pieceIdx);
                }
                if (result.status == ResponseInfo.Status.RECEIVED) {
                    // System.out.println("=== Received from " + peerPort);
                    synchronized (myPieces) {
                        myPieces.add(pieceIdx);
                    }
                }
                if (myPieces.size() < torrentFile.getPieces().size()) {
                    requestRandomPiece(random, peerPort);
                } else {
                    shutdown();
                    closed = true;
                    downloadResult.downloadStatus = DownloadStatus.FINISHED;
                    return downloadResult;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        downloadResult.downloadStatus = DownloadStatus.NOT_FINISHED;
        return downloadResult;
    }

    public Torrent getTorrentFile() {
        return torrentFile;
    }

    public void shutdown() {
        if (closed) {
            return;
        }
        updateConnectionsTimer.cancel();
        keepAliveHandler.stop();
        for (Integer peerPort : peersInfo.keySet()) {
            try {
                if (peersInfo.get(peerPort).out != null) {
                    peersInfo.get(peerPort).out.close();
                }
                if (peersInfo.get(peerPort).in != null) {
                    peersInfo.get(peerPort).in.close();
                }
                if (peersInfo.get(peerPort).channel != null) {
                    peersInfo.get(peerPort).channel.close();
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
        peersInfo.get(peerPort).peerStatus = PeerStatus.WORKING;
        if (interestingPieces.size() == 0) {
            service.submit(new CollectPiecesInfoTask(peersInfo.get(peerPort), peerPort));
            // System.out.println("=== Wait for info about new pieces from " + peerPort);
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
                peersInfo.get(peerPort)));
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

    private void setConnections() throws ServerNotCorrespondsException, NoSeedsException,
            BadServerReplyException {
        String torrentFileName = torrentFile.getName() + Constants.POSTFIX;
        trackerComm.sendToTracker("show peers " + torrentFileName);
        String message = trackerComm.receiveFromTracker();
        if (null == message) {
            throw new ServerNotCorrespondsException("Server did not show peers");
        }
        String[] words = message.split(" ");
        if (words.length - 1 == 0) {
            throw new NoSeedsException("No peers are uploading the file at the moment");
        }
        int idx = 1;
        while (idx < words.length) {
            int peerPort = Integer.parseInt(words[idx++]);
            int piecesCount = Integer.parseInt(words[idx++]);
            if (peerPort < 0 || piecesCount < 0) {
                throw new BadServerReplyException("Negative peerPort or piecesCount");
            }
            ArrayList<Integer> availablePieces = new ArrayList<>();
            for (int i = 0; i < piecesCount; i++) {
                if (idx >= words.length) {
                    throw new BadServerReplyException("Wrong count of pieces");
                }
                availablePieces.add(Integer.parseInt(words[idx++]));
            }
            if (!peersInfo.containsKey(peerPort)) {
                peersInfo.put(peerPort, new PeerInfo());
                peersInfo.get(peerPort).availablePieces = availablePieces;
            }
            for (Integer piece: availablePieces) {
                if (peersInfo.get(peerPort).availablePieces.contains(piece)) {
                    continue;
                }
                peersInfo.get(peerPort).availablePieces.add(piece);
            }
        }
        for (Integer peerPort : peersInfo.keySet()) {
            if (peersInfo.get(peerPort).channel != null) {
                continue;
            }
            try {
                establishConnection(peerPort);
            } catch (DifferentHandshakesException e) {
                System.err.println("=== " + e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void establishConnection(int peerPort)
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
            PeerInfo peerInfo = peersInfo.get(peerPort);
            peerInfo.channel = currentPeerChannel;
            peerInfo.in = currentPeerChannel.socket().getInputStream();
            peerInfo.out = out;
            peerInfo.lastKeepAliveTimeMillis = System.currentTimeMillis();
            peerInfo.peerStatus = PeerStatus.FREE;
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
                }
            }
        }
    }
}
