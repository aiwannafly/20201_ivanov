package torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionsHandler implements Runnable {
    private final TorrentClient client;
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);

    public ConnectionsHandler(TorrentClient client, ServerSocket serverSocket) {
        this.client = client;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client server is running");
            while (true) {
                Socket newConnection = serverSocket.accept();
                System.out.println("New peer appeared: " + newConnection.getInetAddress().getHostAddress()+
                         " " + newConnection.getLocalPort());
                PrintWriter out = new PrintWriter(newConnection.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(newConnection.getInputStream()));
                String handShakeMessage = in.readLine();
                // System.out.println("Message: " + handShakeMessage);
                Handshake handshake = new Handshake(handShakeMessage);
                Handshake myHandshake = new Handshake(this.client.getHandShakeMessage());
                if (handshake.getInfoHash().equals(myHandshake.getInfoHash())) {
                    this.client.getPeers().add(newConnection);
                    threadPool.execute(new SeedCommunicator(this.client, newConnection, client.getCurrentTorrent().
                            getName()));
                    System.out.println("Added new client");
                } else {
                    System.out.println("Handshakes messages are different");
                }
                out.println(this.client.getHandShakeMessage());
                out.flush();
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
