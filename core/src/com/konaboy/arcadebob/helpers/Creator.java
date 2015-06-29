package com.konaboy.arcadebob.helpers;


import com.badlogic.gdx.math.Vector2;

import java.util.StringTokenizer;

public class Creator {

    protected static final String POSITIION_DELIM = ":";
    protected static final String VECTOR_DELIM = ",";
    protected static final String DOT = ".";

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
