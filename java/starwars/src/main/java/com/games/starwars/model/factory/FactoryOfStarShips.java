package com.games.starwars.model.factory;

import com.games.starwars.model.ships.StarShip;

public interface FactoryOfStarShips {

    StarShip getStarShip(Character code) throws FactoryFailureException;

    void setConfigs(String configsFileName) throws FactoryBadConfigsException;
}
