package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.game.Level;
import com.konaboy.arcadebob.gameobjects.Guardian;

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

    //keys for mapping chars to regions
    private static final String[] MAPPING_KEYS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "F"};

    //properties file containing level config
    private static Properties levelProps;

    public static Level createLevel(int levelNumber) {
        String propetiesFilename = "level_" + levelNumber + ".properties";
        Gdx.app.log("Loading level", propetiesFilename);
        levelProps = loadPropertiesFile(propetiesFilename);
        return new Level();
    }

    public static String[] getLines() {
        String[] lines = new String[16];
        for (int i = 0; i < Level.TILES_Y; i++) {
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

    public static Collection<Guardian> createGuardians() {
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
        return new Guardian(name, trackStartPos, trackEndPos, spawnPos, velocity);
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
}
