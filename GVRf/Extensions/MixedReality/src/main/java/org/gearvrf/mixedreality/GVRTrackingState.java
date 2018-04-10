package org.gearvrf.mixedreality;

public enum GVRTrackingState {
    TRACKING(0),
    PAUSED(1),
    STOPPED(2);

    private int mCode;

    GVRTrackingState(int code) {
        this.mCode = code;
    }

    public int getCode() {
        return this.mCode;
    }
}
