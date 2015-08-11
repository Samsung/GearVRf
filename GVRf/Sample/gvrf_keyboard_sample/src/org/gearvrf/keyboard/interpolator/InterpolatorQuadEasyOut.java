package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorQuadEasyOut implements GVRInterpolator {
    
    private static InterpolatorQuadEasyOut sInstance = null;

    public InterpolatorQuadEasyOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorQuadEasyOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorQuadEasyOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        return -c *(t/=d)*(t-2) + b;
    }
}