package torrent;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class TorrentClient {
    private static final String STOP_WORD = "exit";
    private ServerSocket connectionHandlerSocket = null;
    private PrintWriter out;
    private BufferedReader in;
    private final Set<Socket> peers = new HashSet<>();
    private Torrent torrent = null;
    private String peerId = null;
    private Thread connectionsHandlerThread = null;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(8);
    private ExecutorService fileHandler = Executors.newFixedThreadPool(1);
    private final static String USAGE_GUIDE = """
            The list of the commands:
            show peers               | to print a list of all available peers
            handshake <port_id>      | to try to make a connection with the peer
            add <file.torrent>       | to add a new .torrent file
            create <file>            | to make a .torrent file
            download <file.torrent>  | to download a file
            """;

    public TorrentClient() {
        try {
            Socket trackerSocket = new Socket("localhost", TrackerServer.TRACKER_SERVER_PORT);
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

    public void giveFileTask(Runnable r) {
        fileHandler.execute(r);
    }

    public Torrent getCurrentTorrent() {
        return torrent;
    }

    public void waitToCompleteFileTasks() {
        fileHandler.shutdown();
        try {
            boolean completed = fileHandler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("Execution was not completed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileHandler = Executors.newFixedThreadPool(1);
    }

    public boolean downloadTorrent(String torrentFileName) {
        boolean added = addTorrent(torrentFileName);
        if (!added) {
            return false;
        }
        sendMessageToServer("show peers");
        String message = receiveMessage();
        String[] words = message.split(" ");
        int peersCount = words.length - 1;
        if (peersCount == 0) {
            return false;
        }
        for (int i = 1; i <= peersCount; i++) {
            int peerPort = Integer.parseInt(words[i]);
            try {
                Socket currentPeerSocket = new Socket("localhost", peerPort);
                PrintWriter out = new PrintWriter(currentPeerSocket.getOutputStream(), true);
                Handshake myHandshake = new Handshake(getHandShakeMessage());
                out.println(myHandshake.getMessage());
                out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(currentPeerSocket.getInputStream()));
                Handshake peerHandshake = new Handshake(in.readLine());
                if (myHandshake.getInfoHash().equals(peerHandshake.getInfoHash())) {
                    System.out.println("Successfully connected to " + peerPort);
                    threadPool.execute(new LeechCommunicator(this, currentPeerSocket, torrent));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        threadPool.shutdown();
        try {
            boolean completed = threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            if (!completed) {
                System.out.println("Execution was not completed");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean addTorrent(String fileName) {
        try {
            torrent = TorrentParser.parseTorrent(Settings.PATH + fileName);
        } catch (IOException e) {
            System.out.println("Failed to load torrent: " + fileName);
            return false;
        }
        return true;
    }

    public boolean createTorrent(String fileName) {
        String torrentFileName = fileName + ".torrent";
        File torrentFile = new File(Settings.PATH + torrentFileName);
        File originalFile = new File(Settings.PATH + fileName);
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
            case "download": {
                if (words.length < 2) {
                    System.out.println("Incomplete command");
                }
                String fileName = words[1];
                boolean downloaded = downloadTorrent(fileName);
                String postfix = ".torrent";
                if (fileName.length() <= postfix.length()) {
                    System.err.println("Bad file name, it should end with " + postfix);
                    break;
                }
                int originalFileLength = fileName.length() - postfix.length();
                String originalFileName = Settings.PREFIX + fileName.substring(0, originalFileLength);
                if (downloaded) {
                    System.out.println("Torrent file " + originalFileName + " was downloaded successfully");
                } else {
                    System.out.println("Could not download " + originalFileName);
                }
                break;
            }
            case STOP_WORD: {
                sendMessageToServer("exit");
                connectionsHandlerThread.interrupt();
                try {
                    connectionHandlerSocket.close();
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
        client.connectionsHandlerThread = new Thread(new ConnectionsHandler(client, client.connectionHandlerSocket));
        client.connectionsHandlerThread.setName("Connection handler thread");
        client.connectionsHandlerThread.setDaemon(true);
        client.connectionsHandlerThread.start();
        System.out.println(USAGE_GUIDE);
        try (Scanner sc = new Scanner(System.in)) {
            String command = null;
            while (!(STOP_WORD.equalsIgnoreCase(command))) {
                command = sc.nextLine();
                client.executeCommand(command);
            }
        }
    }
}
