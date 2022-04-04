package com.games.starwars.view.textures;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public interface Texture {

    void appear(Pane pane);

    void updateView(Pane pane);

    void removeFrom(Pane pane);

    Rectangle getTexture();

    void setX(double x);

    void setY(double y);

    void setWidth(double width);

    void setHeight(double height);

}
