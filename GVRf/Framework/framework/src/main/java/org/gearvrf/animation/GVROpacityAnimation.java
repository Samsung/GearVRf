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

package org.gearvrf.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.joml.Vector4f;

/** Animate the opacity. */
public class GVROpacityAnimation extends GVRMaterialAnimation {

    private final float mInitialOpacity;
    private final float mDeltaOpacity;
    private final float[] mInitialColor;

    /**
     * Animate the {@link GVRMaterial#setOpacity(float) opacity} property.
     * 
     * @param target
     *            {@link GVRMaterial} to animate.
     * @param duration
     *            The animation duration, in seconds.
     * @param opacity
     *            A value from 0 to 1
     */
    public GVROpacityAnimation(GVRMaterial target, float duration, float opacity) {
        super(target, duration);

        if (mMaterial.hasUniform("u_opacity"))
        {
            mInitialOpacity = mMaterial.getOpacity();
            mDeltaOpacity = opacity - mInitialOpacity;
            mInitialColor = null;
        }
        else if (mMaterial.hasUniform("diffuse_color"))
        {
            mInitialOpacity = 1.0f;
            mInitialColor = mMaterial.getVec4("diffuse_color");
            mDeltaOpacity = opacity - mInitialColor[3];
        }
        else
        {
            throw new UnsupportedOperationException("Material must have u_opacity or diffuse_color to animate opacity");
        }

    }

    /**
     * Animate the {@link GVRMaterial#setOpacity(float) opacity} property.
     * 
     * @param target
     *            {@link GVRSceneObject} containing a {@link GVRMaterial} to
     *            animate.
     * @param duration
     *            The animation duration, in seconds.
     * @param opacity
     *            A value from 0 to 1
     */
    public GVROpacityAnimation(GVRSceneObject target, float duration,
            float opacity) {
        this(getMaterial(target), duration, opacity);
    }

    @Override
    protected void animate(GVRHybridObject target, float ratio) {
        float opacity = mDeltaOpacity * ratio;
        if (mInitialColor != null)
        {
            mMaterial.setVec4("diffuse_color", mInitialColor[0],
                    mInitialColor[1], mInitialColor[2],
                    mInitialColor[3] + opacity);
        }
        else
        {
            mMaterial.setOpacity(mInitialOpacity + opacity);
        }
    }
}
