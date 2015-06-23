package com.konaboy.arcadebob.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.konaboy.arcadebob.helpers.AssetManager;

public class SplashScreen extends ScreenAdapter {

    private Texture splashTexture = AssetManager.getTexture("splash.png");
    private Image splashImage = new Image(splashTexture);
    private Stage stage = new Stage();

    @Override
    public void show() {
        stage.addActor(splashImage);
        splashImage.addAction(Actions.sequence(
                Actions.alpha(0),
                Actions.fadeIn(1.0f),
                Actions.delay(2),
                Actions.fadeOut(1.0f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        ((com.badlogic.gdx.Game) Gdx.app.getApplicationListener()).setScreen(new MenuScreen());
                    }
                })));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1); //sets clear color to black
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); //clear the batch
        stage.act(); //update all actors
        stage.draw(); //draw all actors on the Stage.getBatch()
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
