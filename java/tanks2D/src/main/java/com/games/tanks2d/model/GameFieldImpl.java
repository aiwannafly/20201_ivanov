package com.games.tanks2d.model;

import com.games.tanks2d.ApplicationMainClass;
import com.games.tanks2d.model.obstacles.*;
import com.games.tanks2d.model.ships.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class GameFieldImpl implements GameField {
    private final double BLOCK_SIZE = 30;
    private final double EXTERMINATOR_SIZE = BLOCK_SIZE;
    private final double PLAYER_SHIP_SIZE = 2 * BLOCK_SIZE;
    private final double EMPIRE_SHIP_SIZE = 2 * BLOCK_SIZE;
    private final double STAR_DESTROYER_SIZE = 4 * BLOCK_SIZE;
    private final char SOLID_BLOCK = 'g';
    private final char FRAGILE_BLOCK = 'r';
    private final char WALL_BLOCK = 'l';
    private final char EMPIRE_SHIP = 'e';
    private final char STAR_DESTROYER = 's';
    private final char EXTERMINATOR = 'f';
    private final char REBELLION_SHIP = 'p';
    private final Character[] BLOCKS_ARRAY = {SOLID_BLOCK, FRAGILE_BLOCK, WALL_BLOCK};
    private final Character[] SHIPS_ARRAY = {EMPIRE_SHIP, STAR_DESTROYER, EXTERMINATOR, REBELLION_SHIP};

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
            parseStringLayout(stringLayout.toString(), width, height);
        }
    }

    private void parseStringLayout(String stringLayout, int width, int height) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (i * width + j >= stringLayout.length()) {
                    break;
                }
                char nextByte = stringLayout.charAt(i * width + j);
                double x = j * BLOCK_SIZE;
                double y = i * BLOCK_SIZE;
                if (List.of(BLOCKS_ARRAY).contains(nextByte)) {
                    Obstacle obstacle = null;
                    switch (nextByte) {
                        case SOLID_BLOCK -> obstacle = new SolidBlock(x, y, BLOCK_SIZE);
                        case FRAGILE_BLOCK -> obstacle = new FragileBlock(x, y, BLOCK_SIZE);
                        case WALL_BLOCK -> obstacle = new WallBlock(x, y, BLOCK_SIZE);
                    }
                    obstacles.add(obstacle);
                } else if (List.of(SHIPS_ARRAY).contains(nextByte)) {
                    switch (nextByte) {
                        case REBELLION_SHIP -> playersStarShip = new RebellionShip(x, y, PLAYER_SHIP_SIZE, this);
                        case EXTERMINATOR -> enemyStarShips.add(new Exterminator(x, y, EXTERMINATOR_SIZE, this));
                        case STAR_DESTROYER -> enemyStarShips.add(new StarDestroyer(x, y, STAR_DESTROYER_SIZE, this));
                        case EMPIRE_SHIP -> enemyStarShips.add(new EmpireStarShip(x, y, EMPIRE_SHIP_SIZE, this));
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
