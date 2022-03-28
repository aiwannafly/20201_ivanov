package com.games.tanks2d;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FXMLPauseMenu {
    @FXML
    private VBox pauseMenuBar;

    @FXML
    private Button backButton;

    @FXML
    private void onResumeButtonClick() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = SceneBuilder.getGameScene(SceneBuilder.getLastLevel());
        stage.setScene(scene);
    }

    @FXML
    private void onBackButtonClick() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
