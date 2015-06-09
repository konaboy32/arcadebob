package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

public class Raindrop extends GameObject {
    public Raindrop(int x, int y, int width, int height, Texture texture) {
        super(x, y, width, height, texture);
    }

    public void update() {
        bounds.y -= 200 * Gdx.graphics.getDeltaTime();
    }

}
