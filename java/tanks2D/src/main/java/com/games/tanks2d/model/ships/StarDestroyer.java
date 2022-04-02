package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.Blast;
import com.games.tanks2d.model.BlastImpl;
import com.games.tanks2d.model.GameField;
import com.games.tanks2d.model.Point2D;

public class StarDestroyer extends EmpireStarShip implements StarShip {
    private final int maxShotsCount = 3;
    private int shotsCount = 0;
    private final int burstReloadTime = 10;
    private int burstReloadCount = 10;
    private int reload = 0;
    private final int shootReloadTime = 50;
    private final double bulletSize = 10;
    private final GameField gameField;
    private final Class type = Class.STAR_DESTROYER;

    public StarDestroyer(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField);
        setSpeed(2);
        setHP(5);
        this.gameField = gameField;
//        this.singleDirTime = 400;
        setType(Class.STAR_DESTROYER);
    }

    @Override
    public void shoot() {
        reload--;
        if (reload > 0) {
            return;
        }
        if (shotsCount >= maxShotsCount) {
            shotsCount = 0;
            reload = shootReloadTime;
        }
        burstReloadCount--;
        if (burstReloadCount > 0) {
            return;
        }
        burstReloadCount = burstReloadTime;
        shotsCount++;
        Point2D p = calcBulletCoords();
        double betweenOffset = bulletSize * 2.5;
        double startOffset = getWidth() * 0.75;
        Blast first = null;
        Blast second = null;
        if (getSide() == Direction.RIGHT || getSide() == Direction.LEFT) {
            if (getSide() == Direction.RIGHT) {
                startOffset *= -1;
            }
            first = new BlastImpl(p.x + startOffset, p.y + betweenOffset, bulletSize, getSide(),
                    gameField, type);
            second = new BlastImpl(p.x + startOffset, p.y - betweenOffset, bulletSize, getSide(),
                    gameField, type);
        } else {
            if (getSide() == Direction.BOTTOM) {
                startOffset *= -1;
            }
            first = new BlastImpl(p.x + betweenOffset, p.y + startOffset, bulletSize, getSide(),
                    gameField, type);
            second = new BlastImpl(p.x - betweenOffset, p.y + startOffset, bulletSize, getSide(),
                    gameField, type);
        }
        gameField.getBullets().add(first);
        gameField.getBullets().add(second);
    }
}
