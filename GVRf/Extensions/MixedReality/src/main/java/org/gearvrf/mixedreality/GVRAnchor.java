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

public abstract class GVRAnchor extends GVRSceneObject {
    private GVRTrackingState mTrackingState;

    protected GVRAnchor(GVRContext gvrContext) {
        super(gvrContext);
    }

    /**
     * Set the anchor tracking state
     *
     * @param state
     */
    public void setTrackingState(GVRTrackingState state) { this.mTrackingState = state; }

    /**
     * Attach a scene object to be displayed on this anchor position
     *
     * @param obj
     */
    public void attachSceneObject(GVRSceneObject obj) {
        addChildObject(obj);
    }

    /**
     *
     * @return The anchor tracking state
     */
    public GVRTrackingState getTrackingState() { return this.mTrackingState; }

    /**
     * Update the anchor based on arcore best knowledge of the world
     *
     * @param viewmtx
     * @param gvrmatrix
     * @param scale
     */
    public void update(float[] viewmtx, float[] gvrmatrix, float scale) {
        if (getParent() == null) {
            return;
        }

        onUpdate(viewmtx, gvrmatrix, scale);
    }

    protected abstract void onUpdate(float[] viewmtx, float[] gvrmatrix, float scale);
}
