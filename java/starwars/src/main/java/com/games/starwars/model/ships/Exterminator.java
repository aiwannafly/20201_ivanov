package com.games.starwars.model.ships;

import com.games.starwars.model.GameField;

public class Exterminator extends EmpireStarShip implements StarShip {

    public Exterminator() {
        super(0, 0, 0, null);
        initStats();
    }

    public Exterminator(double x, double y, double blockSize, GameField gameField) {
        super(x, y, blockSize, gameField);
        initStats();
    }

    @Override
    public void setHeight(double height) {
        super.setHeight(height / 2);
    }

    @Override
    public void setWidth(double height) {
        super.setWidth(height / 2);
    }

    private void initStats() {
        setHP(1);
        setSpeed(getSpeed() * 2);
        setReloadTime((int) (getReloadTime() * 0.75));
    }
}
