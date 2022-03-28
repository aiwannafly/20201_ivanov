package com.games.tanks2d;

import javafx.scene.media.Media;

import java.io.File;

public class SoundPack {
    private final static String PATH = "src/main/resources/com/games/tanks2d/sounds/";
    private final static String EXPL_FILE_PATH = PATH + "explosion.mp3";
    private final static String SHOOT_FILE_PATH = PATH + "shoot.mp3";
    private final static String MAIN_TRACK_FILE_PATH = PATH + "main_soundtrack.mp3";
    public final static Media BOOM_SOUND;
    public final static Media SHOOT_SOUND;
    public final static Media MAIN_SOUNDTRACK;
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
        BOOM_SOUND = loadSound(EXPL_FILE_PATH);
        SHOOT_SOUND = loadSound(SHOOT_FILE_PATH);
        MAIN_SOUNDTRACK = loadSound(MAIN_TRACK_FILE_PATH);
    }
}
