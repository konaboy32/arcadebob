package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.game.Level;
import com.konaboy.arcadebob.gameobjects.Guardian;

import java.util.HashMap;
import java.util.Map;

public class LevelProperties extends PropertiesHelper {

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

    //keys for mapping chars to regions
    private static final String[] MAPPING_KEYS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "F"};

    public static void load(String filename) {
        props = loadPropertiesFile(filename);
    }

    public static String[] getLines() {
        String[] lines = new String[16];
        for (int i = 0; i < Level.TILES_Y; i++) {
            String key = PREFIX_LINE + DOT + String.format("%02d", i);
            lines[i] = props.getProperty(key);
        }
        return lines;
    }

    public static Map<String, Integer> getRegionMappings() {
        Map<String, Integer> mappings = new HashMap<String, Integer>();
        for (String mappingKey : MAPPING_KEYS) {
            mappings.put(mappingKey, Integer.valueOf(props.getProperty(PREFIX_MAPPING + DOT + mappingKey)));
        }
        return mappings;
    }

    public static boolean playerSpawnsFacingRight() {
        return Boolean.valueOf(props.getProperty(KEY_PLAYER_FACING_RIGHT));
    }

    public static Guardian getGuardian(int index) {
        String name = props.getProperty(createKey(KEY_GUARDIAN_NAME, index));
        if (name == null) {
            return null;
        }
        Vector2 trackStartPos = getVectorProperty(createKey(KEY_GUARDIAN_TRACK_START_POSITION, index));
        Vector2 trackEndPos = getVectorProperty(createKey(KEY_GUARDIAN_TRACK_END_POSITION, index));
        Vector2 spawnPos = getVectorProperty(createKey(KEY_GUARDIAN_SPAWN_POSITION, index));
        float velocity = Float.valueOf(props.getProperty(createKey(KEY_GUARDIAN_VELOCITY, index)));
        return new Guardian(name, trackStartPos, trackEndPos, spawnPos, velocity);
    }

    public static Vector2 getPlayerSpawnPosition() {
        return getVectorProperty(KEY_PLAYER_START_POSITION);
    }
}
