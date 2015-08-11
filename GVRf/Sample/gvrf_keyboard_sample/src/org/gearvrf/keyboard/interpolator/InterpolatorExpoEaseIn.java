
package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorExpoEaseIn implements GVRInterpolator {

    private static InterpolatorExpoEaseIn sInstance = null;

    public InterpolatorExpoEaseIn() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorExpoEaseIn getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorExpoEaseIn();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        return (t == 0) ? b : c * (float) Math.pow(2, 10 * (t / d - 1)) + b;
    }

}
