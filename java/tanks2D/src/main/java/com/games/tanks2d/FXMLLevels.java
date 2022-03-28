package com.games.tanks2d;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FXMLLevels {
    @FXML
    private HBox menu;
    @FXML
    private Button backButton;

    private void setLevel(int level) {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = SceneBuilder.getGameScene(level);
        stage.setScene(scene);
    }

    @FXML
    protected void onLevelOneClick() {
        setLevel(1);
    }

    @FXML
    protected void onLevelTwoClick() {
        setLevel(2);
    }

    @FXML
    protected void onBackClick() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setTitle("TANKS 2D");
        stage.setScene(scene);
    }
}
