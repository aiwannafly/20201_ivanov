package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.GameField;

import java.util.ArrayList;
import java.util.Random;

public class EmpireStarShip extends StarShipImpl implements StarShip {
    private double previousX;
    private double previousY;
    protected final int blockWaitTime = 20;
    protected int singleDirTime = 100;
    private int blockReloadTime = 0;
    private int singleDirMovesCount = 0;
    private final ArrayList<Direction> sides = new ArrayList<>();
    Random random = new Random();
    protected final double DANGER_DISTANCE = 1000;
    private final GameField gameField;

    public EmpireStarShip(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField, ShipClass.EMPIRE_SHIP);
        this.gameField = gameField;
        previousX = x;
        previousY = y;
        setHP(1);
        setReloadTime(50);
        initSides();
    }

    @Override
    public void move(Direction side) {
        if (getX() == previousX && getY() == previousY) {
            if (blockReloadTime <= 0) {
                singleDirMovesCount = 0;
                setNewDirection(getNextDirection(getCurrentDirection()));
                blockReloadTime = blockWaitTime;
            }
            blockReloadTime--;
        }
        singleDirMovesCount++;
        if (singleDirMovesCount >= singleDirTime) {
            setNewDirection(getNextDirection(getCurrentDirection()));
            singleDirMovesCount = 0;
        }
        previousX = getX();
        previousY = getY();
        super.move(getCurrentDirection());
    }

    @Override
    public void shoot() {
        if (getDistance(gameField.getPlayersShip()) < DANGER_DISTANCE) {
            super.shoot();
        }
    }

    protected Direction getNextDirection(Direction previousDir) {
        if (previousDir == null) {
            return sides.get(Math.abs(random.nextInt()) % 4);
        }
        sides.remove(previousDir); // we want to change direction
        Direction dir = sides.get(Math.abs(random.nextInt()) % sides.size());
        initSides();
        return dir;
    }

    protected void setSingleDirTime(int time) {
        singleDirTime = time;
    }

    private void initSides() {
        sides.clear();
        sides.add(Direction.TOP);
        sides.add(Direction.RIGHT);
        sides.add(Direction.BOTTOM);
        sides.add(Direction.LEFT);
    }
}
