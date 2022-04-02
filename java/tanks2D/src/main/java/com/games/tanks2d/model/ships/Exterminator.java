package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.GameField;

public class Exterminator extends EmpireStarShip implements StarShip {

    public Exterminator(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField);
        setShipClass(ShipClass.EXTERMINATOR);
        setHP(1);
        setSpeed(getSpeed() * 2);
        setReloadTime((int) (getReloadTime() * 0.75));
    }
}
