
package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorExpoEaseOut implements GVRInterpolator {

    private static InterpolatorExpoEaseOut sInstance = null;

    public InterpolatorExpoEaseOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorExpoEaseOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorExpoEaseOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        return (t == d) ? b + c : c * (-(float) Math.pow(2, -10 * t / d) + 1) + b;
    }

}
