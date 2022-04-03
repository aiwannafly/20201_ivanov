package com.games.starwars.model.obstacles;

public class WallBlock extends SquareBlock {
    private Character codeName = null;

    public WallBlock(double x, double y, double size) {
        super(x, y, size);
    }

    public WallBlock() {
        super(0, 0, 0);
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

    @Override
    public void setCodeName(Character codeName) {
        this.codeName = codeName;
    }

    @Override
    public Character getCodeName() {
        return codeName;
    }
}
