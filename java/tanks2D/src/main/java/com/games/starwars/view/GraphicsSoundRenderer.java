package com.games.starwars.view;

import com.games.starwars.model.*;
import com.games.starwars.model.factory.FactoryBadConfigsException;
import com.games.starwars.model.factory.FactoryFailureException;
import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.factory.*;
import com.games.starwars.view.textures.blasts.BlastTexture;
import com.games.starwars.view.textures.obstacles.ObstacleTexture;
import com.games.starwars.view.textures.ships.ShipTexture;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GraphicsSoundRenderer implements Renderer {
    private final Scene scene;
    private final GameField gameField;
    private final ArrayList<Rectangle> pointsHP = new ArrayList<>();
    private final Map<StarShip, ShipTexture> enemyShips = new HashMap<>();
    private final Map<Obstacle, ObstacleTexture> obstacles = new HashMap<>();
    private final Map<Blast, BlastTexture> blasts = new HashMap<>();
    private final Map<Explosion, Rectangle> explosions = new HashMap<>();
    private ShipTexture playerShipTexture;
    private final Pane pane = new Pane();
    private final ArrayList<Obstacle> util = new ArrayList<>();
    private final FactoryOfStarShipsTextures shipsFactory = new FactoryOfStarShipsTexturesImpl();
    private final FactoryOfBlastsTextures blastsFactory = new FactoryOfBlastsTexturesImpl();
    private final FactoryOfObstaclesTextures obstaclesFactory = new FactoryOfObstaclesTexturesImpl();

    public GraphicsSoundRenderer(GameField gameField) {
        this.gameField = gameField;
        try {
            shipsFactory.setConfigs(Settings.SHIPS_TEXTURES_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            System.err.println(e.getMessage());
        }
        try {
            blastsFactory.setConfigs(Settings.BLAST_TEXTURES_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            System.err.println(e.getMessage());
        }
        try {
            obstaclesFactory.setConfigs(Settings.OBSTACLE_TEXTURES_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            System.err.println(e.getMessage());
        }
        try {
            initObjects();
        } catch (FactoryFailureException e) {
            System.err.println(e.getMessage());
        }
        initStatBar();
        Background b = new Background(new BackgroundImage(TexturePack.backgroundImage, null,
                null, null, null));
        pane.setBackground(b);
        scene = new Scene(pane, SceneBuilder.WIDTH, SceneBuilder.HEIGHT);
    }

    @Override
    public void render() {
        if (null == playerShipTexture) {
            return;
        }
        updateStatusBar();
        util.clear();
        for (Obstacle o: obstacles.keySet()) {
            if (!gameField.getObstacles().contains(o)) {
                // the obstacle was deleted
                pane.getChildren().remove(obstacles.get(o).getTexture());
                util.add(o);
            }
        }
        util.forEach(obstacles.keySet()::remove);
        util.clear();
        for (StarShip e: enemyShips.keySet()) {
            if (!gameField.getEnemyShips().contains(e)) {
                if (Settings.soundsON) {
                    SoundsPlayer.playBigExplosion();
                }
                pane.getChildren().remove(enemyShips.get(e).getTexture());
                util.add(e);
                continue;
            }
            // update coords
            ShipTexture texture = enemyShips.get(e);
            texture.updateView();
        }
        util.forEach(enemyShips.keySet()::remove);
        if (!gameField.getPlayersShip().isCrippled()) {
            playerShipTexture.updateView();
        } else {
            pane.getChildren().remove(playerShipTexture.getTexture());
        }
        for (Blast b: gameField.getBullets()) {
            if (!blasts.containsKey(b)) {
                BlastTexture bt;
                try {
                    bt = blastsFactory.getTexture(b.getCodeName());
                } catch (FactoryFailureException e) {
                    e.printStackTrace();
                    return;
                }
                bt.setBlast(b);
                if (Settings.soundsON) {
                    bt.playSound();
                }
                blasts.put(b, bt);
                pane.getChildren().add(bt.getTexture());
            }
        }
        util.clear();
        for (Blast b: blasts.keySet()) {
            if (!gameField.getBullets().contains(b)) {
                pane.getChildren().remove(blasts.get(b).getTexture());
                util.add(b);
                if (Settings.soundsON) {
                    SoundsPlayer.playExplosion();
                }
                continue;
            }
            blasts.get(b).updateView();
        }
        util.forEach(blasts.keySet()::remove);

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
    public Scene getScene() {
        return scene;
    }

    private void initStatBar() {
        double size = gameField.getPlayersShip().getWidth();
        int hp = gameField.getPlayersShip().getHP();
        for (int i = 0; i <= Settings.BLOCK_WIDTH / 2; i++) {
            Rectangle rect = new Rectangle(i * size,
                    (Settings.BLOCK_HEIGHT) * size / 2, size, size);
            rect.setFill(Color.BLACK);
            pane.getChildren().add(rect);
        }
        int offset = (Settings.BLOCK_WIDTH / 3);
        for (int i = 0; i <= hp + 1; i++) {
            Rectangle hpPoint = new Rectangle((i + offset) * size,
                    (Settings.BLOCK_HEIGHT) * size / 2, size, size);
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

    private void initObjects() throws FactoryFailureException {
        for (Obstacle o: gameField.getObstacles()) {
            ObstacleTexture t = null;
            try {
                t =obstaclesFactory.getTexture(o.getCodeName());
            } catch (FactoryFailureException e) {
                System.err.println(e.getMessage());
            }
            assert t != null;
            t.setObstacle(o);
            obstacles.put(o, t);
            pane.getChildren().add(t.getTexture());
        }
        for (StarShip s: gameField.getEnemyShips()) {
            ShipTexture texture = shipsFactory.getTexture(s.getCodeName());
            texture.setShip(s);
            enemyShips.put(s, texture);
            pane.getChildren().add(texture.getTexture());
        }
        StarShip p = gameField.getPlayersShip();
        ShipTexture texture =shipsFactory.getTexture(p.getCodeName());
        texture.setShip(p);
        playerShipTexture = texture;
        pane.getChildren().add(texture.getTexture());
    }
}
