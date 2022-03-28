package com.games.tanks2d;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.util.Objects;

public class TexturePack {
    public final static ImagePattern imgLeafBlockPattern = new ImagePattern(
            new Image(Objects.requireNonNull(TexturePack.class.getResource(
                    "images/leaf_block.png")).toString()));

    public final static ImagePattern imgRedBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(TexturePack.class.getResource(
                    "images/hd_brick.png")).toString()));

    public final static ImagePattern imgBlueBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(TexturePack.class.getResource(
                    "images/blue_brick.png")).toString()));
    public final static  ImagePattern imgGreenBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(TexturePack.class.getResource(
                    "images/green_brick.png")).toString()));
    public final static ImagePattern imgDarkBlueBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(TexturePack.class.getResource(
                    "images/dark_blue_brick.png")).toString()));
    public final static ImagePattern imgBlackBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(TexturePack.class.getResource(
                    "images/black_brick.png")).toString()));
    public final static ImagePattern imgGrayBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(TexturePack.class.getResource(
                    "images/gray_brick.png")).toString()));

    public final static ImagePattern imgPlayerTankRight = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_right.png")).toString()));
    public final static ImagePattern imgPlayerTankLeft = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_left.png")).toString()));
    public final static ImagePattern imgPlayerTankTop = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_top.png")).toString()));
    public final static ImagePattern imgPlayerTankBottom = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_bottom.png")).toString()));

    public final static ImagePattern imgEnemyTankRight = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_enemy_right.png")).toString()));
    public final static ImagePattern imgEnemyTankLeft = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_enemy_left.png")).toString()));
    public final static ImagePattern imgEnemyTankTop = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_enemy_top.png")).toString()));
    public final static ImagePattern imgEnemyTankBottom = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/tank_enemy_bottom.png")).toString()));

    public final static ImagePattern imgBulletTopPattern = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/bullet_top.png")).toString()));
    public final static ImagePattern imgBulletBottomPattern = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/bullet_bottom.png")).toString()));
    public final static ImagePattern imgBulletLeftPattern = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/bullet_left.png")).toString()));
    public final static ImagePattern imgBulletRightPattern = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/bullet_right.png")).toString()));
    public final static ImagePattern imgExplosionPattern = new ImagePattern(
            new Image(Objects.requireNonNull(SceneBuilder.class.getResource(
                    "images/explosion.png")).toString()));
}
