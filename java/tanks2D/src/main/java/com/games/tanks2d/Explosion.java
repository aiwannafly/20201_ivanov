package com.games.tanks2d;

import javafx.animation.AnimationTimer;
import javafx.scene.shape.Rectangle;
import javafx.scene.media.MediaPlayer;

public class Explosion extends Rectangle {
    private final LevelDesigner environment;
    private long lifeTime = 10;

    public Explosion(double x, double y, double width, double height,
                     LevelDesigner env) {
        super(x, y, width, height);
        setFill(TexturePack.imgExplosionPattern);
        environment = env;
        environment.getChildren().add(this);
//        MediaPlayer mediaPlayer = new MediaPlayer(SoundPack.BOOM_SOUND);
//        mediaPlayer.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
//        mediaPlayer.play();
        timer.start();
    }

    AnimationTimer timer = new AnimationTimer() {
        private long lastUpdateTime = 0;

        @Override
        public void handle(long now) {
            if (now - lastUpdateTime >= Level.DELAY) {
                animation();
                lastUpdateTime = now;
            }
        }
        private void animation() {
            if (lifeTime == 0) {
                environment.getChildren().remove(Explosion.this);
                stop();
            } else {
                lifeTime--;
            }
        }
    };
}
