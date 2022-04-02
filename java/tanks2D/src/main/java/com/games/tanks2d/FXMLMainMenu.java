package com.games.tanks2d;

import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.Settings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class FXMLMainMenu {
    @FXML
    public Button soundButton;

    @FXML
    public Button musicButton;

    @FXML
    private Button playButton;

    @FXML
    protected void onPlayButtonClick() {
        if (Settings.soundsON) {
            ApplicationMainClass.buttonClick.play();
        }
        Stage stage = (Stage) playButton.getScene().getWindow();
        Scene scene = SceneBuilder.getLevelsScene();
        stage.setScene(scene);
    }

    @FXML
    private void onSoundButtonClick() {
        if (Settings.soundsON) {
            ApplicationMainClass.buttonClick.play();
        }
        if (Settings.soundsON) {
            soundButton.setText("TURN ON SOUND");
        } else {
            soundButton.setText("TURN OFF SOUND");
        }
        Settings.soundsON = !Settings.soundsON;
    }

    @FXML
    private void onMusicButtonClick() {
        if (Settings.soundsON) {
            ApplicationMainClass.buttonClick.play();
        }
        if (Settings.musicON) {
            musicButton.setText("TURN ON MUSIC");
            Settings.musicON = false;
            ApplicationMainClass.menuPlayer.stop();
        } else {
            musicButton.setText("TURN OFF MUSIC");
            Settings.musicON = true;
            ApplicationMainClass.menuPlayer.play();
        }
    }

    @FXML
    protected void onExitButtonClick() {
        if (Settings.soundsON) {
            ApplicationMainClass.buttonClick.play();
        }
        Platform.exit();
    }
}