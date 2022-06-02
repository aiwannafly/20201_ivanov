package com.aiwannafly.gui_torrent.torrent.client;

import com.aiwannafly.gui_torrent.TrackerServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TrackerCommunicatorImpl implements TrackerCommunicator {
    private PrintWriter out;
    private BufferedReader in;

    public TrackerCommunicatorImpl() {
        try {
            Socket trackerSocket = new Socket("localhost", TrackerServer.PORT);
            out = new PrintWriter(trackerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendToTracker(String msg) {
        out.println(msg);
        out.flush();
    }

    @Override
    public String receiveFromTracker() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
