package com.konaboy.arcadebob.gameobjects;


import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Guardian {

    public final int WIDTH = 2;
    public final int HEIGHT = 2;
    public final Vector2 startPosition;
    public final Vector2 endPosition;
    public final Vector2 position;
    public final Rectangle bounds;

    public Guardian(Vector2 startPosition, Vector2 endPosition) {
        this.startPosition = this.position = startPosition;
        this.endPosition = endPosition;
        bounds = new Rectangle(position.x, position.y, WIDTH, HEIGHT);
    }

    public Rectangle getBounds() {
        bounds.x = position.x;
        bounds.y = position.y;
        return bounds;
    }
}
