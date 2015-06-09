package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public abstract class GameObject {
    public Rectangle bounds;
    public Texture texture;

    protected GameObject(int x, int y, int width, int height, Texture texture) {
        this.bounds = new Rectangle(x, y, width, height);
        this.texture = texture;
    }
}
