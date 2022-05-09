package torrent;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class TrackerServer {
    public static final int TRACKER_SERVER_PORT = 1000;
    private final ArrayList<Socket> clients = new ArrayList<>();
    private final static Map<Socket, Integer> clientPorts = new HashMap<>();

    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(TRACKER_SERVER_PORT);
            server.setReuseAddress(true);
            System.out.println("Server is running");
            while (true) {
                Socket client = server.accept();
                System.out.println("New client connected" + client.getInetAddress().getHostAddress()+
                        " " + client.getLocalPort());
                clients.add(client);
                Thread currentClientThread = new Thread(new TrackerCommandHandler(client, this));
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

    public Map<Socket, Integer> getClientPorts() {
        return clientPorts;
    }

    public static void main(String[] args) {
        TrackerServer trackerServer = new TrackerServer();
        trackerServer.run();
    }
}