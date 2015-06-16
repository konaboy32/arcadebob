package com.konaboy.arcadebob.gameobjects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Collection;

public class Sprite {
    public final float width;
    public final float height;
    public final TextureRegion[] regions;
    public final float frameDuration;
//    public final Animation animation;

    public Sprite(float width, float height, Collection<TextureRegion> regions, float frameDuration) {
        this.width = width;
        this.height = height;
        this.regions = regions.toArray(new TextureRegion[regions.size()]);
        this.frameDuration = frameDuration;
    }
}
