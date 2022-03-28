package com.games.tanks2d;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Objects;

public class SceneBuilder {
    private static Scene menuScene = null;
    private static Scene levelsScene = null;
    private static Scene gameScene = null;
    private static Scene pauseScene = null;
    private static Scene deathScene = null;
    private static Scene winScene = null;
    private static GameScene gameWidget = null;
    public final static int WIDTH = 1440;
    public final static int HEIGHT = 720;
    private static final String STYLESHEET = Objects.requireNonNull(SceneBuilder.class.getResource(
            "styles.css")).toExternalForm();
    private static int lastLevelNum = 1;

    private static Scene loadWithFXMLAndCSS(String fxmlName, String stylesheetName) {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationTanksGame.class.getResource(
                fxmlName));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
        if (null != scene) {
            scene.getStylesheets().add(stylesheetName);
        }
        return scene;
    }

    static {
        menuScene = loadWithFXMLAndCSS("main-menu.fxml", STYLESHEET);
        levelsScene = loadWithFXMLAndCSS("levels.fxml", STYLESHEET);
        pauseScene = loadWithFXMLAndCSS("pause.fxml", STYLESHEET);
        deathScene = loadWithFXMLAndCSS("end-game.fxml", STYLESHEET);
        winScene = loadWithFXMLAndCSS("win-game.fxml", STYLESHEET);
    }

    public static Scene getDeathScene() {
        return deathScene;
    }

    public static Scene getLevelsScene() {
        return levelsScene;
    }

    public static Scene getMenuScene() {
        return menuScene;
    }

    public static Scene getPauseScene() {
        return pauseScene;
    }

    public static Scene getWinScene() {
        return winScene;
    }

    public static int getLastLevel() {
        return lastLevelNum;
    }

    public static Scene getGameScene(int level) {
        if (gameWidget != null) {
            if (gameWidget.isActive()) {
                gameWidget.continueGame();
                return gameScene;
            }
        }
        try {
            gameWidget = new GameTanksScene(level);
        } catch (IOException | LevelFailLoadException e) {
            e.printStackTrace();
        }
        if (null == gameWidget) {
            return null;
        }
        gameScene = gameWidget.getScene();
        lastLevelNum = level;
        return gameScene;
    }
}
