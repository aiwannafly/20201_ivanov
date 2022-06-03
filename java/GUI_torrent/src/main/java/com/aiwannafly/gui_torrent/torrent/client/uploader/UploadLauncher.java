package com.aiwannafly.gui_torrent.torrent.client.uploader;

import com.aiwannafly.gui_torrent.torrent.client.tracker_communicator.TrackerCommunicator;
import com.aiwannafly.gui_torrent.torrent.client.util.ObservableList;
import com.aiwannafly.gui_torrent.torrent.client.util.torrent.Torrent;
import com.aiwannafly.gui_torrent.torrent.Constants;
import com.aiwannafly.gui_torrent.torrent.client.file_manager.FileManager;
import com.aiwannafly.gui_torrent.torrent.tracker.TrackerCommandHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class UploadLauncher implements Uploader {
    private ServerSocketChannel serverSocketChannel;
    private Thread connectionsHandlerThread;
    private final Torrent torrentFile;
    private final FileManager fileManager;
    private final String peerId;
    private final ObservableList<Integer> availablePieces;
    private final TrackerCommunicator trackerComm;

    public UploadLauncher(Torrent torrentFile, FileManager fileManager, String peerId,
                          ObservableList<Integer> availablePieces, TrackerCommunicator trackerComm) {
        this.torrentFile = torrentFile;
        this.fileManager = fileManager;
        this.availablePieces = availablePieces;
        this.trackerComm = trackerComm;
        this.peerId = peerId;
        try {
            this.serverSocketChannel = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress("localhost", 0);
            this.serverSocketChannel.bind(address);
            this.serverSocketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // listen-port 5000 fileName 30 1 2 3 ... 30
    }

    @Override
    public void launchDistribution() {
        String torrentFileName = torrentFile.getName() + Constants.POSTFIX;
        StringBuilder command = new StringBuilder(TrackerCommandHandler.SET_LISTENING_SOCKET + " " +
                getListeningPort() + " " + torrentFileName + " " + availablePieces.size());
        for (Integer piece : availablePieces) {
            command.append(" ").append(piece);
        }
        trackerComm.sendToTracker(command.toString());
        trackerComm.receiveFromTracker();
        connectionsHandlerThread = new Thread(new UploadHandler(this.torrentFile,
                this.fileManager, this.peerId, this.serverSocketChannel, this.availablePieces));
        connectionsHandlerThread.setName(Constants.CONNECTIONS_THREAD_NAME);
        connectionsHandlerThread.setDaemon(true);
        connectionsHandlerThread.start();
    }

    @Override
    public int getListeningPort() {
        String addr;
        try {
            addr = serverSocketChannel.getLocalAddress().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        int port_idx = 0;
        for (int i = 0; i < addr.length(); i++) {
            if (addr.charAt(i) == ':') {
                port_idx = i + 1;
                break;
            }
        }
        return Integer.parseInt(addr.substring(port_idx));
    }

    @Override
    public void shutdown() {
        connectionsHandlerThread.interrupt();
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
