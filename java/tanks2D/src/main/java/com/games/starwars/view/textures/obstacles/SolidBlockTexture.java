package com.games.starwars.view.textures.obstacles;

import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.view.textures.TexturePack;
import com.games.starwars.view.textures.TextureImpl;
import javafx.scene.layout.Pane;

public class SolidBlockTexture extends TextureImpl implements ObstacleTexture {

    public SolidBlockTexture() {
        super(0, 0, 0 ,0);
        getTexture().setFill(TexturePack.imgGrayBrickPattern);
    }

    public SolidBlockTexture(double x, double y, double width, double height) {
        super(x, y, width, height);
        getTexture().setFill(TexturePack.imgGrayBrickPattern);
    }

    @Override
    public void updateView(Pane pane) {

    }

    @Override
    public void removeFrom(Pane pane) {
        pane.getChildren().remove(getTexture());
    }

    @Override
    public void appear(Pane pane) {
        pane.getChildren().add(getTexture());
    }

    @Override
    public void setObstacle(Obstacle o) {
        getTexture().setX(o.getX());
        getTexture().setY(o.getY());
        getTexture().setWidth(o.getWidth());
        getTexture().setHeight(o.getHeight());
        getTexture().setFill(TexturePack.imgGrayBrickPattern);
    }
}
