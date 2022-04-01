package com.games.tanks2d.controller;

import com.games.tanks2d.ApplicationMainClass;
import com.games.tanks2d.view.SceneBuilder;
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

    public Runner(int levelNum) {
        gameEngine = new GameTanksEngine(levelNum);
        scene = new Scene(gameEngine.getRenderer().getPane(),
                SceneBuilder.WIDTH, SceneBuilder.HEIGHT);
        switch (levelNum) {
            case 2 -> mediaPlayer = new MediaPlayer(SoundPack.MARCH_SOUNDTRACK);
            case 1 -> mediaPlayer = new MediaPlayer(SoundPack.ALERT_SOUNDTRACK);
        }
    }

    public void run(Stage stage) {
        ApplicationMainClass.player.stop();
        mediaPlayer.play();
        Image backImage = new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                "images/background_star_wars.jpg")).toString());
        Background b = new Background(new BackgroundImage(backImage, null,
                null, null, null));
        gameEngine.getRenderer().getPane().setBackground(b);
        scene.setOnKeyPressed(gameEngine::handlePressedKeyEvent);
        scene.setOnKeyReleased(gameEngine::handleReleasedKeyEvent);
        stage.setScene(scene);
        timer.start();
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
            if (reload <= 0) {
                stop();
                mediaPlayer.stop();
                ApplicationMainClass.player.play();
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
