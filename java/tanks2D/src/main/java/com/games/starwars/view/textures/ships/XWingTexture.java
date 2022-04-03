package com.games.starwars.view.textures.ships;

import com.games.starwars.view.textures.TexturePack;
import javafx.scene.layout.Pane;

public class XWingTexture extends StarDestroyerTexture implements ShipTexture {

    @Override
    protected void setFill() {
        getTexture().setFill(TexturePack.getRebellionShipTexture(getShip().getCurrentDirection()));
    }

    @Override
    protected void initHealthBar() {

    }

    @Override
    protected void updateHealthBar() {

    }
}
