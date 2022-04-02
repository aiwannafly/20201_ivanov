package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.GameField;
import com.games.tanks2d.model.Shape;

import java.util.ArrayList;
import java.util.Random;

public class EmpireStarShip extends StarShipImpl implements StarShip {
    private double previousX;
    private double previousY;
    protected final int blockWaitTime = 0;
    protected int singleDirTime = 100;
    private int blockReloadTime = 20;
    private int singleDirMovesCount = 0;
    private final ArrayList<Direction> sides = new ArrayList<>();
    Random random = new Random();
    protected final double DANGER_DISTANCE = 1000;
    private final GameField gameField;

    public EmpireStarShip(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField, Class.EMPIRE_SHIP);
        this.gameField = gameField;
        previousX = x;
        previousY = y;
        setHP(1);
        setReloadTime(50);
        sides.add(Direction.TOP);
        sides.add(Direction.RIGHT);
        sides.add(Direction.BOTTOM);
        sides.add(Direction.LEFT);
    }

    @Override
    public void move(Direction side) {
        if (side == null) {
            super.move(sides.get(Math.abs(random.nextInt()) % 4));
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

    @Override
    public void shoot() {
        if (getDistance(gameField.getPlayersShip()) < DANGER_DISTANCE) {
            super.shoot();
        }
    }
}
