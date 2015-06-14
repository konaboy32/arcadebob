package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class MapLoader {

    private enum TileType {
        Solid, Impassable, Collapsible, Hazard, ConveyorLeft, ConveyorRight, Collectable, Exit, ExitControl, Special
    }

    public static final int TILE_SIZE = 16;
    public static final int TILES_X = 32;
    public static final int TILES_Y = 16;
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_TOUCHED = "TOUCHED";
    private static final char EMPTY_TILE = '.';
    private static final Integer COLLAPSE_LIMIT = 14;

    private TiledMap map;
    private Collection<Rectangle> rectangles;
    private LevelProperties properties;
    private TiledMapTileLayer layer;

    public MapLoader(int level) {
        this.properties = new LevelProperties("level" + level + ".properties");
        rectangles = new ArrayList<Rectangle>();
        map = new TiledMap();
    }

    public void load(Texture texture) {
        TextureRegion[] blocks = TextureRegionHelper.getRegions(texture, 660, 2, 288, 320, TILE_SIZE);
        String[] lines = properties.getLines();
        Map<String, Integer> regionMappings = properties.getRegionMappings();
        layer = new TiledMapTileLayer(TILES_X, TILES_Y, TILE_SIZE, TILE_SIZE);
        for (int y = 0; y < TILES_Y; y++) {
            for (int x = 0; x < TILES_X; x++) {
                char tileType = lines[y].charAt(x);
                if (!(tileType == EMPTY_TILE)) {
                    int regionIndex = regionMappings.get("" + tileType);
                    Cell cell = new Cell();
                    StaticTiledMapTile tile = new StaticTiledMapTile(blocks[regionIndex]);
                    Enum tileTypeEnum = mapCharToTileTypeEnum(tileType);
                    tile.getProperties().put(KEY_TYPE, tileTypeEnum);
                    if (TileType.Collapsible.equals(tileTypeEnum)) {
                        tile.getProperties().put(KEY_TOUCHED, 0);
                    }
                    cell.setTile(tile);
                    layer.setCell(x, y, cell);
                    rectangles.add(new Rectangle(x, y, 1, 1));
                }
            }
        }
        map.getLayers().add(layer);
    }

    public boolean isConveyerLeft(Rectangle rect) {
        return getTileType(rect).equals(TileType.ConveyorLeft);
    }

    public boolean isConveyerRight(Rectangle rect) {
        return getTileType(rect).equals(TileType.ConveyorRight);
    }

    public boolean isImpassable(Rectangle rect) {
        return getTileType(rect).equals(TileType.Impassable);
    }

    public boolean isCollapsible(Rectangle rect) {
        return getTileType(rect).equals(TileType.Collapsible);
    }

    public boolean isCollectable(Rectangle rect) {
        return getTileType(rect).equals(TileType.Collectable);
    }

    public boolean isHazard(Rectangle rect) {
        return getTileType(rect).equals(TileType.Hazard);
    }

    public boolean updateCollapsible(Rectangle rect) {
        Integer touched = (Integer) layer.getCell((int) rect.x, (int) rect.y).getTile().getProperties().get(KEY_TOUCHED);
        if (touched > COLLAPSE_LIMIT) {
            removeTile(rect);
            return true;
        }
        layer.getCell((int) rect.x, (int) rect.y).getTile().getProperties().put(KEY_TOUCHED, ++touched);
        return false;
    }

    public void removeTile(Rectangle rect) {
        layer.setCell((int) rect.x, (int) rect.y, null);
        rectangles.remove(rect);
    }

    private TileType getTileType(Rectangle rect) {
        return (TileType) layer.getCell((int) rect.x, (int) rect.y).getTile().getProperties().get(KEY_TYPE);
    }

    public TiledMap getMap() {
        return map;
    }

    public Collection<Rectangle> getRectangles() {
        return rectangles;
    }

    public LevelProperties getLevelProperties() {
        return properties;
    }

    private TileType mapCharToTileTypeEnum(char s) {
        switch (s) {
            case '1':
            case '2':
                return TileType.Solid;
            case '3':
                return TileType.Impassable;
            case '4':
                return TileType.Collapsible;
            case '5':
            case '6':
                return TileType.Hazard;
            case '7':
                return TileType.ConveyorLeft;
            case '8':
                return TileType.ConveyorRight;
            case '9':
                return TileType.Collectable;
            case 'A':
                return TileType.Exit;
            case 'B':
                return TileType.ExitControl;
            case 'F':
                return TileType.Special;
            default:
                return null;
        }
    }


}
