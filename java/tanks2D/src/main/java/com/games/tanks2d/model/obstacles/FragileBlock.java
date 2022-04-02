package com.games.tanks2d.model.obstacles;

public class FragileBlock extends SquareBlock {
    private boolean isCrippled = false;

    public FragileBlock(double x, double y, double size) {
        super(x, y, size);
    }

    @Override
    public boolean isCrippled() {
        return isCrippled;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }

    @Override
    public void hit() {
        isCrippled = true;
    }
}
