package torrent;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;

import java.io.*;
import java.net.*;
import java.util.*;

class TorrentClient {
    private static final String PATH = "src/main/resources/torrent/";
    private static final String STOP_WORD = "exit";
    private Socket trackerSocket = null;
    private Socket clientSocket = null;
    private ServerSocket connectionHandlerSocket = null;
    private PrintWriter out;
    private BufferedReader in;
    private final Set<Socket> peers = new HashSet<>();
    private Torrent torrent = null;
    private String peerId = null;
    private ConnectionType currentConnection = ConnectionType.TRACKER_SERVER;

    enum ConnectionType {
        TRACKER_SERVER, OTHER_CLIENT
    }

    public TorrentClient() {
        try {
            trackerSocket = new Socket("localhost", TrackerServer.TRACKER_SERVER_PORT);
            connectionHandlerSocket = new ServerSocket(0);
            out = new PrintWriter(trackerSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
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

    public boolean createTorrent(String fileName) {
        String torrentFileName = fileName + ".torrent";
        File torrentFile = new File(PATH + torrentFileName);
        File originalFile = new File(PATH + fileName);
        try {
            TorrentCreator.createTorrent(torrentFile, originalFile, "127.0.0.1");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getHandShakeMessage() {
        return new Handshake(torrent.getInfo_hash(), peerId).getMessage();
    }

    public Set<Socket> getPeers() {
        return peers;
    }

    public void sendServerSocket() {
        String msg = "server-port " + connectionHandlerSocket.getLocalPort();
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
        return connectionHandlerSocket;
    }

    private void executeCommand(String command) {
        String[] words = command.split(" ");
        switch (words[0]) {
            case "add": {
                if (words.length < 2) {
                    System.out.println("Incomplete command");
                }
                String torrentFileName = words[1];
                boolean added = addTorrent(torrentFileName);
                if (added) {
                    System.out.println("Torrent file " + torrentFileName + " was uploaded successfully");
                } else {
                    System.out.println("Could not upload " + torrentFileName);
                }
                break;
            }
            case "create": {
                if (words.length < 2) {
                    System.out.println("Incomplete command");
                }
                String fileName = words[1];
                boolean created = createTorrent(fileName);
                if (created) {
                    System.out.println("Torrent file " + fileName + ".torrent was created successfully");
                } else {
                    System.out.println("Could not create a torrent for " + fileName);
                }
                break;
            }
            case "handshake", "hs": {
                if (words.length < 2) {
                    System.out.println("Incomplete command");
                }
                int peerPort = Integer.parseInt(words[1]);
                try {
                    clientSocket = new Socket("localhost", peerPort);
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    Handshake myHandshake = new Handshake(getHandShakeMessage());
                    out.println(myHandshake.getMessage());
                    out.flush();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    Handshake handshake = new Handshake(in.readLine());
                    System.out.println("Reply: " + handshake.getMessage());
                    if (myHandshake.getInfoHash().equals(handshake.getInfoHash())) {
                        System.out.println("Successfully connected to " + peerPort);
                        currentConnection = ConnectionType.OTHER_CLIENT;
                        this.in = in;
                        this.out = out;
                        Thread communicationThread = new Thread(new LeechCommunicator(clientSocket, torrent));
                        communicationThread.start();
                    }
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

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public static void main(String[] args) {
        TorrentClient client = new TorrentClient();
        Thread thread = new Thread(new ConnectionsHandler(client, client.connectionHandlerSocket));
        thread.start();
        client.addTorrent("wallpaper.jpg.torrent");
        System.out.println("file.txt.torrent was uploaded");
        try (Scanner sc = new Scanner(System.in)) {
            String command = null;
            while (!(STOP_WORD.equalsIgnoreCase(command))) {
                command = sc.nextLine();
                client.executeCommand(command);
            }
        }
    }
}
