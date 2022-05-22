package torrent.client;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.handlers.UploadHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;

public class ConnectionsReceiver {
    private ServerSocketChannel serverSocketChannel;
    private Thread connectionsHandlerThread;
    private final Torrent torrentFile;
    private final FileManager fileManager;
    private final String peerId;

    public ConnectionsReceiver(Torrent torrentFile, FileManager fileManager, String peerId) {
        this.torrentFile = torrentFile;
        this.fileManager = fileManager;
        this.peerId = peerId;
        try {
            this.serverSocketChannel = ServerSocketChannel.open();
            InetSocketAddress address = new InetSocketAddress("localhost", 0);
            this.serverSocketChannel.bind(address);
            this.serverSocketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        connectionsHandlerThread = new Thread(new UploadHandler(this.torrentFile,
                this.fileManager, this.peerId, this.serverSocketChannel));
        connectionsHandlerThread.setName(Constants.CONNECTIONS_THREAD_NAME);
        connectionsHandlerThread.setDaemon(true);
        connectionsHandlerThread.start();
    }

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

    public void shutdown() {
        connectionsHandlerThread.interrupt();
        try {
            serverSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
