package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.game.Constants;
import com.konaboy.arcadebob.helpers.AssetManager;
import com.konaboy.arcadebob.helpers.SpriteCreator;

public class Player {

    public enum State {
        Standing, Walking, Jumping
    }

    //constants
    public static final float WIDTH = 1f / Constants.TILE_SIZE * 20;
    public static final float HEIGHT = 1f / Constants.TILE_SIZE * 32;
    public static final float MAX_VELOCITY_X = 3.6f;
    public static final float JUMP_VELOCITY_Y = 7.0f;
    public static final float JUMP_VELOCITY_X = 3.0f;
    public static final float FALL_THRESHOLD = -1f;
    public static final float MAX_FALL_VELOCITY = -JUMP_VELOCITY_Y;
    public static final float DAMPING = 0.5f;
    public static final float SENSOR_THICKNESS = 0.1f;
    public static final float BOUNDS_SHRINK_X = 0.4f;
    public static final float BOUNDS_SHRINK_TOP = 0.2f;

    //public properties
    public static Vector2 position;
    public static Vector2 velocity;
    public static State state = State.Standing;
    public static float stateTime = 0;
    public static boolean facesRight = true;
    public static boolean grounded = true;
    public static boolean onLeftConveyer = false;
    public static boolean onRightConveyer = false;
    public static TextureRegion standingFrame;
    public static Sprite sprite;
    public static boolean obstacleOnLeft;
    public static boolean obstacleOnRight;

    //private properties
    private static Rectangle bounds;
    private static Rectangle leftSensor;
    private static Rectangle rightSensor;
    private static Rectangle topSensor;
    private static Rectangle bottomSensor;

    public static void init(Vector2 spawnPosition, boolean spawnfacingRight) {
        position = spawnPosition;
        velocity = new Vector2(0, 0);
        bounds = new Rectangle(0, 0, WIDTH - 2 * BOUNDS_SHRINK_X, HEIGHT - BOUNDS_SHRINK_TOP);
        leftSensor = new Rectangle(0, 0, SENSOR_THICKNESS, HEIGHT - 2 * SENSOR_THICKNESS);
        rightSensor = new Rectangle(0, 0, SENSOR_THICKNESS, HEIGHT - 2 * SENSOR_THICKNESS);
        topSensor = new Rectangle(0, 0, WIDTH - 2 * SENSOR_THICKNESS, SENSOR_THICKNESS);
        bottomSensor = new Rectangle(0, 0, WIDTH - 2 * SENSOR_THICKNESS, SENSOR_THICKNESS);
        facesRight = spawnfacingRight;
        sprite = SpriteCreator.createSprite(SpriteCreator.PLAYER_SPRITE_NAME);
        standingFrame = Player.sprite.regions[1];
    }

    public static boolean goingLeft() {
        return velocity.x < 0;
    }

    public static Rectangle getBounds() {
        bounds.x = position.x + BOUNDS_SHRINK_X;
        bounds.y = position.y;
        return bounds;
    }

    public static Rectangle getLeftSensor() {
        leftSensor.x = position.x;
        leftSensor.y = position.y + SENSOR_THICKNESS;
        return leftSensor;
    }

    public static Rectangle getRightSensor() {
        rightSensor.x = position.x + WIDTH - SENSOR_THICKNESS;
        rightSensor.y = position.y + SENSOR_THICKNESS;
        return rightSensor;
    }

    public static Rectangle getTopSensor() {
        topSensor.x = position.x + SENSOR_THICKNESS;
        topSensor.y = position.y + HEIGHT - SENSOR_THICKNESS;
        return topSensor;
    }

    public static Rectangle getBottomSensor() {
        bottomSensor.x = position.x + SENSOR_THICKNESS;
        bottomSensor.y = position.y;
        return bottomSensor;
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
            AssetManager.getSound(AssetManager.SOUND_JUMP).play();
        }
    }

    public static void walkLeft() {
        if (onRightConveyer) {
            return;
        }
        if (velocity.x <= 0) {
            if (grounded) {
                velocity.x = -MAX_VELOCITY_X;
            } else {
                velocity.x = -JUMP_VELOCITY_X;
            }
            facesRight = false;
        }
        if (grounded) {
            state = State.Walking;
        }
    }

    public static void walkRight() {
        if (onLeftConveyer) {
            return;
        }
        if (velocity.x >= 0) {
            if (grounded) {
                velocity.x = MAX_VELOCITY_X;
            } else {
                velocity.x = JUMP_VELOCITY_X;
            }
            facesRight = true;
        }
        if (grounded) {
            state = State.Walking;
        }
    }

    public static void clampFallVelocity() {
        //stop horizontal movement if walking off platforms
        if (velocity.y < FALL_THRESHOLD && !state.equals(State.Jumping)) {
            velocity.x = 0;
            grounded = false;
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
        stateTime += deltaTime;

        if (onLeftConveyer && !obstacleOnLeft) {
            walkLeft();
        }

        if (onRightConveyer && !obstacleOnRight) {
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
