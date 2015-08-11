
package org.gearvrf.keyboard.interpolator;

import org.gearvrf.animation.GVRInterpolator;

public class ElasticInterpolator implements GVRInterpolator {

    private static ElasticInterpolator sInstance = null;

    /** Get the (lazy-created) singleton */
    public static synchronized ElasticInterpolator getInstance() {
        if (sInstance == null) {
            sInstance = new ElasticInterpolator();
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
        if ((t /= d) == 1)
            return b + c;

        float p = d * .3f;
        float a = c;
        float s = p / 4;

        return (a * (float) Math.pow(2, -10 * t) * (float) Math.sin((t * d - s) * (2 * (float) Math.PI) / p) + c + b);

    }

}
