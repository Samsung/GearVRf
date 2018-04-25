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


public abstract class GVRPlane extends GVRSceneObject{

    protected int mType;
    private GVRTrackingState mTrackingState;
    protected float[] mCenterPose = new float[16];
    protected float mWidth;
    protected float mHeight;
    protected GVRSceneObject mSceneObject;


    public GVRPlane(GVRContext gvrContext) {
        super(gvrContext);
    }

    public void setTrackingState(GVRTrackingState state) {
        this.mTrackingState = state;
    }

    protected abstract void setCenter(float[] pose);

    protected abstract void setType();

    protected abstract void setWidth(float width);

    protected abstract void setHeight(float height);

    public void setSceneObject(GVRSceneObject obj) {
        mSceneObject = obj;
        addChildObject(mSceneObject);
    }

    public GVRTrackingState getTrackingState() {
        return this.mTrackingState;
    }

    public float[] getCenterPose() {
        return this.mCenterPose;
    }

    public int getPlaneType() {
        return this.mType;
    }

    public float getWidth() {
        return this.mWidth;
    }

    public float getHeight() {
        return this.mHeight;
    }

    public void update(float[] viewmtx, float[] gvrmatrix, float scale) {
        onUpdate(viewmtx, gvrmatrix, scale);

        if (mSceneObject != null) {
            mSceneObject.getTransform().setScale(mWidth * 0.95f, mHeight * 0.95f, 1.0f);
        }
    }

    protected abstract void onUpdate(float[] viewmtx, float[] gvrmatrix, float scale);
}
