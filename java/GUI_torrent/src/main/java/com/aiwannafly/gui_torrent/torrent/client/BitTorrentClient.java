package com.aiwannafly.gui_torrent.torrent.client;

import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.TorrentParser;
import com.aiwannafly.gui_torrent.torrent.Constants;
import com.aiwannafly.gui_torrent.torrent.client.downloader.Downloader;
import com.aiwannafly.gui_torrent.torrent.client.downloader.MultyDownloadManager;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.*;
import com.aiwannafly.gui_torrent.torrent.client.uploader.UploadLauncher;
import com.aiwannafly.gui_torrent.torrent.client.uploader.Uploader;
import com.aiwannafly.gui_torrent.torrent.client.util.TorrentFileCreator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.aiwannafly.gui_torrent.torrent.tracker.TrackerCommandHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BitTorrentClient implements TorrentClient {
    private String peerId;
    private TrackerCommunicator trackerComm;
    private final FileManager fileManager;
    private Downloader downloader;
    private final Map<String, Map<Integer, ArrayList<Integer>>> peersPieces = new HashMap<>();
    private final Map<String, Uploader> uploaders = new HashMap<>();
    private final Map<String, ObservableList<Integer>> myPieces = new HashMap<>();

    public BitTorrentClient() {
        fileManager = new FileManagerImpl();
    }

    @Override
    public void download(String torrentFilePath) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException, BadServerReplyException {
        String torrentFileName = torrentFilePath.substring(torrentFilePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(torrentFilePath);
        } catch (IOException e) {
            throw new BadTorrentFileException("Could not open torrent file " + e.getMessage());
        }
        if (trackerComm == null) {
            initTrackerCommunicator();
        }
        ObservableList<Integer> myPieces = FXCollections.observableList(new ArrayList<>());
        this.myPieces.put(torrentFileName, myPieces);
        distributePart(torrentFile, torrentFileName, myPieces);
        trackerComm.sendToTracker("show peers " + torrentFileName);
        String message = trackerComm.receiveFromTracker();
        if (null == message) {
            throw new ServerNotCorrespondsException("Server did not show peers");
        }
        String[] words = message.split(" ");
        if (words.length - 1 == 0) {
            throw new NoSeedsException("No peers are uploading the file at the moment");
        }
        Map<Integer, ArrayList<Integer>> peersPieces = this.peersPieces.get(torrentFileName);
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
            peersPieces.put(peerPort, availablePieces);
        }
        if (downloader == null) {
            downloader = new MultyDownloadManager(fileManager, peerId);
        }
        downloader.addTorrentForDownloading(torrentFile, peersPieces, myPieces);
        downloader.launchDownloading();
    }

    @Override
    public void distribute(String torrentFilePath) throws BadTorrentFileException, ServerNotCorrespondsException {
        ObservableList<Integer> allPieces = FXCollections.observableList(new ArrayList<>());
        String torrentFileName = torrentFilePath.substring(torrentFilePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        myPieces.put(torrentFileName, allPieces);
        String postfix = Constants.POSTFIX;
        if (torrentFileName.length() <= postfix.length()) {
            throw new BadTorrentFileException("Bad name");
        }
        if (!torrentFileName.endsWith(postfix)) {
            throw new BadTorrentFileException("Bad name");
        }
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(torrentFilePath);
        } catch (IOException e) {
            throw new BadTorrentFileException("Failed to load torrent: " + e.getMessage());
        }
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            allPieces.add(i);
        }
        distributePart(torrentFile, torrentFileName, allPieces);
    }

    private void distributePart(Torrent torrentFile, String fileName, ObservableList<Integer> pieces)
    throws ServerNotCorrespondsException {
        if (peersPieces.containsKey(fileName)) {
            return;
        }
        if (trackerComm == null) {
            initTrackerCommunicator();
        }
        peersPieces.put(fileName, new HashMap<>());
        Uploader uploader = new UploadLauncher(torrentFile, fileManager, peerId, pieces);
        uploader.launchDistribution();
        // listen-port 5000 fileName 30 1 2 3 ... 30
        StringBuilder command = new StringBuilder(TrackerCommandHandler.SET_LISTENING_SOCKET + " " +
                uploader.getListeningPort() + " " + fileName + " " + pieces.size());
        for (Integer piece : pieces) {
            command.append(" ").append(piece);
        }
        trackerComm.sendToTracker(command.toString());
        trackerComm.receiveFromTracker();
        uploaders.put(fileName, uploader);
    }

    @Override
    public void createTorrent(String filePath) throws TorrentCreateFailureException,
            BadTorrentFileException, ServerNotCorrespondsException {
        String torrentFilePath = filePath + Constants.POSTFIX;
        String torrentFileName = torrentFilePath.substring(torrentFilePath.lastIndexOf(Constants.PATH_DIVIDER) + 1);
        File torrentFile = new File(Constants.TORRENT_PATH + torrentFileName);
        File originalFile = new File(filePath);
        try {
            TorrentFileCreator.createTorrent(torrentFile, originalFile, Constants.TRACKER_URL);
        } catch (IOException e) {
            throw new TorrentCreateFailureException("Could not make .torrent file");
        }
        distribute(Constants.TORRENT_PATH + torrentFileName);
    }

    @Override
    public void stopDownloading(String torrentFileName) throws BadTorrentFileException {
        downloader.stopDownloading(torrentFileName);
    }

    @Override
    public void resumeDownloading(String torrentFileName) throws BadTorrentFileException {
        downloader.resumeDownloading(torrentFileName);
    }

    @Override
    public ObservableList<Integer> getCollectedPieces(String torrentFileName) throws BadTorrentFileException {
        if (!myPieces.containsKey(torrentFileName)) {
            throw new BadTorrentFileException("=== File not found");
        }
        return myPieces.get(torrentFileName);
    }

    @Override
    public void close() {
        if (trackerComm != null) {
            trackerComm.sendToTracker(Constants.STOP_COMMAND);
            trackerComm.close();
        }
        for (Uploader uploader : uploaders.values()) {
            uploader.shutdown();
        }
        if (downloader != null) {
            downloader.shutdown();
        }
        try {
            fileManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTrackerCommunicator() throws ServerNotCorrespondsException {
        trackerComm = new TrackerCommunicatorImpl();
        trackerComm.sendToTracker("get peer_id");
        peerId = trackerComm.receiveFromTracker();
    }
}
