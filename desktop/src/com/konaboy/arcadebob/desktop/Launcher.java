package com.konaboy.arcadebob.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.konaboy.arcadebob.game.Constants;
import com.konaboy.arcadebob.game.Game;

public class Launcher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Tile Demo";
        config.width = Constants.WIDTH_PX;
        config.height = Constants.HEIGHT_PX;
        new LwjglApplication(new Game(), config);
    }
}
