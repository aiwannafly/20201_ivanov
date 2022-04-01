package com.games.tanks2d.view;

import javafx.scene.media.Media;

import java.io.File;

public class SoundPack {
    private final static String PATH = "src/main/resources/com/games/tanks2d/sounds/";
    public final static String EXPL_FILE_PATH = PATH + "explosion.mp3";
    public final static String LASER_FILE_PATH = PATH + "shoot_laser.mp3";
    public final static String MARCH_FILE_PATH = PATH + "march.mp3";
    public final static String ALERT_FILE_PATH = PATH + "imperial_alert.mp3";
    private final static String MAIN_TRACK_FILE_PATH = PATH + "main_star_wars_theme.mp3";
    public final static Media MAIN_SOUNDTRACK;
    public final static Media MARCH_SOUNDTRACK;
    public final static Media ALERT_SOUNDTRACK;
    public final static double SOUNDTRACK_VOLUME = 0.8;
    public final static double GAME_SOUNDS_VOLUME = 0.2;

    private static Media loadSound(String filePath) {
        File musicFile = new File(filePath);
        if (!musicFile.exists()) {
            System.err.println("File " + filePath + " was not found.\n");
        }
        return new Media(musicFile.toURI().toString());
    }

    static {
        MAIN_SOUNDTRACK = loadSound(MAIN_TRACK_FILE_PATH);
        MARCH_SOUNDTRACK = loadSound(MARCH_FILE_PATH);
        ALERT_SOUNDTRACK = loadSound(ALERT_FILE_PATH);
    }
}
