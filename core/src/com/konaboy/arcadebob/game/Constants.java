package com.konaboy.arcadebob.game;


public class Constants {

    public enum BlockType {
        Solid, Impassable, Collapsible, Hazard, ConveyorLeft, ConveyorRight, Collectable, Exit, ExitControl, Special
    }

    public static final int TILE_SIZE = 16;
    public static final int TILES_X = 32;
    public static final int TILES_Y = 16;
    public static final int DEBUG_LINES = 4;
    public static final int WIDTH_PX = 1024;
    public static final int HEIGHT_PX = 512 + (512 / TILES_Y * DEBUG_LINES);
    public static final float GRAVITY = -0.15f;
    public static final float ANIMATION_FRAME_DURATION = 0.1f;

}
