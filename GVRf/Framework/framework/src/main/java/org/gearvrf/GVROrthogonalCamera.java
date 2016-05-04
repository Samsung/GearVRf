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

/** A {@link GVRCamera camera} with an orthogonal projection. */
public class GVROrthogonalCamera extends GVRCamera implements GVRCameraClippingDistanceInterface {
    /**
     * Constructor.
     * 
     * @param gvrContext
     *            {@link GVRContext} the app is using.
     */
    public GVROrthogonalCamera(GVRContext gvrContext) {
        super(gvrContext, NativeOrthogonalCamera.ctor());
    }

    /**
     * @return Distance from the origin to the left clipping plane.
     */
    public float getLeftClippingDistance() {
        return NativeOrthogonalCamera.getLeftClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the left clipping plane.
     * 
     * @param left
     *            Distance to the left clipping plane.
     */
    public void setLeftClippingDistance(float left) {
        NativeOrthogonalCamera.setLeftClippingDistance(getNative(), left);
    }

    /**
     * @return Distance from the origin to the right clipping plane.
     */
    public float getRightClippingDistance() {
        return NativeOrthogonalCamera.getRightClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the right clipping plane.
     * 
     * @param right
     *            Distance to the right clipping plane.
     */
    public void setRightClippingDistance(float right) {
        NativeOrthogonalCamera.setRightClippingDistance(getNative(), right);
    }

    /**
     * @return Distance from the origin to the bottom clipping plane.
     */
    public float getBottomClippingDistance() {
        return NativeOrthogonalCamera.getBottomClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the bottom clipping plane.
     * 
     * @param bottom
     *            Distance to the bottom clipping plane.
     */
    public void setBottomClippingDistance(float bottom) {
        NativeOrthogonalCamera.setBottomClippingDistance(getNative(), bottom);
    }

    /**
     * @return Distance from the origin to the top clipping plane.
     */
    public float getTopClippingDistance() {
        return NativeOrthogonalCamera.getTopClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the top clipping plane.
     * 
     * @param top
     *            Distance to the top clipping plane.
     */
    public void setTopClippingDistance(float top) {
        NativeOrthogonalCamera.setTopClippingDistance(getNative(), top);
    }

    /**
     * @return Distance from the origin to the near clipping plane.
     */
    @Override
    public float getNearClippingDistance() {
        return NativeOrthogonalCamera.getNearClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the near clipping plane.
     * 
     * @param near
     *            Distance to the near clipping plane.
     */
    @Override
    public void setNearClippingDistance(float near) {
        NativeOrthogonalCamera.setNearClippingDistance(getNative(), near);
    }

    /**
     * @return Distance from the origin to the far clipping plane.
     */
    @Override
    public float getFarClippingDistance() {
        return NativeOrthogonalCamera.getFarClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the far clipping plane.
     * 
     * @param far
     *            Distance to the far clipping plane.
     */
    @Override
    public void setFarClippingDistance(float far) {
        NativeOrthogonalCamera.setFarClippingDistance(getNative(), far);
    }
}

class NativeOrthogonalCamera {
    static native long ctor();

    static native float getLeftClippingDistance(long camera);

    static native void setLeftClippingDistance(long camera, float left);

    static native float getRightClippingDistance(long camera);

    static native void setRightClippingDistance(long camera, float right);

    static native float getBottomClippingDistance(long camera);

    static native void setBottomClippingDistance(long camera, float bottom);

    static native float getTopClippingDistance(long camera);

    static native void setTopClippingDistance(long camera, float top);

    static native float getNearClippingDistance(long camera);

    static native void setNearClippingDistance(long camera, float near);

    static native float getFarClippingDistance(long camera);

    static native void setFarClippingDistance(long camera, float far);
}
