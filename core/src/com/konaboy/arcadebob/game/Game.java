package com.konaboy.arcadebob.game;

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
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.konaboy.arcadebob.gameobjects.Guardian;
import com.konaboy.arcadebob.gameobjects.Player;
import com.konaboy.arcadebob.helpers.AssetManager;
import com.konaboy.arcadebob.helpers.GdxTest;
import com.konaboy.arcadebob.helpers.LevelCreator;
import com.konaboy.arcadebob.helpers.OverlapHelper;

import java.util.Collection;

public class Game extends GdxTest {

    private final static float MAX_VELOCITY = 4f;
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
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private Body body;
    private Fixture fixture;
    Fixture playerPhysicsFixture;
    Fixture playerSensorFixture;
    private Body playerbox;
    private long lastGroundTime = 0;
    private float stillTime = 0;
    private boolean jump = false;


    @Override
    public void create() {
        Gdx.app.log("Creating game", "");

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
        Player.init(level.getPlayerSpawnPosition(), level.playerSpawnsFacingRight());

        world = new World(new Vector2(0, -10), true);
        debugRenderer = new Box2DDebugRenderer();
        body = world.createBody(Player.bodyDef);
        PolygonShape character = new PolygonShape();
        character.setAsBox(0.5f, 1f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = character;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f; // Make it bounce a little bit
        fixture = body.createFixture(fixtureDef);

        playerbox = createPlayer();
        playerbox.setTransform(4.0f, 4.0f, 0);
        playerbox.setFixedRotation(true);

        for (Rectangle rect : level.getRectangles()) {
            BodyDef groundBodyDef = new BodyDef();
            groundBodyDef.position.set(new Vector2(rect.x + 0.5f, rect.y + 0.5f));
            Body groundBody = world.createBody(groundBodyDef);
            PolygonShape groundBox = new PolygonShape();
            groundBox.setAsBox(0.5f, 0.5f);
            groundBody.createFixture(groundBox, 0.0f);
        }

        Gdx.input.setInputProcessor(this);

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

        render2d(deltaTime);

        world.step(deltaTime, 6, 2);
        debugRenderer.render(world, gameCamera.combined);
    }

    private void render2d(float deltaTime) {
        Vector2 vel = playerbox.getLinearVelocity();
        Vector2 pos = playerbox.getPosition();
        boolean grounded = isPlayerGrounded(Gdx.graphics.getDeltaTime());
        if (grounded) {
            lastGroundTime = TimeUtils.nanoTime();
        } else {
            if (TimeUtils.nanoTime() - lastGroundTime < 100000000) {
                grounded = true;
            }
        }

        // cap max velocity on x
        if (Math.abs(vel.x) > MAX_VELOCITY) {
            vel.x = Math.signum(vel.x) * MAX_VELOCITY;
            playerbox.setLinearVelocity(vel.x, vel.y);
        }

        // calculate stilltime & damp
        if (!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D)) {
            stillTime += Gdx.graphics.getDeltaTime();
            playerbox.setLinearVelocity(vel.x * 0.9f, vel.y);
        } else {
            stillTime = 0;
        }

        // disable friction while jumping
        if (!grounded) {
            playerPhysicsFixture.setFriction(0f);
            playerSensorFixture.setFriction(0f);
        } else {
            if (!Gdx.input.isKeyPressed(Keys.A) && !Gdx.input.isKeyPressed(Keys.D) && stillTime > 0.2) {
                playerPhysicsFixture.setFriction(1000f);
                playerSensorFixture.setFriction(1000f);
            } else {
                playerPhysicsFixture.setFriction(0.2f);
                playerSensorFixture.setFriction(0.2f);
            }

//            // dampen sudden changes in x/y of a MovingPlatform a little bit, otherwise
//            // character hops :)
//            if (groundedPlatform != null && groundedPlatform instanceof MovingPlatform
//                    && ((MovingPlatform) groundedPlatform).dist == 0) {
//                player.applyLinearImpulse(0, -24, pos.x, pos.y, true);
//            }
        }

        // since Box2D 2.2 we need to reset the friction of any existing contacts
        Array<Contact> contacts = world.getContactList();
        for (int i = 0; i < world.getContactCount(); i++) {
            Contact contact = contacts.get(i);
            contact.resetFriction();
        }

        // apply left impulse, but only if max velocity is not reached yet
        if (Gdx.input.isKeyPressed(Keys.A) && vel.x > -MAX_VELOCITY) {
            playerbox.applyLinearImpulse(-2f, 0, pos.x, pos.y, true);
        }

        // apply right impulse, but only if max velocity is not reached yet
        if (Gdx.input.isKeyPressed(Keys.D) && vel.x < MAX_VELOCITY) {
            playerbox.applyLinearImpulse(2f, 0, pos.x, pos.y, true);
        }

        System.out.println("grunded: " + grounded + " jump: " + jump);

        // jump, but only when grounded
        if (jump) {
            jump = false;
            if (grounded) {
                playerbox.setLinearVelocity(vel.x, 0);
                System.out.println("jump before: " + playerbox.getLinearVelocity());
                playerbox.setTransform(pos.x, pos.y + 0.01f, 0);
                playerbox.applyLinearImpulse(0, 10, pos.x, pos.y, true);
                System.out.println("jump, " + playerbox.getLinearVelocity());
            }
        }

        playerbox.setAwake(true);


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
//        create();
    }

