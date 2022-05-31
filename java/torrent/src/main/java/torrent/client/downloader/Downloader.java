package torrent.client.downloader;

import be.christophedetroyer.torrent.Torrent;
import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;

import java.util.ArrayList;
import java.util.Map;

public interface Downloader {

    void launchDownloading();

    void addTorrentForDownloading(Torrent torrent, Map<Integer, ArrayList<Integer>> peersPieces)
            throws NoSeedsException;

    void stopDownloading(String torrentFileName) throws BadTorrentFileException;

    void resumeDownloading(String torrentFileName) throws BadTorrentFileException;

    void shutdown();
}
