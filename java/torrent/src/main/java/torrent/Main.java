package torrent;

import torrent.client.BitTorrentClient;
import torrent.client.TorrentClient;
import torrent.client.exceptions.BadTorrentFileException;
import torrent.client.exceptions.NoSeedsException;
import torrent.client.exceptions.ServerNotCorrespondsException;
import torrent.client.exceptions.TorrentCreateFailureException;

import java.util.Scanner;

public class Main {
    private final static String INCOMPLETE_COMMAND = "Incomplete command";
    private final static String INVALID_COMMAND = "Invalid command";
    private final static String ADD_COMMAND = "add";
    private final static String CREATE_COMMAND = "create";
    private final static String DOWNLOAD_COMMAND = "download";
    private final static String STOP_DOWNLOAD_COMMAND = "stop";
    private final static String RESUME_DOWNLOAD_COMMAND = "resume";
    private final static String USAGE_GUIDE = """
            The list of the commands:
            add <file.torrent>       | to add (distribute) a new .torrent file
            create <file>            | to make a .torrent file
            download <file.torrent>  | to download a file
            stop <file.torrent>      | to stop downloading a file
            resume <file.torrent>    | to resume downloading a file
            """;

    private static void executeCommand(TorrentClient client, String command) {
        String[] words = command.split(" ");
        String instruction = words[0];
        switch (instruction) {
            case ADD_COMMAND -> {
                if (words.length < 2) {
                    System.err.println(INCOMPLETE_COMMAND);
                    break;
                }
                String torrentFileName = words[1];
                try {
                    client.distribute(torrentFileName);
                    System.out.println("Torrent file " + torrentFileName + " is distributed");
                } catch (BadTorrentFileException e) {
                    System.err.println("Could not upload " + torrentFileName + ": " + e.getMessage());
                }
            }
            case CREATE_COMMAND -> {
                if (words.length < 2) {
                    System.err.println(INCOMPLETE_COMMAND);
                    break;
                }
                String fileName = words[1];
                try {
                    client.createTorrent(fileName);
                    System.out.println("Torrent file " + fileName + ".torrent was created successfully. " +
                            "It is distributed for others clients.");
                } catch (BadTorrentFileException | TorrentCreateFailureException e) {
                    System.err.println("Could not create a torrent for " + fileName +
                            ": " + e.getMessage());
                }
            }
            case DOWNLOAD_COMMAND -> {
                if (words.length < 2) {
                    System.err.println("Incomplete command");
                    break;
                }
                String fileName = words[1];
                String postfix = ".torrent";
                int originalFileLength = fileName.length() - postfix.length();
                String originalFileName;
                if (originalFileLength <= 0) {
                    originalFileName = "[torrent]";
                } else {
                    originalFileName = Constants.PREFIX + fileName.substring(0, originalFileLength);
                }
                try {
                    client.download(fileName);
                    if (fileName.length() <= postfix.length()) {
                        System.err.println("Bad file name, it should end with " + postfix);
                    }
                } catch (BadTorrentFileException |
                        NoSeedsException | ServerNotCorrespondsException e) {
                    System.err.println("Could not download " + originalFileName
                            + ": " + e.getMessage());
                }
            }
            case STOP_DOWNLOAD_COMMAND -> {
                if (words.length < 2) {
                    System.err.println(INCOMPLETE_COMMAND);
                    break;
                }
                String torrentFileName = words[1];
                try {
                    client.stopDownloading(torrentFileName);
                } catch (BadTorrentFileException e) {
                    System.err.println("=== The file is not downloading now");
                    break;
                }
                System.out.println("=== Downloading of a file " + torrentFileName + " was stopped.");
            }
            case RESUME_DOWNLOAD_COMMAND -> {
                if (words.length < 2) {
                    System.err.println(INCOMPLETE_COMMAND);
                    break;
                }
                String torrentFileName = words[1];
                try {
                    client.resumeDownloading(torrentFileName);
                } catch (BadTorrentFileException e) {
                    System.err.println("=== The file is not downloading now");
                    break;
                }
                System.out.println("=== Downloading of a file " + torrentFileName + " was resumed.");
            }
            case Constants.STOP_COMMAND -> {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            default -> System.err.println(INVALID_COMMAND);
        }
    }

    public static void main(String[] args) {
        try (TorrentClient client = new BitTorrentClient()) {
            System.out.println(USAGE_GUIDE);
            System.out.println("Enter command: ");
            try (Scanner sc = new Scanner(System.in)) {
                String command = null;
                while (!(Constants.STOP_COMMAND.equalsIgnoreCase(command))) {
                    command = sc.nextLine();
                    executeCommand(client, command);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
