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

import com.google.ar.core.Anchor;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRTrackingState;

/**
 * Represents a ARCore anchor in the scene.
 *
 */
public class ARCoreAnchor extends GVRAnchor {
    private Anchor mAnchor;
    private ARCorePose mPose;

    public ARCoreAnchor(GVRContext gvrContext) {
        super(gvrContext);
        mPose = new ARCorePose();
    }

    /**
     * Sets ARCore anchor
     *
     * @param anchor ARCore Anchor instance
     */
    public void setAnchorAR(Anchor anchor) {
        this.mAnchor = anchor;
    }

    /**
     * @return ARCore Anchor instance
     */
    public Anchor getAnchorAR() {
        return this.mAnchor;
    }

    /**
     * Converts from ARCore world space to GVRf's world space.
     *
     * @param arViewMatrix Phone's camera view matrix.
     * @param vrCamMatrix GVRf Camera matrix.
     * @param scale Scale from AR to GVRf world.
     */
    @Override
    protected void onUpdate(float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        if (mAnchor == null)
            return;

        if (mAnchor.getTrackingState() == TrackingState.TRACKING) {
            setTrackingState(GVRTrackingState.TRACKING);
            mPose.update(mAnchor.getPose(), arViewMatrix, vrCamMatrix, scale);
            getTransform().setModelMatrix(mPose.getPoseMatrix());
        } else if(mAnchor.getTrackingState() == TrackingState.PAUSED){
            setTrackingState(GVRTrackingState.PAUSED);
        } else {
            setTrackingState(GVRTrackingState.STOPPED);
        }

    }
}
