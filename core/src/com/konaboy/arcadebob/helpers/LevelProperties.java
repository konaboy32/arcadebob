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
    private static final String KEY_PLAYER_START_POSITION = "player.start.position";
    private static final String KEY_PLAYER_FACING_RIGHT = "player.facingright";

    //guardian property key  prefixes
    private static final String PREFIX_GUARDIAN_TEXTURE = "guardian.texture.";
    private static final String PREFIX_GUARDIAN_TEXTUREREGION = "guardian.textureregion.";
    private static final String PREFIX_GUARDIAN_START_POSITION = "guardian.start.position.";
    private static final String PREFIX_GUARDIAN_END_POSITION = "guardian.end.position.";

    //common stuff
    private static final String VECTOR_DELIM = ",";
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
        mappings.put("1", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "1")));
        mappings.put("2", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "2")));
        mappings.put("3", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "3")));
        mappings.put("4", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "4")));
        mappings.put("5", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "5")));
        mappings.put("6", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "6")));
        mappings.put("7", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "7")));
        mappings.put("8", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "8")));
        mappings.put("9", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "9")));
        mappings.put("A", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "A")));
        mappings.put("B", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "B")));
        mappings.put("F", Integer.valueOf(props.getProperty(PREFIX_MAPPING + "F")));
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
        Guardian guardian = new Guardian();
        String startKey = PREFIX_GUARDIAN_START_POSITION + String.format("%02d", count);
        guardian.startPosition = getPositionProperty(startKey);
        if (guardian.startPosition == null) {
            return null;
        }
        String endKey = PREFIX_GUARDIAN_END_POSITION + String.format("%02d", count);
        guardian.endPosition = getPositionProperty(endKey);
        return guardian;
    }

    public Vector2 getPlayerStartPosition() {
        return getPositionProperty(KEY_PLAYER_START_POSITION);
    }

    private Vector2 getPositionProperty(String key) {
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
