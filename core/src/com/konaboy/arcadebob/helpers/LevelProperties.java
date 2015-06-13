package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.math.Vector2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class LevelProperties {
    ;
    private static final String PREFIX_LINE = "line.";
    private static final String PREFIX_MAPPING = "char.";
    private Properties prop;

    public LevelProperties(String filename) {
        load(filename);
    }

    public String[] getLines() {
        String[] lines = new String[16];
        for (int i = 0; i < MapLoader.TILES_Y; i++) {
            String key = PREFIX_LINE + String.format("%02d", i);
            lines[i] = prop.getProperty(key);
        }
        return lines;
    }

    public Map<String, Integer> getRegionMappings() {
        Map<String, Integer> mappings = new HashMap<String, Integer>();
        mappings.put("1", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "1")));
        mappings.put("2", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "2")));
        mappings.put("3", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "3")));
        mappings.put("4", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "4")));
        mappings.put("5", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "5")));
        mappings.put("6", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "6")));
        mappings.put("7", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "7")));
        mappings.put("8", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "8")));
        mappings.put("9", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "9")));
        mappings.put("A", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "A")));
        mappings.put("B", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "B")));
        mappings.put("F", Integer.valueOf(prop.getProperty(PREFIX_MAPPING + "F")));
        return mappings;
    }

    public boolean startFacingRight() {
        return Boolean.valueOf(prop.getProperty("start.facingright"));
    }

    public Vector2 getStartPosition() {
        float x = Float.valueOf(prop.getProperty("start.position.x"));
        float y = Float.valueOf(prop.getProperty("start.position.y"));
        return new Vector2(x, y);
    }

    private void load(String filename) {
        prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(filename);
            prop.load(input);
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
