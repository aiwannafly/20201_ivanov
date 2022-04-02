package com.games.tanks2d.model;

import com.games.tanks2d.model.obstacles.SquareBlock;

public class ExplosionImpl extends SquareBlock implements Explosion {
    private int lifeTime = 10;

    public ExplosionImpl(double x, double y, double size) {
        super(x, y, size);
    }

    @Override
    public boolean isCrippled() {
        return lifeTime <= 0;
    }

    @Override
    public void hit() {
        lifeTime--;
    }

    @Override
    public boolean isTransparent() {
        return false;
    }
}
