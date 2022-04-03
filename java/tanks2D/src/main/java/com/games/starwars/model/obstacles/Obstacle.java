package com.games.starwars.model.obstacles;

import com.games.starwars.model.Shape;

public interface Obstacle extends Shape {

    boolean isCrippled();

    void hit();

    void setCodeName(Character codeName);

    Character getCodeName();
}
