package com.games.tanks2d.view;

import com.games.tanks2d.ApplicationMainClass;
import com.games.tanks2d.controller.Runner;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Objects;

public class SceneBuilder {
    private static Runner runner = null;
    private static Scene menuScene = null;
    private static Scene levelsScene = null;
    private static Scene gameScene = null;
    private static Scene pauseScene = null;
    private static Scene deathScene = null;
    private static Scene winScene = null;
    public final static int WIDTH = 1440 + 30 * 3;
    public final static int HEIGHT = 780;
    private static final String STYLESHEET = Objects.requireNonNull(ApplicationMainClass.class.getResource(
            "styles.css")).toExternalForm();
    private static int lastLevelNum = 1;

    private static Scene loadWithFXMLAndCSS(String fxmlName, String stylesheetName) {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationMainClass.class.getResource(
                fxmlName));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        } catch (IOException exception) {
            System.err.println("Could not load a scene.");
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

    public static Runner getGameRunner(int level) {
        if (null == runner) {
            runner = new Runner(level);
        }
        if (!runner.isActive()) {
            runner = new Runner(level);
        }
        lastLevelNum = level;
        return runner;
    }
}
