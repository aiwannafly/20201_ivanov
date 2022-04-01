package com.games.tanks2d.model;

public class StarShipImpl extends SquareBlock implements StarShip {
    private Direction currentDir = Direction.RIGHT;
    private boolean vertical = false;
    private boolean positive = false;
    private final int tankSpeed = 3;
    boolean isCrippled = false;
    private final GameField gameField;
    private final double bulletWidth = 10;
    private final double bulletHeight = 10;
    private final Team team;
    protected int shootReloadTime = 20;
    private int reload = shootReloadTime;

    public StarShipImpl(double x, double y, double size, GameField gameField,
                        Team team) {
        super(x, y, size);
        this.team = team;
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
            deltaX = (positive ? tankSpeed : -tankSpeed);
            newX += deltaX;
        } else {
            deltaY = (positive ? tankSpeed : -tankSpeed);
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
                gameField, team);
        gameField.getBullets().add(blast);
    }

    @Override
    public void stop() {

    }

    @Override
    public void release() {

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
        if (this.team == Team.ENEMIES) {
            if (intersectsObstacle(newX, newY, gameField.getPlayersTank())) {
                double intSize = getIntersectionSize(newX, newY, gameField.getPlayersTank());
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
        Explosion e = new ExplosionImpl(getX(), getY(), getWidth());
        gameField.getExplosions().add(e);
        isCrippled = true;
    }

    @Override
    public Direction getSide() {
        return currentDir;
    }

    private Point2D calcBulletCoords() {
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
