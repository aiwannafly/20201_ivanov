package torrent.client.uploader;

public interface Uploader {
    void launchDistribution();

    int getListeningPort();

    void shutdown();
}
