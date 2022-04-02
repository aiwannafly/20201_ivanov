package com.games.tanks2d.view;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

import java.nio.file.Paths;

public class SoundsPlayer {
    private static final AudioClip buttonClick = new AudioClip(Paths.get(SoundPack.BUTTON_FILE_PATH).
            toUri().toString());
    private static MediaPlayer menuPlayer = new MediaPlayer(SoundPack.mainSoundtrack);

    static {
        menuPlayer.setVolume(SoundPack.SOUNDTRACK_VOLUME);
        menuPlayer.setAutoPlay(true);
        buttonClick.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
    }

    public static void playButtonClick() {
        buttonClick.play();
    }

    public static void playMenuSoundtrack() {
        menuPlayer.play();
    }

    public static void stopMenuSoundtrack() {
        menuPlayer.stop();
    }
}
