package com.games.starwars;

import com.games.starwars.view.SceneBuilder;
import com.games.starwars.view.SoundsPlayer;
import com.games.starwars.view.textures.TexturePack;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ApplicationMainClass extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = SceneBuilder.getMenuScene();
        if (null == scene) {
            System.err.println("Main menu scene was not loaded");
            return;
        }
        SoundsPlayer.playMenuSoundtrack();
        stage.getIcons().add(TexturePack.icon);
        stage.setResizable(false);
        stage.setTitle(Settings.WINDOW_NAME);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}