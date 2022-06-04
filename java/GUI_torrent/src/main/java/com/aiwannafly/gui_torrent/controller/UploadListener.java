package com.aiwannafly.gui_torrent.controller;

import com.aiwannafly.gui_torrent.view.GUITorrentRenderer;
import javafx.application.Platform;

import java.util.concurrent.Flow;

public class UploadListener implements Flow.Subscriber<Boolean> {
    private final GUITorrentRenderer.FileSection fileSection;
    private long lastUpdateTime = System.currentTimeMillis();

    public UploadListener(GUITorrentRenderer.FileSection fileSection) {
        this.fileSection = fileSection;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {

    }

    @Override
    public void onNext(Boolean item) {
        Platform.runLater(() -> {
            System.out.println("next");
            showUploadSpeed(1);
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }

    private void showUploadSpeed(int newPiecesCount) {
        long currentTime = System.currentTimeMillis();
        double estimatedTime = currentTime - lastUpdateTime;
        long downloadedBytes = newPiecesCount * fileSection.torrent.getPieceLength();
        double estimatedTimeSecs = estimatedTime / 1000;
        double speed = downloadedBytes / estimatedTimeSecs;
        long speedKB = (long) speed / 1024;
        lastUpdateTime = currentTime;
        fileSection.uSpeedLabel.setText(String.valueOf(speedKB));
    }
}
