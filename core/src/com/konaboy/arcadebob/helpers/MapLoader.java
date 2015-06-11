package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

import java.util.Map;

public class MapLoader {

    private static final int TILE_SIZE = 16;
    private TiledMap tiledMap;
    private Rectangle[] rectangles;
    LevelProperties properties;

    public MapLoader(int level) {
        this.properties = new LevelProperties("level" + level + ".properties");
    }

    public void load(Texture texture) {
        TextureRegion[] blocks = TextureRegionHelper.getRegions(texture, 644, 2, 304, 320, TILE_SIZE);
        String[] lines = properties.getLines();
        Map regionMappings = properties.getRegionMappings();
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public Rectangle[] getRectangles() {
        return rectangles;
    }


}
