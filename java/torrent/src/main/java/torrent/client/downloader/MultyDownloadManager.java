package torrent.client.downloader;

import be.christophedetroyer.torrent.Torrent;
import torrent.Constants;
import torrent.client.FileManager;
import torrent.client.exceptions.NoSeedsException;

import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static torrent.client.downloader.MultyDownloadManager.Status.FINISHED;

public class MultyDownloadManager {
    private final ExecutorService leechPool = Executors.newFixedThreadPool(
            Constants.DOWNLOAD_MAX_THREADS_COUNT);
    private final FileManager fileManager;
    private final String peerId;
    private final ArrayList<Torrent> removalList = new ArrayList<>();
    private final Map<Torrent, DownloadManager> downloadManagers = new HashMap<>();
    private final ExecutorService downloader = Executors.newSingleThreadExecutor();
    private final CompletionService<Status> downloadService =
            new ExecutorCompletionService<>(downloader);
    private final Queue<Torrent> stoppedTorrents = new ArrayDeque<>();

    enum Status {
        FINISHED, NOT_FINISHED
    }

    public MultyDownloadManager(FileManager fileManager, String peerId) {
        this.fileManager = fileManager;
        this.peerId = peerId;
    }

    public void addTorrent(Torrent torrent, int[] peerPorts,
                           ArrayList<Integer> leftPieces) throws NoSeedsException {
        DownloadManager downloadManager = new DownloadManager(torrent,
                fileManager, peerId, peerPorts, leftPieces, leechPool);
        downloadManagers.put(torrent, downloadManager);
    }

    public void run() {
        downloadService.submit(() -> {
            while (!downloadManagers.isEmpty()) {
                for (Torrent torrent: downloadManagers.keySet()) {
                    if (stoppedTorrents.contains(torrent)) {
                        continue;
                    }
                    DownloadManager downloadManager = downloadManagers.get(torrent);
                    DownloadManager.Result result = downloadManager.downloadNextPiece();
                    if (result.status == DownloadManager.Status.FINISHED) {
                        removalList.add(torrent);
                    }
                }
                for (Torrent torrent: removalList) {
                    downloadManagers.remove(torrent).shutdown();
                }
                removalList.clear();
            }
            return FINISHED;
        });
    }

    public void stop(Torrent torrent) {
        stoppedTorrents.add(torrent);
    }

    public void resume(Torrent torrent) {
        stoppedTorrents.remove(torrent);
    }

    public void shutdown() {
        leechPool.shutdown();
        for (Torrent torrent: downloadManagers.keySet()) {
            downloadManagers.get(torrent).shutdown();
        }
        downloader.shutdown();
    }
}
