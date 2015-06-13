package com.konaboy.arcadebob.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.konaboy.arcadebob.gameobjects.Player;
import com.konaboy.arcadebob.helpers.CollisionDetector;
import com.konaboy.arcadebob.helpers.MapLoader;
import com.konaboy.arcadebob.helpers.TextureRegionHelper;
import com.konaboy.arcadebob.utils.GdxTest;

import java.util.Collection;


public class Manic extends GdxTest {

    public static final int DEBUG_LINES = 4;
    private BitmapFont font;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Texture manicSpriteSheet;
    private Animation walk;
    private TextureRegion standingFrame;
    private MapLoader mapLoader;
    private Rectangle debugRect;

    private static final float GRAVITY = -0.8f;

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
        map = mapLoader.getMap();

        //Create renderers and cameras
        renderer = new OrthogonalTiledMapRenderer(map, 1f / MapLoader.TILE_SIZE);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, MapLoader.TILES_X, MapLoader.TILES_Y + DEBUG_LINES);
        camera.update();

        //Create renderer for debugging
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);

        //Create a font and debug area
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
        camera.update();

        //Render the map
        renderer.setView(camera);
        renderer.render();

        //Render the Player
        renderPlayer();

        //Update the Player
        updatePlayer(deltaTime);

        debug();
    }

    private void debug() {
        renderRectangle(debugRect, ShapeRenderer.ShapeType.Filled, Color.DARK_GRAY);
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
        checkObjectCollisions(overlaps);
        checkMapCollisions(overlaps);
    }

    private void checkObjectCollisions(Collection<Rectangle> overlaps) {
        for (Rectangle rect : overlaps) {
            if (mapLoader.isCollectable(rect)) {
                handleCollectable(rect);
                overlaps.remove(rect);
                break;
            } else if (mapLoader.isHazard(rect)) {
                handleHazard();
                break;
            }
        }
    }

    private void handleHazard() {
        renderRectangle(Player.getBounds(), ShapeRenderer.ShapeType.Filled, Color.RED);
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
                if (rect.y < Player.position.y - rect.width / 2f) {
                    Player.stopY();
                    Player.position.y = rect.y + rect.height;
                    Player.grounded = true;
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

    private void movePlayer(float deltaTime) {
        // clamp the velocity to the maximum, x-axis only
        if (Math.abs(Player.velocity.x) > Player.MAX_VELOCITY) {
            Player.velocity.x = Math.signum(Player.velocity.x) * Player.MAX_VELOCITY;
        }

        // clamp the velocity to 0 if it's < 1, and set the state to standing
        if (Math.abs(Player.velocity.x) < 1) {
            Player.stopX();
            if (Player.grounded && !Player.state.equals(Player.State.Standing)) {
                standingFrame = walk.getKeyFrame(Player.stateTime);
                Player.state = Player.State.Standing;
            }
        }

        Player.velocity.add(0, GRAVITY);

        // multiply by delta time so we know how far we go in this frame
        Player.velocity.scl(deltaTime);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        Player.position.add(Player.velocity);
        Player.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        Player.velocity.x *= Player.DAMPING;
    }

    private void checkInputs() {
        // check input and apply to velocity & state
        if ((Gdx.input.isKeyPressed(Keys.SPACE) || isTouched(0.5f, 1)) && Player.grounded) {
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

        Batch batch = renderer.getBatch();
        batch.begin();
        if (Player.facesRight) {
            batch.draw(frame, Player.position.x, Player.position.y, Player.WIDTH, Player.HEIGHT);
        } else {
            batch.draw(frame, Player.position.x + Player.WIDTH, Player.position.y, -Player.WIDTH, Player.HEIGHT);
        }
        batch.end();
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

