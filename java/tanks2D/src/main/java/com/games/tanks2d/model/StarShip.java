package com.games.tanks2d.model;

public interface StarShip extends Obstacle {

    enum Team {
        PLAYERS, ENEMIES
    }

    enum Direction {
        TOP, RIGHT, BOTTOM, LEFT;

        public boolean isVertical() {
            return !(this == TOP || this == BOTTOM);
        }
    }

    void move(Direction side);

    void shoot();

    void stop();

    void release();

    Direction getSide();


}
