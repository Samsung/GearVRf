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

package org.gearvrf;

/** A version of {@link GVRCamera} with a custom projection matrix. */
public class GVRCustomCamera extends GVRCamera {
    public GVRCustomCamera(GVRContext gvrContext) {
        super(gvrContext, NativeCustomCamera.ctor());
    }

    /**
     * Set the custom projection matrix with individual matrix elements.
     */
    public void setProjectionMatrix(float x1, float y1, float z1, float w1,
            float x2, float y2, float z2, float w2, float x3, float y3,
            float z3, float w3, float x4, float y4, float z4, float w4) {
        NativeCustomCamera.setProjectionMatrix(getNative(), x1, y1, z1, w1, x2,
                y2, z2, w2, x3, y3, z3, w3, x4, y4, z4, w4);
    }

    /**
     * @return Distance from the origin to the near clipping plane.
     * Note: this is a no-op for this class and will return 0.0f.
     */
    @Override
    public float getNearClippingDistance() {
        return 0.0f;
    }

    /**
     * Sets the distance from the origin to the near clipping plane.
     * Note: this is a no-op for this class.
     * 
     * @param near
     *            Distance to the near clipping plane.
     */
    @Override
    public void setNearClippingDistance(float near) {
        return;
    }

    /**
     * @return Distance from the origin to the far clipping plane.
     * Note: this is a no-op for this class and will return 0.0f.
     */
    @Override
    public float getFarClippingDistance() {
        return 0.0f;
    }

    /**
     * Sets the distance from the origin to the far clipping plane.
     * Note: this is a no-op for this class.
     * 
     * @param far
     *            Distance to the far clipping plane.
     */
    @Override
    public void setFarClippingDistance(float far) {
        return;
    }
}

class NativeCustomCamera {
    static native long ctor();

    static native void setProjectionMatrix(long ptr, float x1, float y1,
            float z1, float w1, float x2, float y2, float z2, float w2,
            float x3, float y3, float z3, float w3, float x4, float y4,
            float z4, float w4);
}
