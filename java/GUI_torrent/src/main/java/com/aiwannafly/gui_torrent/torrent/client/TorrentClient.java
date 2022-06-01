package com.aiwannafly.gui_torrent.torrent.client;

import com.aiwannafly.gui_torrent.torrent.client.exceptions.*;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public interface TorrentClient extends AutoCloseable {

    void download(String torrentFileName) throws BadTorrentFileException,
            NoSeedsException, ServerNotCorrespondsException, BadServerReplyException;

    void distribute(String torrentFileName) throws BadTorrentFileException;

    void createTorrent(String fileName) throws TorrentCreateFailureException,
            BadTorrentFileException;

    void stopDownloading(String torrentFileName) throws BadTorrentFileException;

    void resumeDownloading(String torrentFileName) throws BadTorrentFileException;

    ObservableList<Integer> getCollectedPieces(String torrentFileName) throws BadTorrentFileException;
}
