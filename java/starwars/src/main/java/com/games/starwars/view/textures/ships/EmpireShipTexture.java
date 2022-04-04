package com.games.starwars.view.textures.ships;

import com.games.starwars.view.textures.TexturePack;

public class EmpireShipTexture extends StarDestroyerTexture implements ShipTexture {

    @Override
    protected void setFill() {
        getTexture().setFill(TexturePack.getEmpireStarShipTexture(getShip().getCurrentDirection()));
    }

}
