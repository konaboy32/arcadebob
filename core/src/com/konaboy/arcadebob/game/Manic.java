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
import com.badlogic.gdx.utils.Pool;
import com.konaboy.arcadebob.gameobjects.Player;
import com.konaboy.arcadebob.helpers.CollisionDetector;
import com.konaboy.arcadebob.helpers.MapLoader;
import com.konaboy.arcadebob.helpers.TextureRegionHelper;
import com.konaboy.arcadebob.utils.GdxTest;

import java.util.Collection;


public class Manic extends GdxTest {

    private static final float CUSHION = 0.1f;

    private BitmapFont font;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Texture manicSpriteSheet;
    private Animation walk;
    private Player player;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>(512, 512) {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private TextureRegion standingFrame;
    private MapLoader mapLoader;

    private static final float GRAVITY = -1f;

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
        renderer = new OrthogonalTiledMapRenderer(map, 1f / MapLoader.TILE_SIZE);

        //Create an orthographic camera, shows us 32x16 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, MapLoader.TILES_X, MapLoader.TILES_Y);
        camera.update();

        //Create renderer for debugging
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);

        //Create a font
        font = new BitmapFont();

        //Create the Player we want to move around the world
        Player.position.set(4, 4);
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
    }


    private void updatePlayer(float deltaTime) {
        if (deltaTime == 0) return;
        Player.stateTime += deltaTime;
        checkInputs();
        collisionDetect();
        movePlayer(deltaTime);
    }

    private void collisionDetect() {

//        renderRectangles(mapLoader.getRectangles(), ShapeRenderer.ShapeType.Line, Color.WHITE);
//        renderRectangle(Player.rectangle, ShapeRenderer.ShapeType.Filled, Color.WHITE);

        Collection<Rectangle> overlaps = CollisionDetector.getOverlappingRectangles(Player.rectangle, mapLoader.getRectangles());
        renderRectangles(overlaps, ShapeRenderer.ShapeType.Filled, Color.RED);

        if (Player.velocity.x < 0) {
            for (Rectangle rect : overlaps) {
                if (rect.x < Player.position.x && rect.y >= Player.position.y) {
                    if (mapLoader.isImpassable(rect)) {
                        Player.velocity.x = 0;
                        Player.position.x = rect.x + rect.width;
                    }
                    break;
                }
            }
        } else if (Player.velocity.x > 0) {
            for (Rectangle rect : overlaps) {
                if (rect.x > Player.position.x && rect.y >= Player.position.y) {
                    if (mapLoader.isImpassable(rect)) {
                        Player.velocity.x = 0;
                        Player.position.x = rect.x - Player.WIDTH;
                    }
                    break;
                }
            }
        }

        if (Player.velocity.y < 0) {
            for (Rectangle rect : overlaps) {
                if (rect.y < Player.position.y) {
                    Player.velocity.y = 0;
                    Player.position.y = (float) Math.ceil(Player.position.y);
                    Player.grounded = true;
                    break;
                }
            }
        } else if (Player.velocity.y > 0) {
            for (Rectangle rect : overlaps) {
                if (rect.y > Player.position.y + 1) {
                    if (mapLoader.isImpassable(rect)) {
                        Player.velocity.y = 0;
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
            Player.velocity.x = 0;
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

        //Update the rectangle
        Player.rectangle.set(Player.position.x, Player.position.y, Player.WIDTH, Player.HEIGHT);
    }

    private void checkInputs() {
        // check input and apply to velocity & state
        if ((Gdx.input.isKeyPressed(Keys.SPACE) || isTouched(0.5f, 1)) && Player.grounded) {
            Player.velocity.y += Player.JUMP_VELOCITY;
            Player.state = Player.State.Jumping;
            Player.grounded = false;
        }

        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
            Player.velocity.x = -Player.MAX_VELOCITY;
            if (Player.grounded) {
                Player.state = Player.State.Walking;
            }
            Player.facesRight = false;
        }

        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
            Player.velocity.x = Player.MAX_VELOCITY;
            if (Player.grounded) {
                Player.state = Player.State.Walking;
            }
            Player.facesRight = true;
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
        System.out.println(Player.state);

        // based on the Player state, get the animation frame
        TextureRegion frame = standingFrame;
        switch (Player.state) {
            case Walking:
            case Jumping:
                frame = walk.getKeyFrame(Player.stateTime);
        }

        // draw the Player, depending on the current velocity
        // on the x-axis, draw the Player facing either right
        // or left
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

