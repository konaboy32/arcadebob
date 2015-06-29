package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.konaboy.arcadebob.game.Constants;
import com.konaboy.arcadebob.game.Level;
import com.konaboy.arcadebob.gameobjects.Guardian;
import com.konaboy.arcadebob.gameobjects.Sprite;

import java.util.*;

public class LevelCreator extends Creator {

    //level property key prefixes
    private static final String PREFIX_LINE = "line";
    private static final String PREFIX_MAPPING = "char";

    //player property keys
    private static final String KEY_PLAYER_START_POSITION = "player.spawn.position";
    private static final String KEY_PLAYER_FACING_RIGHT = "player.spawn.facingright";

    //guardian property key prefixes
    private static final String KEY_GUARDIAN_NAME = "guardian.name";
    private static final String KEY_GUARDIAN_TRACK_START_POSITION = "guardian.track.start.position";
    private static final String KEY_GUARDIAN_TRACK_END_POSITION = "guardian.track.end.position";
    private static final String KEY_GUARDIAN_SPAWN_POSITION = "guardian.spawn.position";
    private static final String KEY_GUARDIAN_VELOCITY = "guardian.velocity";

    private static final char EMPTY_TILE = '.';
    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_TOUCHED = "TOUCHED";
    public static final String KEY_RECTANGLE = "RECTANGLE";

