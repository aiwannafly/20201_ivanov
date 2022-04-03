package com.games.starwars;

import com.games.starwars.view.SoundsPlayer;
import com.games.starwars.view.SceneBuilder;
import com.games.starwars.view.Settings;
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
            SoundsPlayer.playButtonClick();
        }
        Stage stage = (Stage) playButton.getScene().getWindow();
        Scene scene = SceneBuilder.getLevelsScene();
        stage.setScene(scene);
    }

    @FXML
    private void onSoundButtonClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
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
            SoundsPlayer.playButtonClick();
        }
        if (Settings.musicON) {
            musicButton.setText("TURN ON MUSIC");
            Settings.musicON = false;
            SoundsPlayer.stopMenuSoundtrack();
        } else {
            musicButton.setText("TURN OFF MUSIC");
            Settings.musicON = true;
            SoundsPlayer.playMenuSoundtrack();
        }
    }

    @FXML
    protected void onExitButtonClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        Platform.exit();
    }
}