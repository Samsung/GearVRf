package org.gearvrf.widgetlib.thread;

import android.os.SystemClock;

import org.gearvrf.widgetlib.log.Log;

/**
 * Utility to check FPS
 */
public class FPSCounter {
    /**
     * This method should be called every frame in {@link org.gearvrf.GVRMain#onStep}
     * to do FPS calculation.
     * If main debug subsystem is enabled the FPS measurements will be printed out each frame.
     */
    public static void tick() {
        sumFrames++;
        if (startTime == 0) {
            startTime = SystemClock.uptimeMillis();
        }

        ++frames;
        long currentTime = System.currentTimeMillis();
        if (currentTime - startTimeMillis >= interval) {
            int fps = (int) (frames * 1000 / interval);
            Log.v("FPSCounter", "FPS : " + fps);
            frames = 0;
            startTimeMillis = currentTime;
        }
    }

    /**
     * Computes FPS average
     */
    public static void count() {
        long duration = SystemClock.uptimeMillis() - startTime;
        float avgFPS = sumFrames / (duration / 1000f);

        Log.v("FPSCounter",
                "total frames = %d, total elapsed time = %d ms, average fps = %f",
                sumFrames, duration, avgFPS);
    }

    /**
     * Resets the FPS counting data
     */
    public static void pause() {
        startTime = 0;
        sumFrames = 0;
    }

    /**
     * Start check of execution time
     * @param extra
     */
    public static void startCheck(String extra) {
        startCheckTime = System.currentTimeMillis();
        nextCheckTime = startCheckTime;
        Log.d(Log.SUBSYSTEM.TRACING, "FPSCounter" , "[%d] startCheck %s",  startCheckTime, extra);
    }

    /**
     * Computes execution time
     * @param extra
     */
    public static void timeCheck(String extra) {
        if (startCheckTime > 0) {
            long now = System.currentTimeMillis();
            long diff = now - nextCheckTime;
            nextCheckTime = now;
            Log.d(Log.SUBSYSTEM.TRACING, "FPSCounter", "[%d, %d] timeCheck: %s", now, diff, extra);
        }
    }

    /**
     * Stop check of execution time
     * @param extra
     */
    public static void stopCheck(String extra) {
        if (startCheckTime > 0) {
            long now = System.currentTimeMillis();
            long diff = now - startCheckTime;
            Log.d(Log.SUBSYSTEM.TRACING, "FPSCounter", "[%d, %d] stopCheck: %s", now, diff, extra);
        }
        startCheckTime = 0;
        nextCheckTime = 0;
    }

    private static int frames = 0;
    private static long startTimeMillis = 0;
    private static final long interval = 1000;
    private static int sumFrames = 0;
    private static long startTime = 0;


    private static long startCheckTime = 0;
    private static long nextCheckTime = 0;
}
