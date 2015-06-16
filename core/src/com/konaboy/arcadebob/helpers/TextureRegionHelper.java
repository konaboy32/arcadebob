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
}
