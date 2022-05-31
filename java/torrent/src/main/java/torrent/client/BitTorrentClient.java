package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import torrent.Constants;
import torrent.client.downloader.Downloader;
import torrent.client.downloader.MultyDownloadManager;
import torrent.client.exceptions.*;
import torrent.client.uploader.UploadLauncher;
import torrent.client.uploader.Uploader;
import torrent.client.util.TorrentFileCreator;
import torrent.tracker.TrackerCommandHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BitTorrentClient implements TorrentClient {
    private final String peerId;
    private final TrackerCommunicator trackerComm;
    private final FileManager fileManager;
    private final Downloader downloader;
    private final Map<String, Map<Integer, ArrayList<Integer>>> peersPieces = new HashMap<>();
    private final Map<String, Uploader> uploaders = new HashMap<>();

    public BitTorrentClient() {
        fileManager = new FileManagerImpl();
        trackerComm = new TrackerCommunicatorImpl();
        trackerComm.sendToTracker("get peer_id");
        peerId = trackerComm.receiveFromTracker();
        downloader = new MultyDownloadManager(fileManager, peerId);
    }

    @Override
    public void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException, BadServerReplyException {
        distribute(torrentFileName);
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
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(Constants.PATH + torrentFileName);
        } catch (IOException e) {
            throw new BadTorrentFileException("Could not open torrent file " + torrentFileName);
        }
        downloader.addTorrentForDownloading(torrentFile, peersPieces);
        downloader.launchDownloading();
    }

    @Override
    public void distribute(String fileName) throws BadTorrentFileException {
        ArrayList<Integer> allPieces = new ArrayList<>();
        String postfix = ".torrent";
        if (fileName.length() <= postfix.length()) {
            throw new BadTorrentFileException("Bad name");
        }
        if (!fileName.endsWith(postfix)) {
            throw new BadTorrentFileException("Bad name");
        }
        if (peersPieces.containsKey(fileName)) {
            return;
        }
        Torrent torrentFile;
        try {
            torrentFile = TorrentParser.parseTorrent(Constants.PATH + fileName);
        } catch (IOException e) {
            throw new BadTorrentFileException("Failed to load torrent: " + fileName);
        }
        for (int i = 0; i < torrentFile.getPieces().size(); i++) {
            allPieces.add(i);
        }
        distributePart(torrentFile, fileName, allPieces);
    }

    private void distributePart(Torrent torrentFile, String fileName, ArrayList<Integer> pieces) {
        if (!peersPieces.containsKey(fileName)) {
            peersPieces.put(fileName, new HashMap<>());
        }
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
    public void stopDownloading(String torrentFileName) throws BadTorrentFileException {
        downloader.stopDownloading(torrentFileName);
    }

    @Override
    public void resumeDownloading(String torrentFileName) throws BadTorrentFileException {
        downloader.resumeDownloading(torrentFileName);
    }

    @Override
    public void close() {
        trackerComm.sendToTracker(Constants.STOP_COMMAND);
        for (Uploader uploader : uploaders.values()) {
            uploader.shutdown();
        }
        trackerComm.close();
        downloader.shutdown();
        try {
            fileManager.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
