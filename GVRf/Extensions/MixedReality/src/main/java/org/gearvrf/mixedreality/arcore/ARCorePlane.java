/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.mixedreality.arcore;

import org.joml.Matrix4f;
import android.support.annotation.NonNull;

import com.google.ar.core.Plane;
import com.google.ar.core.Pose;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;

import java.nio.FloatBuffer;


class ARCorePlane extends GVRPlane {
    private Plane mARPlane;
    private ARCorePose mPose;

    protected ARCorePlane(GVRContext gvrContext, Plane plane) {
        super(gvrContext);
        mPose = new ARCorePose();
        mARPlane = plane;

        if (mARPlane.getType() == Plane.Type.HORIZONTAL_DOWNWARD_FACING) {
            mPlaneType = Type.HORIZONTAL_DOWNWARD_FACING;
        }
        else if (mARPlane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING) {
            mPlaneType = Type.HORIZONTAL_UPWARD_FACING;
        }
        else {
            mPlaneType = Type.VERTICAL;
        }
    }

    /**
     * Set the plane tracking state
     *
     * @param state
     */
    protected void setTrackingState(GVRTrackingState state) {
        mTrackingState = state;
    }

    /**
     * Set the parent plane (only when plane is merged)
     *
     * @param plane
     */
    protected void setParentPlane(GVRPlane plane) {
        mParentPlane = plane;
    }

    @Override
    public GVRTrackingState getTrackingState() {
        return mTrackingState;
    }

    @Override
    public void getCenterPose(@NonNull float[] poseOut) {
        if(poseOut.length != 16 ){
            throw new IllegalArgumentException("Array must be 16");
        }
        mARPlane.getCenterPose().toMatrix(poseOut, 0);
    }

    @Override
    public float getWidth() {
        return mARPlane.getExtentX();
    }

    @Override
    public float getHeight() {
        return mARPlane.getExtentZ();
    }

    @Override
    public FloatBuffer getPolygon() {
        return mARPlane.getPolygon();
    }

    @Override
    public GVRPlane getParentPlane() {
        return mParentPlane;
    }

    @Override
    public boolean isPoseInPolygon(float[] pose) {

        float[] translation = new float[3];
        float[] rotation = new float[4];
        float[] arPose;

        arPose = pose.clone();

        ARCoreSession.gvr2ar(arPose);
        ARCoreSession.convertMatrixPoseToVector(arPose, translation, rotation);

        return mARPlane.isPoseInPolygon(new Pose(translation, rotation));
    }

    /**
     * Update the plane based on arcore best knowledge of the world
     *
     * @param scale
     */
    protected void update(float scale) {
        GVRSceneObject owner = getOwnerObject();
        if (isEnabled() && (owner != null) && owner.isEnabled())
        {
            float w = getWidth();
            float h = getHeight();
            mPose.update(mARPlane.getCenterPose(), scale);
            Matrix4f m = new Matrix4f();
            m.set(mPose.getPoseMatrix());
            m.scaleLocal(w * 0.95f, h * 0.95f, 1.0f);
            owner.getTransform().setModelMatrix(m);
        }
    }
    
    /**
     * Converts from ARCore world space to GVRf's world space.
     *
     * @param scale Scale from AR to GVRf world
     */
    private void convertFromARtoVRSpace(float scale) {
        mPose.update(mARPlane.getCenterPose(), scale);
        getTransform().setModelMatrix(mPose.getPoseMatrix());
    }
}
