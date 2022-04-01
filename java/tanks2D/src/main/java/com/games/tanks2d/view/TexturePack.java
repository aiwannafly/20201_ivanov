package com.games.tanks2d.view;

import com.games.tanks2d.ApplicationMainClass;
import com.games.tanks2d.model.StarShip;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.util.Objects;

public class TexturePack {

    public static ImagePattern getPlayerTexture(StarShip.Direction dir) {
        ImagePattern pat = null;
        switch (dir) {
            case TOP -> pat = TexturePack.imgPlayerShipTop;
            case BOTTOM -> pat = TexturePack.imgPlayerShipBottom;
            case LEFT -> pat = TexturePack.imgPlayerShipLeft;
            case RIGHT -> pat = TexturePack.imgPlayerShipRight;
        }
        return pat;
    }

    public static ImagePattern getEnemyTexture(StarShip.Direction dir) {
        ImagePattern pat = null;
        switch (dir) {
            case TOP -> pat = TexturePack.imgEnemyShipTop;
            case BOTTOM -> pat = TexturePack.imgEnemyShipBottom;
            case LEFT -> pat = TexturePack.imgEnemyShipLeft;
            case RIGHT -> pat = TexturePack.imgEnemyShipRight;
        }
        return pat;
    }

    public final static Image icon = new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
            "images/icon.png")).toString());

    public final static ImagePattern imgMetalBlock = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/metal.jpg")).toString()));

    public final static ImagePattern imgRedBulletHor = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/red_bullet_hor.png")).toString()));
    public final static ImagePattern imgRedBulletVert = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/red_bullet_vert.png")).toString()));

    public final static ImagePattern imgBlueBulletHor = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/blue_bullet_hor.png")).toString()));
    public final static ImagePattern imgBlueBulletVert = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/blue_bullet_vert.png")).toString()));

    public final static ImagePattern imgPlayerShipRight = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/player_ship_right.png")).toString()));
    public final static ImagePattern imgPlayerShipBottom = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/player_ship_bottom.png")).toString()));
    public final static ImagePattern imgPlayerShipLeft = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/player_ship_left.png")).toString()));
    public final static ImagePattern imgPlayerShipTop = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/player_ship_top.png")).toString()));

    public final static ImagePattern imgEnemyShipRight = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/enemy_ship_right.png")).toString()));
    public final static ImagePattern imgEnemyShipBottom = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/enemy_ship_bottom.png")).toString()));
    public final static ImagePattern imgEnemyShipLeft = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/enemy_ship_left.png")).toString()));
    public final static ImagePattern imgEnemyShipTop = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/enemy_ship_top.png")).toString()));

//    public final static ImagePattern imgLeafBlockPattern = new ImagePattern(
//            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
//                    "images/leaf_block.png")).toString()));
//
//    public final static ImagePattern imgBlueBrickPattern = new ImagePattern(
//            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
//                    "images/blue_brick.png")).toString()));
//    public final static  ImagePattern imgGreenBrickPattern = new ImagePattern(
//            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
//                    "images/green_brick.png")).toString()));
//    public final static ImagePattern imgDarkBlueBrickPattern = new ImagePattern(
//            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
//                    "images/dark_blue_brick.png")).toString()));
    public final static ImagePattern imgBlackBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/black_brick.png")).toString()));
    public final static ImagePattern imgGrayBrickPattern = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/gray_brick.png")).toString()));

    public final static ImagePattern imgExplosionPattern = new ImagePattern(
            new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                    "images/explosion.gif")).toString()));
}
