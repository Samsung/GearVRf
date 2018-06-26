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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

import java.nio.FloatBuffer;

/**
 * Represents the  current best knowledge of a real-world planar surface.
 */
public abstract class GVRPlane extends GVRSceneObject {
    protected Type mType;
    protected GVRTrackingState mTrackingState;
    protected GVRPlane mParentPlane;
    protected GVRSceneObject mSceneObject;

    protected GVRPlane(GVRContext gvrContext) {
        super(gvrContext);
    }

    /**
     * Set a scene object to represent the plane
     *
     * @param obj
     */
    public void setSceneObject(GVRSceneObject obj) {
        mSceneObject = obj;
        addChildObject(mSceneObject);
    }

    /**
     *
     * @return The scene object that represents the plane
     */
    public GVRSceneObject getSceneObject() {
        return this.mSceneObject;
    }

    /**
     *
     * @return The plane tracking state
     */
    public abstract GVRTrackingState getTrackingState();

    /**
     *
     * @return The plane center pose
     */
    public abstract float[] getCenterPose();

    /**
     *
     * @return The plane type
     */
    public abstract Type getPlaneType();

    /**
     *
     * @return The plane width
     */
    public abstract float getWidth();

    /**
     *
     * @return The plane height
     */
    public abstract float getHeight();

    /**
     *
     * @return The polygon that best represents the plane
     */
    public abstract FloatBuffer getPolygon();

    /**
     *
     * @return The parent plane
     */
    public abstract GVRPlane getParentPlane();

    /**
     * Describes the possible types of planes
     */
    public enum Type {
        HORIZONTAL_DOWNWARD_FACING,
        HORIZONTAL_UPWARD_FACING,
        VERTICAL
    }
}
