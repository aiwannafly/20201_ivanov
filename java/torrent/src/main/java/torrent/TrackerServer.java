package torrent;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

class TrackerServer {
    private final ArrayList<Socket> clients = new ArrayList<>();

    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(Settings.TRACKER_SERVER_PORT);
            server.setReuseAddress(true);
            System.out.println("Server is running");
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected" + client.getInetAddress().getHostAddress()+
                        " " + client.getLocalPort());
                clients.add(client);
                Thread currentClientThread = new Thread(new ClientHandler(client, this));
                currentClientThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<Socket> getClients() {
        return clients;
    }

}