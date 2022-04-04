package com.games.starwars.model.factory;

import com.games.starwars.model.obstacles.Obstacle;

public interface FactoryOfObstacles {

    Obstacle getObstacle(Character code) throws FactoryFailureException;

    void setConfigs(String configsFileName) throws FactoryBadConfigsException;
}
