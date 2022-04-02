package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.GameField;
import com.games.tanks2d.model.ships.EmpireStarShip;
import com.games.tanks2d.model.ships.StarShip;

public class Exterminator extends EmpireStarShip implements StarShip {

    public Exterminator(double x, double y, double size, GameField gameField) {
        super(x, y, size, gameField);
        setHP(1);
        setSpeed(getSpeed() * 2);
        setReloadTime((int) (getReloadTime() * 0.75));
    }
}
