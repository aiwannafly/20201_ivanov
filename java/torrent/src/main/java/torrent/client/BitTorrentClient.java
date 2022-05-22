package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import torrent.Constants;
import torrent.client.util.TorrentFileCreator;
import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.exceptions.ServerNotCorrespondsException;
import torrent.client.exceptions.TorrentCreateFailureException;
import torrent.tracker.TrackerCommandHandler;

import java.io.*;

public class BitTorrentClient implements TorrentClient {
    private ConnectionsReceiver connReceiver = null;
    private final String peerId;
    private final TrackerCommunicator trackerComm;
    private final FileManager fileManager;

    public BitTorrentClient() {
        fileManager = new FileManagerImpl();
        trackerComm = new TrackerCommunicatorImpl();
        trackerComm.sendToTracker("get peer_id");
        peerId = trackerComm.receiveFromTracker();
    }

    @Override
    public void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException {
        distribute(torrentFileName);
        trackerComm.sendToTracker("show peers " + torrentFileName);
        String message = trackerComm.receiveFromTracker();
        if (null == message) {
            throw new ServerNotCorrespondsException("Server did not show peers");
        }
        String[] words = message.split(" ");
        int peersCount = words.length - 1;
        if (peersCount == 0) {
            throw new NoSeedsException("No peers are uploading the file at the moment");
        }
        int[] peerPorts = new int[peersCount];
        for (int i = 1; i <= peersCount; i++) {
            peerPorts[i - 1] = Integer.parseInt(words[i]);
        }
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(Constants.PATH + torrentFileName);
        } catch (IOException e) {
            throw new BadTorrentFileException("Could not open torrent file " + torrentFileName);
        }
        DownloadManager downloadManager;
        downloadManager = new DownloadManager(torrentFile, fileManager, peerId, peerPorts);
        downloadManager.download();
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
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(Constants.PATH + fileName);
        } catch (IOException e) {
            throw new BadTorrentFileException("Failed to load torrent: " + fileName);
        }
        connReceiver = new ConnectionsReceiver(torrentFile, fileManager, peerId);
        connReceiver.run();
        String command = TrackerCommandHandler.SET_LISTENING_SOCKET + " " + connReceiver.getListeningPort() +
                " " + fileName;
        trackerComm.sendToTracker(command);
        trackerComm.receiveFromTracker();
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
    public void stopDownloading() {

    }

    @Override
    public void resumeDownloading() {

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
    }
}
