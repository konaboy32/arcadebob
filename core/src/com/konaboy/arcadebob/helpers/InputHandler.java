package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.konaboy.arcadebob.gameobjects.Bob;

public class InputHandler extends InputAdapter {

    private Bob bucket;
    public InputHandler(Bob bucket) {
        this.bucket = bucket;
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                bucket.requestMoveLeft();
                break;
            case Input.Keys.RIGHT:
                bucket.requestMoveRight();
                break;
            case Input.Keys.SPACE:
                bucket.requestJump();
                break;
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                bucket.requestStop();
                break;
            case Input.Keys.RIGHT:
                bucket.requestStop();
                break;
        }
        return true;
    }
}
