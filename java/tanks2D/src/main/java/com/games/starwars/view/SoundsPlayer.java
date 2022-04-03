package com.games.starwars.view;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

import java.nio.file.Paths;

public class SoundsPlayer {
    private static final AudioClip explosionSound = new AudioClip(
            Paths.get(SoundPack.EXPL_FILE_PATH).toUri().toString());
    private static final AudioClip bigExplosionSound = new AudioClip(
            Paths.get(SoundPack.EXPL_FILE_PATH).toUri().toString());
    private static final AudioClip laserGunSound = new AudioClip(
            Paths.get(SoundPack.LASER_FILE_PATH).toUri().toString());
    private static final AudioClip shipBlastGunSound = new AudioClip(
            Paths.get(SoundPack.SHIP_BLAST_FILE_PATH).toUri().toString());
    private static final AudioClip buttonClick = new AudioClip(Paths.get(SoundPack.BUTTON_FILE_PATH).
            toUri().toString());
    private static MediaPlayer menuPlayer = new MediaPlayer(SoundPack.mainSoundtrack);

    static {
        menuPlayer.setVolume(SoundPack.SOUNDTRACK_VOLUME);
        menuPlayer.setAutoPlay(true);
        buttonClick.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
        explosionSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME / 2);
        laserGunSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
        shipBlastGunSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
        bigExplosionSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME * 2);
    }

    public static void playButtonClick() {
        buttonClick.play();
    }

    public static void playLaserShoot() {
        laserGunSound.play();
    }

    public static void playShipShoot() {
        shipBlastGunSound.play();
    }

    public static void playExplosion() {
        explosionSound.play();
    }

    public static void playBigExplosion() {
        bigExplosionSound.play();
    }

    public static void playMenuSoundtrack() {
        menuPlayer.play();
    }

    public static void stopMenuSoundtrack() {
        menuPlayer.stop();
    }
}
