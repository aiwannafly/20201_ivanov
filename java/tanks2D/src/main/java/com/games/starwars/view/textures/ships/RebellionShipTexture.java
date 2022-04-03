package com.games.starwars.view.textures.ships;

import com.games.starwars.view.TexturePack;

public class RebellionShipTexture extends StarDestroyerTexture implements ShipTexture {

    @Override
    protected void setFill() {
        getTexture().setFill(TexturePack.getRebellionShipTexture(getShip().getCurrentDirection()));
    }
}
