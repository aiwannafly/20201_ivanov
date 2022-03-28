package com.games.tanks2d;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class FXMLMainMenu {
    @FXML
    private VBox menu;

    @FXML
    private Label welcomeText;

    @FXML
    private Button playButton;

    @FXML
    protected void onPlayButtonClick() throws IOException {
        Stage stage = (Stage) playButton.getScene().getWindow();
        Scene scene = SceneBuilder.getLevelsScene();
        stage.setScene(scene);
    }

    @FXML
    protected void onExitButtonClick() {
        Platform.exit();
    }
}