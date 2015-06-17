package com.konaboy.arcadebob.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.konaboy.arcadebob.gameobjects.Guardian;
import com.konaboy.arcadebob.helpers.AssetManager;
import com.konaboy.arcadebob.helpers.Constants;
import com.konaboy.arcadebob.helpers.LevelCreator;
import com.konaboy.arcadebob.helpers.TextureRegionHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class Level {

    private enum BlockType {
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
    private TiledMapTileLayer layer;

    public Level() {
        rectangles = new ArrayList<Rectangle>();
        map = new TiledMap();
        loadMap();
    }

    public boolean isConveyerLeft(Rectangle rect) {
        return getBlockType(rect).equals(BlockType.ConveyorLeft);
    }

    public boolean isConveyerRight(Rectangle rect) {
        return getBlockType(rect).equals(BlockType.ConveyorRight);
    }

    public boolean isImpassable(Rectangle rect) {
        return getBlockType(rect).equals(BlockType.Impassable);
    }

    public boolean isCollapsible(Rectangle rect) {
        return getBlockType(rect).equals(BlockType.Collapsible);
    }

    public boolean isCollectable(Rectangle rect) {
        return getBlockType(rect).equals(BlockType.Collectable);
    }

    public boolean isHazard(Rectangle rect) {
        return getBlockType(rect).equals(BlockType.Hazard);
    }

    public boolean updateCollapsible(Rectangle rect) {
        Integer touched = (Integer) layer.getCell((int) rect.x, (int) rect.y).getTile().getProperties().get(KEY_TOUCHED);
        if (touched > COLLAPSE_LIMIT) {
            removeTile(rect);
            return true;
        }
        TiledMapTile tile = layer.getCell((int) rect.x, (int) rect.y).getTile();
        tile.setOffsetY(-touched);
        tile.getProperties().put(KEY_TOUCHED, ++touched);
        return false;
    }

    public void removeTile(Rectangle rect) {
        layer.setCell((int) rect.x, (int) rect.y, null);
        rectangles.remove(rect);
    }

    private BlockType getBlockType(Rectangle rect) {
        return (BlockType) layer.getCell((int) rect.x, (int) rect.y).getTile().getProperties().get(KEY_TYPE);
    }

    public TiledMap getMap() {
        return map;
    }

    public Collection<Rectangle> getRectangles() {
        return rectangles;
    }

    public Vector2 getPlayerSpawnPosition() {
        return LevelCreator.getPlayerSpawnPosition();
    }

    public boolean playerSpawnsFacingRight() {
        return LevelCreator.playerSpawnsFacingRight();
    }

    public Collection<Guardian> getGuardians() {
        Collection<Guardian> guardians = new ArrayList<Guardian>();
        int count = 0;
        while (true) {
            Guardian guardian = LevelCreator.createGuardian(count++);
            if (guardian == null) {
                break;
            }
            guardians.add(guardian);
        }
        return guardians;
    }

    private void loadMap() {
        Gdx.app.log("Loading map", "");
        Texture texture = AssetManager.getTexture(AssetManager.MANIC_SPRITES);
        TextureRegion[] blocks = TextureRegionHelper.getRegions(texture, 660, 2, 288, 320, TILE_SIZE);
        String[] lines = LevelCreator.getLines();
        Map<String, Integer> regionMappings = LevelCreator.getRegionMappings();
        layer = new TiledMapTileLayer(TILES_X, TILES_Y, TILE_SIZE, TILE_SIZE);
        for (int y = 0; y < TILES_Y; y++) {
            for (int x = 0; x < TILES_X; x++) {
                char blockTypeChar = lines[y].charAt(x);
                if (blockTypeChar == EMPTY_TILE) {
                    continue;
                }
                int regionIndex = regionMappings.get("" + blockTypeChar);
                Cell cell = new Cell();
                Enum blockTypeEnum = mapCharToBlockType(blockTypeChar);
                TiledMapTile tile;
                if (BlockType.Collectable.equals(blockTypeEnum)) {
                    tile = createAnimatedTile(blocks, regionIndex);
                } else if (BlockType.ConveyorLeft.equals(blockTypeEnum)) {
                    tile = createAnimatedTile(blocks, regionIndex);
                } else {
                    tile = new StaticTiledMapTile(blocks[regionIndex]);
                    if (BlockType.Collapsible.equals(blockTypeEnum)) {
                        tile.getProperties().put(KEY_TOUCHED, 0);
                    }
                }
                tile.getProperties().put(KEY_TYPE, blockTypeEnum);
                cell.setTile(tile);
                layer.setCell(x, y, cell);
                rectangles.add(new Rectangle(x, y, 1, 1));
            }
        }
        map.getLayers().add(layer);
        Gdx.app.log("Finiahed loading map, total tiles", "" + rectangles.size());
    }

    private TiledMapTile createAnimatedTile(TextureRegion[] blocks, int regionIndex) {
        TiledMapTile tile;
        Array<StaticTiledMapTile> tiles = new Array<StaticTiledMapTile>();
        for (int i = 0; i < 4; i++) {
            tiles.add(new StaticTiledMapTile(blocks[regionIndex + i]));
        }
        tile = new AnimatedTiledMapTile(Constants.ANIMATION_FRAME_DURATION, tiles);
        return tile;
    }

    private BlockType mapCharToBlockType(char s) {
        switch (s) {
            case '1':
            case '2':
                return BlockType.Solid;
            case '3':
                return BlockType.Impassable;
            case '4':
                return BlockType.Collapsible;
            case '5':
            case '6':
                return BlockType.Hazard;
            case '7':
                return BlockType.ConveyorLeft;
            case '8':
                return BlockType.ConveyorRight;
            case '9':
                return BlockType.Collectable;
            case 'A':
                return BlockType.Exit;
            case 'B':
                return BlockType.ExitControl;
            case 'F':
                return BlockType.Special;
            default:
                return null;
        }
    }


}
