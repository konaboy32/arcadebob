package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.helpers.MapLoader;

public class Player {

    public enum State {
        Standing, Walking, Jumping
    }

    //constants
    public static final float WIDTH = 1f / MapLoader.TILE_SIZE * 20;
    public static final float HEIGHT = 1f / MapLoader.TILE_SIZE * 32;
    public static final float MAX_VELOCITY = 5f;
    public static final float JUMP_VELOCITY = 16f;
    public static final float DAMPING = 0.8f;
    public static final Rectangle rectangle = new Rectangle();
    public static final Vector2 position = new Vector2();
    public static final Vector2 velocity = new Vector2();

    //variables
    public static State state = State.Standing;
    public static float stateTime = 0;
    public static boolean facesRight = true;
    public static boolean grounded = true;
    public static boolean canWalkLeft = true;
    public static boolean canWalkRight = true;

    public static boolean goingLeft() {
        return velocity.x < 0;
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

    public static void stopVelocityX() {
        velocity.x = 0;
    }

    public static void stopVelocityY() {
        velocity.y = 0;
    }
}