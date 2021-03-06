package com.konaboy.arcadebob.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.konaboy.arcadebob.game.Constants;
import com.konaboy.arcadebob.game.Runner;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Tile Demo";
        config.width = Constants.WIDTH_PX;
        config.height = Constants.HEIGHT_PX;
        System.out.println("width: " + config.width);
        System.out.println("height: " + config.height);
        new LwjglApplication(new Runner(), config);
    }
}
