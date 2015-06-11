package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Collection;

public class CollisionDetector {

    public static Collection<Rectangle> getOverlappingRectangles(Rectangle rectToCheck, Collection<Rectangle> rects) {
        Collection<Rectangle> overlaps = new ArrayList<Rectangle>();
        for (Rectangle rect : rects) {
            if (rectToCheck.overlaps(rect)) {
                overlaps.add(rect);
            }
        }
        return overlaps;
    }

    public static boolean overlaps(Rectangle rectToCheck, Collection<Rectangle> rects) {
        for (Rectangle rect : rects) {
            if (rectToCheck.overlaps(rect)) {
                return true;
            }
        }
        return false;
    }

    public static Rectangle getIntersection(Rectangle rectToCheck, Rectangle rect) {
        Rectangle intersection = new Rectangle();
        Intersector.intersectRectangles(rectToCheck, rect, intersection);
        return intersection;
    }
}
