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
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.konaboy.arcadebob.helpers.MapLoader;
import com.konaboy.arcadebob.helpers.TextureRegionHelper;
import com.konaboy.arcadebob.utils.GdxTest;


public class Manic extends GdxTest {

    class NearbyTile {
        Rectangle rect;
        int id;
    }

    /**
     * The player character, has state and state time,
     */
    static class Koala {
        static float WIDTH;
        static float HEIGHT;
        static float MAX_VELOCITY = 5f;
        static float JUMP_VELOCITY = 17f;
        static float DAMPING = 0.8f;

        enum State {
            Standing, Walking, Jumping
        }

        final Vector2 position = new Vector2();
        final Vector2 velocity = new Vector2();
        State state = State.Walking;
        float stateTime = 0;
        boolean facesRight = true;
        boolean grounded = false;
    }

    private static final float TILE_SIZE = 16f;
    private BitmapFont font;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Texture manicSpriteSheet;
    private Animation walk;
    private Animation jump;
    private Koala koala;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>(512, 512) {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private Array<NearbyTile> tiles = new Array<NearbyTile>();
    private TextureRegion standingFrame;
    private MapLoader mapLoader;

    private static final float GRAVITY = -1f;

    @Override
    public void create() {

        // load the koala frames, split them, and assign them to Animations
        manicSpriteSheet = new Texture("ManicSpriteSheet2.png");

        //Create player
        TextureRegion[] playerRegions = TextureRegionHelper.getPlayerRegions(manicSpriteSheet);
        walk = new Animation(0.1f, playerRegions);
        jump = walk;
        standingFrame = playerRegions[1];
        walk.setPlayMode(Animation.PlayMode.LOOP);

        // figure out the width and height of the koala for collision
        // detection and rendering by converting a koala frames pixel
        // size into world units (1 unit == 16 pixels)
        Koala.WIDTH = 1 / TILE_SIZE * 20;
        Koala.HEIGHT = 1 / TILE_SIZE * 32;

        mapLoader = new MapLoader(1);
        mapLoader.load(manicSpriteSheet);

        // load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
        map = mapLoader.getMap();
        renderer = new OrthogonalTiledMapRenderer(map, 1 / TILE_SIZE);

        // create an orthographic camera, shows us 32x16 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 32, 16);
        camera.update();

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setAutoShapeType(true);

        // create the Koala we want to move around the world
        koala = new Koala();
        koala.position.set(4, 4);

        font = new BitmapFont();
    }

    @Override
    public void render() {

        // clear the screen
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        // let the camera follow the koala, x-axis only
        camera.update();

        // set the tile map rendere view based on what the
        // camera sees and render the map
        renderer.setView(camera);
        renderer.render();

        // render the koala
        renderKoala(deltaTime);

        // update the koala (process input, collision detection, position update)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        updateKoala(deltaTime);
        shapeRenderer.end();
    }


    private void updateKoala(float deltaTime) {
        if (deltaTime == 0) return;

        koala.stateTime += deltaTime;

        // check input and apply to velocity & state
        if ((Gdx.input.isKeyPressed(Keys.SPACE) || isTouched(0.5f, 1)) && koala.grounded) {
            koala.velocity.y += Koala.JUMP_VELOCITY;
            koala.state = Koala.State.Jumping;
            koala.grounded = false;
        }

        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
            koala.velocity.x = -Koala.MAX_VELOCITY;
            if (koala.grounded) {
                koala.state = Koala.State.Walking;
            }
            koala.facesRight = false;
        }

        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
            koala.velocity.x = Koala.MAX_VELOCITY;
            if (koala.grounded) {
                koala.state = Koala.State.Walking;
            }
            koala.facesRight = true;
        }

