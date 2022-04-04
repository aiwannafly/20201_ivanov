package com.games.starwars.model;

import com.games.starwars.ApplicationMainClass;
import com.games.starwars.model.factory.*;
import com.games.starwars.model.obstacles.*;
import com.games.starwars.model.ships.*;
import com.games.starwars.Settings;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;


public class GameFieldImpl implements GameField {
    private final double BLOCK_SIZE = 30;
    private final char SPACE_SYMBOL = '_';
    private final ArrayList<StarShip> enemyStarShips = new ArrayList<>();
    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private final ArrayList<Blast> blasts = new ArrayList<>();
    private final ArrayList<Explosion> explosions = new ArrayList<>();
    private StarShip playersStarShip;

    @Override
    public void loadLevel(String fileName) throws LevelFailLoadException {
        InputStream inputStream = ApplicationMainClass.class.getResourceAsStream(
                Settings.LEVELS_DIR_PATH + fileName);
        if (null == inputStream) {
            throw new LevelFailLoadException("File " + fileName + " was not opened.");
        }
        try (Scanner scanner = new Scanner(inputStream)) {
            int width = scanner.nextInt();
            int height = scanner.nextInt();
            StringBuilder stringLayout = new StringBuilder();
            while (scanner.hasNext()) {
                stringLayout.append(scanner.next());
            }
            parseStringLayout(stringLayout.toString(), width, height);
        }
    }

    private void parseStringLayout(String stringLayout, int width, int height) throws LevelFailLoadException {
        FactoryOfStarShips factoryOfStarShips = new FactoryOfShipsImpl();
        try {
            factoryOfStarShips.setConfigs(Settings.MODEL_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            throw new LevelFailLoadException(e.getMessage());
        }
        FactoryOfObstacles factoryOfObstacles = new FactoryOfObstaclesImpl();
        try {
            factoryOfObstacles.setConfigs(Settings.MODEL_CONFIGS);
        } catch (FactoryBadConfigsException e) {
            throw new LevelFailLoadException(e.getMessage());
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i * width + j >= stringLayout.length()) {
                    break;
                }
                char nextByte = stringLayout.charAt(i * width + j);
                if (nextByte == SPACE_SYMBOL) {
                    continue;
                }
                double x = j * BLOCK_SIZE;
                double y = i * BLOCK_SIZE;
                try {
                    StarShip ship = factoryOfStarShips.getStarShip(nextByte);
                    ship.setX(x);
                    ship.setY(y);
                    ship.setWidth(BLOCK_SIZE);
                    ship.setHeight(BLOCK_SIZE);
                    ship.setGameField(this);
                    ship.setCodeName(nextByte);
                    if (ship.getCodeName() == Settings.PLAYER_SHIP_CODE) {
                        playersStarShip = ship;
                    } else {
                        enemyStarShips.add(ship);
                    }
                } catch (FactoryFailureException exception) {
                    try {
                        Obstacle o = factoryOfObstacles.getObstacle(nextByte);
                        o.setX(x);
                        o.setY(y);
                        o.setHeight(BLOCK_SIZE);
                        o.setWidth(BLOCK_SIZE);
                        o.setCodeName(nextByte);
                        obstacles.add(o);
                    } catch (FactoryFailureException e) {
                        throw new LevelFailLoadException(e.getMessage());
                    }
                }
            }
        }
    }

    @Override
    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    @Override
    public ArrayList<StarShip> getEnemyShips() {
        return enemyStarShips;
    }

    @Override
    public StarShip getPlayersShip() {
        return playersStarShip;
    }

    @Override
    public double getBlockSize() {
        return BLOCK_SIZE;
    }

    @Override
    public ArrayList<Blast> getBullets() {
        return blasts;
    }

    @Override
    public ArrayList<Explosion> getExplosions() {
        return explosions;
    }

}
