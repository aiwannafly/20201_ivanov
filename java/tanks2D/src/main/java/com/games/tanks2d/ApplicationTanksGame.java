package com.games.tanks2d;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;


public class ApplicationTanksGame extends Application {
    @Override
    public void start(Stage stage) {
        MediaPlayer player = new MediaPlayer(SoundPack.MAIN_SOUNDTRACK);
        player.setVolume(SoundPack.SOUNDTRACK_VOLUME);
        player.setAutoPlay(true);
        player.play();
        Scene scene = SceneBuilder.getMenuScene();
        stage.setTitle("TANKS 2D");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}