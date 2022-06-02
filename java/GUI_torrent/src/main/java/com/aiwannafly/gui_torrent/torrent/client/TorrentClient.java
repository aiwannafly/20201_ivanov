package com.aiwannafly.gui_torrent.torrent.client;

import com.aiwannafly.gui_torrent.torrent.client.exceptions.*;
import javafx.collections.ObservableList;


public interface TorrentClient extends AutoCloseable {

    void download(String torrentFilePath) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException, BadServerReplyException;

    void distribute(String torrentFilePath) throws BadTorrentFileException;

    void createTorrent(String filePath) throws TorrentCreateFailureException,
            BadTorrentFileException;

    void stopDownloading(String torrentFileName) throws BadTorrentFileException;

    void resumeDownloading(String torrentFileName) throws BadTorrentFileException;

    ObservableList<Integer> getCollectedPieces(String torrentFileName) throws BadTorrentFileException;
}
