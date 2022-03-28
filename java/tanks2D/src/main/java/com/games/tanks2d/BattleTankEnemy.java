package com.games.tanks2d;

import javafx.animation.AnimationTimer;

import java.util.ArrayList;
import java.util.Random;

public class BattleTankEnemy extends BattleTank {
    private double previousX;
    private double previousY;
    private final int waitTime = 10;
    private int reloadTime = 10;
    private final ArrayList<Direction> sides = new ArrayList<>();
    Random random = new Random();

    public BattleTankEnemy(double x, double y, double width, double height, LevelDesigner env) {
        super(x, y, width, height, env);
        previousX = x;
        previousY = y;
        setFill(TexturePack.imgEnemyTankRight);
        setId("enemy");
        sides.add(Direction.TOP);
        sides.add(Direction.RIGHT);
        sides.add(Direction.BOTTOM);
        sides.add(Direction.LEFT);
        timer.start();
    }

    @Override
    public void move(Direction side) {
        if (side == null) {
            super.move(Direction.RIGHT);
            return;
        }
        if (getX() == previousX && getY() == previousY) {
            if (reloadTime <= 0) {
                Direction currentSide = getSide();
                sides.remove(currentSide);
                changeDirection(sides.get(Math.abs(random.nextInt()) % 3));
                sides.add(currentSide);
                reloadTime = waitTime;
            }
        }
        previousX = getX();
        previousY = getY();
        reloadTime--;
        super.move(getSide());
    }

    @Override
    protected void changeDirection(Direction side) {
        super.changeDirection(side);
        switch (side) {
            case TOP -> this.setFill(TexturePack.imgEnemyTankTop);
            case BOTTOM -> this.setFill(TexturePack.imgEnemyTankBottom);
            case LEFT -> this.setFill(TexturePack.imgEnemyTankLeft);
            case RIGHT -> this.setFill(TexturePack.imgEnemyTankRight);
        }
    }

    @Override
    public void stop() {
        timer.stop();
    }

    @Override
    public void release() {
        timer.start();
    }

    private final AnimationTimer timer = new AnimationTimer() {
        private long lastUpdateTime = 0;
        private final int shootSpeed = 50;
        private int enemyReload = shootSpeed;

        @Override
        public void handle(long now) {
            if (now - lastUpdateTime >= Level.DELAY) {
                animation();
                lastUpdateTime = now;
            }
        }

        private void animation() {
            move(getSide());
            if (enemyReload <= 0) {
                enemyReload = shootSpeed;
                shoot();
            }
            enemyReload--;
        }
    };
}
