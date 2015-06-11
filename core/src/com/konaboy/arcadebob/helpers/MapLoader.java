package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;

import java.util.Map;

public class MapLoader {

    private static final int TILE_SIZE = 16;
    private static final int TILES_X = 32;
    private static final int TILES_Y = 16;
    private static final char EMPTY_TILE = '.';
    private TiledMap map;
    private Rectangle[] rectangles;
    LevelProperties properties;

    public MapLoader(int level) {
        this.properties = new LevelProperties("level" + level + ".properties");
    }

    public void load(Texture texture) {
        TextureRegion[] blocks = TextureRegionHelper.getRegions(texture, 644, 2, 304, 320, TILE_SIZE);
        String[] lines = properties.getLines();
        Map<String, Integer> regionMappings = properties.getRegionMappings();
        map = new TiledMap();
        TiledMapTileLayer layer = new TiledMapTileLayer(TILES_X, TILES_Y, TILE_SIZE, TILE_SIZE);
        for (int y = 0; y < TILES_Y; y++) {
            for (int x = 0; x < TILES_X; x++) {
                char tileType = lines[y].charAt(x);
                if (!(tileType == EMPTY_TILE)) {
                    int regionIndex = regionMappings.get("" + tileType);
                    Cell cell = new Cell();
                    cell.setTile(new StaticTiledMapTile(blocks[regionIndex]));
                    layer.setCell(x, y, cell);
                }
            }
        }
        map.getLayers().add(layer);
    }

    public TiledMap getMap() {
        return map;
    }

    public Rectangle[] getRectangles() {
        return rectangles;
    }


}
