package com.games.starwars.view.factory;

import com.games.starwars.model.factory.FactoryFailureException;
import com.games.starwars.model.factory.ReflexiveFactoryOfObjects;
import com.games.starwars.view.textures.blasts.BlastTexture;

public class FactoryOfBlastsTexturesImpl extends ReflexiveFactoryOfObjects implements FactoryOfBlastsTextures {

    @Override
    public BlastTexture getTexture(Character code) throws FactoryFailureException {
        try {
            return (BlastTexture) getObject(code);
        } catch (Exception e) {
            throw new FactoryFailureException(e.getMessage());
        }
    }
}
