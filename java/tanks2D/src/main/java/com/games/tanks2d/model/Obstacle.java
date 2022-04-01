package com.games.tanks2d.model;

public interface Obstacle extends Shape {

    boolean isCrippled();

    void hit();
}
