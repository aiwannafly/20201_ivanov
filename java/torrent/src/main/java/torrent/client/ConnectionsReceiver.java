package torrent.client;

import torrent.Constants;
import torrent.client.handlers.ConnectionsHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.Random;

public class ConnectionsReceiver {
    private ServerSocketChannel serverSocketChannel;
    private InetSocketAddress address;
    private Thread connectionsHandlerThread;
    private final BitTorrentClient client;
    private static int port = 65200;

    public ConnectionsReceiver(BitTorrentClient client) {
        this.client = client;
        try {
            this.serverSocketChannel = ServerSocketChannel.open();
            Random random = new Random();
            port += random.nextInt(10);
            this.address = new InetSocketAddress("localhost", port);
            System.out.println("PORT: " + address.getPort());
            this.serverSocketChannel.bind(address);
            this.serverSocketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        connectionsHandlerThread = new Thread(new ConnectionsHandler(client, this.serverSocketChannel));
        connectionsHandlerThread.setName(Constants.CONNECTIONS_THREAD_NAME);
        connectionsHandlerThread.setDaemon(true);
        connectionsHandlerThread.start();
    }

    public int getListeningPort() {
        return address.getPort();
    }

    public void shutdown() {
        connectionsHandlerThread.interrupt();
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
