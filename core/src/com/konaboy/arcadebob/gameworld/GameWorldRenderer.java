package com.konaboy.arcadebob.gameworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.konaboy.arcadebob.gameobjects.Block;
import com.konaboy.arcadebob.helpers.AssetLoader;

public class GameWorldRenderer {
    private GameWorldController world;
    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;

    public GameWorldRenderer(GameWorldController world) {
        this.world = world;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(camera.combined);
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    public void render() {
        refresh();
        spriteBatch.begin();
        drawBucket();
        drawBlocks();
        drawScore();
        spriteBatch.end();
    }

    private void refresh() {
        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
    }

    private void drawBucket() {
        spriteBatch.draw(world.bob.texture, world.bob.bounds.x, world.bob.bounds.y);
    }

    private void drawScore() {
        AssetLoader.font.draw(spriteBatch, "Drops Collected: " + world.score, 0, 480);
    }

    private void drawBlocks() {
        for (Block block : world.blocks) {
            spriteBatch.draw(block.texture, block.bounds.x, block.bounds.y);
        }
    }
}
