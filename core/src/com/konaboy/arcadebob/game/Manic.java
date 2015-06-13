package com.konaboy.arcadebob.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.gameobjects.Player;
import com.konaboy.arcadebob.helpers.CollisionDetector;
import com.konaboy.arcadebob.helpers.MapLoader;
import com.konaboy.arcadebob.helpers.TextureRegionHelper;
import com.konaboy.arcadebob.utils.GdxTest;

import java.util.Collection;

public class Manic extends GdxTest {

    public static final int DEBUG_LINES = 4;
    public static final int WIDTH_PX = 1024;
    public static final int HEIGHT_PX = 512 + (512 / MapLoader.TILES_Y * Manic.DEBUG_LINES);
    private BitmapFont font;
    private OrthogonalTiledMapRenderer tileRenderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private Batch tileBatch;
    private OrthographicCamera gameCamera;
    private OrthographicCamera debugCamera;
    private Texture manicSpriteSheet;
    private Animation walk;
    private TextureRegion standingFrame;
    private MapLoader mapLoader;
    private Rectangle debugRect;
    private int touchingTiles;

    private static final float GRAVITY = -0.15f;

    @Override
    public void create() {

        //Create player graphics
        manicSpriteSheet = new Texture("ManicSpriteSheet2.png");
        TextureRegion[] playerRegions = TextureRegionHelper.getPlayerRegions(manicSpriteSheet);
        walk = new Animation(0.1f, playerRegions);
        standingFrame = playerRegions[1];
        walk.setPlayMode(Animation.PlayMode.LOOP);

        //load the map
        mapLoader = new MapLoader(1);
        mapLoader.load(manicSpriteSheet);

        //Create renderers and cameras
        tileRenderer = new OrthogonalTiledMapRenderer(mapLoader.getMap(), 1f / MapLoader.TILE_SIZE);
        tileBatch = tileRenderer.getBatch();
        gameCamera = new OrthographicCamera();
        gameCamera.setToOrtho(false, MapLoader.TILES_X, MapLoader.TILES_Y + DEBUG_LINES);
        gameCamera.update();

        //Create tileRenderer for debugging
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(gameCamera.combined);
        shapeRenderer.setAutoShapeType(true);

        //init player
        initPlayer();

        //Create font and debug renderer
        debugCamera = new OrthographicCamera();
        debugCamera.setToOrtho(false, WIDTH_PX / 2, HEIGHT_PX / 2);
        debugCamera.update();
        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(debugCamera.combined);
        font = new BitmapFont();
        debugRect = new Rectangle(0, MapLoader.TILES_Y, MapLoader.TILES_X, DEBUG_LINES);
    }

    @Override
    public void render() {

        //Clear the screen
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //Get the delta time and update
        float deltaTime = Gdx.graphics.getDeltaTime();
        gameCamera.update();

        //Render the map
        tileRenderer.setView(gameCamera);
        tileRenderer.render();

        //Render the Player
        renderPlayer();

        //Update the Player
        updatePlayer(deltaTime);

        debug();
    }

    private void debug() {
        renderRectangle(debugRect, ShapeRenderer.ShapeType.Filled, Color.DARK_GRAY);
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
        font.draw(spriteBatch, "Coll: " + touchingTiles, 370, 310);

        spriteBatch.end();
    }

    private String formatFloat(float f) {
        return String.format("%.2f", f);
    }

    private String formatVector(Vector2 v) {
        return formatFloat(v.x) + " , " + formatFloat(v.y);
    }


    private void updatePlayer(float deltaTime) {
        if (deltaTime == 0) return;
        Player.stateTime += deltaTime;
        checkInputs();
        collisionDetect();
        movePlayer(deltaTime);
    }

    private void collisionDetect() {
        Collection<Rectangle> overlaps = CollisionDetector.getOverlaps(Player.getBounds(), mapLoader.getRectangles());
        touchingTiles = overlaps.size();
        checkObjectCollisions(overlaps);
        checkMapCollisions(overlaps);
        checkGuardianCollisions();
    }

    private void checkGuardianCollisions() {
    }

    private void checkObjectCollisions(Collection<Rectangle> overlaps) {
        for (Rectangle rect : overlaps) {
            if (mapLoader.isCollectable(rect)) {
                handleCollectable(rect);
                overlaps.remove(rect);
                return;
            }
            if (mapLoader.isHazard(rect)) {
                handleHazard();
                return;
            }
        }
    }

    private void handleHazard() {
        renderRectangle(Player.getBounds(), ShapeRenderer.ShapeType.Filled, Color.RED);
//        initPlayer();
    }

    private void initPlayer() {
        Player.init(mapLoader.getLevelProperties().getStartPosition(), mapLoader.getLevelProperties().startFacingRight());
    }

