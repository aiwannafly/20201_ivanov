package com.games.tanks2d;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;

public class BattleTankPlayer extends BattleTank {
    private boolean goUp = false;
    private boolean goRight = false;
    private boolean goDown = false;
    private boolean goLeft = false;
    private boolean shootEnabled = false;

    public BattleTankPlayer(double x, double y, double width, double height, LevelDesigner env) {
        super(x, y, width, height, env);
        setFill(TexturePack.imgPlayerTankRight);
        setId("player");
    }

    @Override
    public void release() {
        Scene scene = this.getScene();
        if (null == scene) {
            System.err.println("No scene found for players tank");
            return;
        }
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP, W -> goUp = true;
                case LEFT, A -> goLeft = true;
                case DOWN, S -> goDown = true;
                case RIGHT, D -> goRight = true;
                case SPACE -> shootEnabled = true;
            }
        });
        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case UP, W -> goUp = false;
                case LEFT, A -> goLeft = false;
                case DOWN, S -> goDown = false;
                case RIGHT, D -> goRight = false;
                case SPACE -> shootEnabled = false;
            }
        });
        timer.start();
    }

    @Override
    protected void changeDirection(Direction side) {
        super.changeDirection(side);
        switch (side) {
            case TOP -> this.setFill(TexturePack.imgPlayerTankTop);
            case BOTTOM -> this.setFill(TexturePack.imgPlayerTankBottom);
            case LEFT -> this.setFill(TexturePack.imgPlayerTankLeft);
            case RIGHT -> this.setFill(TexturePack.imgPlayerTankRight);
        }
    }

    private final AnimationTimer timer = new AnimationTimer() {
        private long lastUpdateTime = 0;
        private final int shootSpeed = 50;
        private int playerReload = shootSpeed;

        @Override
        public void handle(long now) {
            if (now - lastUpdateTime >= Level.DELAY) {
                animation();
                lastUpdateTime = now;
            }
        }

        private void animation() {
            if (goUp) {
                move(Tank.Direction.TOP);
            } else if (goLeft) {
                move(Tank.Direction.LEFT);
            } else if (goRight) {
                move(Tank.Direction.RIGHT);
            } else if (goDown) {
                move(Tank.Direction.BOTTOM);
            }
            if (playerReload <= 0) {
                if (shootEnabled) {
                    playerReload = shootSpeed;
                    shoot();
                }
            }
            playerReload--;
        }
    };
}
