package com.konaboy.arcadebob.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.game.Constants;
import com.konaboy.arcadebob.game.Level;
import com.konaboy.arcadebob.gameobjects.Guardian;
import com.konaboy.arcadebob.gameobjects.Player;
import com.konaboy.arcadebob.helpers.AssetManager;
import com.konaboy.arcadebob.helpers.LevelCreator;
import com.konaboy.arcadebob.helpers.OverlapHelper;

import java.util.ArrayList;
import java.util.Collection;

public class GameScreen extends ScreenAdapter {

    private static final String SOUND_LAND = "land.wav";
    private static final int LEVEL = 2;

    private BitmapFont font;
    private OrthogonalTiledMapRenderer tileRenderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private Batch tileBatch;
    private OrthographicCamera gameCamera;
    private Level level;
    private Rectangle debugRect;

    @Override
    public void show() {
        Gdx.app.log("Creating game", "");

        //create the level from properties file
        level = LevelCreator.createLevel(LEVEL);

        //Create renderers and cameras for map, its objects and the player
        tileRenderer = new OrthogonalTiledMapRenderer(level.getMap(), 1f / Constants.TILE_SIZE);
        tileBatch = tileRenderer.getBatch();
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, Constants.TILES_X, Constants.TILES_Y + Constants.DEBUG_LINES);
        gameCamera.update();

        //Create special shape renderer for debugging
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.setAutoShapeType(true);

        //Create debug renderer, camera and font
        OrthographicCamera debugCamera = new OrthographicCamera();
        debugCamera.setToOrtho(false, Constants.WIDTH_PX / 2, Constants.HEIGHT_PX / 2);
        debugCamera.update();
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(debugCamera.combined);
        font = new BitmapFont();
        debugRect = new Rectangle(0, Constants.TILES_Y, Constants.TILES_X, Constants.DEBUG_LINES);

        //initialize our player
        Player.init(level.getPlayerSpawnPosition(), level.playerSpawnsFacingRight());

