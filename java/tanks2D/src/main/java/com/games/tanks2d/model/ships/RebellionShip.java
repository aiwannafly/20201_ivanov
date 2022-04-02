package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.GameField;

public class RebellionShip extends StarShipImpl implements StarShip {

    public RebellionShip(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField, Class.REBELLION_SHIP);
        setSpeed(getSpeed() + 1);
    }

    @Override
    public void shoot() {
        setReloadTime(-1);
        super.shoot();
    }
}
