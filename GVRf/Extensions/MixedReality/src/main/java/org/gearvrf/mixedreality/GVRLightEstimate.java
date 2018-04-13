package org.gearvrf.mixedreality;

import org.gearvrf.GVRContext;

public abstract class GVRLightEstimate {

    protected GVRLightEstimate(GVRContext gvrContext) {  }

    public float getPixelIntensity() { return onGetPixelIntensity(); }

    public GVRLightEstimateState getLightEstimateState() { return onGetLightEstimateState(); }

    public enum GVRLightEstimateState {
        NOT_VALID(0),
        VALID(1);

        private int mCode;

        GVRLightEstimateState(int code) {
            this.mCode = code;
        }

        public int getCode() {
            return this.mCode;
        }
    }

    protected abstract GVRLightEstimateState onGetLightEstimateState();
    protected abstract float onGetPixelIntensity();
}
