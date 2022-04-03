package com.games.starwars.model.obstacles;

public class FragileBlock extends SquareBlock {
    private boolean isCrippled = false;
    private Character codeName = null;

    public FragileBlock() {
        super(0, 0, 0);
    }

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

    @Override
    public void setCodeName(Character codeName) {
        this.codeName = codeName;
    }

    @Override
    public Character getCodeName() {
        return codeName;
    }
}
