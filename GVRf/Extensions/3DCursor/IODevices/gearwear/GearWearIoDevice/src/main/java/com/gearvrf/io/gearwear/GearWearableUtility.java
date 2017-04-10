package com.gearvrf.io.gearwear;

public abstract class GearWearableUtility {
    /**
     * Check if a position is within a circle
     *
     * @param x       x position
     * @param y       y position
     * @param centerX center x of circle
     * @param centerY center y of circle
     * @return true if within circle, false otherwise
     */
    static boolean isInCircle(float x, float y, float centerX, float centerY, float
            radius) {
        return Math.abs(x - centerX) < radius && Math.abs(y - centerY) < radius;
    }

    static float square(float x) {
        return x * x;
    }
}
