package com.konaboy.arcadebob.helpers;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class AssetLoader {
    public static Texture bucketTexture;
    public static Texture raindropTexture;
    public static Texture bobTexture;
    public static Texture grassTexture;
    public static Sound dropSound;
    public static Music music;
    public static BitmapFont font;

    public static void load() {
        loadGraphics();
        loadAudio();
        loadFonts();
    }

    private static void loadFonts() {
        font = new BitmapFont();
    }

    private static void loadAudio() {
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
        music = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
        music.setLooping(true);
    }

    private static void loadGraphics() {
        bucketTexture = new Texture(Gdx.files.internal("bob.png"));
        raindropTexture = new Texture(Gdx.files.internal("droplet.png"));
        bobTexture = new Texture(Gdx.files.internal("bob.png"));
        grassTexture = new Texture(Gdx.files.internal("grass.png"));
    }

    public static void dispose() {
        bucketTexture.dispose();
        raindropTexture.dispose();
        bobTexture.dispose();
        grassTexture.dispose();
        dropSound.dispose();
        music.dispose();
        font.dispose();
    }
}
