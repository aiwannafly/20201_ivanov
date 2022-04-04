package com.games.starwars.view.textures.obstacles;

import com.games.starwars.model.obstacles.Obstacle;
import com.games.starwars.view.textures.Texture;

public interface ObstacleTexture extends Texture {
    void setObstacle(Obstacle o);
}
