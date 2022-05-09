package torrent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TrackerCommandHandler implements Runnable {
    private final Socket clientSocket;
    private final TrackerServer server;
    private final static int PEER_ID_LENGTH = 20;
    private final static String GET_COMMAND = "get";
    private final static String SET_SERVER_SOCKET = "server-port";
    private final static String HANDSHAKE_COMMAND = "handshake";
    private final static String SHOW_COMMAND = "show";
    private final static String PEERS_LIST = "peers";
    private final static String PEER_ID = "peer_id";
    private final static String WRONG_COMMAND_MSG = "Wrong command";
    private final static String INCOMPLETE_COMMAND_MSG = "Incomplete command";
    private PrintWriter out = null;
    private BufferedReader in = null;

    public TrackerCommandHandler(Socket socket, TrackerServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    private String generatePeerId() {
        if (!server.getClientPorts().containsKey(clientSocket)) {
            return null;
        }
        StringBuilder peerId = new StringBuilder();
        peerId.append("PEER_AIW_");
        peerId.append(server.getClientPorts().get(clientSocket));
        peerId.append(peerId.toString().hashCode());
        return peerId.substring(0, PEER_ID_LENGTH);
    }

    private String getResponse(String command) {
        String[] words = command.split(" ");
        switch (words[0]) {
            case GET_COMMAND: {
                if (words.length < 2) {
                    return INCOMPLETE_COMMAND_MSG;
                }
                switch (words[1]) {
                    case PEER_ID: {
                        return generatePeerId();
                    }
                    default: {
                        return WRONG_COMMAND_MSG;
                    }
                }
            }
            case SHOW_COMMAND: {
                if (words.length < 2) {
                    return INCOMPLETE_COMMAND_MSG;
                }
                switch (words[1]) {
                    case PEERS_LIST: {
                        StringBuilder message = new StringBuilder();
                        message.append("Peers: ");
                        if (server.getClients().size() == 1) {
                            message.append("none");
                        }
                        for (Socket client: server.getClients()) {
                            if (client == clientSocket) {
                                continue;
                            }
                            if (server.getClientPorts().containsKey(client)) {
                                message.append(server.getClientPorts().get(client)).append(" ");
                            }
                        }
                        return message.toString();
                    }
                    default: {
                        return WRONG_COMMAND_MSG;
                    }
                }
            }
            case SET_SERVER_SOCKET: {
                Integer port = Integer.parseInt(words[1]);
                server.getClientPorts().put(clientSocket, port);
            }
        }
        return WRONG_COMMAND_MSG;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while (true) {
                String command = in.readLine();
                if (null == command) {
                    break;
                }
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
