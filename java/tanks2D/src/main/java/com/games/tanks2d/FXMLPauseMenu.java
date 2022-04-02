package com.games.tanks2d;

import com.games.tanks2d.controller.Runner;
import com.games.tanks2d.view.SoundsPlayer;
import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.Settings;
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
        Runner runner = SceneBuilder.getGameRunner(SceneBuilder.getLastLevel());
        runner.run(stage);
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
        Runner runner = SceneBuilder.getGameRunner(SceneBuilder.getLastLevel());
        if (Settings.musicON) {
            musicButton.setText("TURN ON MUSIC");
            Settings.musicON = false;
            runner.mediaPlayer.stop();
            SoundsPlayer.stopMenuSoundtrack();
        } else {
            musicButton.setText("TURN OFF MUSIC");
            Settings.musicON = true;
            runner.mediaPlayer.play();
            SoundsPlayer.playMenuSoundtrack();
        }
    }

    @FXML
    private void onBackButtonClick() {
        if (Settings.soundsON) {
            SoundsPlayer.playButtonClick();
        }
        Runner runner = SceneBuilder.getGameRunner(SceneBuilder.getLastLevel());
        runner.stop();
        Stage stage = (Stage) backButton.getScene().getWindow();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setScene(scene);
    }
}
