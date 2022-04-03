package com.games.starwars.view.textures.blasts;

import com.games.starwars.model.Blast;
import com.games.starwars.view.textures.Texture;

public interface BlastTexture extends Texture {

    void playSound();

    void setBlast(Blast blast);
}
