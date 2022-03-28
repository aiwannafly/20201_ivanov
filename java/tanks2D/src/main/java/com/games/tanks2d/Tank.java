package com.games.tanks2d;

public interface Tank {

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

    void kill();

    boolean isAlive();

    Direction getSide();
}
