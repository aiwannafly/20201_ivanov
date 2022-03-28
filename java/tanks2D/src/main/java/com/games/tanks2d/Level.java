package com.games.tanks2d;

import javafx.scene.shape.Rectangle;

import java.util.ArrayList;

public interface Level {
    long DELAY = 10_000_000;
    int BLOCK_SIZE = 30;
    int WIDTH_IN_BLOCKS = SceneBuilder.WIDTH / BLOCK_SIZE;
    int HEIGHT_IN_BLOCKS = SceneBuilder.HEIGHT / BLOCK_SIZE;

    void loadLevel(String fileName) throws LevelFailLoadException;

    ArrayList<Rectangle> getObstacles();

    ArrayList<BattleTank> getEnemyTanks();

    BattleTank getPlayersTank();

    void destroyObstacle(Rectangle block);

    void destroyTank(BattleTank tank);

    void stopAction();

    void continueAction();

    void clearLevel();
}
