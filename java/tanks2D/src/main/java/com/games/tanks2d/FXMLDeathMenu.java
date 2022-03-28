package com.games.tanks2d;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FXMLDeathMenu {

    @FXML
    private HBox endGameMenuBar;

    @FXML
    public void onReplayButtonClick() {
        Stage stage = (Stage) endGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getGameScene(SceneBuilder.getLastLevel());
        stage.setScene(scene);
    }

    @FXML
    public void onBackButtonClick() {
        Stage stage = (Stage) endGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