    private void handleCollectable(Rectangle rect) {
        Gdx.app.log("Collectable", rect.toString());
        AssetManager.getSound(SOUND_COLLECT).play();
        level.removeTile(rect);
    }

    private void checkMapCollisions(Collection<Rectangle> overlaps) {
//        drawRectangles(overlaps, ShapeRenderer.ShapeType.Filled, Color.WHITE);
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
                        System.out.println(Player.position.x);
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
            playerbox.applyLinearImpulse(new Vector2(0, 1), body.getWorldCenter(), true);
//            Player.jump();
        }
        if (Gdx.input.isKeyPressed(Keys.LEFT) || Gdx.input.isKeyPressed(Keys.A) || isTouched(0, 0.25f)) {
            playerbox.applyLinearImpulse(new Vector2(-1, 0), body.getWorldCenter(), true);
//            Player.walkLeft();
        }
        if (Gdx.input.isKeyPressed(Keys.RIGHT) || Gdx.input.isKeyPressed(Keys.D) || isTouched(0.25f, 0.5f)) {
            playerbox.applyLinearImpulse(new Vector2(1, 0), body.getWorldCenter(), true);
//            Player.walkRight();
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

    private Body createPlayer() {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        Body box = world.createBody(def);

        PolygonShape poly = new PolygonShape();
        poly.setAsBox(0.45f, 0.75f);
        playerPhysicsFixture = box.createFixture(poly, 1);
        poly.dispose();

        CircleShape circle = new CircleShape();
        circle.setRadius(0.45f);
        circle.setPosition(new Vector2(0, -0.75f));
        playerSensorFixture = box.createFixture(circle, 0);
        circle.dispose();

        box.setBullet(true);

        return box;
    }

    private boolean isPlayerGrounded(float deltaTime) {
//        groundedPlatform = null;
        Array<Contact> contactList = world.getContactList();
        System.out.println(contactList.size);
        for (int i = 0; i < contactList.size; i++) {

            Contact contact = contactList.get(i);
            if (contact.isTouching()
                    && (contact.getFixtureA() == playerSensorFixture
                    || contact.getFixtureB() == playerSensorFixture)) {

                System.out.println("CONTACT");
                Vector2 pos = playerbox.getPosition();
                System.out.println(playerbox.getPosition().x + " " + playerbox.getPosition().y);
                WorldManifold manifold = contact.getWorldManifold();
                boolean below = true;
                for (int j = 0; j < manifold.getNumberOfContactPoints(); j++) {
                    System.out.println("" + manifold.getPoints()[j].y);
                    below &= (manifold.getPoints()[j].y < pos.y - 1f);
                }

                return below;

            }
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        System.out.println("KEYDOWN");
        if (keycode == Keys.W) {
            System.out.println("JUMP ***************************************");
            jump = true;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        System.out.println("KEYUP *****************************************");
        if (keycode == Keys.W) jump = false;
        return false;
    }

}

