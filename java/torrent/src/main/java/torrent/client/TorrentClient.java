package torrent.client;

import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.exceptions.ServerNotCorrespondsException;
import torrent.client.exceptions.TorrentCreateFailureException;

public interface TorrentClient {
    void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException;

    void upload(String torrentFileName) throws BadTorrentFileException;

    void createTorrent(String fileName) throws TorrentCreateFailureException,
            BadTorrentFileException;

    void sendToTracker(String message);

    String receiveFromTracker();

    void shutdown();

}
