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

package org.gearvrf.mixedreality;

/**
 * Represents an intersection between a ray and estimated a real world geometry.
 */
public class GVRHitResult {
    private float[] mPose;
    private float mDistance;
    private GVRPlane mPlane;

    /**
     * Set the pose of hit
     *
     * @param pose
     */
    public void setPose(float[] pose) {
        this.mPose = pose;
    }

    /**
     * Set the distance of hit
     *
     * @param distance
     */
    public void setDistance(float distance) {
        this.mDistance = distance;
    }


    /**
     * Set the plane hit
     *
     * @param plane
     */
    public void setPlane(GVRPlane plane) {
        this.mPlane = plane;
    }

    /**
     *
     * @return The hit pose
     */
    public float[] getPose() {
        return mPose;
    }

    /**
     *
     * @return The distance of hit pose
     */
    public float getDistance() {
        return mDistance;
    }

    /**
     *
     * @return The plane hit
     */
    public GVRPlane getPlane() {
        return mPlane;
    }
}
