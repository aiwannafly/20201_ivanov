package torrent;

import java.io.*;
import java.net.*;
import java.util.*;

class TorrentClient {
    private Socket serverSocket = null;
    private PrintWriter out;
    private BufferedReader in;
    private static final String STOP_WORD = "exit";

    enum ConnectionState {
        CHOKED, UNCHOKED
    }

    public TorrentClient() {
        try {
            serverSocket = new Socket("localhost", Settings.TRACKER_SERVER_PORT);
            out = new PrintWriter(serverSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            String line = null;
            while (!(STOP_WORD.equalsIgnoreCase(line))) {
                line = sc.nextLine();
                out.println(line);
                out.flush();
                try {
                    System.out.println("=== Server=== " + in.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}