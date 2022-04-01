package com.games.tanks2d.controller;

import com.games.tanks2d.view.Renderer;
import javafx.scene.input.KeyEvent;

public interface Engine {

    enum Status {
        IN_PROGRESS, WIN, LOSE
    }

    void handlePressedKeyEvent(KeyEvent event);

    void handleReleasedKeyEvent(KeyEvent event);

    Status update();

    void render();

    Renderer getRenderer();
}
