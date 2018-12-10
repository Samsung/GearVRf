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

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;

/**
 * Represents a fixed location and orientation in the real world.
 */
public abstract class GVRAnchor extends GVRBehavior
{
    static private long TYPE_ANCHOR = newComponentType(GVRAnchor.class);

    protected GVRTrackingState mTrackingState;

    protected GVRAnchor(GVRContext GVRContext)
    {
        super(GVRContext);
        mType = getComponentType();
    }

    static public long getComponentType() { return TYPE_ANCHOR; }

    /**
     *
     * @return The anchor tracking state
     */
    public abstract GVRTrackingState getTrackingState();

    /**
     *
     * @return The cloud anchor ID
     */
    public abstract String getCloudAnchorId();

    public abstract float[] getPose();
//    public abstract float[] makeTranslate(float x, float y, float z);
}
