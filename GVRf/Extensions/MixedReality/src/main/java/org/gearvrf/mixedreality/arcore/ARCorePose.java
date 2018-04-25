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

import android.opengl.Matrix;

import com.google.ar.core.Pose;

/**
 * Represents a ARCore pose in the scene.
 */
public class ARCorePose {
    // Aux matrix to convert from AR world space to AR cam space.
    private static float[] mModelViewMatrix = new float[16];
    // Represents a AR Pose at GVRf's world space
    private float[] mPoseMatrix = new float[16];

    /**
     * Returns the ARCore Pose matrix in GVRf's world space
     *
     * @return The pose matrix in GVRf's world space.
     */
    public float[] getPoseMatrix() {
        return mPoseMatrix;
    }

    /**
     * Converts from ARCore world space to GVRf's world space
     *
     * @param pose AR Core Pose instance
     * @param arViewMatrix Phone's camera view matrix
     * @param vrCamMatrix GVRf Camera matrix
     * @param scale Scale from AR to GVRf world
     */
    public void update(Pose pose, float[] arViewMatrix, float[] vrCamMatrix, float scale) {
        pose.toMatrix(mPoseMatrix, 0);

        ar2gvr(arViewMatrix, vrCamMatrix, scale);
    }

    /**
     * Converts from AR world space to GVRf world space.
     */
    private void ar2gvr(float[] ARViewMatrix, float[] GVRCamMatrix, float scale) {
        // From AR world space to AR camera space.
        Matrix.multiplyMM(mModelViewMatrix, 0, ARViewMatrix, 0, mPoseMatrix, 0);
        // From AR Camera space to GVRf world space
        Matrix.multiplyMM(mPoseMatrix, 0, GVRCamMatrix, 0, mModelViewMatrix, 0);

        // Real world scale
        Matrix.scaleM(mPoseMatrix, 0, scale, scale, scale);
        mPoseMatrix[12] = mPoseMatrix[12] * scale;
        mPoseMatrix[13] = mPoseMatrix[13] * scale;
        mPoseMatrix[14] = mPoseMatrix[14] * scale;
    }
}
