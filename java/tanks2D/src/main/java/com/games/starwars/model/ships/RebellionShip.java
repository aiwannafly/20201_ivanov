package com.games.starwars.model.ships;

import com.games.starwars.model.GameField;

public class RebellionShip extends StarShipImpl implements StarShip {

    public RebellionShip() {
        super(0, 0, 0 , null);
        initStats();
    }

    private void initStats() {
        setSpeed(getSpeed() + 1);
    }

    public RebellionShip(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField);
        initStats();
    }

    @Override
    public void shoot() {
        setReloadTime(0);
        super.shoot();
    }

}
