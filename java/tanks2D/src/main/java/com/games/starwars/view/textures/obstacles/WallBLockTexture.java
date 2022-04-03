package com.games.starwars.view.textures.obstacles;

import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.view.TexturePack;
import com.games.starwars.view.textures.Texture;
import com.games.starwars.view.textures.TextureImpl;

public class WallBLockTexture extends TextureImpl implements ObstacleTexture {

    public WallBLockTexture() {
        super(0, 0, 0, 0);
        getTexture().setFill(TexturePack.imgMetalBlock);
    }

    public WallBLockTexture(double x, double y, double width, double height) {
        super(x, y, width, height);
        getTexture().setFill(TexturePack.imgMetalBlock);
    }

    @Override
    public void updateView() {

    }

    @Override
    public void setObstacle(Obstacle o) {
        getTexture().setX(o.getX());
        getTexture().setY(o.getY());
        getTexture().setWidth(o.getWidth());
        getTexture().setHeight(o.getHeight());
        getTexture().setFill(TexturePack.imgMetalBlock);
    }
}
