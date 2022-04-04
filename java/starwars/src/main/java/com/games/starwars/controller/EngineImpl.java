package com.games.starwars.controller;

import com.games.starwars.model.*;
import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.Renderer;
import com.games.starwars.view.GraphicsSoundRenderer;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.util.ArrayList;

public class EngineImpl implements Engine {
    private final GameField gameField;
    private final Renderer renderer;
    private boolean goUp = false;
    private boolean goRight = false;
    private boolean goDown = false;
    private boolean goLeft = false;
    private boolean shootEnabled = false;
    private final StarShip playersStarShip;
    private final ArrayList<Obstacle> util = new ArrayList<>();
    private boolean paused = false;
    private final int reloadTime = 20;
    private int playerGunReload = 0;
    private final ArrayList<StarShip.Direction> directionsStack = new ArrayList<>();

    public EngineImpl(int levelNum) {
        gameField = new GameFieldImpl();
        try {
            gameField.loadLevel("level" + levelNum);
        } catch (LevelFailLoadException e) {
            e.printStackTrace();
        }
        renderer = new GraphicsSoundRenderer(gameField);
        playersStarShip = gameField.getPlayersShip();
    }

    @Override
    public void handlePressedKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case UP, W -> {
                if (!directionsStack.contains(StarShip.Direction.TOP))
                directionsStack.add(StarShip.Direction.TOP);
                goUp = true;
            }
            case LEFT, A -> {
                if (!directionsStack.contains(StarShip.Direction.LEFT))
                directionsStack.add(StarShip.Direction.LEFT);
                goLeft = true;
            }
            case DOWN, S -> {
                if (!directionsStack.contains(StarShip.Direction.BOTTOM))
                directionsStack.add(StarShip.Direction.BOTTOM);
                goDown = true;
            }
            case RIGHT, D -> {
                if (!directionsStack.contains(StarShip.Direction.RIGHT))
                directionsStack.add(StarShip.Direction.RIGHT);
                goRight = true;
            }
            case SPACE -> shootEnabled = true;
            case ESCAPE -> paused = true;
        }
    }

    @Override
    public void handleReleasedKeyEvent(KeyEvent event) {
        switch (event.getCode()) {
            case UP, W -> {
                directionsStack.remove(StarShip.Direction.TOP);
            }
            case LEFT, A -> {
                directionsStack.remove(StarShip.Direction.LEFT);
            }
            case DOWN, S -> {
                directionsStack.remove(StarShip.Direction.BOTTOM);
            }
            case RIGHT, D -> {
                directionsStack.remove(StarShip.Direction.RIGHT);
            }
            case SPACE -> shootEnabled = false;
        }
    }

    @Override
    public void handleClickEvent(MouseEvent event) {
        shootEnabled = true;
    }

    @Override
    public void handleClickReleasedEvent(MouseEvent event) {
        shootEnabled = false;
    }

    @Override
    public Status update() {
        if (paused) {
            paused = false;
            return Status.PAUSE;
        }
        if (directionsStack.size() != 0) {
            playersStarShip.move(directionsStack.get(directionsStack.size() - 1));
        }
        if (playerGunReload >= 0) {
            playerGunReload--;
        }
        if (shootEnabled) {
            if (playerGunReload <= 0) {
                playersStarShip.shoot();
                playerGunReload = reloadTime;
            }
        }
        for (StarShip t: gameField.getEnemyShips()) {
            t.shoot();
            t.move(t.getCurrentDirection());
        }
        for (Explosion e: gameField.getExplosions()) {
            e.hit();
        }
        for (Blast b: gameField.getBullets()) {
            b.fly();
        }
        checkHits();
        if (playersStarShip.isCrippled()) {
            return Status.LOSE;
        }
        if (gameField.getEnemyShips().isEmpty()) {
            return Status.WIN;
        }
        return Status.IN_PROGRESS;
    }

    @Override
    public void render() {
        renderer.render();
    }

    @Override
    public Renderer getRenderer() {
        return renderer;
    }

    private void checkHits() {
        util.clear();
        for (Obstacle o: gameField.getObstacles()) {
            if (o.isCrippled()) {
                util.add(o);
            }
        }
        gameField.getObstacles().removeAll(util);
        util.clear();
        for (StarShip t: gameField.getEnemyShips()) {
            if (t.isCrippled()) {
                util.add(t);
            }
        }
        gameField.getEnemyShips().removeAll(util);
        util.clear();
        for (Blast b: gameField.getBullets()) {
            if (b.isCrippled()) {
                util.add(b);
            }
        }
        gameField.getBullets().removeAll(util);
        util.clear();
        for (Explosion e: gameField.getExplosions()) {
            if (e.isCrippled()) {
                util.add(e);
            }
        }
        gameField.getExplosions().removeAll(util);
    }
}
