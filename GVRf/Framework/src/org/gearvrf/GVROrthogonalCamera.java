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
public class GVROrthogonalCamera extends GVRCamera {
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
        return NativeOrthogonalCamera.getLeftClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the left clipping plane.
     * 
     * @param left
     *            Distance to the left clipping plane.
     */
    public void setLeftClippingDistance(float left) {
        NativeOrthogonalCamera.setLeftClippingDistance(getPtr(), left);
    }

    /**
     * @return Distance from the origin to the right clipping plane.
     */
    public float getRightClippingDistance() {
        return NativeOrthogonalCamera.getRightClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the right clipping plane.
     * 
     * @param right
     *            Distance to the right clipping plane.
     */
    public void setRightClippingDistance(float right) {
        NativeOrthogonalCamera.setRightClippingDistance(getPtr(), right);
    }

    /**
     * @return Distance from the origin to the bottom clipping plane.
     */
    public float getBottomClippingDistance() {
        return NativeOrthogonalCamera.getBottomClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the bottom clipping plane.
     * 
     * @param bottom
     *            Distance to the bottom clipping plane.
     */
    public void setBottomClippingDistance(float bottom) {
        NativeOrthogonalCamera.setBottomClippingDistance(getPtr(), bottom);
    }

    /**
     * @return Distance from the origin to the top clipping plane.
     */
    public float getTopClippingDistance() {
        return NativeOrthogonalCamera.getTopClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the top clipping plane.
     * 
     * @param top
     *            Distance to the top clipping plane.
     */
    public void setTopClippingDistance(float top) {
        NativeOrthogonalCamera.setTopClippingDistance(getPtr(), top);
    }

    /**
     * @return Distance from the origin to the near clipping plane.
     */
    public float getNearClippingDistance() {
        return NativeOrthogonalCamera.getNearClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the near clipping plane.
     * 
     * @param near
     *            Distance to the near clipping plane.
     */
    public void setNearClippingDistance(float near) {
        NativeOrthogonalCamera.setNearClippingDistance(getPtr(), near);
    }

    /**
     * @return Distance from the origin to the far clipping plane.
     */
    public float getFarClippingDistance() {
        return NativeOrthogonalCamera.getFarClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the far clipping plane.
     * 
     * @param far
     *            Distance to the far clipping plane.
     */
    public void setFarClippingDistance(float far) {
        NativeOrthogonalCamera.setFarClippingDistance(getPtr(), far);
    }
}

class NativeOrthogonalCamera {
    public static native long ctor();

    public static native float getLeftClippingDistance(long camera);

    public static native void setLeftClippingDistance(long camera, float left);

    public static native float getRightClippingDistance(long camera);

    public static native void setRightClippingDistance(long camera, float right);

    public static native float getBottomClippingDistance(long camera);

    public static native void setBottomClippingDistance(long camera,
            float bottom);

    public static native float getTopClippingDistance(long camera);

    public static native void setTopClippingDistance(long camera, float top);

    public static native float getNearClippingDistance(long camera);

    public static native void setNearClippingDistance(long camera, float near);

    public static native float getFarClippingDistance(long camera);

    public static native void setFarClippingDistance(long camera, float far);
}
