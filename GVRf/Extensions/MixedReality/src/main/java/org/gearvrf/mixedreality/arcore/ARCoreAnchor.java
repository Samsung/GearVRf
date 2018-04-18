package org.gearvrf.mixedreality.arcore;

import com.google.ar.core.Anchor;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRTrackingState;

public class ARCoreAnchor extends GVRAnchor {
    private Anchor mAnchor;
    ARCoreSession mSession;
    private ARCorePose mPose;

    public ARCoreAnchor(GVRContext gvrContext, ARCoreSession session, GVRSceneObject sceneObject) {
        super(gvrContext, sceneObject);
        this.mSession = session;
        mPose = new ARCorePose();
    }

    public void setAnchorAR(Anchor anchor) {
        this.mAnchor = anchor;
    }

    public Anchor getAnchorAR() {
        return this.mAnchor;
    }

    @Override
    protected void onUpdate(float[] arViewMatrix, float[] gvrCamMatrix, float scale) {

        if (mAnchor == null)
            return;

        if (mAnchor.getTrackingState() == TrackingState.TRACKING) {
            setTrackingState(GVRTrackingState.TRACKING);
            mPose.update(mAnchor.getPose(), arViewMatrix, gvrCamMatrix, scale);
            getTransform().setModelMatrix(mPose.getPoseMatrix());
        } else if(mAnchor.getTrackingState() == TrackingState.PAUSED){
            setTrackingState(GVRTrackingState.PAUSED);
        } else {
            setTrackingState(GVRTrackingState.STOPPED);
        }

    }
}
