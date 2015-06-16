package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.game.Level;
import com.konaboy.arcadebob.helpers.Constants;

public class Player {

    public enum State {
        Standing, Walking, Jumping
    }

    //constants
    public static final float WIDTH = 1f / Level.TILE_SIZE * 20;
    public static final float HEIGHT = 1f / Level.TILE_SIZE * 32;
    public static final float MAX_VELOCITY_X = 3.6f;
    public static final float JUMP_VELOCITY_Y = 7f;
    public static final float JUMP_VELOCITY_X = 2.8f;
    public static final float FALL_THRESHOLD = -1f;
    public static final float MAX_FALL_VELOCITY = -JUMP_VELOCITY_Y;
    public static final float DAMPING = 0.5f;

    //variables
    public static Vector2 position;
    public static Vector2 velocity;
    private static Rectangle bounds;
    public static State state = State.Standing;
    public static float stateTime = 0;
    public static boolean facesRight = true;
    public static boolean grounded = true;
    public static boolean onLeftConveyer = false;
    public static boolean onRightConveyer = false;
    public static TextureRegion standingFrame;
    public static Sprite sprite;

    public static void init(Vector2 spawnPosition, boolean spawnfacingRight) {
        position = spawnPosition;
        velocity = new Vector2(0, 0);
        bounds = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
        facesRight = spawnfacingRight;
    }

    public static boolean goingLeft() {
        return velocity.x < 0;
    }

    public static Rectangle getBounds() {
        bounds.x = position.x;
        bounds.y = position.y;
        return bounds;
    }

    public static boolean goingRight() {
        return velocity.x > 0;
    }

    public static boolean goingUp() {
        return velocity.y > 0;
    }

    public static boolean goingDown() {
        return velocity.y < 0;
    }

    public static void stopMovingX() {
        velocity.x = 0;
    }

    public static void stopMovingY() {
        velocity.y = 0;
    }

    public static void jump() {
        if (grounded) {
            velocity.y += JUMP_VELOCITY_Y;
            if (goingRight()) {
                velocity.x = JUMP_VELOCITY_X;
            } else if (goingLeft()) {
                velocity.x = -JUMP_VELOCITY_X;
            }
            state = State.Jumping;
            grounded = false;
        }
    }

    public static void walkLeft() {
        if (!grounded || onRightConveyer) {
            return;
        }
        velocity.x = -MAX_VELOCITY_X;
        state = State.Walking;
        facesRight = false;
    }

    public static void walkRight() {
        if (!grounded || onLeftConveyer) {
            return;
        }
        velocity.x = MAX_VELOCITY_X;
        state = State.Walking;
        facesRight = true;
    }

    public static void clampFallVelocity() {
        //stop horizontal movement if walking of platforms
        if (velocity.y < FALL_THRESHOLD && !state.equals(State.Jumping)) {
            velocity.x = 0;
        }
        //limit vertical speed if falling
        if (velocity.y < MAX_FALL_VELOCITY) {
            velocity.y = MAX_FALL_VELOCITY;
            velocity.x = 0;
        }
    }

    public static void dampHorizontalMovement() {
        if (grounded) {
            velocity.x *= DAMPING;
        }
    }

    public static void move(float deltaTime) {
        if (onLeftConveyer) {
            walkLeft();
        }

        if (onRightConveyer) {
            walkRight();
        }

        // clamp the velocity to 0 if it's < 1, and set the state to standing
        if (Math.abs(velocity.x) < 1) {
            stopMovingX();
            if (grounded && !state.equals(State.Standing)) {
                standingFrame = sprite.animation.getKeyFrame(stateTime);
                state = State.Standing;
            }
        }

        //apply gravity to y-axis
        velocity.add(0, Constants.GRAVITY);

        //clamp fall velocity
        clampFallVelocity();

        // multiply by delta time so we know how far we go in this frame
        velocity.scl(deltaTime);

        // unscale the velocity by the inverse delta time and set to the latest position
        position.add(velocity);
        velocity.scl(1 / deltaTime);

        // Apply damping to the velocity so we don't animation infinitely once a key was pressed
        dampHorizontalMovement();
    }
}
