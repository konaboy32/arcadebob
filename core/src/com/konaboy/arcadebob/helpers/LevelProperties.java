package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.gameobjects.Guardian;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class LevelProperties {

    //level property key prefixes
    private static final String PREFIX_LINE = "line.";
    private static final String PREFIX_MAPPING = "char.";

    //player property keys
    private static final String KEY_PLAYER_START_POSITION = "player.spawn.position";
    private static final String KEY_PLAYER_FACING_RIGHT = "player.spawn.facingright";

    //guardian property key prefixes
    private static final String PREFIX_GUARDIAN_TEXTURE = "guardian.texture.";
    private static final String PREFIX_GUARDIAN_TEXTUREREGION = "guardian.textureregion.";
    private static final String PREFIX_GUARDIAN_TRACK_START_POSITION = "guardian.track.start.position.";
    private static final String PREFIX_GUARDIAN_TRACK_END_POSITION = "guardian.track.end.position.";
    private static final String PREFIX_GUARDIAN_SPAWN_POSITION = "guardian.spawn.position.";
    private static final String PREFIX_GUARDIAN_VELOCITY = "guardian.velocity.";

    //common stuff
    private static final String VECTOR_DELIM = ",";
    private static final String[] MAPPING_KEYS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "F"};
    private Properties props;

    public LevelProperties(String filename) {
        load(filename);
    }

    public String[] getLines() {
        String[] lines = new String[16];
        for (int i = 0; i < MapLoader.TILES_Y; i++) {
            String key = PREFIX_LINE + String.format("%02d", i);
            lines[i] = props.getProperty(key);
        }
        return lines;
    }

    public Map<String, Integer> getRegionMappings() {
        Map<String, Integer> mappings = new HashMap<String, Integer>();
        for (String mappingKey : MAPPING_KEYS) {
            mappings.put(mappingKey, Integer.valueOf(props.getProperty(PREFIX_MAPPING + mappingKey)));
        }
        return mappings;
    }

    public boolean startFacingRight() {
        return Boolean.valueOf(props.getProperty(KEY_PLAYER_FACING_RIGHT));
    }

    public Collection<Guardian> getGuardians() {
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

    private Guardian createGuardian(int count) {
        String key = PREFIX_GUARDIAN_TRACK_START_POSITION + String.format("%02d", count);

        //track start position
        Vector2 trackStartPos = getVectorProperty(key);
        if (trackStartPos == null) {
            return null;
        }

        //track end position
        key = PREFIX_GUARDIAN_TRACK_END_POSITION + String.format("%02d", count);
        Vector2 trackEndPos = getVectorProperty(key);

        //spawn point
        key = PREFIX_GUARDIAN_SPAWN_POSITION + String.format("%02d", count);
        Vector2 spawnPos = getVectorProperty(key);

        //velocity
        key = PREFIX_GUARDIAN_VELOCITY + String.format("%02d", count);
        float velocity = Float.valueOf(props.getProperty(key));

        //create and return the guardian
        return new Guardian(trackStartPos, trackEndPos, spawnPos, velocity);
    }

    public Vector2 getPlayerSpawnPosition() {
        return getVectorProperty(KEY_PLAYER_START_POSITION);
    }

    private Vector2 getVectorProperty(String key) {
        String positionStr = props.getProperty(key);
        if (positionStr == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(positionStr, VECTOR_DELIM);
        float x = Float.valueOf(st.nextToken());
        float y = Float.valueOf(st.nextToken());
        return new Vector2(x, y);
    }

    private void load(String filename) {
        props = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(filename);
            props.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
