package com.games.starwars.model.factory;

import com.games.starwars.model.ships.StarShip;

public class FactoryOfShipsImpl extends ReflexiveFactoryOfObjects implements FactoryOfStarShips{

    @Override
    public StarShip getStarShip(Character code) throws FactoryFailureException {
        try {
            return (StarShip) getObject(code);
        } catch (ClassCastException exception) {
            throw new FactoryFailureException("Cast exception: " + exception.getMessage());
        }
    }
}
