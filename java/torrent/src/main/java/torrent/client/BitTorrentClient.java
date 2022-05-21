package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import torrent.client.handlers.DownloadHandler;
import torrent.client.util.BitTorrentHandshake;
import torrent.Constants;
import torrent.client.util.Handshake;
import torrent.client.util.TorrentFileCreator;
import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.exceptions.ServerNotCorrespondsException;
import torrent.client.exceptions.TorrentCreateFailureException;
import torrent.tracker.TrackerCommandHandler;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BitTorrentClient implements TorrentClient {
    private final ConnectionsReceiver connReceiver;
    private final String peerId;
    private final TrackerCommunicator trackerComm;
    private final ExecutorService leechPool = Executors.newFixedThreadPool(
            Constants.DOWNLOAD_MAX_THREADS_COUNT);
    private final FileManager fileManager;
    private Torrent torrentFile = null;

    public BitTorrentClient() {
        fileManager = new FileManagerImpl();
        connReceiver = new ConnectionsReceiver(this);
        connReceiver.run();
        trackerComm = new TrackerCommunicatorImpl();
        String command = TrackerCommandHandler.SET_LISTENING_SOCKET + " " + connReceiver.getListeningPort();
        trackerComm.sendToTracker(command);
        trackerComm.receiveFromTracker();
        trackerComm.sendToTracker("get peer_id");
        peerId = trackerComm.receiveFromTracker();
    }

    @Override
    public void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException {
        distribute(torrentFileName);
        trackerComm.sendToTracker("show peers");
        String message = trackerComm.receiveFromTracker();
        if (null == message) {
            throw new ServerNotCorrespondsException("Server did not show peers");
        }
        String[] words = message.split(" ");
        int peersCount = words.length - 1;
        if (peersCount == 0) {
            throw new NoSeedsException("No peers are uploading the file at the moment");
        }
        ArrayList<Integer> leftPieces = new ArrayList<>();
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            leftPieces.add(i);
        }
        for (int i = 1; i <= peersCount; i++) {
            int peerPort = Integer.parseInt(words[i]);
            try {
                SocketChannel currentPeerChannel = SocketChannel.open();
                InetSocketAddress address = new InetSocketAddress("localhost", peerPort);
                currentPeerChannel.connect(address);
                // currentPeerChannel.configureBlocking(false);
                Handshake myHandshake = new BitTorrentHandshake(getHandShakeMessage());
                PrintWriter out = new PrintWriter(currentPeerChannel.socket().getOutputStream(), true);
                out.print(myHandshake.getMessage());
                out.flush();
                ByteBuffer buf = ByteBuffer.allocate(myHandshake.getMessage().length());
                currentPeerChannel.read(buf);
                Handshake peerHandshake = new BitTorrentHandshake(new String(buf.array()));
                System.out.println("=== Seed  HS: " + peerHandshake.getMessage());
                System.out.println("=== Leech HS: " + myHandshake.getMessage());
                if (myHandshake.getInfoHash().equals(peerHandshake.getInfoHash())) {
                    System.out.println("Successfully connected to " + peerPort);
                    leechPool.execute(new DownloadHandler(currentPeerChannel, torrentFile, fileManager, leftPieces));
                } else {
                    System.err.println("Handshakes are different, reject connection");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void distribute(String fileName) throws BadTorrentFileException {
        String postfix = ".torrent";
        if (fileName.length() <= postfix.length()) {
            throw new BadTorrentFileException("Bad name");
        }
        if (!fileName.endsWith(postfix)) {
            throw new BadTorrentFileException("Bad name");
        }
        try {
            torrentFile = TorrentParser.parseTorrent(Constants.PATH + fileName);
        } catch (IOException e) {
            throw new BadTorrentFileException("Failed to load torrent: " + fileName);
        }
    }

    @Override
    public void createTorrent(String fileName) throws TorrentCreateFailureException,
            BadTorrentFileException {
        String torrentFileName = fileName + ".torrent";
        File torrentFile = new File(Constants.PATH + torrentFileName);
        File originalFile = new File(Constants.PATH + fileName);
        try {
            TorrentFileCreator.createTorrent(torrentFile, originalFile, Constants.TRACKER_URL);
        } catch (IOException e) {
            throw new TorrentCreateFailureException("Could not make .torrent file");
        }
        distribute(torrentFileName);
    }

    @Override
    public void close() {
        trackerComm.sendToTracker(Constants.STOP_COMMAND);
        connReceiver.shutdown();
        trackerComm.close();
        try {
            fileManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        leechPool.shutdown();
        try {
            boolean completed = leechPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("Execution was not completed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public String getHandShakeMessage() {
        if (null == torrentFile) {
            return null;
        }
        return new BitTorrentHandshake(torrentFile.getInfo_hash(), peerId).getMessage();
    }

    public Torrent getCurrentTorrentFile() {
        return torrentFile;
    }

}
