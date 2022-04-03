package com.games.starwars.model.ships;

import com.games.starwars.model.GameField;
import com.games.starwars.model.obstacles.Obstacle;

public interface StarShip extends Obstacle {

    enum Direction {
        TOP, RIGHT, BOTTOM, LEFT;
    }

    void move(Direction side);

    void shoot();

    void setSpeed(int speed);

    void setHP(int HP);

    void setReloadTime(int reloadTime);

    void setGameField(GameField gameField);

    void setCodeName(Character code);

    Character getCodeName();

    int getHP();

    int getSpeed();

    int getReloadTime();

    Direction getCurrentDirection();

}
