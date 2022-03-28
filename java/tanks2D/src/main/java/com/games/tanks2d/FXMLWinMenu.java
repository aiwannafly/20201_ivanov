package com.games.tanks2d;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class FXMLWinMenu {

    @FXML
    private HBox winGameMenuBar;

    @FXML
    public void onNextButtonClick() {
        Stage stage = (Stage) winGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getLevelsScene();
        stage.setScene(scene);
    }

    @FXML
    public void onBackButtonClick() {
        Stage stage = (Stage) winGameMenuBar.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
