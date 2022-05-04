package torrent;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

class TorrentClient {
    private static final String PATH = "src/main/resources/torrent/";
    private static final String STOP_WORD = "exit";
    private Socket clientSocket = null;
    private ServerSocket serverSocket = null;
    private PrintWriter out;
    private BufferedReader in;
    private final Set<Socket> peers = new HashSet<>();
    private Torrent torrent = null;
    private static final String PSTR = "BitTorrent protocol";
    private static final int PSTRLEN = 19;
    private static final int RESERVED_LEN = 8;
    private String peerId = null;

    enum ConnectionState {
        CHOKED, UNCHOKED
    }

    public TorrentClient() {
        try {
            clientSocket = new Socket("localhost", TrackerServer.TRACKER_SERVER_PORT);
            serverSocket = new ServerSocket(0);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            sendServerSocket();
            receiveMessage();
            sendMessageToServer("get peer_id");
            peerId = receiveMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean addTorrent(String fileName) {
        try {
            torrent = TorrentParser.parseTorrent(PATH + fileName);
        } catch (IOException e) {
            System.out.println("Failed to load torrent: " + fileName);
            return false;
        }
        return true;
    }

    public String getHandShakeMessage() {
        return (char) PSTRLEN +
                PSTR +
                String.valueOf((char) 0).repeat(RESERVED_LEN) +
                torrent.getInfo_hash() +
                peerId;
    }

    public Set<Socket> getPeers() {
        return peers;
    }

    public void sendServerSocket() {
        String msg = "server-port " + serverSocket.getLocalPort();
        out.println(msg);
        out.flush();
    }

    public void sendMessageToServer(String msg) {
        out.println(msg);
        out.flush();
    }

    public String receiveMessage() {
        try {
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ServerSocket getServer() {
        return serverSocket;
    }

    private void executeCommand(String command) {
        String[] words = command.split(" ");
        switch (words[0]) {
            case "add": {
                if (words.length < 2) {
                    System.out.println("Incomplete command");
                }
                String torrentFileName = words[1];
                addTorrent(torrentFileName);
                System.out.println("Torrent file was uploaded successfully");
                break;
            }
            case "handshake": {
                if (words.length < 2) {
                    System.out.println("Incomplete command");
                }
                int peerPort = Integer.parseInt(words[1]);
                try {
                    Socket socket = new Socket("localhost", peerPort);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(getHandShakeMessage());
                    BufferedReader in = new
                            BufferedReader(new InputStreamReader(socket.getInputStream()));
                    System.out.println("Reply: " + in.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                break;
            }
            default: {
                sendMessageToServer(command);
                System.out.println("=== Server=== " + receiveMessage());
            }
        }
    }

    public static void main(String[] args) {
        TorrentClient client = new TorrentClient();
        Thread thread = new Thread(new ClientCommunicator(client, client.serverSocket));
        thread.start();
        try (Scanner sc = new Scanner(System.in)) {
            String command = null;
            while (!(STOP_WORD.equalsIgnoreCase(command))) {
                command = sc.nextLine();
                client.executeCommand(command);
            }
        }
    }
}
