package com.games.tanks2d.model;

import com.games.tanks2d.ApplicationMainClass;
import com.games.tanks2d.model.obstacles.*;
import com.games.tanks2d.model.ships.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;


public class GameFieldImpl implements GameField {
    public final double BLOCK_SIZE = 30;
    private final ArrayList<StarShip> enemyStarShips = new ArrayList<>();
    private final ArrayList<Obstacle> obstacles = new ArrayList<>();
    private final ArrayList<Blast> blasts = new ArrayList<>();
    private final ArrayList<Explosion> explosions = new ArrayList<>();
    private StarShip playersStarShip;

    @Override
    public void loadLevel(String fileName) throws LevelFailLoadException {
        InputStream inputStream = ApplicationMainClass.class.getResourceAsStream("levels/" + fileName);
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
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (i * width +j >= stringLayout.length()) {
                        break;
                    }
                    char nextByte = stringLayout.charAt(i * width + j);
                    double size = BLOCK_SIZE;
                    double x = j * BLOCK_SIZE;
                    double y = i * BLOCK_SIZE;
                    if (nextByte == 'g' || nextByte == 'r' || nextByte == 'l') {
                        Obstacle obstacle = null;
                        switch (nextByte) {
                            case 'g' -> obstacle = new SolidBlock(x, y, size);
                            case 'r' -> obstacle = new FragileBlock(x, y, size);
                            case 'l' -> obstacle = new WallBlock(x, y, size);
                        }
                        obstacles.add(obstacle);
                    } else if (nextByte == 'e' || nextByte == 'p' || nextByte == 's'
                    || nextByte == 'f') {
                        if (nextByte == 'p') {
                            playersStarShip = new RebellionShip(x, y, 2 * size, this);
                        } else if (nextByte == 'e') {
                            enemyStarShips.add(new EmpireStarShip(x, y, 2 * size, this));
                        } else if (nextByte == 's') {
                            enemyStarShips.add(new StarDestroyer(x, y, 4 * size, this));
                        } else if (nextByte == 'f') {
                            enemyStarShips.add(new Exterminator(x, y, size, this));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void destroyObstacle(Obstacle obstacle) {

    }

    @Override
    public void destroyTank(StarShip starShip) {

    }

    @Override
    public ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }

    @Override
    public ArrayList<StarShip> getEnemyTanks() {
        return enemyStarShips;
    }

    @Override
    public StarShip getPlayersShip() {
        return playersStarShip;
    }

    @Override
    public ArrayList<Blast> getBullets() {
        return blasts;
    }

    @Override
    public ArrayList<Explosion> getExplosions() {
        return explosions;
    }

    @Override
    public void update() {

    }
}
