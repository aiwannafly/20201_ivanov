package com.games.starwars.view.factory;

import com.games.starwars.model.factory.FactoryFailureException;
import com.games.starwars.model.factory.ReflexiveFactoryOfObjects;
import com.games.starwars.view.textures.ships.ShipTexture;

public class FactoryOfStarShipsTexturesImpl extends ReflexiveFactoryOfObjects implements FactoryOfStarShipsTextures {

    @Override
    public ShipTexture getTexture(Character code) throws FactoryFailureException {
        try {
            return (ShipTexture) getObject(code);
        } catch (Exception e) {
            throw new FactoryFailureException(e.getMessage());
        }
    }
}
