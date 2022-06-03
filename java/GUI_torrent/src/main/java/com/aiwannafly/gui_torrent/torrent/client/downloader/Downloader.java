package com.aiwannafly.gui_torrent.torrent.client.downloader;

import com.aiwannafly.gui_torrent.torrent.ObservableList;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.BadTorrentFileException;
import com.aiwannafly.gui_torrent.torrent.client.exceptions.NoSeedsException;

import java.util.ArrayList;
import java.util.Map;

public interface Downloader {

    void launchDownloading();

    void addTorrentForDownloading(Torrent torrent, Map<Integer, ArrayList<Integer>> peersPieces,
                                  ObservableList<Integer> myPieces)
            throws NoSeedsException;

    void stopDownloading(String torrentFileName) throws BadTorrentFileException;

    void resumeDownloading(String torrentFileName) throws BadTorrentFileException;

    void shutdown();
}
