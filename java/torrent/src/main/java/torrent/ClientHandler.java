package torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final TrackerServer server;
    private final static String HANDSHAKE_COMMAND = "handshake";
    private final static String SHOW_COMMAND = "show";
    private final static String PEERS_LIST = "peers";
    private final static String WRONG_COMMAND_MSG = "Wrong command";
    private static int availablePort = 777;

    public ClientHandler(Socket socket, TrackerServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    private String getResponse(String command) {
        String[] words = command.split(" ");
        if (words.length < 2) {
            return WRONG_COMMAND_MSG;
        }
        switch (words[0]) {
            case SHOW_COMMAND -> {
                switch (words[1]) {
                    case PEERS_LIST -> {
                        StringBuilder message = new StringBuilder();
                        message.append("List of available hosts: ");
                        for (Socket client: server.getClients()) {
                            message.append(client.getRemoteSocketAddress().toString().substring(1)).append(" ");
                        }
                        return message.toString();
                    }
                    default -> {
                        return WRONG_COMMAND_MSG;
                    }
                }
            }
            case HANDSHAKE_COMMAND -> {
                String address = words[1];
                for (Socket client: server.getClients()) {
                    if (client.getRemoteSocketAddress().toString().substring(1).equals(address)) {
                        String response =  "Available port: " + availablePort;
                        availablePort++;
                    }
                }
            }
        }
        return WRONG_COMMAND_MSG;
    }

    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String command;
            while ((command = in.readLine()) != null) {
                System.out.printf("Sent from the client: %s\n", command);
                String message = getResponse(command);
                out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
