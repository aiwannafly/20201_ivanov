package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.obstacles.Obstacle;

public interface StarShip extends Obstacle {

    enum ShipClass {
        REBELLION_SHIP, EMPIRE_SHIP, STAR_DESTROYER, EXTERMINATOR
    }

    enum Direction {
        TOP, RIGHT, BOTTOM, LEFT;
    }

    void move(Direction side);

    void shoot();

    void setSpeed(int speed);

    void setHP(int HP);

    void setShipClass(ShipClass shipClass);

    void setReloadTime(int reloadTime);

    int getHP();

    int getSpeed();

    ShipClass getShipClass();

    int getReloadTime();

    Direction getCurrentDirection();

}
