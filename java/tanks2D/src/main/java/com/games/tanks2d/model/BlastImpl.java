package com.games.tanks2d.model;

public class BlastImpl extends SquareBlock implements Blast {
    private boolean isCrippled = false;
    private final StarShip.Direction direction;
    private final StarShip.Team team;
    private final GameField env;
    private final double bulletSpeed = 10;

    public BlastImpl(double x, double y, double size, StarShip.Direction dir,
                     GameField env, StarShip.Team team) {
        super(x, y, size);
        this.direction = dir;
        this.env = env;
        this.team = team;
    }

    @Override
    public void fly() {
        double newX = getX();
        double newY = getY();
        switch (direction) {
            case LEFT -> newX -= bulletSpeed;
            case TOP -> newY -= bulletSpeed;
            case RIGHT -> newX += bulletSpeed;
            case BOTTOM -> newY += bulletSpeed;
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
        return direction;
    }

    @Override
    public StarShip.Team getTeam() {
        return team;
    }

    @Override
    public void hit() {
        isCrippled = true;
        double explosionSize = 30;
        double eX = getX();
        double eY = getY();
        double blockSize = 30;
        double offset = (blockSize - getWidth()) / 2;
        if (direction == StarShip.Direction.LEFT ||
                direction == StarShip.Direction.RIGHT) {
            eY -= offset;
        } else {
            eX -= offset;
        }
        switch (direction) {
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
        if (team == StarShip.Team.PLAYERS) {
            for (StarShip t: env.getEnemyTanks()) {
                if (intersectsObstacle(newX, newY, t)) {
                    t.hit();
                    return true;
                }
            }
        } else {
            if (env.getPlayersTank().isCrippled()) {
                return false;
            }
            if (intersectsObstacle(newX, newY, env.getPlayersTank())) {
                env.getPlayersTank().hit();
                return true;
            }
        }
        return false;
    }
}
