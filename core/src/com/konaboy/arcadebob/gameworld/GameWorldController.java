package com.konaboy.arcadebob.gameworld;

import com.badlogic.gdx.utils.Array;
import com.konaboy.arcadebob.gameobjects.Block;
import com.konaboy.arcadebob.gameobjects.Bob;
import com.konaboy.arcadebob.helpers.AssetLoader;


public class GameWorldController {

    public Bob bob;
    public Array<Block> blocks;
    public int score;

    public GameWorldController() {
        bob = new Bob(800 / 2 - 64 / 2, 100, 64, 64, AssetLoader.bobTexture);
        blocks = new Array<Block>();
        createBlocks();
    }

    public void update() {
        bob.update();
        collisionDetect();
    }

    public void collisionDetect() {
        for (Block block : blocks) {
            if (bob.bounds.overlaps(block.bounds)) {
                bob.requestImmediateStop();
                return;
            }
        }
    }

    public void createBlocks() {
        Block block1 = new Block(64, 100, 64, 64, AssetLoader.grassTexture);
        Block block2 = new Block(128, 100, 64, 64, AssetLoader.grassTexture);
        Block block3 = new Block(192, 100, 64, 64, AssetLoader.grassTexture);
        blocks.add(block1);
        blocks.add(block2);
        blocks.add(block3);
    }
}
