package com.games.starwars.view.textures;

import javafx.scene.shape.Rectangle;

public interface Texture {

    Rectangle getTexture();

    void updateView();

    void setX(double x);

    void setY(double y);

    void setWidth(double width);

    void setHeight(double height);

}
