package com.games.starwars.view.textures.ships;

import com.games.starwars.view.textures.TexturePack;
import javafx.scene.paint.Color;

public class XWingTexture extends StarDestroyerTexture implements ShipTexture {

    @Override
    protected void setFill() {
        getTexture().setFill(TexturePack.getRebellionShipTexture(getShip().getCurrentDirection()));
    }

    @Override
    protected void initHealthBar() {
        setHpBarColor(Color.LIMEGREEN);
        super.initHealthBar();
    }
}
