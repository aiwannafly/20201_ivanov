package com.games.starwars.model;

import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.model.obstacles.SquareBlock;
import com.games.starwars.model.ships.StarShip;

public class BlastImpl extends SquareBlock implements Blast {
    private final StarShip.Direction FLY_DIRECTION;
    private final double SPEED = 10;
    private final GameField env;
    private boolean isCrippled = false;
    private Character codeName;

    public BlastImpl(double x, double y, double size, StarShip.Direction dir,
                     GameField env, Character codeName) {
        super(x, y, size);
        this.FLY_DIRECTION = dir;
        this.env = env;
        this.codeName = codeName;
    }

    @Override
    public void fly() {
        double newX = getX();
        double newY = getY();
        switch (FLY_DIRECTION) {
            case LEFT -> newX -= SPEED;
            case TOP -> newY -= SPEED;
            case RIGHT -> newX += SPEED;
            case BOTTOM -> newY += SPEED;
        }
        if (hitObstacles(newX, newY)) {
            hit();
        } else {
            setX(newX);
            setY(newY);
        }
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
    public StarShip.Direction getSide() {
        return FLY_DIRECTION;
    }

    @Override
    public void setCodeName(Character codeName) {
        this.codeName = codeName;
    }

    @Override
    public Character getCodeName() {
        return codeName;
    }

    @Override
    public void hit() {
        isCrippled = true;
        double explosionSize = 30;
        double eX = getX();
        double eY = getY();
        double blockSize = 30;
        double offset = (blockSize - getWidth()) / 2;
        if (FLY_DIRECTION == StarShip.Direction.LEFT ||
                FLY_DIRECTION == StarShip.Direction.RIGHT) {
            eY -= offset;
        } else {
            eX -= offset;
        }
        switch (FLY_DIRECTION) {
            case LEFT -> eX -= offset * 2;
            case TOP -> eY -= offset * 2;
        }
        Explosion e = new ExplosionImpl(eX, eY, explosionSize);
        env.getExplosions().add(e);
    }

    private boolean hitObstacles(double newX, double newY) {
        boolean hits = false;
        for (Obstacle o: env.getObstacles()) {
            if (intersectsObstacle(newX, newY, o)) {
                o.hit();
                hits = true;
            }
        }
        if (hits) {
            return true;
        }
        if (getCodeName() == 'p') {
            for (StarShip t: env.getEnemyShips()) {
                if (intersectsObstacle(newX, newY, t)) {
                    t.hit();
                    return true;
                }
            }
        } else {
            if (env.getPlayersShip().isCrippled()) {
                return false;
            }
            if (intersectsObstacle(newX, newY, env.getPlayersShip())) {
                env.getPlayersShip().hit();
                return true;
            }
        }
        return false;
    }
}
