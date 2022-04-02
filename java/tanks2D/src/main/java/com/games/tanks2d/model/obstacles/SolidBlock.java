package com.games.tanks2d.model.obstacles;

public class SolidBlock extends SquareBlock {
    public SolidBlock(double x, double y, double size) {
        super(x, y, size);
    }

    @Override
    public boolean isCrippled() {
        return false;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public void hit() {
        // does not matter
    }
}
