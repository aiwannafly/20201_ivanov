package torrent.client;

public interface TrackerCommunicator {

    void sendToTracker(String message);

    String receiveFromTracker();

    void close();
}
