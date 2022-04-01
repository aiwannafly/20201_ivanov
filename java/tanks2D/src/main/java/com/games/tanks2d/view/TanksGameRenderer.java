package com.games.tanks2d.view;

import com.games.tanks2d.model.*;
import com.games.tanks2d.model.obstacles.FragileBlock;
import com.games.tanks2d.model.obstacles.WallBlock;
import com.games.tanks2d.model.obstacles.SolidBlock;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.shape.Rectangle;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TanksGameRenderer implements Renderer {
    private final GameField gameField;
    private final Map<StarShip, Rectangle> enemyTanks = new HashMap<>();
    private final Map<Obstacle, Rectangle> obstacles = new HashMap<>();
    private final Map<Blast, Rectangle> bullets = new HashMap<>();
    private final Map<Explosion, Rectangle> explosions = new HashMap<>();
    private Rectangle playerTankTexture;
    private final Pane pane = new Pane();
    private final ArrayList<Obstacle> util = new ArrayList<>();
    AudioClip explosionSound = new AudioClip(
            Paths.get(SoundPack.EXPL_FILE_PATH).toUri().toString());
    AudioClip bigExplosionSound = new AudioClip(
            Paths.get(SoundPack.EXPL_FILE_PATH).toUri().toString());
    AudioClip laserGunSound = new AudioClip(
            Paths.get(SoundPack.LASER_FILE_PATH).toUri().toString());

    public TanksGameRenderer(GameField gameField) {
        this.gameField = gameField;
        explosionSound.setVolume(0.2);
        laserGunSound.setVolume(0.4);
        bigExplosionSound.setVolume(0.8);
        initObjects();
    }

    @Override
    public void render() {
        util.clear();
        for (Obstacle o: obstacles.keySet()) {
            if (!gameField.getObstacles().contains(o)) {
                // the obstacle was deleted
                pane.getChildren().remove(obstacles.get(o));
                util.add(o);
            }
        }
        util.forEach(obstacles.keySet()::remove);
        util.clear();
        for (StarShip e: enemyTanks.keySet()) {
            if (!gameField.getEnemyTanks().contains(e)) {
                bigExplosionSound.play();
                pane.getChildren().remove(enemyTanks.get(e));
                util.add(e);
                continue;
            }
            // update coords
            Rectangle texture = enemyTanks.get(e);
            texture.setFill(TexturePack.getEnemyTexture(e.getSide()));
            texture.relocate(e.getX(), e.getY());
        }
        util.forEach(enemyTanks.keySet()::remove);
        if (!gameField.getPlayersTank().isCrippled()) {
            Rectangle texture = playerTankTexture;
            StarShip p = gameField.getPlayersTank();
            texture.setFill(TexturePack.getPlayerTexture(p.getSide()));
            texture.relocate(p.getX(), p.getY());
        } else {
            pane.getChildren().remove(playerTankTexture);
        }
        for (Blast b: gameField.getBullets()) {
            if (!bullets.containsKey(b)) {
                laserGunSound.play();
                Rectangle bTexture = getBulletTexture(b);
                bullets.put(b, bTexture);
                pane.getChildren().add(bTexture);
            }
        }
        util.clear();
        for (Blast b: bullets.keySet()) {
            if (!gameField.getBullets().contains(b)) {
                pane.getChildren().remove(bullets.get(b));
                util.add(b);
                explosionSound.play();
                continue;
            }
            bullets.get(b).relocate(b.getX(), b.getY());
        }
        util.forEach(bullets.keySet()::remove);

        for (Explosion b: gameField.getExplosions()) {
            if (!explosions.containsKey(b)) {
                Rectangle bTexture = new Rectangle(b.getX(),
                        b.getY(), b.getWidth(), b.getHeight());
                bTexture.setFill(TexturePack.imgExplosionPattern);
                explosions.put(b, bTexture);
                pane.getChildren().add(bTexture);
            }
        }
        util.clear();
        for (Explosion b: explosions.keySet()) {
            if (!gameField.getExplosions().contains(b)) {
                pane.getChildren().remove(explosions.get(b));
                util.add(b);
            }
        }
        util.forEach(explosions.keySet()::remove);
    }

    @Override
    public Pane getPane() {
        return pane;
    }

    private Rectangle getBulletTexture(Blast b) {
        Rectangle r = null;
        switch (b.getSide()) {
            case TOP, BOTTOM -> {
                r = new Rectangle(b.getX(), b.getY(),  b.getWidth(), 4 * b.getHeight());
                if (b.getTeam() == StarShip.Team.PLAYERS) {
                    r.setFill(TexturePack.imgBlueBulletVert);
                } else {
                    r.setFill(TexturePack.imgRedBulletVert);
                }
            }
            case RIGHT, LEFT -> {
                r = new Rectangle(b.getX(), b.getY(), 4 * b.getWidth(), b.getHeight());
                if (b.getTeam() == StarShip.Team.PLAYERS) {
                    r.setFill(TexturePack.imgBlueBulletHor);
                } else {
                    r.setFill(TexturePack.imgRedBulletHor);
                }
            }
        }
        return r;
    }

    private void initObjects() {
        for (Obstacle o: gameField.getObstacles()) {
            Rectangle texture = new Rectangle(o.getX(), o.getY(),
                    o.getWidth(), o.getHeight());
            if (o instanceof SolidBlock) {
                texture.setFill(TexturePack.imgGrayBrickPattern);
            } else if (o instanceof FragileBlock) {
                texture.setFill(TexturePack.imgBlackBrickPattern);
            } else if (o instanceof WallBlock) {
                texture.setFill(TexturePack.imgMetalBlock);
            }
            obstacles.put(o, texture);
        }
        pane.getChildren().addAll(obstacles.values());
        for (StarShip t: gameField.getEnemyTanks()) {
            Rectangle tankTexture = new Rectangle(t.getX(), t.getY(), t.getWidth(), t.getHeight());
            enemyTanks.put(t, tankTexture);
        }
        pane.getChildren().addAll(enemyTanks.values());
        StarShip p = gameField.getPlayersTank();
        playerTankTexture = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());
        pane.getChildren().add(playerTankTexture);
    }
}
