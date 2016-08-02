package org.gearvrf.utility;
import org.gearvrf.GVRTime;
import org.gearvrf.utility.Log;

public class GVRTimer {
    private String mName;
    private long mStartTimeNano;
    private long mStopTimeNano;
    private static long NANO_TO_MILLIS = 1000000;

    public GVRTimer(String name) {
        mName = name;
    }

    public void reset() {
        mStartTimeNano = 0;
        mStopTimeNano = 0;
    }

    public void start() {
        mStartTimeNano = GVRTime.getNanoTime();
    }

    public void stop() {
        mStopTimeNano = GVRTime.getNanoTime();
    }

    public long getNanoDiff() {
        return mStopTimeNano - mStartTimeNano;
    }

    public void log() {
        Log.d("Timer", "%s %f ms", mName, ((float)getNanoDiff()) / NANO_TO_MILLIS);
    }
}
