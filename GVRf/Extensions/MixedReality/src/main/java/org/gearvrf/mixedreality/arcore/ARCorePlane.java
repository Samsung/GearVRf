package org.gearvrf.mixedreality.arcore;


import android.opengl.GLES30;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderPass;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRPlane;

import java.util.List;

class ARCorePlane extends GVRPlane {
    private Plane mARPlane;
    private ARCorePose mPose;
    private GVRSceneObject mSceneObject = null;

    public ARCorePlane(GVRContext gvrContext) {
        super(gvrContext);
        mPose = new ARCorePose();
        // TODO: Create method to user set plane object
        mSceneObject = createPlaneMesh(gvrContext, 1.0f, 1.0f);
        setEnable(false);
        addChildObject(mSceneObject);
    }

    @Override
    protected void setCenter(float[] pose) {
        mCenterPose = pose;
    }

    @Override
    protected void setType() {
        // FIXME: Create a enum type
        //mType = mARPlane.getType();
    }

    @Override
    protected void setTrackingState() {
        // FIXME: Create a enum tracking state
        //mTrackingState = mARPlane.getTrackingState();
    }

    @Override
    protected void setWidth(float width) {
        mWidth = width;
    }

    @Override
    protected void setHeight(float height) {
        mHeight = height;
    }


    public void updateFromLookingAt(List<HitResult> hitResult, float[] ARViewMatrix, float[] GVRCamMatrix, float scale) {
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
            update(mARPlane, ARViewMatrix, GVRCamMatrix, scale);
        } else {
            setEnable(false);
        }
    }

    public void update(Plane plane, float[] ARViewMatrix, float[] GVRCamMatrix, float scale) {
        mARPlane = plane;
        mPose.update(plane.getCenterPose(), ARViewMatrix, GVRCamMatrix, scale);
        getTransform().setModelMatrix(mPose.getGVRModelMatrixMatrix());

        setCenter(mPose.getPoseMatrix());
        setType();
        setTrackingState();
        setWidth(mARPlane.getExtentX());
        setHeight(mARPlane.getExtentZ());

        mSceneObject.getTransform().setScale(mWidth, 1.0f, mHeight);
    }

    private GVRSceneObject createPlaneMesh(GVRContext gvrContext,
                                                  float width, float height) {

        GVRSceneObject quad = new GVRSceneObject(gvrContext, width, height);

        GVRMaterial mat = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.Phong.ID);
        mat.setDiffuseColor(0, 0.5f, 0, 0.2f);

        quad.getRenderData().setMaterial(mat);
        quad.getRenderData().setAlphaBlend(true);
        quad.getRenderData().setDrawMode(GLES30.GL_TRIANGLES);
        quad.getRenderData().setCullFace(GVRRenderPass.GVRCullFaceEnum.Front);

        return quad;
    }
}
