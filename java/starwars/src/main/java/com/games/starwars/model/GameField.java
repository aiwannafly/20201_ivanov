package com.games.starwars.model;

import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.model.ships.StarShip;

import java.util.ArrayList;

public interface GameField {

    void loadLevel(String fileName) throws LevelFailLoadException;

    ArrayList<Obstacle> getObstacles();

    ArrayList<StarShip> getEnemyShips();

    ArrayList<Blast> getBullets();

    ArrayList<Explosion> getExplosions();

    StarShip getPlayersShip();

    double getBlockSize();
}
