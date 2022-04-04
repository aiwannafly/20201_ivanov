package com.games.starwars.model;

import com.games.starwars.model.obstacles.SquareBlock;

public class ExplosionImpl extends SquareBlock implements Explosion {
    private int lifeTime = 10;
    private Character codeName = null;

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
    public void setCodeName(Character codeName) {
        this.codeName = codeName;
    }

    @Override
    public Character getCodeName() {
        return codeName;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }
}
