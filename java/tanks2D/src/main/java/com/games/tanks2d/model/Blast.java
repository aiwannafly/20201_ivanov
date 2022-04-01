package com.games.tanks2d.model;

public interface Blast extends Obstacle {

    void fly();

    StarShip.Direction getSide();

    StarShip.Team getTeam();
}
