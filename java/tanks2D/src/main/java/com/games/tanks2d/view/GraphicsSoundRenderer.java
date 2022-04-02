package com.games.tanks2d.view;

import com.games.tanks2d.model.*;
import com.games.tanks2d.model.obstacles.FragileBlock;
import com.games.tanks2d.model.obstacles.Obstacle;
import com.games.tanks2d.model.ships.StarDestroyer;
import com.games.tanks2d.model.obstacles.WallBlock;
import com.games.tanks2d.model.obstacles.SolidBlock;
import com.games.tanks2d.model.ships.EmpireStarShip;
import com.games.tanks2d.model.ships.Exterminator;
import com.games.tanks2d.model.ships.StarShip;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphicsSoundRenderer implements Renderer {
    private final GameField gameField;
    private final ArrayList<Rectangle> pointsHP = new ArrayList<>();
    private final Map<StarShip, Rectangle> enemyShips = new HashMap<>();
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
    AudioClip shipBlastGunSound = new AudioClip(
            Paths.get(SoundPack.SHIP_BLAST_FILE_PATH).toUri().toString());

    public GraphicsSoundRenderer(GameField gameField) {
        this.gameField = gameField;
        explosionSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME / 2);
        laserGunSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
        shipBlastGunSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME);
        bigExplosionSound.setVolume(SoundPack.GAME_SOUNDS_VOLUME * 2);
        initObjects();
        initStatBar();
    }

    @Override
    public void render() {
        updateStatusBar();
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
        for (StarShip e: enemyShips.keySet()) {
            if (!gameField.getEnemyTanks().contains(e)) {
                if (Settings.soundsON) {
                    bigExplosionSound.play();
                }
                pane.getChildren().remove(enemyShips.get(e));
                util.add(e);
                continue;
            }
            // update coords
            Rectangle texture = enemyShips.get(e);
            if (e instanceof Exterminator) {
                texture.setFill(TexturePack.getExtStarShipTexture(e.getSide()));
            } else if (e instanceof StarDestroyer) {
                 texture.setFill(TexturePack.getStarDestroyerTexture(e.getSide()));
            } else if (e instanceof EmpireStarShip){
                texture.setFill(TexturePack.getEmpireStarShipTexture(e.getSide()));
            }
            texture.relocate(e.getX(), e.getY());
        }
        util.forEach(enemyShips.keySet()::remove);
        if (!gameField.getPlayersShip().isCrippled()) {
            Rectangle texture = playerTankTexture;
            StarShip p = gameField.getPlayersShip();
            texture.setFill(TexturePack.getPlayerTexture(p.getSide()));
            texture.relocate(p.getX(), p.getY());
        } else {
            pane.getChildren().remove(playerTankTexture);
        }
        for (Blast b: gameField.getBullets()) {
            if (!bullets.containsKey(b)) {
                if (Settings.soundsON) {
                    switch (b.getTeam()) {
                        case STAR_DESTROYER -> shipBlastGunSound.play();
                        case EMPIRE_SHIP, REBELLION_SHIP -> laserGunSound.play();
                    }
                }
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
                if (Settings.soundsON) {
                    explosionSound.play();
                }
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
                if (b.getTeam() == StarShip.Class.REBELLION_SHIP) {
                    r.setFill(TexturePack.imgBlueBulletVert);
                } else if (b.getTeam() == StarShip.Class.EMPIRE_SHIP) {
                    r.setFill(TexturePack.imgRedBulletVert);
                } else if (b.getTeam() == StarShip.Class.STAR_DESTROYER) {
                    r.setFill(TexturePack.imgGreenBlastVert);
                }
            }
            case RIGHT, LEFT -> {
                r = new Rectangle(b.getX(), b.getY(), 4 * b.getWidth(), b.getHeight());
                if (b.getTeam() == StarShip.Class.REBELLION_SHIP) {
                    r.setFill(TexturePack.imgBlueBulletHor);
                } else if (b.getTeam() == StarShip.Class.EMPIRE_SHIP) {
                    r.setFill(TexturePack.imgRedBulletHor);
                } else if (b.getTeam() == StarShip.Class.STAR_DESTROYER) {
                    r.setFill(TexturePack.imgGreenBlastHor);
                }
            }
        }
        return r;
    }

    private void initStatBar() {
        double size = gameField.getPlayersShip().getWidth();
        int hp = gameField.getPlayersShip().getHP();
        for (int i = 0; i <= Settings.LEVEL_WIDTH / 2; i++) {
            Rectangle rect = new Rectangle(i * size,
                    (Settings.LEVEL_HEIGHT) * size / 2, size, size);
            rect.setFill(Color.BLACK);
            pane.getChildren().add(rect);
        }
        int offset = (Settings.LEVEL_WIDTH / 3);
        for (int i = 0; i <= hp + 1; i++) {
            Rectangle hpPoint = new Rectangle((i + offset) * size,
                    (Settings.LEVEL_HEIGHT) * size / 2, size, size);
            if (i == 0 || i == hp + 1) {
                hpPoint.setFill(Color.BLACK);
            } else {
                hpPoint.setFill(TexturePack.heartIcon);
                pointsHP.add(hpPoint);
            }
            pane.getChildren().add(hpPoint);
        }
    }

    private void updateStatusBar() {
        int hp = gameField.getPlayersShip().getHP();
        if (hp == pointsHP.size()) {
            return;
        }
        for (int i = pointsHP.size() - 1; i > hp - 1; i--) {
            pane.getChildren().remove(pointsHP.get(i));
            pointsHP.remove(pointsHP.get(i));
        }
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
            enemyShips.put(t, tankTexture);
        }
        pane.getChildren().addAll(enemyShips.values());
        StarShip p = gameField.getPlayersShip();
        playerTankTexture = new Rectangle(p.getX(), p.getY(), p.getWidth(), p.getHeight());
        pane.getChildren().add(playerTankTexture);
    }
}
