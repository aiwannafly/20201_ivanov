package com.games.starwars.controller;

import com.games.starwars.view.Renderer;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public interface Engine {

    enum Status {
        IN_PROGRESS, WIN, LOSE, PAUSE
    }

    void handlePressedKeyEvent(KeyEvent event);

    void handleReleasedKeyEvent(KeyEvent event);

    void handleClickEvent(MouseEvent event);

    void handleClickReleasedEvent(MouseEvent event);

    Status update();

    void render();

    Renderer getRenderer();
}
