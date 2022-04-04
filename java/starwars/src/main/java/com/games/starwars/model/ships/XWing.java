package com.games.starwars.model.ships;

import com.games.starwars.model.Blast;
import com.games.starwars.model.BlastImpl;
import com.games.starwars.model.GameField;
import com.games.starwars.model.Point2D;

public class XWing extends StarShipImpl implements StarShip {
    private final double BLAST_SIZE = 10;
    private GameField gameField;

    public XWing() {
        super(0, 0, 0 , null);
        initStats();
    }

    private void initStats() {
        setSpeed(getSpeed() + 1);
    }

    public XWing(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField);
        initStats();
    }

    @Override
    public void shoot() {
        setReloadTime(0);
        Point2D p = calcBulletCoords();
        double betweenOffset = BLAST_SIZE * 2.4;
        double startOffset = 0;
        Blast first;
        Blast second;
        if (getCurrentDirection() == Direction.RIGHT || getCurrentDirection() == Direction.LEFT) {
            if (getCurrentDirection() == Direction.RIGHT) {
                startOffset -= betweenOffset;
            }
            first = new BlastImpl(p.x + startOffset, p.y + betweenOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, getCodeName());
            second = new BlastImpl(p.x + startOffset, p.y - betweenOffset, BLAST_SIZE, getCurrentDirection(),
                    gameField, getCodeName());
        } else {
            if (getCurrentDirection() == Direction.BOTTOM) {
                startOffset -= betweenOffset;
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
