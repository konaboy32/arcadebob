package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureRegionHelper {

    public static TextureRegion[] getRegions(Texture texture, int startX, int startY, int sizeX, int sizeY, int regionSize) {
        final int xRegions = sizeX / regionSize;
        final int yRegions = sizeY / regionSize;
        final int totalRegions = xRegions * yRegions;
        TextureRegion[] regions = new TextureRegion[totalRegions];
        int count = 0;
        for (int y = startY; y < startY + sizeY; y = y + regionSize) {
            for (int x = startX; x < startX + sizeX; x = x + regionSize) {
                regions[count++] = new TextureRegion(texture, x, y, regionSize, regionSize);
            }
        }
        return regions;
    }

    public static TextureRegion[] getPlayerRegions(Texture texture) {
        TextureRegion[] regions = new TextureRegion[8];
        regions[0] = new TextureRegion(texture, 0 + 2, 328, 20, 32);
        regions[1] = new TextureRegion(texture, 32 + 6, 328, 20, 32);
        regions[2] = new TextureRegion(texture, 64 + 10, 328, 20, 32);
        regions[3] = new TextureRegion(texture, 96 + 14, 328, 20, 32);
        regions[4] = new TextureRegion(texture, 128 + 2, 328, 20, 32);
        regions[5] = new TextureRegion(texture, 160 + 6, 328, 20, 32);
        regions[6] = new TextureRegion(texture, 192 + 10, 328, 20, 32);
        regions[7] = new TextureRegion(texture, 224 + 14, 328, 20, 32);
        return regions;
    }
}
