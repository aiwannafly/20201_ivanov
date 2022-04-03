package com.games.starwars.view.textures.ships;

import com.games.starwars.view.TexturePack;

public class ExterminatorTexture extends StarDestroyerTexture implements ShipTexture {

    @Override
    protected void setFill() {
        getTexture().setFill(TexturePack.getExtStarShipTexture(getShip().getCurrentDirection()));
    }
}
