package com.games.starwars.FXML;

import com.games.starwars.Settings;
import com.games.starwars.controller.Runner;
import com.games.starwars.view.SoundsPlayer;
import com.games.starwars.view.SceneBuilder;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class FXMLPauseMenu {
    @FXML
    public Button soundButton;

    @FXML
    public Button musicButton;

    @FXML
    private Button backButton;

    @FXML
    private void onResumeButtonClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        Stage stage = (Stage) backButton.getScene().getWindow();
        Runner runner = SceneBuilder.getGameRunner(stage, SceneBuilder.getLastLevel());
        runner.run();
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
        Stage stage = (Stage) backButton.getScene().getWindow();
        Runner runner = SceneBuilder.getGameRunner(stage, SceneBuilder.getLastLevel());
        if (Settings.musicON) {
            musicButton.setText("TURN ON MUSIC");
            Settings.musicON = false;
            runner.stopPlayingMusic();
            SoundsPlayer.stopMenuSoundtrack();
        } else {
            musicButton.setText("TURN OFF MUSIC");
            Settings.musicON = true;
            runner.playMusic();
            SoundsPlayer.playMenuSoundtrack();
        }
    }

    @FXML
    private void onBackButtonClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        Stage stage = (Stage) backButton.getScene().getWindow();
        Runner runner = SceneBuilder.getGameRunner(stage, SceneBuilder.getLastLevel());
        runner.stop();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
