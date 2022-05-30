package torrent.client.exceptions;

public class UnknownMessageTypeException extends Exception {
    public UnknownMessageTypeException(String msg) {
        super(msg);
    }
}
