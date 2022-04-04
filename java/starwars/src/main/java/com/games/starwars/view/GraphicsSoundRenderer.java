package com.games.starwars.view;

import com.games.starwars.Settings;
import com.games.starwars.factory.Factory;
import com.games.starwars.factory.FactoryImpl;
import com.games.starwars.model.*;
import com.games.starwars.factory.FactoryBadConfigsException;
import com.games.starwars.factory.FactoryFailureException;
import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.textures.TexturePack;
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
    private Scene scene = null;
    private final GameField gameField;
    private final ArrayList<Rectangle> pointsHP = new ArrayList<>();
    private final Map<StarShip, ShipTexture> enemyShips = new HashMap<>();
    private final Map<Obstacle, ObstacleTexture> obstacles = new HashMap<>();
    private final Map<Blast, BlastTexture> blasts = new HashMap<>();
    private final Map<Explosion, Rectangle> explosions = new HashMap<>();
    private ShipTexture playerShipTexture;
    private final Pane pane = new Pane();
    private final ArrayList<Obstacle> util = new ArrayList<>();
    private final Factory<ShipTexture> shipsFactory = new FactoryImpl<>();
    private final Factory<BlastTexture> blastsFactory = new FactoryImpl<>();
    private final Factory<ObstacleTexture> obstaclesFactory = new FactoryImpl<>();

    public GraphicsSoundRenderer(GameField gameField) {
        this.gameField = gameField;
        try {
            shipsFactory.setConfigs(Settings.SHIPS_TEXTURES_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            System.err.println(e.getMessage());
            return;
        }
        try {
            blastsFactory.setConfigs(Settings.BLAST_TEXTURES_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            System.err.println(e.getMessage());
            return;
        }
        try {
            obstaclesFactory.setConfigs(Settings.OBSTACLE_TEXTURES_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            System.err.println(e.getMessage());
            return;
        }
        try {
            initObjects();
        } catch (FactoryFailureException e) {
            System.err.println(e.getMessage());
            return;
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
            System.err.println("Player ship texture was not set");
            return;
        }
        updateStatusBar();
        util.clear();
        for (Obstacle o: obstacles.keySet()) {
            if (!gameField.getObstacles().contains(o)) { // if the obstacle was deleted
                obstacles.get(o).removeFrom(pane);
                util.add(o);
            }
        }
        util.forEach(obstacles.keySet()::remove); // remove deleted obstacles
        util.clear();
        for (StarShip e: enemyShips.keySet()) {
            if (!gameField.getEnemyShips().contains(e)) {
                if (Settings.soundsON) {
                    SoundsPlayer.playBigExplosion();
                }
                enemyShips.get(e).removeFrom(pane);
                util.add(e);
                continue;
            }
            // update coords
            enemyShips.get(e).updateView(pane);
        }
        util.forEach(enemyShips.keySet()::remove);
        if (!gameField.getPlayersShip().isCrippled()) {
            playerShipTexture.updateView(pane);
        } else {
            playerShipTexture.removeFrom(pane);
        }
        for (Blast b: gameField.getBullets()) {
            if (!blasts.containsKey(b)) { // a new blast appeared
                BlastTexture bt;
                try {
                    bt = blastsFactory.getObject(b.getCodeName());
                } catch (FactoryFailureException e) {
                    e.printStackTrace();
                    return;
                }
                bt.setBlast(b);
                if (Settings.soundsON) {
                    bt.playSound();
                }
                blasts.put(b, bt);
                bt.appear(pane);
            }
        }
        util.clear();
        for (Blast b: blasts.keySet()) {
            if (!gameField.getBullets().contains(b)) {
                blasts.get(b).removeFrom(pane);
                util.add(b);
                continue;
            }
            blasts.get(b).updateView(pane);
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
            ObstacleTexture t;
            t = obstaclesFactory.getObject(o.getCodeName());
            t.setObstacle(o);
            obstacles.put(o, t);
            t.appear(pane);
        }
        for (StarShip s: gameField.getEnemyShips()) {
            ShipTexture texture = shipsFactory.getObject(s.getCodeName());
            texture.setShip(s);
            enemyShips.put(s, texture);
            texture.appear(pane);
        }
        StarShip p = gameField.getPlayersShip();
        ShipTexture texture = shipsFactory.getObject(p.getCodeName());
        texture.setShip(p);
        playerShipTexture = texture;
        texture.appear(pane);
    }
}
