package com.konaboy.arcadebob.helpers;


import com.badlogic.gdx.math.Vector2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

public class Creator {

    protected static final String POSITIION_DELIM = ":";
    protected static final String VECTOR_DELIM = ",";
    protected static final String DOT = ".";

    protected static Properties loadPropertiesFile(String filename) {
        Properties props = new Properties();
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
        return props;
    }

    protected static Vector2 stringToVector(String positionStr) {
        StringTokenizer st = new StringTokenizer(positionStr, VECTOR_DELIM);
        float x = Float.valueOf(st.nextToken());
        float y = Float.valueOf(st.nextToken());
        return new Vector2(x, y);
    }

    protected static String createKey(String key, int index) {
        return String.format("%02d", index) + DOT + key;
    }

    protected static String createKey(String key, String name) {
        return name + DOT + key;
    }
}
