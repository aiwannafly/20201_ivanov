package com.games.starwars.view;

import com.games.starwars.ApplicationMainClass;
import com.games.starwars.controller.Runner;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

import static com.games.starwars.Settings.FXML_PATH;

public class SceneBuilder {

    private static final Scene menuScene;
    private static final Scene levelsScene;
    private static final Scene pauseScene;
    private static final Scene deathScene;
    private static final Scene winScene;
    private static Runner runner = null;
    public final static int WIDTH = 1530;
    public final static int HEIGHT = 780;
    private static final String STYLESHEET = Objects.requireNonNull(ApplicationMainClass.class.getResource(
            "styles.css")).toExternalForm();
    private static int lastLevelNum = 1;

    static {
        menuScene = loadWithFXMLAndCSS(FXML_PATH + "main-menu.fxml", STYLESHEET);
        levelsScene = loadWithFXMLAndCSS(FXML_PATH + "levels.fxml", STYLESHEET);
        pauseScene = loadWithFXMLAndCSS(FXML_PATH + "pause.fxml", STYLESHEET);
        deathScene = loadWithFXMLAndCSS(FXML_PATH + "end-game.fxml", STYLESHEET);
        winScene = loadWithFXMLAndCSS(FXML_PATH + "win-game.fxml", STYLESHEET);
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

    public static Runner getGameRunner(Stage stage, int level) {
        if (null == runner) {
            runner = new Runner(stage, level);
        }
        if (!runner.isActive()) {
            runner = new Runner(stage, level);
        }
        lastLevelNum = level;
        return runner;
    }

    private static Scene loadWithFXMLAndCSS(String fxmlName, String stylesheetName) {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationMainClass.class.getResource(
                fxmlName));
        Scene scene = null;
        try {
            scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        } catch (IOException exception) {
            System.err.println("Could not load a scene: " + exception.getMessage());
        }
        if (null != scene) {
            scene.getStylesheets().add(stylesheetName);
        }
        return scene;
    }
}
