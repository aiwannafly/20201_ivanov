package com.games.starwars.view;

import javafx.scene.media.Media;

import java.io.File;

import static com.games.starwars.Settings.SOUNDS_PATH;

public class SoundPack {
    public final static String BUTTON_FILE_PATH = SOUNDS_PATH + "button.mp3";
    public final static String EXPL_FILE_PATH = SOUNDS_PATH + "explosion.mp3";
    public final static String LASER_FILE_PATH = SOUNDS_PATH + "shoot_laser.mp3";
    public final static String SHIP_BLAST_FILE_PATH = SOUNDS_PATH + "ship_blast.mp3";
    public final static String CLONE_MARCH_FILE_PATH = SOUNDS_PATH + "march.mp3";
    public final static String EMPIRE_MARCH_FILE_PATH = SOUNDS_PATH + "level3.mp3";
    public final static String LUKE_VS_VADER_FILE_PATH = SOUNDS_PATH + "Luke_vs_Vader.mp3";
    public final static String ALERT_FILE_PATH = SOUNDS_PATH + "level1.mp3";
    private final static String MAIN_TRACK_FILE_PATH = SOUNDS_PATH + "main_star_wars_theme.mp3";
    public static double SOUNDTRACK_VOLUME = 0.8;
    public static double GAME_SOUNDS_VOLUME = 0.2;
    public static Media mainSoundtrack = null;
    public static Media cloneMarchSoundtrack = null;
    public static Media anakinVsObiwanSoundtrack = null;
    public static Media empireMarchSoundtrack = null;
    public static Media lukeVsVaderSoundtrack = null;

    private static Media loadSound(String filePath) throws SoundNotFoundException {
        File musicFile = new File(filePath);
        if (!musicFile.exists()) {
            throw new SoundNotFoundException("File " + filePath + " was not found.\n");
        }
        return new Media(musicFile.toURI().toString());
    }

    static {
        try {
            mainSoundtrack = loadSound(MAIN_TRACK_FILE_PATH);
        } catch (SoundNotFoundException e) {
            e.printStackTrace();
        }
        try {
            cloneMarchSoundtrack = loadSound(CLONE_MARCH_FILE_PATH);
        } catch (SoundNotFoundException e) {
            e.printStackTrace();
        }
        try {
            anakinVsObiwanSoundtrack = loadSound(ALERT_FILE_PATH);
        } catch (SoundNotFoundException e) {
            e.printStackTrace();
        }
        try {
            empireMarchSoundtrack = loadSound(EMPIRE_MARCH_FILE_PATH);
        } catch (SoundNotFoundException e) {
            e.printStackTrace();
        }
        try {
            lukeVsVaderSoundtrack = loadSound(LUKE_VS_VADER_FILE_PATH);
        } catch (SoundNotFoundException e) {
            e.printStackTrace();
        }
    }
}
