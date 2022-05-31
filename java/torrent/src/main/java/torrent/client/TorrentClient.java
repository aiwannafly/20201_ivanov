package torrent.client;

import torrent.client.exceptions.*;

public interface TorrentClient extends AutoCloseable {

    void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException, BadServerReplyException;

    void distribute(String torrentFileName) throws BadTorrentFileException;

    void createTorrent(String fileName) throws TorrentCreateFailureException,
            BadTorrentFileException;

    void stopDownloading(String torrentFileName) throws BadTorrentFileException;

    void resumeDownloading(String torrentFileName) throws BadTorrentFileException;

}
