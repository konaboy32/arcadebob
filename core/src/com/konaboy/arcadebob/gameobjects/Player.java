package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.helpers.MapLoader;

public class Player {

    public static void init(Vector2 startPosition, boolean b) {
        position = startPosition;
        velocity = new Vector2(0, 0);
        bounds = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }

    public enum State {
        Standing, Walking, Jumping
    }

    //constants
    public static final float WIDTH = 1f / MapLoader.TILE_SIZE * 20;
    public static final float HEIGHT = 1f / MapLoader.TILE_SIZE * 32;
    public static final float MAX_VELOCITY = 5f;
    public static final float JUMP_VELOCITY = 16f;
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

    public static void stopX() {
        velocity.x = 0;
    }

    public static void stopY() {
        velocity.y = 0;
    }

    public static void jump() {
        if (grounded) {
            velocity.y += JUMP_VELOCITY;
            state = State.Jumping;
            grounded = false;
        }
    }

    public static void walkLeft() {
        if (onRightConveyer) {
            return;
        }
        velocity.x = -MAX_VELOCITY;
        if (grounded) {
            state = State.Walking;
        }
        facesRight = false;
    }

    public static void walkRight() {
        if (onLeftConveyer) {
            return;
        }
        velocity.x = MAX_VELOCITY;
        if (grounded) {
            state = State.Walking;
        }
        facesRight = true;
    }
}
