package com.games.starwars.view.factory;

import com.games.starwars.model.factory.FactoryBadConfigsException;
import com.games.starwars.model.factory.FactoryFailureException;
import com.games.starwars.view.textures.ships.ShipTexture;

public interface FactoryOfStarShipsTextures {

    ShipTexture getTexture(Character code) throws FactoryFailureException;

    void setConfigs(String configsFileName) throws FactoryBadConfigsException;
}
