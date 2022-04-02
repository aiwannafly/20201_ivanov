package com.games.tanks2d.model.obstacles;

public class WallBlock extends SquareBlock {
    public WallBlock(double x, double y, double size) {
        super(x, y, size);
    }

    @Override
    public boolean isCrippled() {
        return false;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public void hit() {
        // not destructible
    }
}
