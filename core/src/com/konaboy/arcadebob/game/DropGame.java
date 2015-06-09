package com.konaboy.arcadebob.game;

import com.badlogic.gdx.Game;
import com.konaboy.arcadebob.helpers.AssetLoader;
import com.konaboy.arcadebob.screens.GameScreen;

public class DropGame extends Game {

    public void create() {
        AssetLoader.load();
        setScreen(new GameScreen());
    }

    public void dispose() {
        super.dispose();
        AssetLoader.dispose();
    }

}
