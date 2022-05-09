package torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionsHandler implements Runnable {
    private final TorrentClient client;
    private final ServerSocket serverSocket;

    public ConnectionsHandler(TorrentClient client, ServerSocket serverSocket) {
        this.client = client;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client server is running");
            while (true) {
                Socket newClient = serverSocket.accept();
                System.out.println("New peer appeared: " + newClient.getInetAddress().getHostAddress()+
                        " " + newClient.getLocalPort());
                PrintWriter out = new PrintWriter(newClient.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(newClient.getInputStream()));
                String handShakeMessage = in.readLine();
                System.out.println("Message: " + handShakeMessage);
                Handshake handshake = new Handshake(handShakeMessage);
                Handshake myHandshake = new Handshake(this.client.getHandShakeMessage());
                if (handshake.getInfoHash().equals(myHandshake.getInfoHash())) {
                    this.client.getPeers().add(newClient);
                    Thread seedThread = new Thread(new SeedCommunicator(newClient, "wallpaper.jpg"));
                    seedThread.setName("Seed thread");
                    seedThread.start();
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
