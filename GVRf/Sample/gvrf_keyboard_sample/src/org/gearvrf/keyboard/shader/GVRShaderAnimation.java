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

package org.gearvrf.keyboard.shader;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRMaterialAnimation;

/** Animate the opacity. */
public class GVRShaderAnimation extends GVRMaterialAnimation {

    private final float mInitialValue;
    private final float mDeltaValue;
    private final String mKey;

    /**
     * Animate the {@link GVRMaterial#setFloat(key,float) blur} property.
     * 
     * @param target {@link GVRMaterial} to animate.
     * @param duration The animation duration, in seconds.
     * @param opacity A value from 0 to 1
     */
    public GVRShaderAnimation(GVRMaterial target, String key, float duration, float finalValue) {
        super(target, duration);
        mKey = key;
        mInitialValue = mMaterial.getFloat(mKey);
        mDeltaValue = finalValue - mInitialValue;

    }

    /**
     * Animate the {@link GVRMaterial#setFloat(key,float) blur} property.
     * 
     * @param target {@link GVRSceneObject} containing a {@link GVRMaterial} to
     *            animate.
     * @param duration The animation duration, in seconds.
     * @param opacity A value from 0 to 1
     */
    public GVRShaderAnimation(GVRSceneObject target, String key, float duration,
            float finalValue) {
        this(getMaterial(target), key, duration, finalValue);
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {
        mMaterial.setFloat(mKey, mInitialValue + mDeltaValue * ratio);
    }
}
