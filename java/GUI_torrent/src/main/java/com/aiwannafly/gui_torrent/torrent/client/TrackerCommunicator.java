package com.aiwannafly.gui_torrent.torrent.client;

public interface TrackerCommunicator {

    void sendToTracker(String message);

    String receiveFromTracker();

    void close();
}
