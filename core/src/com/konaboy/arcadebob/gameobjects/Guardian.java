package com.konaboy.arcadebob.gameobjects;


import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Guardian {

    private static final float MAX_VELOCITY = Player.MAX_VELOCITY_X;
    public final float WIDTH = 1.5f;
    public final float HEIGHT = 2f;
    public final Vector2 trackStartPosition;
    public final Vector2 trackEndPosition;
    public final Vector2 position;
    public final Rectangle bounds;
    public final Vector2 velocity;
    public final boolean isHorizontalTrack;

    public Guardian(Vector2 trackStartPosition,
                    Vector2 trackEndPosition,
                    Vector2 spawnPosition,
                    int spawnVelocity) {

        //put in spawn position
        position = spawnPosition;

        //bounds for collision detection
        bounds = new Rectangle(spawnPosition.x, spawnPosition.y, WIDTH, HEIGHT);

        //direction
        velocity = new Vector2();
        if (spawnVelocity < 0) {
            velocity.x = velocity.y = -MAX_VELOCITY;
        } else {
            velocity.x = velocity.y = MAX_VELOCITY;
        }

        //track that the guardian will travel along
        this.trackStartPosition = trackStartPosition;
        this.trackEndPosition = trackEndPosition;
        isHorizontalTrack = (int) trackStartPosition.y == (int) trackEndPosition.y;
    }

    public Rectangle getBounds() {
        bounds.x = position.x;
        bounds.y = position.y;
        return bounds;
    }

    public void move(float deltaTime) {
        if (isHorizontalTrack) {
            velocity.x *= deltaTime;
            position.x += velocity.x;
            velocity.x *= 1 / deltaTime;
            checkHorizontalLimits();
        } else {
            velocity.y *= deltaTime;
            position.y += velocity.y;
            velocity.y *= 1 / deltaTime;
            checkVerticalLimits();
        }
    }

    private void checkHorizontalLimits() {
        System.out.println(position.x + " " + trackEndPosition.x);
        if (position.x + WIDTH > trackEndPosition.x) {
            velocity.x = -MAX_VELOCITY;
        } else if (position.x < trackStartPosition.x) {
            velocity.x = MAX_VELOCITY;
        }
    }

    private void checkVerticalLimits() {
        if (position.y + HEIGHT > trackEndPosition.y) {
            velocity.y = -MAX_VELOCITY;
        } else if (position.y < trackStartPosition.y) {
            velocity.y = MAX_VELOCITY;
        }
    }
}