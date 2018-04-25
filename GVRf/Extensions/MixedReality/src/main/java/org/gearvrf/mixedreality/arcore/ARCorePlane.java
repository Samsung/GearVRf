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

import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.GVRTrackingState;


class ARCorePlane extends GVRPlane {
    private Plane mARPlane;
    private ARCorePose mPose;

    public ARCorePlane(GVRContext gvrContext) {
        super(gvrContext);
        mPose = new ARCorePose();
        setEnable(false);
    }

    /**
     * Sets ARCore plane
     *
     * @param plane ARCore Plane instance
     */
    public void setARPlane(Plane plane) {
        mARPlane = plane;

        setEnable(plane != null && plane.getTrackingState() == TrackingState.TRACKING
                && plane.getSubsumedBy() == null);
    }

    /**
     * @return ARCore Plane instance
     */
    public Plane getARPlane() {
        return mARPlane;
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
    protected void setWidth(float width) {
        mWidth = width;
    }

    @Override
    protected void setHeight(float height) {
        mHeight = height;
    }

    /**
     * Converts from ARCore world space to GVRf's world space.
     *
     * @param arViewMatrix Phone's camera view matrix
     * @param vrCamMatrix GVRf Camera matrix
     * @param scale Scale from AR to GVRf world
     */
    @Override
    protected void onUpdate(float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        if (mARPlane == null) {
            setEnable(false);
        }

        if (mARPlane.getTrackingState() != TrackingState.TRACKING
                || mARPlane.getSubsumedBy() != null) {
            setTrackingState(GVRTrackingState.STOPPED);
            mARPlane = null;
            setEnable(false);
        } else {
            setTrackingState(GVRTrackingState.TRACKING);
            mPose.update(mARPlane.getCenterPose(), arViewMatrix, vrCamMatrix, scale);

            getTransform().setModelMatrix(mPose.getPoseMatrix());
            setWidth(mARPlane.getExtentX());
            setHeight(mARPlane.getExtentZ());
            setEnable(true);
        }

    }

}
