package com.games.tanks2d;

import com.games.tanks2d.view.SoundPack;
import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.TexturePack;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;


public class ApplicationMainClass extends Application {
    public static MediaPlayer player = new MediaPlayer(SoundPack.MAIN_SOUNDTRACK);

    @Override
    public void start(Stage stage) {
        player.setVolume(SoundPack.SOUNDTRACK_VOLUME);
        player.setAutoPlay(true);
        player.play();
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