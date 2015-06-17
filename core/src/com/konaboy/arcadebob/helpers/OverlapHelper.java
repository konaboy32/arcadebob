package com.konaboy.arcadebob.helpers;

import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class OverlapHelper {

    public static Collection<Rectangle> getOverlaps(final Rectangle rectToCheck, final Collection<Rectangle> rects) {
        final Collection<Rectangle> overlaps = new ArrayList<Rectangle>();
        for (Rectangle rect : rects) {
            if (rectToCheck.overlaps(rect)) {
                overlaps.add(rect);
            }
        }
        return overlaps;
    }

    public static void removeNonOverlaps(final Rectangle rectToCheck, final Collection<Rectangle> rects) {
        Iterator<Rectangle> i = rects.iterator();
        while (i.hasNext()) {
            if (!rectToCheck.overlaps(i.next())) {
                i.remove();
            }
        }
    }

}
