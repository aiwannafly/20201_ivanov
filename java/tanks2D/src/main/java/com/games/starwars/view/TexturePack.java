package com.games.starwars.view;

import com.games.starwars.ApplicationMainClass;
import com.games.starwars.model.ships.StarShip;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import java.util.Objects;

public class TexturePack {

    public static ImagePattern getRebellionShipTexture(StarShip.Direction dir) {
        ImagePattern pat = null;
        switch (dir) {
            case TOP -> pat = TexturePack.imgPlayerShipTop;
            case BOTTOM -> pat = TexturePack.imgPlayerShipBottom;
            case LEFT -> pat = TexturePack.imgPlayerShipLeft;
            case RIGHT -> pat = TexturePack.imgPlayerShipRight;
        }
        return pat;
    }

    public static ImagePattern getEmpireStarShipTexture(StarShip.Direction dir) {
        ImagePattern pat = null;
        switch (dir) {
            case TOP -> pat = TexturePack.imgEmpireStarShipTop;
            case BOTTOM -> pat = TexturePack.imgEmpireStarShipBottom;
            case LEFT -> pat = TexturePack.imgEmpireStarShipLeft;
            case RIGHT -> pat = TexturePack.imgEmpireStarShipRight;
        }
        return pat;
    }

    public static ImagePattern getExtStarShipTexture(StarShip.Direction dir) {
        ImagePattern pat = null;
        switch (dir) {
            case TOP -> pat = TexturePack.imgExtStarShipTop;
            case BOTTOM -> pat = TexturePack.imgExtStarShipBottom;
            case LEFT -> pat = TexturePack.imgExtStarShipLeft;
            case RIGHT -> pat = TexturePack.imgExtStarShipRight;
        }
        return pat;
    }

    public static ImagePattern getStarDestroyerTexture(StarShip.Direction dir) {
        ImagePattern pat = null;
        switch (dir) {
            case TOP -> pat = TexturePack.starDestroyerTop;
            case BOTTOM -> pat = TexturePack.starDestroyerBottom;
            case LEFT -> pat = TexturePack.starDestroyerLeft;
            case RIGHT -> pat = TexturePack.starDestroyerRight;
        }
        return pat;
    }

    public static ImagePattern getImagePattern(String imgName) {
        return new ImagePattern(new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
                        imgName)).toString()));
    }

    public final static Image icon = new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
            "images/icon.png")).toString());

    public final static Image backgroundImage = new Image(Objects.requireNonNull(ApplicationMainClass.class.getResource(
            "images/background_star_wars.jpg")).toString());

    public final static ImagePattern starDestroyerRight =
            getImagePattern("images/star_destroyer_right.png");
    public final static ImagePattern starDestroyerBottom =
            getImagePattern("images/star_destroyer_bottom.png");
    public final static ImagePattern starDestroyerLeft =
            getImagePattern("images/star_destroyer_left.png");
    public final static ImagePattern starDestroyerTop =
            getImagePattern("images/star_destroyer_top.png");

    public final static ImagePattern heartIcon = getImagePattern("images/heart.png");
    public final static ImagePattern imgGreenBlastHor = getImagePattern("images/green_blast_hor.png");
    public final static ImagePattern imgGreenBlastVert = getImagePattern("images/green_blast_vert.png");
    public final static ImagePattern imgMetalBlock = getImagePattern("images/metal.png");

    public final static ImagePattern imgRedBulletHor = getImagePattern("images/red_bullet_hor.png");
    public final static ImagePattern imgRedBulletVert = getImagePattern("images/red_bullet_vert.png");

    public final static ImagePattern imgBlueBulletHor = getImagePattern("images/blue_bullet_hor.png");
    public final static ImagePattern imgBlueBulletVert = getImagePattern("images/blue_bullet_vert.png");

    public final static ImagePattern imgPlayerShipRight = getImagePattern("images/player_ship_right.png");
    public final static ImagePattern imgPlayerShipBottom = getImagePattern("images/player_ship_bottom.png");
    public final static ImagePattern imgPlayerShipLeft = getImagePattern("images/player_ship_left.png");
    public final static ImagePattern imgPlayerShipTop = getImagePattern("images/player_ship_top.png");

    public final static ImagePattern imgEmpireStarShipRight = getImagePattern("images/enemy_ship_right.png");
    public final static ImagePattern imgEmpireStarShipBottom = getImagePattern("images/enemy_ship_bottom.png");
    public final static ImagePattern imgEmpireStarShipLeft = getImagePattern("images/enemy_ship_left.png");
    public final static ImagePattern imgEmpireStarShipTop = getImagePattern("images/enemy_ship_top.png");

    public final static ImagePattern imgExtStarShipRight = getImagePattern("images/ext_ship_right.png");
    public final static ImagePattern imgExtStarShipBottom = getImagePattern("images/ext_ship_bottom.png");
    public final static ImagePattern imgExtStarShipLeft = getImagePattern("images/ext_ship_left.png");
    public final static ImagePattern imgExtStarShipTop = getImagePattern("images/ext_ship_top.png");

    public final static ImagePattern imgBlackBrickPattern = getImagePattern("images/black_brick.jpg");
    public final static ImagePattern imgGrayBrickPattern = getImagePattern("images/gray_brick.png");
    public final static ImagePattern imgBlueBrickPattern = getImagePattern("images/blue_brick.png");

    public final static ImagePattern imgExplosionPattern = getImagePattern("images/explosion.gif");
}
