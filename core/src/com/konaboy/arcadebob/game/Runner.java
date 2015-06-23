package com.konaboy.arcadebob.game;

import com.badlogic.gdx.Game;
import com.konaboy.arcadebob.screens.SplashScreen;

public class Runner extends Game {

    @Override
    public void create() {
        setScreen(new SplashScreen());
    }
}
