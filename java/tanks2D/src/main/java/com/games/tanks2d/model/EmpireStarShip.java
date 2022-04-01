package com.games.tanks2d.model;

import java.util.ArrayList;
import java.util.Random;

public class EmpireStarShip extends StarShipImpl implements StarShip {
    private double previousX;
    private double previousY;
    private final int blockWaitTime = 20;
    private final int singleDirTime = 100;
    private int blockReloadTime = 20;
    private int singleDirMovesCount = 0;
    private final ArrayList<Direction> sides = new ArrayList<>();
    Random random = new Random();

    public EmpireStarShip(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField, Team.ENEMIES);
        previousX = x;
        previousY = y;
        this.shootReloadTime = 50;
        sides.add(Direction.TOP);
        sides.add(Direction.RIGHT);
        sides.add(Direction.BOTTOM);
        sides.add(Direction.LEFT);
    }

    @Override
    public void move(Direction side) {
        if (side == null) {
            super.move(Direction.RIGHT);
            return;
        }
        if (getX() == previousX && getY() == previousY) {
            if (blockReloadTime <= 0) {
                Direction currentSide = getSide();
                sides.remove(currentSide);
                changeDirection(sides.get(Math.abs(random.nextInt()) % 3));
                singleDirMovesCount = 0;
                sides.add(currentSide);
                blockReloadTime = blockWaitTime;
            }
        }
        singleDirMovesCount++;
        if (singleDirMovesCount >= singleDirTime) {
            Direction currentSide = getSide();
            sides.remove(currentSide);
            changeDirection(sides.get(Math.abs(random.nextInt()) % 3));
            singleDirMovesCount = 0;
            sides.add(currentSide);
        }
        previousX = getX();
        previousY = getY();
        blockReloadTime--;
        super.move(getSide());
    }
}
