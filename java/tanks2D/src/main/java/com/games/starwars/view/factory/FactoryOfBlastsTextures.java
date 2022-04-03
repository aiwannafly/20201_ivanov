package com.games.starwars.view.factory;

import com.games.starwars.model.factory.FactoryBadConfigsException;
import com.games.starwars.model.factory.FactoryFailureException;
import com.games.starwars.view.textures.blasts.BlastTexture;

public interface FactoryOfBlastsTextures {
    BlastTexture getTexture(Character code) throws FactoryFailureException;

    void setConfigs(String configsFileName) throws FactoryBadConfigsException;
}
