package torrent.client.exceptions;

public class BadServerReplyException extends Exception {
    public BadServerReplyException(String msg) {
        super(msg);
    }
}
