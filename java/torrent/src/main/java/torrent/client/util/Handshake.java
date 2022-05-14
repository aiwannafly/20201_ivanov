package torrent.client.util;

public interface Handshake {
    String getMessage();

    String getInfoHash();

    String getPeerId();
}
