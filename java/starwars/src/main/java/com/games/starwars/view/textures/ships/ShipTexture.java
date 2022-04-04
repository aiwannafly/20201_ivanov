package com.games.starwars.view.textures.ships;

import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.textures.Texture;

public interface ShipTexture extends Texture {

    void setShip(StarShip ship);
}