        Gdx.app.log("Finished creating game", "");
    }

    @Override
    public void render(float delta) {

        //clear the screen
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //get the delta time and update
        float deltaTime = Gdx.graphics.getDeltaTime();
        gameCamera.update();

        //render the map
        tileRenderer.setView(gameCamera);
        tileRenderer.render();

        //draw player and guardians
        tileBatch.begin();
        drawPlayer();
        drawGuardians();
        tileBatch.end();

        //update guardians
        updateGuardians(deltaTime);

        //update the player
        updatePlayer(deltaTime);

        //print some debug info to the screen
        debugText();
//        debugRectangles();
    }

    private void updateGuardians(float deltaTime) {
        if (deltaTime == 0) return;
        for (Guardian guardian : level.getGuardians()) {
            guardian.move(deltaTime);
        }
    }

    private void drawGuardians() {
        for (Guardian guardian : level.getGuardians()) {
            TextureRegion frame = guardian.sprite.animation.getKeyFrame(guardian.stateTime);
            if (guardian.goingRight()) {
                tileBatch.draw(frame, guardian.bounds.x, guardian.bounds.y, guardian.bounds.width, guardian.bounds.height);
            } else {
                tileBatch.draw(frame, guardian.bounds.x + guardian.bounds.width, guardian.bounds.y, -guardian.bounds.width, guardian.bounds.height);
            }
        }
    }

    private void debugText() {
        drawRectangle(debugRect, ShapeRenderer.ShapeType.Filled, Color.DARK_GRAY);
        spriteBatch.begin();
        //column 1
        font.draw(spriteBatch, "Pos: " + formatVector(Player.position), 10, 310);
        font.draw(spriteBatch, "Vel: " + formatVector(Player.velocity), 10, 290);
        //column 2
        font.draw(spriteBatch, "State: " + Player.state, 140, 310);
        font.draw(spriteBatch, "Ground: " + Player.grounded, 140, 290);
        //column 3
        font.draw(spriteBatch, "ConvLeft: " + Player.onLeftConveyer, 250, 310);
        font.draw(spriteBatch, "ConvRight: " + Player.onRightConveyer, 250, 290);
        //column 4
        font.draw(spriteBatch, "Obs L: " + Player.obstacleOnLeft, 370, 310);
        font.draw(spriteBatch, "Obs R: " + Player.obstacleOnRight, 370, 290);
        spriteBatch.end();
    }

    private void debugRectangles() {
        drawRectangle(Player.getLeftSensor(), ShapeRenderer.ShapeType.Filled, Color.GREEN);
        drawRectangle(Player.getRightSensor(), ShapeRenderer.ShapeType.Filled, Color.GREEN);
        drawRectangle(Player.getTopSensor(), ShapeRenderer.ShapeType.Filled, Color.RED);
        drawRectangle(Player.getBottomSensor(), ShapeRenderer.ShapeType.Filled, Color.RED);
        drawRectangle(Player.getBounds(), ShapeRenderer.ShapeType.Filled, Color.WHITE);
    }

    private String formatFloat(float f) {
        return String.format("%.2f", f);
    }

    private String formatVector(Vector2 v) {
        return formatFloat(v.x) + " , " + formatFloat(v.y);
    }

    private void updatePlayer(float deltaTime) {
        if (deltaTime == 0) return;
        checkInputs();
        collisionDetect();
        Player.move(deltaTime);
    }

    private void collisionDetect() {
        Collection<Rectangle> nearbyRectangles = getNearbyRectangles();
        checkObjectCollisions(nearbyRectangles);
        checkMapCollisions(nearbyRectangles);
        checkGuardianCollisions();
    }

    private void checkGuardianCollisions() {
        for (Guardian guardian : level.getGuardians()) {
            if (guardian.bounds.overlaps(Player.getBounds())) {
                handleHazard();
                break;
            }
        }
    }

    private void checkObjectCollisions(Collection<Rectangle> nearbyRectangles) {
        Collection<Rectangle> overlaps = OverlapHelper.getOverlaps(Player.getBounds(), nearbyRectangles);
        for (Rectangle rect : overlaps) {
            if (level.isCollectable(rect)) {
                handleCollectable(rect);
                nearbyRectangles.remove(rect);
                return;
            }
            if (level.isHazard(rect)) {
                handleHazard();
                return;
            }
        }
    }

    private void handleHazard() {
        Gdx.app.log("You died", "");
        AssetManager.getSound(AssetManager.SOUND_DIE).play();
//        create();
    }

    private void handleCollectable(Rectangle rect) {
        Gdx.app.log("Collected", rect.toString());
        AssetManager.getSound(AssetManager.SOUND_COLLECT).play();
        level.removeTile(rect);
    }

    private void checkMapCollisions(Collection<Rectangle> nearbyRectangles) {
        checkHorizontalMapCollisions(nearbyRectangles);
        checkVerticalCollisions(nearbyRectangles);
    }

    private void checkHorizontalMapCollisions(Collection<Rectangle> nearbyRectangles) {
        if (Player.goingLeft()) {
            Player.obstacleOnRight = false;
            Collection<Rectangle> leftSensorOverlaps = OverlapHelper.getOverlaps(Player.getLeftSensor(), nearbyRectangles);
            for (Rectangle rect : leftSensorOverlaps) {
                if (level.isImpassable(rect)) {
                    Player.stopMovingX();
//                    Player.position.x = rect.x + rect.width;
                    Player.obstacleOnLeft = true;
                }
            }
            if (leftSensorOverlaps.isEmpty()) {
                Player.obstacleOnLeft = false;
            }
        } else if (Player.goingRight()) {
            Player.obstacleOnLeft = false;
            Collection<Rectangle> rightSensorOverlaps = OverlapHelper.getOverlaps(Player.getRightSensor(), nearbyRectangles);
            for (Rectangle rect : rightSensorOverlaps) {
                if (level.isImpassable(rect)) {
                    Player.stopMovingX();
//                    Player.position.x = rect.x - Player.WIDTH;
                    Player.obstacleOnRight = true;
                }
            }
            if (rightSensorOverlaps.isEmpty()) {
                Player.obstacleOnRight = false;
            }
        }
    }


    private void checkVerticalCollisions(Collection<Rectangle> nearbyRectangles) {
        if (Player.goingDown()) {
            Collection<Rectangle> bottomSensorOverlaps = OverlapHelper.getOverlaps(Player.getBottomSensor(), nearbyRectangles);
            for (Rectangle rect : bottomSensorOverlaps) {
                if (rect.y < Player.position.y - 0.6f) {
                    Player.stopMovingY();
                    Player.position.y = rect.y + rect.height;
                    if (!Player.grounded) {
                        AssetManager.getSound(SOUND_LAND).play();
                    }
                    Player.grounded = true;
                    checkIfStandingOnConveyer(rect);
                    checkIfStandingOnCollapsible(rect);
                }
            }
        } else if (Player.goingUp()) {
            Collection<Rectangle> topSensorOverlaps = OverlapHelper.getOverlaps(Player.getTopSensor(), nearbyRectangles);
            for (Rectangle rect : topSensorOverlaps) {
                if (level.isImpassable(rect)) {
                    Player.stopMovingY();
                }
            }
        }
    }

    private void checkIfStandingOnCollapsible(Rectangle rect) {
        if (level.isCollapsible(rect)) {
            int touches = 1;
            if (Player.state.equals(Player.State.Jumping)) {
                touches = 3;
            }
            boolean collapsed = level.updateCollapsible(rect, touches);
            if (collapsed) {
                Gdx.app.log("Collapsed", rect.toString());
                AssetManager.getSound(AssetManager.SOUND_COLLAPSE).play();
            }
        }
    }

    private void checkIfStandingOnConveyer(Rectangle rect) {
        //standing on left conveyer
        if (level.isConveyerLeft(rect)) {
            Player.onLeftConveyer = true;
            Player.onRightConveyer = false;
            return;
        }
        //standing on right conveyer
        if (level.isConveyerRight(rect)) {
            Player.onRightConveyer = true;
            Player.onLeftConveyer = false;
            return;
        }
        //standing on something else
        Player.onLeftConveyer = false;
        Player.onRightConveyer = false;
    }

    private void checkInputs() {
        if ((Gdx.input.isKeyPressed(Keys.SPACE) || Gdx.input.isKeyPressed(Keys.ENTER) || isTouched(0.5f, 1))) {
            Player.jump();
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.Z) || isTouched(0, 0.25f)) {
            Player.walkLeft();
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.X) || isTouched(0.25f, 0.5f)) {
            Player.walkRight();
        }
    }

    private boolean isTouched(float startX, float endX) {
        // check if any finge is touch the area between startX and endX
        // startX/endX are given between 0 (left edge of the screen) and 1 (right edge of the screen)
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX(i) / (float) Gdx.graphics.getWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }

    private void drawPlayer() {
        // based on the Player state, get the animation frame
        TextureRegion frame = Player.standingFrame;
        if (Player.goingLeft() || Player.goingRight()) {
            frame = Player.sprite.animation.getKeyFrame(Player.stateTime);
        }
        if (Player.facesRight) {
            tileBatch.draw(frame, Player.position.x, Player.position.y, Player.WIDTH, Player.HEIGHT);
        } else {
            tileBatch.draw(frame, Player.position.x + Player.WIDTH, Player.position.y, -Player.WIDTH, Player.HEIGHT);
        }
    }

    private void drawRectangles(Collection<Rectangle> rects, ShapeRenderer.ShapeType shapeType, Color color) {
        shapeRenderer.begin(shapeType);
        shapeRenderer.setColor(color);
        for (Rectangle rect : rects) {
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.end();
    }

    private void drawRectangle(Rectangle rect, ShapeRenderer.ShapeType shapeType, Color color) {
        shapeRenderer.begin(shapeType);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        shapeRenderer.end();
    }

    private Collection<Rectangle> getNearbyRectangles() {
        int startX = (int) Player.position.x - 1;
        int startY = (int) Player.position.y - 1;
        int width = 4;
        int height = 5;

//        Rectangle rect = new Rectangle(startX, startY, width, height);
//        drawRectangle(rect, ShapeRenderer.ShapeType.Line, Color.YELLOW);

        final Collection<Rectangle> rects = new ArrayList<Rectangle>();
        for (int y = startY; y < startY + height; y++) {
            for (int x = startX; x < startX + width; x++) {
                Rectangle rect = level.getRectangle(x, y);
                if (rect != null) {
                    rects.add(rect);
                }
            }
        }
        return rects;
    }

    @Override
    public void dispose() {
        Gdx.app.log("Disposing game", "");
    }
}