    //keys for mapping chars to regions
    private static final String[] MAPPING_KEYS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "F"};

    //properties file containing level config
    private static Properties levelProps;

    public static Level createLevel(int levelNumber) {
        String propetiesFilename = "level_" + levelNumber + ".properties";
        Gdx.app.log("Loading level", propetiesFilename);
        levelProps = AssetManager.getProperties(propetiesFilename);
        TiledMap map = loadMap();
        Collection<Guardian> guardians = loadGuardians();
        Vector2 playerSpawnPosition = getPlayerSpawnPosition();
        boolean playerSpawnsFacingRight = playerSpawnsFacingRight();
        return new Level(map, guardians, playerSpawnPosition, playerSpawnsFacingRight);
    }

    public static String[] getLines() {
        String[] lines = new String[16];
        for (int i = 0; i < Constants.TILES_Y; i++) {
            String key = PREFIX_LINE + DOT + String.format("%02d", i);
            lines[i] = levelProps.getProperty(key);
        }
        return lines;
    }

    public static Map<String, Integer> getRegionMappings() {
        Map<String, Integer> mappings = new HashMap<String, Integer>();
        for (String mappingKey : MAPPING_KEYS) {
            mappings.put(mappingKey, Integer.valueOf(levelProps.getProperty(PREFIX_MAPPING + DOT + mappingKey)));
        }
        return mappings;
    }

    public static boolean playerSpawnsFacingRight() {
        return Boolean.valueOf(levelProps.getProperty(KEY_PLAYER_FACING_RIGHT));
    }

    public static Collection<Guardian> loadGuardians() {
        Collection<Guardian> guardians = new ArrayList<Guardian>();
        int count = 0;
        while (true) {
            Guardian guardian = createGuardian(count++);
            if (guardian == null) {
                break;
            }
            guardians.add(guardian);
        }
        return guardians;
    }

    private static Guardian createGuardian(int index) {
        String name = levelProps.getProperty(createKey(KEY_GUARDIAN_NAME, index));
        if (name == null) {
            return null;
        }
        Gdx.app.log("Loading guardian", name);
        Vector2 trackStartPos = getVectorProperty(createKey(KEY_GUARDIAN_TRACK_START_POSITION, index));
        Vector2 trackEndPos = getVectorProperty(createKey(KEY_GUARDIAN_TRACK_END_POSITION, index));
        Vector2 spawnPos = getVectorProperty(createKey(KEY_GUARDIAN_SPAWN_POSITION, index));
        float velocity = Float.valueOf(levelProps.getProperty(createKey(KEY_GUARDIAN_VELOCITY, index)));
        Sprite sprite = SpriteCreator.createSprite(name);
        return new Guardian(name, trackStartPos, trackEndPos, spawnPos, velocity, sprite);
    }

    public static Vector2 getPlayerSpawnPosition() {
        return getVectorProperty(KEY_PLAYER_START_POSITION);
    }

    private static Vector2 getVectorProperty(String key) {
        String positionStr = levelProps.getProperty(key);
        if (positionStr == null) {
            return null;
        }
        return stringToVector(positionStr);
    }

    private static TiledMap loadMap() {
        Gdx.app.log("Generating level map", "");
        TiledMap map = new TiledMap();
        Texture texture = AssetManager.getTexture(AssetManager.MANIC_SPRITES);
        TextureRegion[] blocks = TextureRegionHelper.getRegions(texture, 660, 2, 288, 320, Constants.TILE_SIZE);
        String[] lines = LevelCreator.getLines();
        Map<String, Integer> regionMappings = LevelCreator.getRegionMappings();
        TiledMapTileLayer layer = new TiledMapTileLayer(Constants.TILES_X, Constants.TILES_Y, Constants.TILE_SIZE, Constants.TILE_SIZE);
        for (int y = Constants.TILES_Y - 1; y >= 0; y--) {
            Gdx.app.log("Processing line", lines[y]);
            for (int x = 0; x < Constants.TILES_X; x++) {
                char blockTypeChar = lines[y].charAt(x);
                if (blockTypeChar == EMPTY_TILE) {
                    continue;
                }
                int regionIndex = regionMappings.get("" + blockTypeChar);
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                Enum blockTypeEnum = mapCharToBlockType(blockTypeChar);
                TiledMapTile tile;
                if (Constants.BlockType.Collectable.equals(blockTypeEnum)) {
                    tile = createAnimatedTile(blocks, regionIndex, false);
                } else if (Constants.BlockType.ConveyorLeft.equals(blockTypeEnum)) {
                    tile = createAnimatedTile(blocks, regionIndex, false);
                } else if (Constants.BlockType.ConveyorRight.equals(blockTypeEnum)) {
                    tile = createAnimatedTile(blocks, regionIndex, true);
                } else {
                    tile = new StaticTiledMapTile(blocks[regionIndex]);
                    if (Constants.BlockType.Collapsible.equals(blockTypeEnum)) {
                        tile.getProperties().put(KEY_TOUCHED, 0);
                    }
                }
                tile.getProperties().put(KEY_TYPE, blockTypeEnum);
                tile.getProperties().put(KEY_RECTANGLE, new Rectangle(x, y, 1, 1));
                cell.setTile(tile);
                layer.setCell(x, y, cell);
            }
        }
        map.getLayers().add(layer);
        Gdx.app.log("Finiahed generating level map", "");
        return map;
    }

    private static TiledMapTile createAnimatedTile(TextureRegion[] blocks, int regionIndex, boolean reverse) {
        TiledMapTile tile;
        Array<StaticTiledMapTile> tiles = new Array<StaticTiledMapTile>();
        for (int i = 0; i < 4; i++) {
            if (reverse) {
                tiles.add(new StaticTiledMapTile(blocks[regionIndex + 3 - i]));
            } else {
                tiles.add(new StaticTiledMapTile(blocks[regionIndex + i]));
            }
        }
        tile = new AnimatedTiledMapTile(Constants.ANIMATION_FRAME_DURATION, tiles);
        return tile;
    }

    private static Constants.BlockType mapCharToBlockType(char s) {
        switch (s) {
            case '1':
            case '2':
                return Constants.BlockType.Solid;
            case '3':
                return Constants.BlockType.Impassable;
            case '4':
                return Constants.BlockType.Collapsible;
            case '5':
            case '6':
                return Constants.BlockType.Hazard;
            case '7':
                return Constants.BlockType.ConveyorLeft;
            case '8':
                return Constants.BlockType.ConveyorRight;
            case '9':
                return Constants.BlockType.Collectable;
            case 'A':
                return Constants.BlockType.Exit;
            case 'B':
                return Constants.BlockType.ExitControl;
            case 'F':
                return Constants.BlockType.Special;
            default:
                return null;
        }
    }
}
