package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorCubicEasyOut implements GVRInterpolator {
    
    private static InterpolatorCubicEasyOut sInstance = null;

    public InterpolatorCubicEasyOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorCubicEasyOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorCubicEasyOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        return c*((t=t/d-1)*t*t + 1) + b;
    }
}