package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public class AssetManager {
    public static final String MANIC_SPRITES = "manic_sprites.png";
    private static final Map<String, Texture> textures = new HashMap<String, Texture>();

    public static Texture getTexture(String filename) {
        if (textures.containsKey(filename)) {
            return textures.get(filename);
        }
        Texture texture = new Texture(filename);
        textures.put(filename, texture);
        return texture;
    }
}
