package com.games.tanks2d;

import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.ArrayList;

public class BattleTank extends Rectangle implements Tank {

    public BattleTank(double x, double y, double width, double height,
                      LevelDesigner env) {
        super(x, y, width, height);
        environment = env;
    }

    @Override
    public void move(Direction side) {
        if (side != currentSide) {
            changeDirection(side);
        }
        double newX = getX();
        double newY = getY();
        double deltaX = 0;
        double deltaY = 0;
        if (vertical) {
            deltaX += (positive ? tankSpeed : -tankSpeed);
            newX += deltaX;
        } else {
            deltaY += (positive ? tankSpeed : -tankSpeed);
            newY += deltaY;
        }
        if (intersects(newX, newY, environment.getObstacles())) {
            return;
        }
        setX(newX);
        setY(newY);
    }

    @Override
    public void shoot() {
        bulletWidth = 10;
        bulletHeight = 20;
        if (currentSide.isVertical()) {
            bulletWidth = 20;
            bulletHeight = 10;
        }
        Point2D coords = calcBulletCoords();
        Bullet bullet = new Bullet(coords.getX(), coords.getY(), bulletWidth, bulletHeight, environment,
                this, currentSide);
    }

    @Override
    public Direction getSide() {
        return currentSide;
    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

    }

    @Override
    public void kill() {
        stop();
        isAlive = false;
        environment.getChildren().remove(this);
    }

    @Override
    public boolean isAlive() {
        return isAlive;
    }

    protected void changeDirection(Direction side) {
        currentSide = side;
        vertical = side.isVertical();
        positive = side == (vertical ? Direction.RIGHT : Direction.BOTTOM);
    }

    private Point2D calcBulletCoords() {
        double x = getX();
        double y = getY();
        switch (currentSide) {
            case TOP -> {
                x += (getWidth() - bulletWidth) / 2;
                y -= bulletHeight;
            }
            case BOTTOM -> {
                x += (getWidth() - bulletWidth) / 2;
                y += getHeight();
            }
            case RIGHT -> {
                x += getWidth();
                y += (getHeight() - bulletHeight) / 2;
            }
            case LEFT -> {
                y += (getHeight() - bulletHeight) / 2;
                x -= bulletWidth;
            }
        }
        return new Point2D(x, y);
    }

    private boolean intersects(double x, double y, ArrayList<Rectangle> blocks) {
        Rectangle rectangle = new Rectangle(x, y, getWidth(), getHeight());
        for (Rectangle block: blocks) {
            if (!rectangle.intersects(block.getLayoutBounds())) {
                continue;
            }
            Shape intersection = intersect(rectangle, block);
            if (intersection.maxWidth(LevelDesigner.BLOCK_SIZE) < criticalSize ||
                    intersection.maxHeight(LevelDesigner.BLOCK_SIZE) < criticalSize) {
                continue;
            }
            return true;
        }
        return false;
    }

    private boolean isAlive = true;
    private final int criticalSize = 10;
    private int bulletWidth = 10;
    private int bulletHeight = 20;
    private Direction currentSide = null;
    private boolean vertical = false;
    private boolean positive = false;
    private final double tankSpeed = 5;
    private LevelDesigner environment = null;
}
