package com.games.tanks2d;

import com.games.tanks2d.controller.Runner;
import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.Settings;
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
            ApplicationMainClass.buttonClick.play();
        }
        Stage stage = (Stage) endGameMenuBar.getScene().getWindow();
        Runner runner = SceneBuilder.getGameRunner(SceneBuilder.getLastLevel());
        runner.run(stage);
    }

    @FXML
    public void onBackButtonClick() {
        if (Settings.soundsON) {
            ApplicationMainClass.buttonClick.play();
        }
        Stage stage = (Stage) endGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
