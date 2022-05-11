package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import torrent.BitTorrentHandshake;
import torrent.Constants;
import torrent.Handshake;
import torrent.TorrentFileCreator;
import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.exceptions.ServerNotCorrespondsException;
import torrent.client.exceptions.TorrentCreateFailureException;
import torrent.tracker.TrackerServer;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BitTorrentClient implements TorrentClient {
    private ConnectionsReceiver connectionsReceiver;
    private PrintWriter out;
    private BufferedReader in;
    private Torrent torrentFile = null;
    private String peerId = null;
    private final ExecutorService leechPool = Executors.newFixedThreadPool(8);
    private ExecutorService fileThread = Executors.newFixedThreadPool(1);

    public BitTorrentClient() {
        try {
            Socket trackerSocket = new Socket("localhost", TrackerServer.TRACKER_SERVER_PORT);
            out = new PrintWriter(trackerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
            connectionsReceiver = new ConnectionsReceiver(this);
            connectionsReceiver.run();
            sendPortToTracker();
            receiveFromTracker();
            sendToTracker("get peer_id");
            peerId = receiveFromTracker();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendToTracker(String msg) {
        out.println(msg);
        out.flush();
    }

    @Override
    public String receiveFromTracker() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException {
        upload(torrentFileName);
        sendToTracker("show peers");
        String message = receiveFromTracker();
        if (null == message) {
            throw new ServerNotCorrespondsException("Server did not show peers");
        }
        String[] words = message.split(" ");
        int peersCount = words.length - 1;
        if (peersCount == 0) {
            throw new NoSeedsException("No peers are uploading the file at the moment");
        }
        for (int i = 1; i <= peersCount; i++) {
            int peerPort = Integer.parseInt(words[i]);
            try {
                Socket currentPeerSocket = new Socket("localhost", peerPort);
                PrintWriter out = new PrintWriter(currentPeerSocket.getOutputStream(), true);
                Handshake myHandshake = new BitTorrentHandshake(getHandShakeMessage());
                out.println(myHandshake.getMessage());
                out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(currentPeerSocket.getInputStream()));
                Handshake peerHandshake = new BitTorrentHandshake(in.readLine());
                if (myHandshake.getInfoHash().equals(peerHandshake.getInfoHash())) {
                    System.out.println("Successfully connected to " + peerPort);
                    leechPool.execute(new DownloadHandler(this, currentPeerSocket, torrentFile));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    @Override
    public void upload(String fileName) throws BadTorrentFileException {
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
            TorrentFileCreator.createTorrent(torrentFile, originalFile, "127.0.0.1");
        } catch (IOException e) {
            throw new TorrentCreateFailureException("Could not make .torrent file");
        }
        try {
            upload(torrentFileName);
        } catch (BadTorrentFileException e) {
            throw e;
        }
    }

    @Override
    public void shutdown() {
        sendToTracker(Constants.STOP_COMMAND);
        fileThread.shutdown();
        connectionsReceiver.shutdown();
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHandShakeMessage() {
        if (null == torrentFile) {
            return null;
        }
        return new BitTorrentHandshake(torrentFile.getInfo_hash(), peerId).getMessage();
    }

    private void sendPortToTracker() {
        String msg = "server-port " + connectionsReceiver.getListeningPort();
        out.println(msg);
        out.flush();
    }

    public void giveFileTask(Runnable r) {
        fileThread.execute(r);
    }

    public Torrent getCurrentTorrentFile() {
        return torrentFile;
    }

    public void waitToCompleteFileTasks() {
        fileThread.shutdown();
        try {
            boolean completed = fileThread.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("Execution was not completed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileThread = Executors.newFixedThreadPool(1);
    }
}
