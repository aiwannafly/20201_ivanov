package torrent.client;

import torrent.Constants;

import java.io.IOException;
import java.net.ServerSocket;

public class ConnectionsReceiver {
    private ServerSocket connectionHandlerSocket = null;
    private Thread connectionsHandlerThread;
    private final BitTorrentClient client;

    public ConnectionsReceiver(BitTorrentClient client) {
        this.client = client;
        try {
            connectionHandlerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        connectionsHandlerThread = new Thread(new ConnectionsHandler(client, this.connectionHandlerSocket));
        connectionsHandlerThread.setName(Constants.CONNECTIONS_THREAD_NAME);
        connectionsHandlerThread.setDaemon(true);
        connectionsHandlerThread.start();
    }

    public int getListeningPort() {
        return connectionHandlerSocket.getLocalPort();
    }

    public void shutdown() {
        connectionsHandlerThread.interrupt();
        try {
            connectionHandlerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
