package torrent;

public class Main {
    public static void main(String[] args) {
        TrackerServer trackerServer = new TrackerServer();
        trackerServer.run();
    }
}
