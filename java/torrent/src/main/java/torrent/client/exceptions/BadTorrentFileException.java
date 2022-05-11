package torrent.client.exceptions;

public class BadTorrentFileException extends Exception {
    public BadTorrentFileException(String msg) {
        super(msg);
    }
}
