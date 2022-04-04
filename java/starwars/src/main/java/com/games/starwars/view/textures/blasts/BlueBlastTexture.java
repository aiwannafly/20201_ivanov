package com.games.starwars.view.textures.blasts;

import com.games.starwars.model.Blast;
import com.games.starwars.model.ships.StarShip;
import com.games.starwars.view.SoundsPlayer;
import com.games.starwars.view.textures.TexturePack;
import com.games.starwars.view.textures.TextureImpl;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class BlueBlastTexture extends TextureImpl implements BlastTexture {
    private Blast blast;

    public BlueBlastTexture() {
        super(0, 0, 0, 0);
    }

    public BlueBlastTexture(Blast blast) {
        super(blast.getX(), blast.getY(),
                blast.getWidth(), blast.getHeight());
        this.blast = blast;
        setFill();
    }

    @Override
    public Rectangle getTexture() {
        return super.getTexture();
    }

    @Override
    public void updateView(Pane pane) {
//        getTexture().relocate(blast.getX(), blast.getY());
        getTexture().setX(blast.getX());
        getTexture().setY(blast.getY());
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
    public void playSound() {
        SoundsPlayer.playLaserShoot();
    }

    @Override
    public void setBlast(Blast blast) {
        this.blast = blast;
        double scale = 3.6;
        if (blast.getSide() == StarShip.Direction.RIGHT || blast.getSide() == StarShip.Direction.LEFT) {
            getTexture().setWidth(scale * blast.getWidth());
            getTexture().setHeight(blast.getHeight());
        } else {
            getTexture().setWidth(blast.getWidth());
            getTexture().setHeight(scale * blast.getHeight());
        }
        setFill();
        getTexture().setX(blast.getX());
        getTexture().setY(blast.getY());
    }

    protected Blast getBlast() {
        return blast;
    }

    protected void setFill() {
        if (blast.getSide() == StarShip.Direction.RIGHT ||
                blast.getSide() == StarShip.Direction.LEFT) {
            getTexture().setFill(TexturePack.imgBlueBulletHor);
        } else {
            getTexture().setFill(TexturePack.imgBlueBulletVert);
        }
    }

}
