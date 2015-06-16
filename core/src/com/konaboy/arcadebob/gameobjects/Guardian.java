package com.konaboy.arcadebob.gameobjects;


import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Guardian {

    public final String name;
    public final float WIDTH = 1.5f;
    public final float HEIGHT = 2f;
    public final Vector2 trackStartPosition;
    public final Vector2 trackEndPosition;
    public final Rectangle bounds;
    public final Vector2 velocity;
    public final boolean isHorizontalTrack;

    public Guardian(String name,
                    Vector2 trackStartPosition,
                    Vector2 trackEndPosition,
                    Vector2 spawnPosition,
                    float velocity) {
        this.name = name;
        bounds = new Rectangle(spawnPosition.x, spawnPosition.y, WIDTH, HEIGHT);
        this.velocity = new Vector2();
        this.velocity.x = this.velocity.y = Player.MAX_VELOCITY_X * velocity;
        this.trackStartPosition = trackStartPosition;
        this.trackEndPosition = trackEndPosition;
        isHorizontalTrack = (int) trackStartPosition.y == (int) trackEndPosition.y;
    }

    public void move(float deltaTime) {
        if (isHorizontalTrack) {
            velocity.x *= deltaTime;
            bounds.x += velocity.x;
            velocity.x *= 1 / deltaTime;
            checkHorizontalLimits();
        } else {
            velocity.y *= deltaTime;
            bounds.y += velocity.y;
            velocity.y *= 1 / deltaTime;
            checkVerticalLimits();
        }
    }

    private void checkHorizontalLimits() {
        if ((bounds.x + WIDTH > trackEndPosition.x) || (bounds.x < trackStartPosition.x)) {
            velocity.x = -velocity.x;
        }
    }

    private void checkVerticalLimits() {
        if ((bounds.y + HEIGHT > trackEndPosition.y) || (bounds.y < trackStartPosition.y)) {
            velocity.y = -velocity.y;
        }
    }
}