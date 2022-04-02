package com.games.tanks2d;

import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.Settings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXMLWinMenu {

    @FXML
    private VBox winGameMenuBar;

    @FXML
    public void onNextButtonClick() {
        if (Settings.soundsON) {
            ApplicationMainClass.buttonClick.play();
        }
        Stage stage = (Stage) winGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getLevelsScene();
        stage.setScene(scene);
    }

    @FXML
    public void onBackButtonClick() {
        if (Settings.soundsON) {
            ApplicationMainClass.buttonClick.play();
        }
        Stage stage = (Stage) winGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
