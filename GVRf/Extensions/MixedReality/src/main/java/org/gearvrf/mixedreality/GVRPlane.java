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


public abstract class GVRPlane extends GVRSceneObject {
    protected Type mType;
    protected GVRTrackingState mTrackingState;
    protected float[] mCenterPose = new float[16];
    protected float mWidth;
    protected float mHeight;
    protected GVRPlane mParentPlane;
    protected GVRSceneObject mSceneObject;


    protected GVRPlane(GVRContext gvrContext) {
        super(gvrContext);
    }

    /**
     * Set the anchor tracking state
     *
     * @param state
     */
    protected abstract void setTrackingState(GVRTrackingState state);

    protected abstract void setType(Type type);

    protected abstract void setWidth(float width);

    protected abstract void setHeight(float height);

    protected abstract void setParentPlane(GVRPlane plane);

    /**
     * Set the scene object to represent the plane
     *
     * @param obj
     */
    public void setSceneObject(GVRSceneObject obj) {
        mSceneObject = obj;
        addChildObject(mSceneObject);
    }

    /**
     *
     * @return The plane tracking state
     */
    public GVRTrackingState getTrackingState() {
        return this.mTrackingState;
    }

    /**
     *
     * @return The plane center pose
     */
    public float[] getCenterPose() {
        return this.mCenterPose;
    }

    /**
     *
     * @return The plane type
     */
    public Type getPlaneType() {
        return this.mType;
    }

    /**
     *
     * @return The plane width
     */
    public float getWidth() {
        return this.mWidth;
    }

    /**
     *
     * @return The plane height
     */
    public float getHeight() {
        return this.mHeight;
    }

    /**
     *
     * @return The parent plane
     */
    public GVRPlane getParentPlane() {
        return this.mParentPlane;
    }

    /**
     *
     * @return The scene object that represents the plane
     */
    public GVRSceneObject getSceneObject() {
        return this.mSceneObject;
    }

    public void update(float[] viewmtx, float[] gvrmatrix, float scale) {
        onUpdate(viewmtx, gvrmatrix, scale);

        if (mSceneObject != null) {
            mSceneObject.getTransform().setScale(mWidth * 0.95f, mHeight * 0.95f, 1.0f);
        }
    }

    protected abstract void onUpdate(float[] viewmtx, float[] gvrmatrix, float scale);

    public enum Type {
        HORIZONTAL_DOWNWARD_FACING,
        HORIZONTAL_UPWARD_FACING
    }
}
