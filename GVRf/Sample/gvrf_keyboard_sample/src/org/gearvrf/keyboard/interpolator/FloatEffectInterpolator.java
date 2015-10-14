
package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class FloatEffectInterpolator implements GVRInterpolator {

    private static FloatEffectInterpolator sInstance = null;

    public FloatEffectInterpolator() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized FloatEffectInterpolator getInstance() {
        if (sInstance == null) {
            sInstance = new FloatEffectInterpolator();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {

        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        if ((t /= d / 2) < 1)
            return c / 2 * t * t + b;
        return -c / 2 * ((--t) * (t - 2) - 1) + b;
    }
}
