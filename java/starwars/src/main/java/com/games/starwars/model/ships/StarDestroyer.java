package com.games.starwars.model.ships;

import com.games.starwars.model.Blast;
import com.games.starwars.model.BlastImpl;
import com.games.starwars.model.GameField;
import com.games.starwars.model.Point2D;

public class StarDestroyer extends EmpireStarShip implements StarShip {
    private int burstCount = 3;
    private final int BURST_RELOAD_TIME = 10;
    private final int SHOOT_RELOAD_TIME = 30;
    private final double BLAST_SIZE = 10;
    private final int START_HP = 9;
    private GameField gameField;
    private int shotsCount = 0;
    private int burstReloadCount = 10;
    private int reload = 0;
    private int speed = 0;

    public StarDestroyer() {
        super(0, 0, 0 , null);
        initStats();
    }

    private void initStats() {
        speed = getSpeed() - 1;
        setSpeed(speed);
        setHP(START_HP);
        setSingleDirTime(400);
    }

    public StarDestroyer(double x, double y, double blockSize, GameField gameField) {
        super(x, y, blockSize * 4, gameField);
        initStats();
        this.gameField = gameField;
    }

    @Override
    public void setHeight(double height) {
        super.setHeight(2 * height);
    }

    @Override
    public void setWidth(double height) {
        super.setWidth(2 * height);
    }

    @Override
    public void shoot() {
        reload--;
        if (reload > 0) {
            return;
        }
        if (shotsCount >= burstCount) {
            shotsCount = 0;
            if (getHP() <= START_HP / 2) {
                setSpeed(speed + 1);
                burstCount = 4;
            }
            reload = SHOOT_RELOAD_TIME;
        }
        burstReloadCount--;
        if (burstReloadCount > 0) {
            return;
        }
        burstReloadCount = BURST_RELOAD_TIME;
        shotsCount++;
        Point2D p = calcBulletCoords();
        double betweenOffset = BLAST_SIZE * 2.5;
        double startOffset = getWidth() * 0.75;
        Blast first;
        Blast second;
        if (getCurrentDirection() == Direction.RIGHT || getCurrentDirection() == Direction.LEFT) {
            if (getCurrentDirection() == Direction.RIGHT) {
                startOffset *= -1;
            }
            first = new BlastImpl(p.x + startOffset, p.y + betweenOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, getCodeName());
            second = new BlastImpl(p.x + startOffset, p.y - betweenOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, getCodeName());
        } else {
            if (getCurrentDirection() == Direction.BOTTOM) {
                startOffset *= -1;
            }
            first = new BlastImpl(p.x + betweenOffset, p.y + startOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, getCodeName());
            second = new BlastImpl(p.x - betweenOffset, p.y + startOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, getCodeName());
        }
        gameField.getBullets().add(first);
        gameField.getBullets().add(second);
    }

    @Override
    public void setGameField(GameField gameField) {
        this.gameField = gameField;
        super.setGameField(gameField);
    }
}
