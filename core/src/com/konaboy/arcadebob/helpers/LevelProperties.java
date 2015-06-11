package com.konaboy.arcadebob.helpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class LevelProperties {
    private static final int TOTAL_LINES = 16;
    private static final String PREFIX_LINE = "line";
    private static final String PREFIX_MAPPING = "char";
    private Properties prop;

    public LevelProperties(String filename) {
        load(filename);
    }

    public String[] getLines() {
        String[] lines = new String[16];
        for (int i = 1; i <= TOTAL_LINES; i++) {
            String key = PREFIX_LINE + String.format("%02d", i);
            lines[TOTAL_LINES - i] = prop.getProperty(key);
        }
        return lines;
    }

    public Map<String, Integer> getRegionMappings() {
        Map<String, Integer> mappings = new HashMap<String, Integer>();
        int count = 1;
        String value = "";
        while (value != null) {
            String key = PREFIX_MAPPING + String.format("%02d", count++);
            value = prop.getProperty(key);
            if (value != null) {
                StringTokenizer tokenizer = new StringTokenizer(value, ",");
                mappings.put(tokenizer.nextToken(), new Integer(tokenizer.nextToken()));
            }
        }
        return mappings;
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
