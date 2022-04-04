package com.games.starwars.model.obstacles;

public class SolidBlock extends SquareBlock {
    private Character codeName = null;

    public SolidBlock(double x, double y, double size) {
        super(x, y, size);
    }

    public SolidBlock() {
        super(0, 0, 0);
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

    @Override
    public void setCodeName(Character codeName) {
        this.codeName = codeName;
    }

    @Override
    public Character getCodeName() {
        return codeName;
    }
}
