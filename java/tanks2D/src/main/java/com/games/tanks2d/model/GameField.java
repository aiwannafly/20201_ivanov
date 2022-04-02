package com.games.tanks2d.model;

import com.games.tanks2d.model.obstacles.Obstacle;
import com.games.tanks2d.model.ships.StarShip;

import java.util.ArrayList;

public interface GameField {

    void loadLevel(String fileName) throws LevelFailLoadException;

    void destroyObstacle(Obstacle obstacle);

    void destroyTank(StarShip starShip);

    ArrayList<Obstacle> getObstacles();

    ArrayList<StarShip> getEnemyTanks();

    ArrayList<Blast> getBullets();

    ArrayList<Explosion> getExplosions();

    StarShip getPlayersShip();

    void update();

}
