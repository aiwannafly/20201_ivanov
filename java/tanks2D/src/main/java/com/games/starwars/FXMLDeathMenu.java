package com.games.starwars;

import com.games.starwars.controller.Runner;
import com.games.starwars.view.SoundsPlayer;
import com.games.starwars.view.SceneBuilder;
import com.games.starwars.view.Settings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXMLDeathMenu {

    @FXML
    public Label redLabel;

    @FXML
    public Button redButton1;

    @FXML
    public Button redButton2;

    @FXML
    private VBox endGameMenuBar;

    @FXML
    public void onReplayButtonClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        Stage stage = (Stage) endGameMenuBar.getScene().getWindow();
        Runner runner = SceneBuilder.getGameRunner(stage, SceneBuilder.getLastLevel());
        runner.run();
    }

    @FXML
    public void onBackButtonClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        Stage stage = (Stage) endGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
