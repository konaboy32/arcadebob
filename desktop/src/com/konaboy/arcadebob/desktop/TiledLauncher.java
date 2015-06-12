package com.konaboy.arcadebob.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.konaboy.arcadebob.game.Manic;
import com.konaboy.arcadebob.game.Physics;

public class TiledLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Tile Demo";
        config.width = 1024;
        config.height = 512;
        new LwjglApplication(new Manic(), config);
    }
}
