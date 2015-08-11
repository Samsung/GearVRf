package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class InterpolatorQuartEasyOut implements GVRInterpolator {
    
    private static InterpolatorQuartEasyOut sInstance = null;

    public InterpolatorQuartEasyOut() {
    }

    /** Get the (lazy-created) singleton */
    public static synchronized InterpolatorQuartEasyOut getInstance() {
        if (sInstance == null) {
            sInstance = new InterpolatorQuartEasyOut();
        }
        return sInstance;
    }

    @Override
    public float mapRatio(float ratio) {
        float t = ratio;
        float b = 0;
        float c = 1;
        float d = 1;

        return -c * ((t=t/d-1)*t*t*t - 1) + b;
    }
}