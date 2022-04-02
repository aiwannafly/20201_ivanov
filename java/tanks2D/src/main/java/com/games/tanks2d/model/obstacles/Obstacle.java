package com.games.tanks2d.model.obstacles;

import com.games.tanks2d.model.Shape;

public interface Obstacle extends Shape {

    boolean isCrippled();

    void hit();
}
