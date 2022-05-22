package torrent.client;

import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.exceptions.ServerNotCorrespondsException;
import torrent.client.exceptions.TorrentCreateFailureException;

public interface TorrentClient extends AutoCloseable {

    void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException;

    void distribute(String torrentFileName) throws BadTorrentFileException;

    void createTorrent(String fileName) throws TorrentCreateFailureException,
            BadTorrentFileException;

    void stopDownloading(String torrentFileName) throws BadTorrentFileException;

    void resumeDownloading(String torrentFileName) throws NoSeedsException,
            ServerNotCorrespondsException, BadTorrentFileException;

}
