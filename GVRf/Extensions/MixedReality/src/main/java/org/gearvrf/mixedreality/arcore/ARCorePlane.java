package org.gearvrf.mixedreality.arcore;


import android.opengl.Matrix;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRPlane;

import java.util.List;
import java.util.ServiceConfigurationError;

public class ARCorePlane extends GVRPlane {
    private Plane mARPlane;
    private float[] mPoseMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];
    private float[] mGVRModelMatrix = new float[16];

    private float[] mGVRCamMatrix = new float[16];
    private float[] mARViewMatrix = new float[16];
    float AR2VR_SCALE;

    private GVRSceneObject mSceneObject = null;

    public ARCorePlane(GVRContext gvrContext) {
        super(gvrContext);
    }

    public void updateAR(Pose pose) {
        pose.toMatrix(mPoseMatrix, 0);

        ar2gvr();
    }

    public float[] getMatrix() {
        return mPoseMatrix;
    }

    private void ar2gvr() {
        Matrix.multiplyMM(mModelViewMatrix, 0, mARViewMatrix, 0, mPoseMatrix, 0);
        Matrix.multiplyMM(mGVRModelMatrix, 0, mGVRCamMatrix, 0, mModelViewMatrix, 0);

        Matrix.scaleM(mGVRModelMatrix, 0, AR2VR_SCALE, AR2VR_SCALE, AR2VR_SCALE);
        mGVRModelMatrix[12] = mGVRModelMatrix[12] * AR2VR_SCALE;
        mGVRModelMatrix[13] = mGVRModelMatrix[13] * AR2VR_SCALE;
        mGVRModelMatrix[14] = mGVRModelMatrix[14] * AR2VR_SCALE;

        getTransform().setModelMatrix(mGVRModelMatrix);
    }

    public void updateFromLookingAt(List<HitResult> hitResult) {
        for (HitResult hit : hitResult) {
            // Check if any plane was hit, and if it was hit inside the plane polygon
            Trackable trackable = hit.getTrackable();
            // Creates an anchor if a plane or an oriented point was hit.
            if ((trackable instanceof Plane
                    && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                    && ((Plane) trackable).getSubsumedBy() == null) {
                mARPlane = (Plane)trackable;
                break;
            }
        }

        if (mARPlane != null && mARPlane.getTrackingState() == TrackingState.TRACKING) {
            setEnable(true);
            update(mARPlane);
        } else {
            setEnable(false);
        }
    }

    public void update(Plane plane) {
        mSceneObject.getTransform().setScale(plane.getExtentX(), 1.0f,
                plane.getExtentZ());
        mARPlane = plane;

        updateAR(plane.getCenterPose());
    }

    public void setARMatrixes(float[] GVRCamMatrix, float[] ARViewMatrix, float SCALE) {
        this.mARViewMatrix = ARViewMatrix;
        this.mGVRCamMatrix = GVRCamMatrix;
        this.AR2VR_SCALE = SCALE;
    }

    public Plane getPlaneAR() {
        return this.mARPlane;
    }
}
