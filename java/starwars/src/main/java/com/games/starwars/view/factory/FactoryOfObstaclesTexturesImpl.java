package com.games.starwars.view.factory;

import com.games.starwars.model.factory.FactoryFailureException;
import com.games.starwars.model.factory.ReflexiveFactoryOfObjects;
import com.games.starwars.view.textures.obstacles.ObstacleTexture;

public class FactoryOfObstaclesTexturesImpl extends ReflexiveFactoryOfObjects implements FactoryOfObstaclesTextures {

    @Override
    public ObstacleTexture getTexture(Character code) throws FactoryFailureException {
        try {
            return (ObstacleTexture) getObject(code);
        } catch (Exception e) {
            throw new FactoryFailureException(e.getMessage());
        }
    }
}
