package com.konaboy.arcadebob.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.konaboy.arcadebob.gameobjects.Guardian;
import com.konaboy.arcadebob.helpers.LevelCreator;

import java.util.Collection;

public class Level {

    private static final Integer COLLAPSE_LIMIT = 14;

    private final TiledMap map;
    private final Collection<Rectangle> rectangles;
    private final TiledMapTileLayer layer;
    private final Collection<Guardian> guardians;
    private final Vector2 playerSpawnPosition;
    private final boolean playerSpawnsFacingRight;

    @SuppressWarnings("unchecked")
    public Level(TiledMap map,
                 Collection<Guardian> guardians,
                 Vector2 playerSpawnPosition,
                 boolean playerSpawnsFacingRight) {
        this.map = map;
        this.guardians = guardians;
        this.playerSpawnPosition = playerSpawnPosition;
        this.playerSpawnsFacingRight = playerSpawnsFacingRight;
        this.layer = (TiledMapTileLayer) map.getLayers().get(0);
        this.rectangles = (Collection<Rectangle>) map.getProperties().get(LevelCreator.KEY_RECTANGLES);
    }

    public TiledMap getMap() {
        return map;
    }

    public Collection<Guardian> getGuardians() {
        return guardians;
    }

    public boolean isConveyerLeft(Rectangle rect) {
        return getBlockType(rect).equals(Constants.BlockType.ConveyorLeft);
    }

    public boolean isConveyerRight(Rectangle rect) {
        return getBlockType(rect).equals(Constants.BlockType.ConveyorRight);
    }

    public boolean isImpassable(Rectangle rect) {
        return getBlockType(rect).equals(Constants.BlockType.Impassable);
    }

    public boolean isCollapsible(Rectangle rect) {
        return getBlockType(rect).equals(Constants.BlockType.Collapsible);
    }

    public boolean isCollectable(Rectangle rect) {
        return getBlockType(rect).equals(Constants.BlockType.Collectable);
    }

    public boolean isHazard(Rectangle rect) {
        return getBlockType(rect).equals(Constants.BlockType.Hazard);
    }

    public boolean updateCollapsible(Rectangle rect) {
        Integer touched = (Integer) layer.getCell((int) rect.x, (int) rect.y).getTile().getProperties().get(LevelCreator.KEY_TOUCHED);
        if (touched > COLLAPSE_LIMIT) {
            removeTile(rect);
            return true;
        }
        TiledMapTile tile = layer.getCell((int) rect.x, (int) rect.y).getTile();
        tile.setOffsetY(-touched);
        tile.getProperties().put(LevelCreator.KEY_TOUCHED, ++touched);
        return false;
    }

    public void removeTile(Rectangle rect) {
        layer.setCell((int) rect.x, (int) rect.y, null);
        rectangles.remove(rect);
    }

    private Constants.BlockType getBlockType(Rectangle rect) {
        return (Constants.BlockType) layer.getCell((int) rect.x, (int) rect.y).getTile().getProperties().get(LevelCreator.KEY_TYPE);
    }

    public Collection<Rectangle> getRectangles() {
        return rectangles;
    }

    public Vector2 getPlayerSpawnPosition() {
        return playerSpawnPosition;
    }

    public boolean playerSpawnsFacingRight() {
        return playerSpawnsFacingRight;
    }
}
