package torrent.client.downloader;

import be.christophedetroyer.torrent.Torrent;
import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;

public interface Downloader {

    void launchDownloading();

    void addTorrentForDownloading(Torrent torrent, int[] peerPorts) throws NoSeedsException;

    void stopDownloading(String torrentFileName) throws BadTorrentFileException;

    void resumeDownloading(String torrentFileName) throws BadTorrentFileException;

    void shutdown();
}
