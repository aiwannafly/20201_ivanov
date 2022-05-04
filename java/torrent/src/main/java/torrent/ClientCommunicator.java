package torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientCommunicator implements Runnable {
    private TorrentClient client;
    private ServerSocket serverSocket;

    public ClientCommunicator(TorrentClient client, ServerSocket serverSocket) {
        this.client = client;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        try {
            System.out.println("Client server is running");
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("New peer appeared :" + client.getInetAddress().getHostAddress()+
                        " " + client.getLocalPort());
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String handShakeMessage = in.readLine();
                System.out.println("Message: " + in.readLine());
                if (handShakeMessage.equals(this.client.getHandShakeMessage())) {
                    this.client.getPeers().add(client);
                    System.out.println("Added new client");
                }
                out.println(this.client.getHandShakeMessage());

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
