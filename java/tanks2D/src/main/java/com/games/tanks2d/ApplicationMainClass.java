package com.games.tanks2d;

import com.games.tanks2d.view.SceneBuilder;
import com.games.tanks2d.view.Settings;
import com.games.tanks2d.view.SoundsPlayer;
import com.games.tanks2d.view.TexturePack;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ApplicationMainClass extends Application {

    @Override
    public void start(Stage stage) {
        Scene scene = SceneBuilder.getMenuScene();
        if (null == scene) {
            System.err.println("aggsasfa");
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