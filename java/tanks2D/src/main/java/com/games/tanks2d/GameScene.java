package com.games.tanks2d;

import javafx.scene.Scene;

public interface GameScene {
    Scene getScene();

    boolean isActive();

    void continueGame();
}
