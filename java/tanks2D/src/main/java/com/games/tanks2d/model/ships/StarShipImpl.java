package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.*;
import com.games.tanks2d.model.obstacles.Obstacle;
import com.games.tanks2d.model.obstacles.SquareBlock;

public class StarShipImpl extends SquareBlock implements StarShip {
    private Direction currentDir = Direction.RIGHT;
    private int shipSpeed = 3;
    private boolean isCrippled = false;
    private final GameField gameField;
    private final double bulletWidth = 10;
    private final double bulletHeight = 10;
    private Class type;
    private int shootReloadTime = 20;
    private int reload = shootReloadTime;
    private int HP = 3;
    private boolean vertical = false;
    private boolean positive = false;

    public StarShipImpl(double x, double y, double size, GameField gameField,
                        Class aClass) {
        super(x, y, size);
        this.type = aClass;
        this.gameField = gameField;
    }

    @Override
    public void move(Direction side) {
        if (side != currentDir) {
            changeDirection(side);
        }
        double newX = getX();
        double newY = getY();
        double deltaX = 0;
        double deltaY = 0;
        if (vertical) {
            deltaX = (positive ? shipSpeed : -shipSpeed);
            newX += deltaX;
        } else {
            deltaY = (positive ? shipSpeed : -shipSpeed);
            newY += deltaY;
        }
        if (intersectsObstacles(newX, newY)) {
            return;
        }
        setX(newX);
        setY(newY);
    }

    protected void changeDirection(Direction side) {
        currentDir = side;
        vertical = side.isVertical();
        positive = side == (vertical ? Direction.RIGHT : Direction.BOTTOM);
    }

    @Override
    public void shoot() {
        reload--;
        if (reload > 0) {
            return;
        }
        reload = shootReloadTime;
        Point2D p = calcBulletCoords();
        Blast blast = new BlastImpl(p.x, p.y, bulletWidth, currentDir,
                gameField, type);
        gameField.getBullets().add(blast);
    }

    @Override
    public int getHP() {
        return HP;
    }

    @Override
    public int getSpeed() {
        return shipSpeed;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public int getReloadTime() {
        return shootReloadTime;
    }

    @Override
    public void setSpeed(int speed) {
        shipSpeed = speed;
    }

    @Override
    public void setReloadTime(int reloadTime) {
        this.shootReloadTime = reloadTime;
    }

    @Override
    public void setHP(int HP) {
        this.HP = HP;
    }

    @Override
    public void setType(Class type) {
        this.type = type;
    }

    private boolean intersectsObstacles(double newX, double newY) {
        for (Obstacle o : gameField.getObstacles()) {
            if (intersectsObstacle(newX, newY, o)) {
                double intSize = getIntersectionSize(newX, newY, o);
                if (intSize != 0) {
                    return true;
                }
            }
        }
        for (StarShip t: gameField.getEnemyTanks()) {
            if (this == t) {
                continue;
            }
            if (intersectsObstacle(newX, newY, t)) {
                double intSize = getIntersectionSize(newX, newY, t);
                if (intSize != 0) {
                    return true;
                }
            }
        }
        if (this.type == Class.EMPIRE_SHIP) {
            if (intersectsObstacle(newX, newY, gameField.getPlayersShip())) {
                double intSize = getIntersectionSize(newX, newY, gameField.getPlayersShip());
                return intSize != 0;
            }
        }
        return false;
    }

    @Override
    public boolean isCrippled() {
        return isCrippled;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public void hit() {
        HP--;
        if (HP <= 0) {
            Explosion e = new ExplosionImpl(getX(), getY(), getWidth());
            gameField.getExplosions().add(e);
            isCrippled = true;
        }
    }

    @Override
    public Direction getSide() {
        return currentDir;
    }

    protected Point2D calcBulletCoords() {
        double x = getX();
        double y = getY();
        switch (currentDir) {
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
}
