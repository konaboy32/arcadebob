package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Bob extends GameObject {

    private enum PlayerState {
        Standing, WalkingLeft, WalkingRight, JumpingUp, JumpingLeft, JumpingRight, Falling
    }

    private enum JumpState {
        GoingUp, GoingDown
    }

    private static final int MAX_JUMP_HEIGHT = 60;
    private static final int JUMP_STEP_X = 80;
    private static final int JUMP_STEP_Y = 150;
    private static final int WALK_STEP_X = 150;

    private int jumpHeight;
    private boolean stopWhenOnGround;
    private PlayerState playerState;
    private JumpState jumpState;

    public Bob(int x, int y, int width, int height, Texture texture) {
        super(x, y, width, height, texture);
        playerState = PlayerState.Standing;
        jumpState = JumpState.GoingUp;
    }

    public void requestMoveLeft() {
        if (isGrounded()) {
            playerState = PlayerState.WalkingLeft;
        }
        stopWhenOnGround = false;
    }

    public void requestMoveRight() {
        if (isGrounded()) {
            playerState = PlayerState.WalkingRight;
        }
        stopWhenOnGround = false;
    }

    public void requestStop() {
        if (isGrounded()) {
            playerState = PlayerState.Standing;
        } else {
            stopWhenOnGround = true;
        }
    }

    public void requestImmediateStop() {
        playerState = PlayerState.Standing;
    }

    public void requestJump() {
        switch (playerState) {
            case Standing:
                initJump();
                playerState = PlayerState.JumpingUp;
                break;
            case WalkingLeft:
                initJump();
                playerState = PlayerState.JumpingLeft;
                break;
            case WalkingRight:
                initJump();
                playerState = PlayerState.JumpingRight;
                break;
        }
    }

    public void update() {
        checkLimits();

        final float time = Gdx.graphics.getDeltaTime();
        switch (playerState) {
            case JumpingUp:
                jumpUp(time);
                break;
            case JumpingLeft:
                jumpLeft(time);
                break;
            case JumpingRight:
                jumpRight(time);
                break;
            case WalkingLeft:
                walkLeft(time);
                break;
            case WalkingRight:
                walkRight(time);
                break;
        }
    }


    private void jumpUp(float time) {
        moveVertical(time);
        if (jumpFinished()) {
            playerState = PlayerState.Standing;
        }
    }

    private void jumpLeft(float time) {
        moveVertical(time);
        bounds.x -= JUMP_STEP_X * time;
        if (jumpFinished()) {
            if (stopWhenOnGround) {
                playerState = PlayerState.Standing;
                stopWhenOnGround = false;
            } else {
                playerState = PlayerState.WalkingLeft;
            }
        }
    }

    private void jumpRight(float time) {
        moveVertical(time);
        bounds.x += JUMP_STEP_X * time;
        if (jumpFinished()) {
            if (stopWhenOnGround) {
                playerState = PlayerState.Standing;
                stopWhenOnGround = false;
            } else {
                playerState = PlayerState.WalkingRight;
            }
        }
    }

    private boolean jumpFinished() {
        return jumpHeight == 0;
    }

    private void moveVertical(float time) {
        switch (jumpState) {
            case GoingUp:
                jumpHeight++;
                bounds.y += JUMP_STEP_Y * time;
                break;
            case GoingDown:
                jumpHeight--;
                bounds.y -= JUMP_STEP_Y * time;
                break;
        }
        if (jumpHeight == MAX_JUMP_HEIGHT) {
            jumpState = JumpState.GoingDown;
        }
    }

    private void walkLeft(float time) {
        bounds.x -= WALK_STEP_X * time;
    }

    private void walkRight(float time) {
        bounds.x += WALK_STEP_X * time;
    }

    private void checkLimits() {
        if (bounds.x < 0) {
            bounds.x = 0;
        }
        if (bounds.x > 800 - 64) {
            bounds.x = 800 - 64;
        }
    }

    private void initJump() {
        jumpHeight = 0;
        jumpState = JumpState.GoingUp;
    }

    private boolean isGrounded() {
        return playerState.equals(PlayerState.Standing) || playerState.equals(PlayerState.WalkingLeft) || playerState.equals(PlayerState.WalkingRight);
    }
}
