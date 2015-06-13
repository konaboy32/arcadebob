package com.konaboy.arcadebob.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.konaboy.arcadebob.game.Manic;
import com.konaboy.arcadebob.helpers.MapLoader;

public class TiledLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Tile Demo";
        config.width = 1024;
        config.height = 512 + (512 / MapLoader.TILES_Y) * Manic.DEBUG_LINES; //4 lines of debug
        new LwjglApplication(new Manic(), config);
    }
}
