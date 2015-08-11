package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorQuintEasyOut implements GVRInterpolator {
    
    private static InterpolatorQuintEasyOut sInstance = null;

    public InterpolatorQuintEasyOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorQuintEasyOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorQuintEasyOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        return c*((t=t/d-1)*t*t*t*t + 1) + b;
    }
}