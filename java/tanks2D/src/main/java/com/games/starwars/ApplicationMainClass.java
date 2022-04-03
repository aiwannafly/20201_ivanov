package com.games.starwars;

import com.games.starwars.model.factory.FactoryBadConfigsException;
import com.games.starwars.model.factory.FactoryFailureException;
import com.games.starwars.model.factory.FactoryOfObjects;
import com.games.starwars.model.factory.ReflexiveFactoryOfObjects;
import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.SceneBuilder;
import com.games.starwars.view.Settings;
import com.games.starwars.view.SoundsPlayer;
import com.games.starwars.view.TexturePack;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class ApplicationMainClass extends Application {

    @Override
    public void start(Stage stage) {
//        FactoryOfObjects f = new ReflexiveFactoryOfObjects();
//        try {
//            f.setConfigs("model_configs.txt");
//        } catch (FactoryBadConfigsException e) {
//            System.err.println(e.getMessage());
//            return;
//        }
//        Object o = null;
//        try {
//            o = f.getObject('r');
//        } catch (FactoryFailureException e) {
//            System.err.println(e.getMessage());
//            return;
//        }
//        StarShip ship = (StarShip) o;
//        if (null == ship) {
//            System.out.println("NULL");
//        } else {
//            System.out.println("OMG");
//        }
        Scene scene = SceneBuilder.getMenuScene();
        if (null == scene) {
            System.err.println("Main menu scene was not found");
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