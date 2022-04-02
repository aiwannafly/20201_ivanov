package com.games.tanks2d;

import com.games.tanks2d.controller.Runner;
import com.games.tanks2d.view.SoundsPlayer;
import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.Settings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class FXMLLevels {

    @FXML
    private Button backButton;

    private void setLevel(int level) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Runner runner = SceneBuilder.getGameRunner(level);
        runner.run(stage);
    }

    @FXML
    protected void onLevelOneClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        setLevel(1);
    }

    @FXML
    protected void onLevelTwoClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        setLevel(2);
    }

    @FXML
    protected void onLevelThreeClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        setLevel(3);
    }

    @FXML
    protected void onBackClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setTitle("TANKS 2D");
        stage.setScene(scene);
    }
}
