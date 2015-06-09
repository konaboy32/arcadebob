package com.konaboy.arcadebob.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.konaboy.arcadebob.gameworld.GameWorldRenderer;
import com.konaboy.arcadebob.gameworld.GameWorldController;
import com.konaboy.arcadebob.helpers.InputHandler;

public class GameScreen extends ScreenAdapter {

    private GameWorldController world;
    private GameWorldRenderer renderer;

    public GameScreen() {
        world = new GameWorldController();
        renderer = new GameWorldRenderer(world);
        Gdx.input.setInputProcessor(new InputHandler(world.bob));
    }

    @Override
    public void render(float delta) {
        world.update();
        renderer.render();
    }

    @Override
    public void show() {
//        AssetLoader.music.play();
    }
}
