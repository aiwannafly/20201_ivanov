package com.games.tanks2d.model.ships;

import com.games.tanks2d.model.obstacles.Obstacle;

public interface StarShip extends Obstacle {

    enum Class {
        REBELLION_SHIP, EMPIRE_SHIP, STAR_DESTROYER, EXTERMINATOR
    }

    enum Direction {
        TOP, RIGHT, BOTTOM, LEFT;

        public boolean isVertical() {
            return !(this == TOP || this == BOTTOM);
        }
    }

    void move(Direction side);

    void shoot();

    void setSpeed(int speed);

    void setHP(int HP);

    void setType(Class type);

    void setReloadTime(int reloadTime);

    int getHP();

    int getSpeed();

    Class getType();

    int getReloadTime();

    Direction getSide();

}
