package com.games.starwars.view.textures;

import javafx.scene.shape.Rectangle;

abstract public class TextureImpl implements Texture {
    private final Rectangle texture;

    public TextureImpl(double x, double y, double width, double height) {
        texture = new Rectangle(x, y, width, height);
    }

    @Override
    public Rectangle getTexture() {
        return texture;
    }

    @Override
    public void setX(double x) {
        texture.setX(x);
    }

    @Override
    public void setY(double y) {
        texture.setY(y);
    }

    @Override
    public void setWidth(double width) {
        texture.setWidth(width);
    }

    @Override
    public void setHeight(double height) {
        texture.setHeight(height);
    }
}
