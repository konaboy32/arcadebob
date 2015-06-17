package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    public static final String MANIC_SPRITES = "manic_sprites.png";
    private static final Map<String, Texture> TEXTURE_CACHE = new HashMap<String, Texture>();

    public static Texture getTexture(String filename) {
        if (TEXTURE_CACHE.containsKey(filename)) {
            Gdx.app.log("Fetching texture from cache", filename);
            return TEXTURE_CACHE.get(filename);
        }
        Gdx.app.log("Loading texture", filename);
        Texture texture = new Texture(filename);
        TEXTURE_CACHE.put(filename, texture);
        return texture;
    }
}
