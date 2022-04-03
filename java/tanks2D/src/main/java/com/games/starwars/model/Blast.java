package com.games.starwars.model;

import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.model.ships.StarShip;

public interface Blast extends Obstacle {

    void fly();

    StarShip.Direction getSide();

    void setCodeName(Character codeName);

    Character getCodeName();
}
