package com.konaboy.arcadebob.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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
import com.konaboy.arcadebob.gameobjects.Guardian;
import com.konaboy.arcadebob.gameobjects.Player;
import com.konaboy.arcadebob.helpers.AssetManager;
import com.konaboy.arcadebob.helpers.LevelCreator;
import com.konaboy.arcadebob.helpers.OverlapHelper;
import com.konaboy.arcadebob.helpers.SpriteCreator;

import java.util.Collection;

public class Game extends ApplicationAdapter {

    private static final String SOUND_COLLECT = "collect.wav";
    private static final String SOUND_DIE = "die.wav";
    private static final String SOUND_COLLAPSE = "collapse.wav";

    private BitmapFont font;
    private OrthogonalTiledMapRenderer tileRenderer;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private Batch tileBatch;
    private OrthographicCamera gameCamera;
    private Level level;
    private Rectangle debugRect;
    private int touchingTiles;

    @Override
    public void create() {
        Gdx.app.log("Creating game", "");

        //create player graphics and animation
        Player.sprite = SpriteCreator.createSprite(SpriteCreator.PLAYER_SPRITE_NAME);
        Player.standingFrame = Player.sprite.regions[1];

        //create the level from properties file
        level = LevelCreator.createLevel(1);

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
        initPlayer();

        Gdx.app.log("Finished creating game", "");
    }

    @Override
    public void render() {

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
        debug();
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

    private void debug() {
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
        font.draw(spriteBatch, "Coll: " + touchingTiles, 370, 310);
        font.draw(spriteBatch, "Guard: " + level.getGuardians().size(), 370, 290);
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
        checkInputs();
        collisionDetect();
        Player.move(deltaTime);
    }

    private void collisionDetect() {
        Collection<Rectangle> overlaps = OverlapHelper.getOverlaps(Player.getBounds(), level.getRectangles());
//        drawRectangles(overlaps, ShapeRenderer.ShapeType.Filled, Color.RED);
        touchingTiles = overlaps.size();
        checkObjectCollisions(overlaps);
        checkMapCollisions(overlaps);
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

    private void checkObjectCollisions(Collection<Rectangle> overlaps) {
        for (Rectangle rect : overlaps) {
            if (level.isCollectable(rect)) {
                handleCollectable(rect);
                overlaps.remove(rect);
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
        AssetManager.getSound(SOUND_DIE).play();
        create();
    }

    private void initPlayer() {
        Player.init(level.getPlayerSpawnPosition(), level.playerSpawnsFacingRight());
    }

    private void handleCollectable(Rectangle rect) {
        Gdx.app.log("Collectable", rect.toString());
        AssetManager.getSound(SOUND_COLLECT).play();
        level.removeTile(rect);
    }

    private void checkMapCollisions(Collection<Rectangle> overlaps) {
        checkHorizontalMapCollisions(overlaps);
        //we may have adjusted position of player horizontally in previous step, less overlaps now...
        OverlapHelper.removeNonOverlaps(Player.getBounds(), overlaps);
        checkVerticalCollisions(overlaps);
    }

    private void checkVerticalCollisions(Collection<Rectangle> overlaps) {
        if (Player.goingDown()) {
            for (Rectangle rect : overlaps) {
                if (rect.y < Player.position.y - 0.6f) {
                    Player.stopMovingY();
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
                    if (level.isImpassable(rect)) {
                        Player.stopMovingY();
                    }
                    break;
                }
            }
        }
    }

    private void checkHorizontalMapCollisions(Collection<Rectangle> overlaps) {
        if (Player.goingLeft()) {
            for (Rectangle rect : overlaps) {
                if (rect.x < Player.position.x && rect.y > Player.position.y) {
                    if (level.isImpassable(rect)) {
                        Player.position.x = rect.x + rect.width;
                    }
                    break;
                }
            }
        } else if (Player.goingRight()) {
            for (Rectangle rect : overlaps) {
                if (rect.x > Player.position.x && rect.y > Player.position.y) {
                    if (level.isImpassable(rect)) {
                        Player.position.x = rect.x - Player.WIDTH;
                    }
                    break;
                }
            }
        }
    }

    private void checkIfStandingOnCollapsible(Rectangle rect, Collection<Rectangle> overlaps) {
        if (level.isCollapsible(rect)) {
            int touches = 1;
            if (Player.state.equals(Player.State.Jumping)) {
                touches = 4;
            }
            boolean collapsed = level.updateCollapsible(rect, touches);
            if (collapsed) {
                overlaps.remove(rect);
                AssetManager.getSound(SOUND_COLLAPSE).play();
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

    @Override
    public void dispose() {
        Gdx.app.log("Disposing game", "");
    }
}

