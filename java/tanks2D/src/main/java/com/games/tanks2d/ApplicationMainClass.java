package com.games.tanks2d;

import com.games.tanks2d.view.SoundPack;
import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.TexturePack;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.nio.file.Paths;


public class ApplicationMainClass extends Application {
    public static MediaPlayer menuPlayer = new MediaPlayer(SoundPack.MAIN_SOUNDTRACK);
    public static AudioClip buttonClick = new AudioClip(Paths.get(SoundPack.BUTTON_FILE_PATH).
            toUri().toString());

    @Override
    public void start(Stage stage) {
        menuPlayer.setVolume(SoundPack.SOUNDTRACK_VOLUME);
        buttonClick.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
        menuPlayer.setAutoPlay(true);
        menuPlayer.play();
        Scene scene = SceneBuilder.getMenuScene();
        if (null == scene) {
            return;
        }
        stage.getIcons().add(TexturePack.icon);
        stage.setResizable(false);
        stage.setTitle("GALACTIC WARS");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}