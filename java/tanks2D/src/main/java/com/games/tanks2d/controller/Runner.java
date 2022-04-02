package com.games.tanks2d.controller;

import com.games.tanks2d.ApplicationMainClass;
import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.Settings;
import com.games.tanks2d.view.SoundPack;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.util.Objects;

public class Runner {
    public MediaPlayer mediaPlayer;
    private final Engine gameEngine;
    private final Scene scene;
    boolean isActive = true;

    public Runner(int levelNum) {
        gameEngine = new EngineImpl(levelNum);
        scene = new Scene(gameEngine.getRenderer().getPane(),
                SceneBuilder.WIDTH, SceneBuilder.HEIGHT);
        switch (levelNum) {
            case 3 -> mediaPlayer = new MediaPlayer(SoundPack.EMPIRE_MARCH_SOUNDTRACK);
            case 2 -> mediaPlayer = new MediaPlayer(SoundPack.CLONE_MARCH_SOUNDTRACK);
            case 1 -> mediaPlayer = new MediaPlayer(SoundPack.ALERT_SOUNDTRACK);
        }
        mediaPlayer.setVolume(SoundPack.SOUNDTRACK_VOLUME);
    }

    public void run(Stage stage) {
        ApplicationMainClass.menuPlayer.stop();
        if (Settings.musicON) {
            mediaPlayer.play();
        }
        Image backImage = new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                "images/background_star_wars.jpg")).toString());
        Background b = new Background(new BackgroundImage(backImage, null,
                null, null, null));
        gameEngine.getRenderer().getPane().setBackground(b);
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

    public void stop() {
        timer.stop();
        isActive = false;
        mediaPlayer.stop();
        if (Settings.musicON) {
            ApplicationMainClass.menuPlayer.play();
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
                Stage stage = (Stage) scene.getWindow();
                stage.setScene(SceneBuilder.getPauseScene());
            }
            if (reload <= 0) {
                Runner.this.stop();
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
