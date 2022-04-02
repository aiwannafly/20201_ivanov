package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.Blast;
import com.games.tanks2d.model.BlastImpl;
import com.games.tanks2d.model.GameField;
import com.games.tanks2d.model.Point2D;

public class StarDestroyer extends EmpireStarShip implements StarShip {
    private final int BURST_VALUE = 3;
    private final int BURST_RELOAD_TIME = 10;
    private final int SHOOT_RELOAD_TIME = 50;
    private final double BLAST_SIZE = 10;
    private final GameField gameField;
    private final ShipClass type = ShipClass.STAR_DESTROYER;
    private int shotsCount = 0;
    private int burstReloadCount = 10;
    private int reload = 0;

    public StarDestroyer(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField);
        setSpeed(getSpeed() - 1);
        setHP(5);
        setSingleDirTime(400);
        setShipClass(ShipClass.STAR_DESTROYER);
        this.gameField = gameField;
    }

    @Override
    public void shoot() {
        reload--;
        if (reload > 0) {
            return;
        }
        if (shotsCount >= BURST_VALUE) {
            shotsCount = 0;
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
                    gameField, type);
            second = new BlastImpl(p.x + startOffset, p.y - betweenOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, type);
        } else {
            if (getCurrentDirection() == Direction.BOTTOM) {
                startOffset *= -1;
            }
            first = new BlastImpl(p.x + betweenOffset, p.y + startOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, type);
            second = new BlastImpl(p.x - betweenOffset, p.y + startOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, type);
        }
        gameField.getBullets().add(first);
        gameField.getBullets().add(second);
    }
}
