package com.games.starwars.model.obstacles;

import com.games.starwars.model.Shape;

abstract public class SquareBlock implements Obstacle {
    protected double x;
    protected double y;
    protected double size;

    public SquareBlock(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public void setSize(double size) {
        setHeight(size);
        setWidth(size);
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }

    @Override
    public void setWidth(double w) {
        this.size = w;
    }

    @Override
    public void setHeight(double h) {
        this.size = h;
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getHeight() {
        return size;
    }

    @Override
    public double getWidth() {
        return size;
    }

    protected double getIntersectionSize(double newX, double newY, Shape o) {
        double xSize = Math.min(Math.abs(newX - (o.getX() + o.getWidth())),
                Math.abs(newX + getWidth() - o.getX()));
        double ySize = Math.min(Math.abs(newY - (o.getY() + o.getHeight())),
                Math.abs(newY + getHeight() - o.getY()));
        return Math.min(xSize, ySize);
    }

    protected boolean crossesObstacle(double newX, double newY, Shape o) {
        if (intersectsObstacle(newX, newY, o)) {
            double intSize = getIntersectionSize(newX, newY, o);
            return intSize != 0;
        }
        return false;
    }

    protected boolean intersectsObstacle(double newX, double newY, Shape o) {
        if (o.isTransparent()) {
            return false;
        }
        boolean leftOutside = false;
        boolean rightOutside = false;
        boolean topOutside = false;
        boolean bottomOutside = false;

        if ((newX > o.getX() && newX > o.getX() + o.getWidth()) ||
                (newX < o.getX() && newX < o.getX() + o.getWidth())) {
            leftOutside = true;
        }
        if ((newX + size > o.getX() && newX + size > o.getX() + o.getWidth()) ||
                (newX + size < o.getX() && newX + size < o.getX() + o.getWidth())) {
            rightOutside = true;
        }
        if (leftOutside && rightOutside) {
            return false;
        }
        if ((newY > o.getY() && newY > o.getY() + o.getHeight()) ||
                (newY < o.getY() && newY < o.getY() + o.getHeight())) {
            topOutside = true;
        }
        if ((newY + size > o.getY() && newY + size > o.getY() + o.getHeight()) ||
                (newY + size < o.getY() && newY + size < o.getY() + o.getHeight())) {
            bottomOutside = true;
        }
        return !(topOutside && bottomOutside);
    }

    protected double getDistance(Shape s) {
        double width = Math.abs(s.getX() - this.getX());
        double height = Math.abs(s.getY() - this.getY());
        return Math.sqrt(width * width + height * height);
    }
}
