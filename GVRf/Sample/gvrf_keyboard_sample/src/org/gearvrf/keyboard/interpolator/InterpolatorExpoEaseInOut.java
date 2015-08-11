
package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorExpoEaseInOut implements GVRInterpolator {

    private static InterpolatorExpoEaseInOut sInstance = null;

    public InterpolatorExpoEaseInOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorExpoEaseInOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorExpoEaseInOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        if (t == 0)
            return b;
        if (t == d)
            return b + c;
        if ((t /= d / 2) < 1)
            return c / 2 * (float) Math.pow(2, 10 * (t - 1)) + b;
        return c / 2 * (-(float) Math.pow(2, -10 * --t) + 2) + b;
    }

}