    private void handleCollectable(Rectangle rect) {
        mapLoader.removeTile(rect);
    }

    private void checkMapCollisions(Collection<Rectangle> overlaps) {
        if (Player.goingLeft()) {
            for (Rectangle rect : overlaps) {
                if (rect.x < Player.position.x && rect.y > Player.position.y) {
                    if (mapLoader.isImpassable(rect)) {
                        Player.stopX();
                        Player.position.x = rect.x + rect.width;
                    }
                    break;
                }
            }
        } else if (Player.goingRight()) {
            for (Rectangle rect : overlaps) {
                if (rect.x > Player.position.x && rect.y > Player.position.y) {
                    if (mapLoader.isImpassable(rect)) {
                        Player.stopX();
                        Player.position.x = rect.x - Player.WIDTH;
                    }
                    break;
                }
            }
        }

        CollisionDetector.removeNonOverlaps(Player.getBounds(), overlaps);
        if (Player.goingDown()) {
            for (Rectangle rect : overlaps) {
                if (rect.y < Player.position.y - (rect.width * 0.9)) {
                    Player.stopY();
                    Player.position.y = rect.y + rect.height;
                    Player.grounded = true;
                    checkIfStandingOnConveyer(rect);
                    checkIfStandingOnCollapsible(rect, overlaps);
                    break;
                }
            }
        } else if (Player.goingUp()) {
            for (Rectangle rect : overlaps) {
                if (rect.y > Player.position.y + 1) {
                    if (mapLoader.isImpassable(rect)) {
                        Player.stopY();
                    }
                    break;
                }
            }
        }
    }

    private void checkIfStandingOnCollapsible(Rectangle rect, Collection<Rectangle> overlaps) {
        if (mapLoader.isCollapsible(rect)) {
            boolean collapsed = mapLoader.updateCollapsible(rect);
            if (collapsed) {
                overlaps.remove(rect);
            }
        }
    }

    private void checkIfStandingOnConveyer(Rectangle rect) {
        if (mapLoader.isConveyerLeft(rect)) {
            Player.onLeftConveyer = true;
            return;
        }
        if (mapLoader.isConveyerRight(rect)) {
            Player.onRightConveyer = true;
            Player.walkRight();
            return;
        }
        Player.onLeftConveyer = false;
        Player.onRightConveyer = false;
    }

    private void movePlayer(float deltaTime) {
        if (Player.onLeftConveyer) {
            Player.walkLeft();
        }

        if (Player.onRightConveyer) {
            Player.walkRight();
        }

        // clamp the velocity to 0 if it's < 1, and set the state to standing
        if (Math.abs(Player.velocity.x) < 1) {
            Player.stopX();
            if (Player.grounded && !Player.state.equals(Player.State.Standing)) {
                standingFrame = walk.getKeyFrame(Player.stateTime);
                Player.state = Player.State.Standing;
            }
        }

        //apply gravity to y-axis
        Player.velocity.add(0, GRAVITY);

        // multiply by delta time so we know how far we go in this frame
        Player.velocity.scl(deltaTime);

        // unscale the velocity by the inverse delta time and set to the latest position
        Player.position.add(Player.velocity);
        Player.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity so we don't walk infinitely once a key was pressed
        if (Player.grounded) {
            Player.velocity.x *= Player.DAMPING;
        }
    }

    private void checkInputs() {
        if ((Gdx.input.isKeyPressed(Keys.SPACE) || isTouched(0.5f, 1))) {
            Player.jump();
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
            Player.walkLeft();
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
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

    private void renderPlayer() {

        // based on the Player state, get the animation frame
        TextureRegion frame = standingFrame;
        switch (Player.state) {
            case Walking:
            case Jumping:
                frame = walk.getKeyFrame(Player.stateTime);
        }

        tileBatch.begin();
        if (Player.facesRight) {
            tileBatch.draw(frame, Player.position.x, Player.position.y, Player.WIDTH, Player.HEIGHT);
        } else {
            tileBatch.draw(frame, Player.position.x + Player.WIDTH, Player.position.y, -Player.WIDTH, Player.HEIGHT);
        }
        tileBatch.end();
    }

    private void renderRectangles(Collection<Rectangle> rects, ShapeRenderer.ShapeType shapeType, Color color) {
        shapeRenderer.begin(shapeType);
        shapeRenderer.setColor(color);
        for (Rectangle rect : rects) {
            shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        }
        shapeRenderer.end();
    }

    private void renderRectangle(Rectangle rect, ShapeRenderer.ShapeType shapeType, Color color) {
        shapeRenderer.begin(shapeType);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height);
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
    }
}

