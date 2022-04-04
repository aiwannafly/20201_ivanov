package com.games.starwars.view.textures.blasts;

import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.SoundsPlayer;
import com.games.starwars.view.textures.TexturePack;

public class GreenBlastTexture extends BlueBlastTexture implements BlastTexture {

    @Override
    protected void setFill() {
        if (getBlast().getSide() == StarShip.Direction.RIGHT ||
                getBlast().getSide() == StarShip.Direction.LEFT) {
            getTexture().setFill(TexturePack.imgGreenBlastHor);
            return;
        }
        getTexture().setFill(TexturePack.imgGreenBlastVert);
    }

    @Override
    public void playSound() {
        SoundsPlayer.playShipShoot();
    }
}
