package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class AssetManager {
    //sprites
    public static final String MANIC_SPRITES = "manic_sprites.png";

    //properties
    public static final String SPRITES_PROPERTIES_FILENAME = "sprites.properties";

    //sounds
    public static final String SOUND_COLLECT = "collect.wav";
    public static final String SOUND_DIE = "die.wav";
    public static final String SOUND_COLLAPSE = "collapse.wav";
    public static final String SOUND_JUMP = "jump.wav";

    //privates
    private static final String SOUND_FOLDER = "sounds/";
    private static final String TEXTURE_FOLDER = "textures/";
    private static final Map<String, Texture> TEXTURE_CACHE = new HashMap<String, Texture>();
    private static final Map<String, Sound> SOUND_CACHE = new HashMap<String, Sound>();
    private static final Map<String, Properties> PROPERTIES_CACHE = new HashMap<String, Properties>();

    public static Texture getTexture(String filename) {
        if (TEXTURE_CACHE.containsKey(filename)) {
            return TEXTURE_CACHE.get(filename);
        }
        Gdx.app.log("Loading texture", filename);
        Texture texture = new Texture(TEXTURE_FOLDER + filename);
        TEXTURE_CACHE.put(filename, texture);
        return texture;
    }

    public static Sound getSound(String filename) {
        if (SOUND_CACHE.containsKey(filename)) {
            return SOUND_CACHE.get(filename);
        }
        Gdx.app.log("Loading sound", filename);
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(SOUND_FOLDER + filename));
        SOUND_CACHE.put(filename, sound);
        return sound;
    }

    public static Properties getProperties(String filename) {
        if (PROPERTIES_CACHE.containsKey(filename)) {
            return PROPERTIES_CACHE.get(filename);
        }
        Gdx.app.log("Loading properties", filename);
        FileHandle handle = Gdx.files.internal(filename);
        Properties props = new Properties();
        try {
            props.load(handle.read());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        PROPERTIES_CACHE.put(filename, props);
        return props;
    }
}
