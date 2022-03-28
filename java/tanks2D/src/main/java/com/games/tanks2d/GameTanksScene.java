package com.games.tanks2d;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GameTanksScene implements GameScene {
    private LevelDesigner level = null;
    FXMLLoader fxmlGameFieldLoader = new FXMLLoader(ApplicationTanksGame.class.getResource(
            "game-field.fxml"));
    private final Pane fieldPane = fxmlGameFieldLoader.load();
    private boolean isAvailable = true;
    private int levelNum = 1;

    public GameTanksScene(int lvl) throws IOException, LevelFailLoadException {
        levelNum = lvl;
    }

    public void pauseGame() {
        level.stopAction();
        Stage stage = (Stage) fieldPane.getScene().getWindow();
        Scene pauseScene = SceneBuilder.getPauseScene();
        stage.setScene(pauseScene);
    }

    public void continueGame() {
        level.continueAction();
    }

    @Override
    public boolean isActive() {
        return isAvailable;
    }

    enum GameStatus {
        WIN, DIED
    }

    public void endGame(GameStatus status) {
        isAvailable = false;
        level.stopAction();
        level = null;
        Stage stage = (Stage) fieldPane.getScene().getWindow();
        if (status == GameStatus.DIED) {
            Scene endScene = SceneBuilder.getDeathScene();
            stage.setScene(endScene);
        } else {
            Scene winScene = SceneBuilder.getWinScene();
            stage.setScene(winScene);
        }
    }

    @Override
    public Scene getScene() {
        Scene scene = new Scene(fieldPane, SceneBuilder.WIDTH, SceneBuilder.HEIGHT);
        level = new LevelDesigner();
        try {
            level.loadLevel("level" + levelNum);
        } catch (LevelFailLoadException e) {
            e.printStackTrace();
        }
        fieldPane.getChildren().setAll(level);
        level.getPlayersTank().release();
        String stylesheet = Objects.requireNonNull(SceneBuilder.class.getResource(
                "styles.css")).toExternalForm();
        scene.getStylesheets().add(stylesheet);
        timer.start();
        return scene;
    }

    AnimationTimer timer = new AnimationTimer() {
        private long lastUpdateTime = 0;
        private final int WAIT_TIME = 30;
        private int reloadTime = WAIT_TIME;

        @Override
        public void handle(long now) {
            if (now - lastUpdateTime >= Level.DELAY) {
                check();
                lastUpdateTime = now;
            }
        }

        private void check() {
            if (level.getEnemyTanks().isEmpty()) {
                if (reloadTime <= 0) {
                    endGame(GameStatus.WIN);
                    stop();
                }
                reloadTime--;
            } else if (!level.getPlayersTank().isAlive()) {
                if (reloadTime <= 0) {
                    endGame(GameStatus.DIED);
                    stop();
                }
                reloadTime--;
            }
        }
    };
}
