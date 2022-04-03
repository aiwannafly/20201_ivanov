package com.games.starwars.model;

public interface Shape {
    void setX(double x);

    void setY(double y);

    void setWidth(double w);

    void setHeight(double h);

    double getX();

    double getY();

    double getHeight();

    double getWidth();

    boolean isTransparent();
}
