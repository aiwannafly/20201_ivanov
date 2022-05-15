package torrent.client;

import torrent.client.util.BitTorrentHandshake;
import torrent.client.util.Handshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ConnectionsHandler implements Runnable {
    private final BitTorrentClient client;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    public ConnectionsHandler(BitTorrentClient client, ServerSocket serverSocket) {
        this.client = client;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Socket newConnection;
                try {
                    newConnection = serverSocket.accept();
                } catch (SocketException e) {
                    break;
                }
                System.out.println("New peer connected: " + newConnection.getInetAddress().getHostAddress()+
                         " " + newConnection.getLocalPort());
                PrintWriter out = new PrintWriter(newConnection.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
                String handShakeMessage = in.readLine();
                Handshake handshake = new BitTorrentHandshake(handShakeMessage);
                synchronized (client) {
                    Handshake myHandshake = new BitTorrentHandshake(this.client.getHandShakeMessage());
                    if (handshake.getInfoHash().equals(myHandshake.getInfoHash())) {
                        threadPool.execute(new UploadHandler(newConnection, client.getCurrentTorrentFile().
                                getName()));
                    } else {
                        System.err.println("Handshakes messages are different");
                    }
                    out.println(this.client.getHandShakeMessage());
                    out.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
