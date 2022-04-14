package com.games.starwars.controller;

import com.games.starwars.view.SceneBuilder;
import com.games.starwars.Settings;
import com.games.starwars.view.SoundPack;
import com.games.starwars.view.SoundsPlayer;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;


public class RunnerImpl implements Runner {
    private MediaPlayer mediaPlayer;
    private final Engine gameEngine;
    boolean isActive = true;
    private final Stage stage;

    public RunnerImpl(Stage stage, int levelNum) {
        this.stage = stage;
        gameEngine = new EngineImpl(levelNum);
        switch (levelNum) {
            case 4 -> mediaPlayer = new MediaPlayer(SoundPack.empireMarchSoundtrack);
            case 2 -> mediaPlayer = new MediaPlayer(SoundPack.lukeVsVaderSoundtrack);
            case 3 -> mediaPlayer = new MediaPlayer(SoundPack.cloneMarchSoundtrack);
            case 1 -> mediaPlayer = new MediaPlayer(SoundPack.anakinVsObiwanSoundtrack);
            default -> mediaPlayer = new MediaPlayer(SoundPack.anakinVsObiwanSoundtrack);
        }
        mediaPlayer.setVolume(SoundPack.SOUNDTRACK_VOLUME);
    }

    public void run() {
        SoundsPlayer.stopMenuSoundtrack();
        if (Settings.musicON) {
            mediaPlayer.play();
        }
        Scene scene = gameEngine.getRenderer().getScene();
        scene.setOnKeyPressed(gameEngine::handlePressedKeyEvent);
        scene.setOnKeyReleased(gameEngine::handleReleasedKeyEvent);
        scene.setOnMousePressed(gameEngine::handleClickEvent);
        scene.setOnMouseReleased(gameEngine::handleClickReleasedEvent);
        stage.setScene(scene);
        timer.start();
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void playMusic() {
        mediaPlayer.play();
    }

    @Override
    public void stopPlayingMusic() {
        mediaPlayer.stop();
    }

    public void stop() {
        timer.stop();
        isActive = false;
        mediaPlayer.stop();
        if (Settings.musicON) {
            SoundsPlayer.playMenuSoundtrack();
        }
    }

    private final AnimationTimer timer = new AnimationTimer() {
        private long lastUpdateTime = 0;
        private final int waitTime = 100;
        private int reload = waitTime;

        @Override
        public void handle(long now) {
            if (now - lastUpdateTime >= 10_000_000) {
                animation();
            }
            lastUpdateTime = now;
        }

        private void animation() {
            Engine.Status status = gameEngine.update();
            if (status == Engine.Status.PAUSE) {
                stop();
                Scene scene = gameEngine.getRenderer().getScene();
                Stage stage = (Stage) scene.getWindow();
                stage.setScene(SceneBuilder.getPauseScene());
            }
            if (reload <= 0) {
                RunnerImpl.this.stop();
                Scene scene = gameEngine.getRenderer().getScene();
                Stage stage = (Stage) scene.getWindow();
                if (status == Engine.Status.WIN) {
                    stage.setScene(SceneBuilder.getWinScene());
                } else {
                    stage.setScene(SceneBuilder.getDeathScene());
                }
            }
            if (status != Engine.Status.IN_PROGRESS) {
                reload--;
            }
            gameEngine.render();
        }
    };
}
