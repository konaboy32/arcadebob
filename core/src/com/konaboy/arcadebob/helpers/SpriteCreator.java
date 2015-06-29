package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.konaboy.arcadebob.game.Constants;
import com.konaboy.arcadebob.gameobjects.Sprite;

import java.util.*;

public class SpriteCreator extends Creator {

    public static final String PLAYER_SPRITE_NAME = "Player";

    private static final String KEY_TEXTURE = "texture";
    private static final String KEY_REGION_WIDTH = "region.width";
    private static final String KEY_REGION_HEIGHT = "region.height";
    private static final String KEY_REGION_POSITIONS = "region.positions";
    private static final String KEY_ANIMATION_SPEED = "animation.speed";
    private static final Map<String, Sprite> SPRITE_CACHE = new HashMap<String, Sprite>();

    public static Sprite createSprite(String name) {
        if (SPRITE_CACHE.containsKey(name)) {
            return SPRITE_CACHE.get(name);
        }
        Properties spriteProps = AssetManager.getProperties(AssetManager.SPRITES_PROPERTIES_FILENAME);
        Gdx.app.log("Loading sprite", name);
        String textureFilename = spriteProps.getProperty(createKey(KEY_TEXTURE, name));
        int width = Integer.valueOf(spriteProps.getProperty(createKey(KEY_REGION_WIDTH, name)));
        int height = Integer.valueOf(spriteProps.getProperty(createKey(KEY_REGION_HEIGHT, name)));
        float frameDuration = Float.valueOf(spriteProps.getProperty(createKey(KEY_ANIMATION_SPEED, name))) * Constants.ANIMATION_FRAME_DURATION;
        String regionPositions = spriteProps.getProperty(createKey(KEY_REGION_POSITIONS, name));
        Collection<TextureRegion> regions = new ArrayList<TextureRegion>();
        StringTokenizer stPosition = new StringTokenizer(regionPositions, POSITIION_DELIM);
        Texture texture = AssetManager.getTexture(textureFilename);
        while (stPosition.hasMoreTokens()) {
            String position = stPosition.nextToken();
            StringTokenizer stXY = new StringTokenizer(position, VECTOR_DELIM);
            int x = Integer.valueOf(stXY.nextToken());
            int y = Integer.valueOf(stXY.nextToken());
            TextureRegion region = new TextureRegion(texture, x, y, width, height);
            regions.add(region);
        }
        Sprite sprite = new Sprite(width, height, regions, frameDuration);
        SPRITE_CACHE.put(name, sprite);
        return sprite;
    }
}
