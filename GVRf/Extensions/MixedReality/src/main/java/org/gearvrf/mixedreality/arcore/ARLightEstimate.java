package org.gearvrf.mixedreality.arcore;

import com.google.ar.core.Frame;
import com.google.ar.core.LightEstimate;

import org.gearvrf.GVRContext;
import org.gearvrf.mixedreality.GVRLightEstimate;

public class ARLightEstimate extends GVRLightEstimate {

    private ARCoreSession mSession;

    public ARLightEstimate(GVRContext gvrContext, ARCoreSession session) {
        super(gvrContext);
        this.mSession = session;
    }

    @Override
    protected GVRLightEstimateState onGetLightEstimateState() {
        GVRLightEstimateState gvrState;
        Frame arFrame = mSession.getARSession().update();
        LightEstimate lightEstimate = arFrame.getLightEstimate();
        LightEstimate.State state = lightEstimate.getState();

        gvrState = (state == LightEstimate.State.VALID) ? GVRLightEstimateState.VALID :
                GVRLightEstimateState.NOT_VALID;

        return gvrState;
    }

    @Override
    protected float onGetPixelIntensity() {
        Frame arFrame = mSession.getARSession().update();
        LightEstimate lightEstimate = arFrame.getLightEstimate();
        return lightEstimate.getPixelIntensity();
    }
}