        // apply gravity if we are falling
        koala.velocity.add(0, GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        if (Math.abs(koala.velocity.x) > Koala.MAX_VELOCITY) {
            koala.velocity.x = Math.signum(koala.velocity.x) * Koala.MAX_VELOCITY;
        }

        // clamp the velocity to 0 if it's < 1, and set the state to standing
        if (Math.abs(koala.velocity.x) < 1) {
            koala.velocity.x = 0;
            if (koala.grounded && !koala.state.equals(Koala.State.Standing)) {
                standingFrame = walk.getKeyFrame(koala.stateTime);
                koala.state = Koala.State.Standing;
            }
        }

        // multiply by delta time so we know how far we go
        // in this frame
        koala.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the koala is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left


        Rectangle koalaRect = rectPool.obtain();
        koalaRect.set(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);

        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(koalaRect.x, koalaRect.y, koalaRect.width, koalaRect.height);

        /*
         * HORIZONTAL COLLISIONS
         */
        int startX, startY, endX, endY;
        if (koala.facesRight) {
            startX = (int) (koala.position.x + Koala.WIDTH + koala.velocity.x);
            endX = startX + 1;
        } else {
            endX = (int) Math.ceil((double) koala.position.x + koala.velocity.x);
            startX = endX - 1;
        }
        startY = (int) koala.position.y;
        endY = (int) (koala.position.y + Koala.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.x += koala.velocity.x;
        for (NearbyTile tile : tiles) {
            if (koalaRect.overlaps(tile.rect) && tile.id != 147) {
                shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                koala.velocity.x = 0;
//                break; //TODO
            }
        }
        koalaRect.x = koala.position.x;

         /*
         * VERTICAL COLLISIONS
         */
        // if the koala is moving upwards, check the tiles to the top of it's
        // top bounding box edge, otherwise check the ones to the bottom
        if (koala.velocity.y > 0) {
            startY = (int) (koala.position.y + Koala.HEIGHT + koala.velocity.y);
            endY = startY + 1;
        } else {
            startY = (int) (koala.position.y + koala.velocity.y);
            endY = startY + 1;
        }
        startX = (int) (koala.position.x);
        endX = (int) Math.ceil((double) koala.position.x + Koala.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.y += koala.velocity.y;
        boolean overlap = false;
        for (NearbyTile tile : tiles) {
            if (koalaRect.overlaps(tile.rect)) {

                shapeRenderer.set(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(Color.BLUE);
                shapeRenderer.rect(tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);

                overlap = true;
                // we actually reset the koala y-position here
                // so it is just below/above the tile we collided with
                // this removes bouncing :)

                //GOING UP
                if (koala.velocity.y > 0) {
                    if (tile.id != 147) {
                        koala.position.y = (tile.rect.y) - Koala.HEIGHT;
                        koala.velocity.y = 0;
                    }
                    // we hit a block jumping upwards, let's destroy it!
                    //TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
                    //layer.setCell((int) tile.x, (int) tile.y, null);

                } else {
                    if (koala.position.y > tile.rect.y + 0.99) {
                        koala.position.y = (tile.rect.y) + (tile.rect.height);
                        // if we hit the ground, mark us as grounded so we can jump
                        koala.grounded = true;
                        koala.velocity.y = 0;
                    }
                }
//                break; //TODO
            }
        }

        if (!overlap && koala.velocity.y < 0) {
            //Falling, stop moving horizontal
            koala.velocity.x = 0;
        }

        rectPool.free(koalaRect);

        // unscale the velocity by the inverse delta time and set
        // the latest position
        koala.position.add(koala.velocity);
        koala.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        koala.velocity.x *= Koala.DAMPING;
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

    private void getTiles(int startX, int startY, int endX, int endY, Array<NearbyTile> tiles) {
        shapeRenderer.set(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(startX, startY, endX - startX, endY - startY);

        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(0);

        for (NearbyTile tile : tiles) {
            rectPool.free(tile.rect);
        }

        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    NearbyTile tile = new NearbyTile();
                    tile.rect = rect;
                    tile.id = cell.getTile().getId();
                    tiles.add(tile);
                }
            }
        }
    }

    private void renderKoala(float deltaTime) {
        // based on the koala state, get the animation frame
        TextureRegion frame = null;

        switch (koala.state) {
            case Standing:
                frame = standingFrame;
                break;
            case Walking:
                frame = walk.getKeyFrame(koala.stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(koala.stateTime);
                break;
        }


//        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
//        shapeRenderer.rect(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
//        shapeRenderer.end();

        // draw the koala, depending on the current velocity
        // on the x-axis, draw the koala facing either right
        // or left
        Batch batch = renderer.getBatch();
        batch.begin();
        if (koala.facesRight) {
            batch.draw(frame, koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        } else {
            batch.draw(frame, koala.position.x + Koala.WIDTH, koala.position.y, -Koala.WIDTH, Koala.HEIGHT);
        }
        String message = koala.position.x + " " + koala.position.y + " " + Koala.WIDTH + " " + Koala.HEIGHT;
//        font.draw(batch, message, 0, 16);
        batch.end();


    }


    @Override
    public void dispose() {
    }
}

