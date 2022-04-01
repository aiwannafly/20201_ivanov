package com.games.tanks2d.model;

import java.util.ArrayList;

public interface GameField {

    void loadLevel(String fileName) throws LevelFailLoadException;

    void destroyObstacle(Obstacle obstacle);

    void destroyTank(StarShip starShip);

    ArrayList<Obstacle> getObstacles();

    ArrayList<StarShip> getEnemyTanks();

    ArrayList<Blast> getBullets();

    ArrayList<Explosion> getExplosions();

    StarShip getPlayersTank();

    void update();

}
