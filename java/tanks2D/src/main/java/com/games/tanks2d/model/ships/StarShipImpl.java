package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.*;
import com.games.tanks2d.model.obstacles.Obstacle;
import com.games.tanks2d.model.obstacles.SquareBlock;

public class StarShipImpl extends SquareBlock implements StarShip {
    private final double BLAST_SIZE = 10;
    private final GameField gameField;
    private ShipClass shipClass;
    private Direction currentDir = Direction.RIGHT;
    private boolean isCrippled = false;
    private int shipSpeed = 3;
    private int shootReloadCapacity = 20;
    private int shootReloadSize = 0;
    private int HP = 3;

    public StarShipImpl(double x, double y, double size, GameField gameField,
                        ShipClass shipClass) {
        super(x, y, size);
        this.shipClass = shipClass;
        this.gameField = gameField;
    }

    @Override
    public void move(Direction side) {
        if (side != currentDir) {
            setNewDirection(side);
        }
        double newX = getX();
        double newY = getY();
        if (side == Direction.RIGHT || side == Direction.LEFT) {
            newX += getSideSign(side) * shipSpeed;
        } else {
            newY += getSideSign(side) * shipSpeed;
        }
        if (crossesFieldObjects(newX, newY)) {
            return;
        }
        setX(newX);
        setY(newY);
    }

    @Override
    public void shoot() {
        shootReloadSize--;
        if (shootReloadSize > 0) {
            return;
        }
        shootReloadSize = shootReloadCapacity;
        Point2D p = calcBulletCoords();
        Blast blast = new BlastImpl(p.x, p.y, BLAST_SIZE, currentDir,
                gameField, shipClass);
        gameField.getBullets().add(blast);
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
    public int getHP() {
        return HP;
    }

    @Override
    public int getSpeed() {
        return shipSpeed;
    }

    @Override
    public ShipClass getShipClass() {
        return shipClass;
    }

    @Override
    public int getReloadTime() {
        return shootReloadCapacity;
    }

    @Override
    public void setSpeed(int speed) {
        shipSpeed = speed;
    }

    @Override
    public void setReloadTime(int reloadTime) {
        this.shootReloadCapacity = reloadTime;
    }

    @Override
    public void setHP(int HP) {
        this.HP = HP;
    }

    @Override
    public void setShipClass(ShipClass shipClass) {
        this.shipClass = shipClass;
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
    public Direction getCurrentDirection() {
        return currentDir;
    }

    protected void setNewDirection(Direction side) {
        currentDir = side;
    }

    protected boolean crossesFieldObjects(double newX, double newY) {
        for (Obstacle o : gameField.getObstacles()) {
            if (crossesObstacle(newX, newY, o)) {
                return true;
            }
        }
        for (StarShip t : gameField.getEnemyShips()) {
            if (this == t) {
                continue;
            }
            if (crossesObstacle(newX, newY, t)) {
                return true;
            }
        }
        if (this != gameField.getPlayersShip()) {
            return crossesObstacle(newX, newY, gameField.getPlayersShip());
        }
        return false;
    }

    protected Point2D calcBulletCoords() {
        double x = getX();
        double y = getY();
        switch (currentDir) {
            case TOP -> {
                x += (getWidth() - BLAST_SIZE) / 2;
                y -= BLAST_SIZE;
            }
            case BOTTOM -> {
                x += (getWidth() - BLAST_SIZE) / 2;
                y += getHeight();
            }
            case RIGHT -> {
                x += getWidth();
                y += (getHeight() - BLAST_SIZE) / 2;
            }
            case LEFT -> {
                y += (getHeight() - BLAST_SIZE) / 2;
                x -= BLAST_SIZE;
            }
        }
        return new Point2D(x, y);
    }

    private int getSideSign(Direction side) {
        if (side == Direction.BOTTOM || side == Direction.RIGHT) {
            return 1;
        }
        return -1;
    }
}
