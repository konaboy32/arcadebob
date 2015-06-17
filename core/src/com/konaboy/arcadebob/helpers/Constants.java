package com.konaboy.arcadebob.helpers;


import com.konaboy.arcadebob.game.Level;

public class Constants {

    public static final int DEBUG_LINES = 4;
    public static final int WIDTH_PX = 1024;
    public static final int HEIGHT_PX = 512 + (512 / Level.TILES_Y * DEBUG_LINES);
    public static final float GRAVITY = -0.15f;
    public static final float ANIMATION_FRAME_DURATION = 0.1f;
}
