package org.gearvrf.vuforiasample;

import android.util.Log;

public class FPSCounter {
    private static int frames = 0;
    private static long startTimeMillis = 0;
    private static final long interval = 10000;

    public static void tick() {
        ++frames;
        if (System.currentTimeMillis() - startTimeMillis >= interval) {
            Log.v("", "FPS : " + frames / (interval / 1000.0f));
            frames = 0;
            startTimeMillis = System.currentTimeMillis();
        }
    }
}
