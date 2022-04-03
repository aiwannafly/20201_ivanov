package com.games.starwars.view.textures.ships;

import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.textures.TexturePack;
import com.games.starwars.view.textures.TextureImpl;
import javafx.scene.shape.Rectangle;

public class StarDestroyerTexture extends TextureImpl implements ShipTexture {
    private StarShip ship;

    public StarDestroyerTexture() {
        super(0, 0, 0, 0);
    }

    public StarDestroyerTexture(StarShip s) {
        super(s.getX(), s.getY(), s.getWidth(), s.getHeight());
        getTexture().setFill(TexturePack.getStarDestroyerTexture(s.getCurrentDirection()));
        ship = s;
    }

    @Override
    public Rectangle getTexture() {
        return super.getTexture();
    }

    @Override
    public void updateView() {
        setFill();
        getTexture().setX(ship.getX());
        getTexture().setY(ship.getY());
    }

    @Override
    public void setShip(StarShip ship) {
        this.ship = ship;
        setX(ship.getX());
        setY(ship.getY());
        setWidth(ship.getWidth());
        setHeight(ship.getHeight());
        setFill();
    }

    protected void setFill() {
        getTexture().setFill(TexturePack.getStarDestroyerTexture(ship.getCurrentDirection()));
    }

    protected StarShip getShip() {
        return ship;
    }
}
