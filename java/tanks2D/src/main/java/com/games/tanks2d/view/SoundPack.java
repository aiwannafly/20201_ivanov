package com.games.tanks2d.view;

import javafx.scene.media.Media;

import java.io.File;

public class SoundPack {
    private final static String PATH = "src/main/resources/com/games/tanks2d/sounds/";
    public final static String BUTTON_FILE_PATH = PATH + "button.mp3";
    public final static String EXPL_FILE_PATH = PATH + "explosion.mp3";
    public final static String LASER_FILE_PATH = PATH + "shoot_laser.mp3";
    public final static String SHIP_BLAST_FILE_PATH = PATH + "ship_blast.mp3";
    public final static String CLONE_MARCH_FILE_PATH = PATH + "march.mp3";
    public final static String EMPIRE_MARCH_FILE_PATH = PATH + "level3.mp3";
    public final static String ALERT_FILE_PATH = PATH + "level1.mp3";
    private final static String MAIN_TRACK_FILE_PATH = PATH + "main_star_wars_theme.mp3";
    public static double SOUNDTRACK_VOLUME = 0.5;
    public static double GAME_SOUNDS_VOLUME = 0.25;
    public final static Media mainSoundtrack;
    public final static Media cloneMarchSoundtrack;
    public final static Media anakinVsObiwanSoundtrack;
    public final static Media empireMarchSoundtrack;

    private static Media loadSound(String filePath) {
        File musicFile = new File(filePath);
        if (!musicFile.exists()) {
            System.err.println("File " + filePath + " was not found.\n");
        }
        return new Media(musicFile.toURI().toString());
    }

    static {
        mainSoundtrack = loadSound(MAIN_TRACK_FILE_PATH);
        cloneMarchSoundtrack = loadSound(CLONE_MARCH_FILE_PATH);
        anakinVsObiwanSoundtrack = loadSound(ALERT_FILE_PATH);
        empireMarchSoundtrack = loadSound(EMPIRE_MARCH_FILE_PATH);
    }
}
