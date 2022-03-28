package com.games.tanks2d;

import javafx.animation.AnimationTimer;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public class Bullet extends Rectangle {
    private final LevelDesigner environment;
    private final double bulletSpeed = 10;
    private final BattleTank ownerTank;
    private final Tank.Direction direction;
    private final ArrayList<Rectangle> intersected = new ArrayList<>();

    public Bullet(double x, double y, double width, double height,
                  LevelDesigner env, BattleTank t, Tank.Direction dir) {
        super(x, y, width, height);
        environment = env;
        ownerTank = t;
        direction = dir;
        switch (direction) {
            case TOP -> setFill(TexturePack.imgBulletTopPattern);
            case BOTTOM -> setFill(TexturePack.imgBulletBottomPattern);
            case RIGHT -> setFill(TexturePack.imgBulletRightPattern);
            case LEFT -> setFill(TexturePack.imgBulletLeftPattern);
        }
        environment.getChildren().add(this);
//        MediaPlayer mediaPlayer = new MediaPlayer(SoundPack.SHOOT_SOUND);
//        mediaPlayer.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
//        mediaPlayer.play();
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
                if (ownerTank.getId().equals("enemy")) {
                    BattleTank tank = environment.getPlayersTank();
                    if (intersects(tank.getLayoutBounds())) {
                        Explosion e = new Explosion(tank.getX(), tank.getY(),
                                tank.getWidth(), tank.getHeight(), environment);
                        environment.destroyTank(tank);
                    }
                } else {
                    for (BattleTank tank: environment.getEnemyTanks()) {
                        if (tank == ownerTank || tank.getId().equals(ownerTank.getId())) {
                            continue;
                        }
                        if (intersects(tank.getLayoutBounds())) {
                            Explosion e = new Explosion(tank.getX(), tank.getY(),
                                    tank.getWidth(), tank.getHeight(), environment);
                            environment.destroyTank(tank);
                        }
                    }
                }
                for (Rectangle obstacle: environment.getObstacles()) {
                    if (intersects(obstacle.getLayoutBounds())) {
                        intersected.add(obstacle);
                    }
                }
                if (!intersected.isEmpty()) {
                    double eX = getX();
                    double eY = getY();
                    double offset = (intersected.get(0).getWidth() - Math.min(getWidth(), getHeight())) / 2;
                    switch (direction) {
                        case TOP, BOTTOM -> eX -= offset;
                        case RIGHT,LEFT -> eY -= offset;
                    }
                    Explosion e = new Explosion(eX, eY,
                            intersected.get(0).getWidth(), intersected.get(0).getHeight(), environment);
                    for (Rectangle obstacle: intersected) {
                        if (obstacle.getId().equals("breakable")) {
                            environment.getChildren().remove(obstacle);
                            environment.destroyObstacle(obstacle);
                        }
                    }
                    environment.getChildren().remove(Bullet.this);
                    stop();
                    return;
                }
                double newX = getX();
                double newY = getY();
                switch (direction) {
                    case TOP -> newY -= bulletSpeed;
                    case BOTTOM -> newY += bulletSpeed;
                    case RIGHT -> newX += bulletSpeed;
                    case LEFT -> newX -= bulletSpeed;
                }
                setX(newX);
                setY(newY);
            }
        };
        timer.start();
    }
}
