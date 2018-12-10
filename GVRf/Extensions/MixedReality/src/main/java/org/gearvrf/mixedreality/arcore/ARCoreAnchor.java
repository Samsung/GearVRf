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
import com.google.ar.core.Pose;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRTrackingState;

/**
 * Represents a ARCore anchor in the scene.
 *
 */
public class ARCoreAnchor extends GVRAnchor {
    private Anchor mAnchor;
    private ARCorePose mPose;

    protected ARCoreAnchor(GVRContext gvrContext) {
        super(gvrContext);
        mPose = new ARCorePose();
    }

    /**
     * Sets ARCore anchor
     *
     * @param anchor ARCore Anchor instance
     */
    protected void setAnchorAR(Anchor anchor) {
        this.mAnchor = anchor;
    }

    /**
     * Set the anchor tracking state
     *
     * @param state
     */
    protected void setTrackingState(GVRTrackingState state) { mTrackingState = state; }

    /**
     * @return ARCore Anchor instance
     */
    protected Anchor getAnchorAR() {
        return this.mAnchor;
    }

    @Override
    public GVRTrackingState getTrackingState() {
        return mTrackingState;
    }

    @Override
    public String getCloudAnchorId() {
        return mAnchor.getCloudAnchorId();
    }
/*
    @Override
    public float[] makeTranslate(float x, float y, float z) {
        float[] newPose = new float[16];
        Pose pose = mAnchor.getPose().compose(Pose.makeTranslation(x, y, z));
        pose.toMatrix(newPose, 0);
        return newPose;
    }
*/
    /**
     * Update the anchor based on arcore best knowledge of the world
     *
     * @param scale
     */
    protected void update(float scale) {
        // Updates only when the plane is in the scene
        GVRSceneObject owner = getOwnerObject();

        if ((owner != null) && isEnabled() && owner.isEnabled())
        {
            convertFromARtoVRSpace(scale);
        }
    }

    /**
     * Converts from ARCore world space to GVRf's world space.
     *
     * @param scale Scale from AR to GVRf world.
     */
    protected void convertFromARtoVRSpace(float scale) {
        mPose.update(mAnchor.getPose(), scale);
        getTransform().setModelMatrix(mPose.getPoseMatrix());
    }

    public float[] getPose()
    {
        return mPose.getPoseMatrix();
    }
}
