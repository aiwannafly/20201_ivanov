package torrent;

public interface Handshake {
    String getMessage();

    String getInfoHash();

    String getPeerId();
}
