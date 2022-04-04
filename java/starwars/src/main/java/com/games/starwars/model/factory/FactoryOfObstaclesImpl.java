package com.games.starwars.model.factory;

import com.games.starwars.model.obstacles.Obstacle;

public class FactoryOfObstaclesImpl extends ReflexiveFactoryOfObjects implements FactoryOfObstacles{

    @Override
    public Obstacle getObstacle(Character code) throws FactoryFailureException {
        try {
            return (Obstacle) getObject(code);
        } catch (ClassCastException exception) {
            throw new FactoryFailureException("Cast exception: " + exception.getMessage());
        }
    }
}
