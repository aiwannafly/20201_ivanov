package com.games.tanks2d;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import static com.games.tanks2d.TexturePack.*;

public class LevelDesigner extends Pane implements Level {
    private final ArrayList<Rectangle> obstacles = new ArrayList<>();
    private final ArrayList<BattleTank> enemyTanks = new ArrayList<>();
    private BattleTank playersTank;

    public LevelDesigner() {
    }

    @Override
    public ArrayList<Rectangle> getObstacles() {
        return obstacles;
    }

    @Override
    public ArrayList<BattleTank> getEnemyTanks() {
        return enemyTanks;
    }

    @Override
    public BattleTank getPlayersTank() {
        return playersTank;
    }

    @Override
    public void destroyObstacle(Rectangle block) {
        obstacles.remove(block);
    }

    @Override
    public void destroyTank(BattleTank tank) {
        enemyTanks.remove(tank);
        tank.kill();
    }

    @Override
    public void stopAction() {
        for (BattleTank tank: enemyTanks) {
            tank.stop();
        }
        playersTank.stop();
    }

    @Override
    public void continueAction() {
        for (BattleTank tank: enemyTanks) {
            tank.release();
        }
        playersTank.release();
    }

    @Override
    public void clearLevel() {
        enemyTanks.clear();
        obstacles.clear();
    }

    @Override
    public void loadLevel(String fileName) throws LevelFailLoadException {
        InputStream inputStream = LevelDesigner.class.getResourceAsStream("levels/" + fileName);
        if (null == inputStream) {
            throw new LevelFailLoadException("File " + fileName + " was not opened.");
        }
        try (Scanner scanner = new Scanner(inputStream)) {
            int width = scanner.nextInt();
            int height = scanner.nextInt();
            StringBuilder layout = new StringBuilder();
            while (scanner.hasNext()) {
                layout.append(scanner.next());
            }
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (i * width +j >= layout.length()) {
                        break;
                    }
                    char nextByte = layout.charAt(i * width + j);
                    System.out.print(nextByte);
                    if (nextByte == 'g' || nextByte == 'r' || nextByte == 'l') {
                        Rectangle block = new Rectangle(j * BLOCK_SIZE,
                                i * BLOCK_SIZE, BLOCK_SIZE, BLOCK_SIZE);
                        switch (nextByte) {
                            case 'g' -> {
                                block.setFill(imgGrayBrickPattern);
                                block.setId("unbreakable");
                            }
                            case 'r' -> {
                                block.setFill(imgBlueBrickPattern);
                                block.setId("breakable");
                            }
                            case 'l' -> {
                                block.setFill(imgLeafBlockPattern);
                                block.setId("non-obstacle");
                                block.setTranslateZ(-200);
                            }
                        }
                        if ('l' != nextByte) {
                            obstacles.add(block);
                        }
                        this.getChildren().add(block);
                    } else if (nextByte == 'e' || nextByte == 'p') {
                        BattleTank tank = null;
                        switch (nextByte) {
                            case 'e' -> {
                                tank = new BattleTankEnemy(LevelDesigner.BLOCK_SIZE * j,
                                        LevelDesigner.BLOCK_SIZE * i, LevelDesigner.BLOCK_SIZE * 2,
                                        LevelDesigner.BLOCK_SIZE * 2, this);
                                enemyTanks.add(tank);
                            }
                            case 'p' -> {
                                tank = new BattleTankPlayer(LevelDesigner.BLOCK_SIZE * j,
                                        LevelDesigner.BLOCK_SIZE * i, LevelDesigner.BLOCK_SIZE * 2,
                                        LevelDesigner.BLOCK_SIZE * 2, this);
                                playersTank = tank;
                            }
                        }
                        getChildren().add(tank);
                    }
                }
            }
        }
    }
}
