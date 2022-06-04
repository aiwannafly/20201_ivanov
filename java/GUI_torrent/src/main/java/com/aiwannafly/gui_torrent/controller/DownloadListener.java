package com.aiwannafly.gui_torrent.controller;

import com.aiwannafly.gui_torrent.torrent.client.util.ObservableList;
import com.aiwannafly.gui_torrent.view.GUITorrentRenderer;
import com.aiwannafly.gui_torrent.view.Renderer;
import javafx.application.Platform;

import java.util.Set;
import java.util.concurrent.Flow;

public class DownloadListener implements Flow.Subscriber<Boolean> {
    private final GUITorrentRenderer.FileSection fileSection;
    private final ObservableList<Integer> collectedPieces;
    private final Set<String> downloadedTorrents;
    private long lastUpdateTime = System.currentTimeMillis();

    public DownloadListener(GUITorrentRenderer.FileSection fileSection, ObservableList<Integer>
            collectedPieces, Set<String> downloadedTorrents) {
        this.fileSection = fileSection;
        this.collectedPieces = collectedPieces;
        this.downloadedTorrents = downloadedTorrents;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(Boolean item) {
        Platform.runLater(() -> {
            int newPiecesCount = collectedPieces.size() - fileSection.sectionsCount;
            while (fileSection.sectionsCount < collectedPieces.size()) {
                Renderer.instance.renderNewSegmentBar(fileSection);
            }
            showDownloadSpeed(newPiecesCount);
            if (collectedPieces.size() == fileSection.torrent.getPieces().size()) {
                downloadedTorrents.add(fileSection.torrentFileName);
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

    private void showDownloadSpeed(int newPiecesCount) {
        long currentTime = System.currentTimeMillis();
        double estimatedTime = currentTime - lastUpdateTime;
        long downloadedBytes = newPiecesCount * fileSection.torrent.getPieceLength();
        double estimatedTimeSecs = estimatedTime / 1000;
        double speed = downloadedBytes / estimatedTimeSecs;
        long speedKB = (long) speed / 1024;
        lastUpdateTime = currentTime;
        fileSection.dSpeedLabel.setText(String.valueOf(speedKB));
    }
}
