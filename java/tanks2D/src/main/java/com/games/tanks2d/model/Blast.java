package com.games.tanks2d.model;

import com.games.tanks2d.model.obstacles.Obstacle;
import com.games.tanks2d.model.ships.StarShip;

public interface Blast extends Obstacle {

    void fly();

    StarShip.Direction getSide();

    StarShip.Class getTeam();
}
