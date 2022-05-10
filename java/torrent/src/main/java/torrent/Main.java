package torrent;

import torrent.client.TorrentClient;

import java.util.Scanner;

public class Main {
    private final static String USAGE_GUIDE = """
            The list of the commands:
            show peers               | to print a list of all available peers
            handshake <port_id>      | to try to make a connection with the peer
            add <file.torrent>       | to add a new .torrent file
            create <file>            | to make a .torrent file
            download <file.torrent>  | to download a file
            """;

    public static void main(String[] args) {
        TorrentClient client = new TorrentClient();
        System.out.println(USAGE_GUIDE);
        try (Scanner sc = new Scanner(System.in)) {
            String command = null;
            while (!(Settings.STOP_COMMAND.equalsIgnoreCase(command))) {
                command = sc.nextLine();
                client.executeCommand(command);
            }
        }
    }
}
