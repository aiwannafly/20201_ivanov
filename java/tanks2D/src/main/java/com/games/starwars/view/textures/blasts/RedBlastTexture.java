package com.games.starwars.view.textures.blasts;

import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.TexturePack;
import com.games.starwars.view.textures.blasts.BlastTexture;
import com.games.starwars.view.textures.blasts.BlueBlastTexture;

public class RedBlastTexture extends BlueBlastTexture implements BlastTexture {

    @Override
    protected void setFill() {
        if (getBlast().getSide() == StarShip.Direction.RIGHT ||
        getBlast().getSide() == StarShip.Direction.LEFT) {
            getTexture().setFill(TexturePack.imgRedBulletHor);
            return;
        }
        getTexture().setFill(TexturePack.imgRedBulletVert);
    }
}
