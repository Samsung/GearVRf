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

/** A {@link GVRCamera camera} with a perspective projection. */
public class GVRPerspectiveCamera extends GVRCamera implements GVRCameraClippingDistanceInterface {

    final static float TO_DEGREES = (float) (180.0f/Math.PI);
    final static float TO_RADIANS = 1/TO_DEGREES;

    /**
     * Constructor.
     * 
     * @param gvrContext
     *            {@link GVRContext} the app is using.
     */
    public GVRPerspectiveCamera(GVRContext gvrContext) {
        super(gvrContext, NativePerspectiveCamera.ctor());
    }

    /**
     * @return The global default 'y' field of view, in degrees.
     */
    public static float getDefaultFovY() {
        return NativePerspectiveCamera.getDefaultFovY() * TO_DEGREES;
    }

    /**
     * Sets the global default 'y' field of view, in degrees.
     * 
     * @param fovY
     *            The default 'y' field of view, in degrees.
     */
    public static void setDefaultFovY(float fovY) {
        NativePerspectiveCamera.setDefaultFovY(fovY * TO_RADIANS);
    }

    /**
     * @return The global default aspect ratio (width/height).
     */
    public static float getDefaultAspectRatio() {
        return NativePerspectiveCamera.getDefaultAspectRatio();
    }

    /**
     * Sets the global default aspect ratio (width/height).
     * 
     * @param aspectRatio
     *            The default aspect ratio.
     */
    public static void setDefaultAspectRatio(float aspectRatio) {
        NativePerspectiveCamera.setDefaultAspectRatio(aspectRatio);
    }

    /**
     * @return Distance from the origin to the near clipping plane for this
     *         camera.
     */
    @Override
    public float getNearClippingDistance() {
        return NativePerspectiveCamera.getNearClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the near clipping plane for this
     * camera.
     * 
     * @param near
     *            Distance to the near clipping plane.
     */
    @Override
    public void setNearClippingDistance(float near) {
        NativePerspectiveCamera.setNearClippingDistance(getNative(), near);
    }

    /**
     * @return Distance from the origin to the far clipping plane for this
     *         camera.
     */
    @Override
    public float getFarClippingDistance() {
        return NativePerspectiveCamera.getFarClippingDistance(getNative());
    }

    /**
     * Sets the distance from the origin to the far clipping plane for this
     * camera.
     * 
     * @param far
     *            Distance to the far clipping plane.
     */
    @Override
    public void setFarClippingDistance(float far) {
        NativePerspectiveCamera.setFarClippingDistance(getNative(), far);
    }

    /**
     * @return The 'y' field of view, in degrees, for this camera.
     */
    public float getFovY() {
        return NativePerspectiveCamera.getFovY(getNative()) * TO_DEGREES;
    }

    /**
     * Sets the 'y' field of view, in degrees, for this camera.
     * 
     * @param fovY
     *            The 'y' field of view, in degrees.
     */
    public void setFovY(float fovY) {
        NativePerspectiveCamera.setFovY(getNative(), fovY * TO_RADIANS);
    }

    /**
     * @return The aspect ratio (width/height) for this camera.
     */
    public float getAspectRatio() {
        return NativePerspectiveCamera.getAspectRatio(getNative());
    }

    /**
     * Sets the aspect ratio (width/height) for this camera.
     * 
     * @param aspectRatio
     *            The aspect ratio.
     */
    public void setAspectRatio(float aspectRatio) {
        NativePerspectiveCamera.setAspectRatio(getNative(), aspectRatio);
    }
}

class NativePerspectiveCamera {
    static native long ctor();

    static native float getDefaultFovY();

    static native void setDefaultFovY(float fovY);

    static native float getDefaultAspectRatio();

    static native void setDefaultAspectRatio(float aspectRatio);

    static native float getNearClippingDistance(long camera);

    static native void setNearClippingDistance(long camera, float near);

    static native float getFarClippingDistance(long camera);

    static native void setFarClippingDistance(long camera, float far);

    static native float getFovY(long camera);

    static native void setFovY(long camera, float fovY);

    static native float getAspectRatio(long camera);

    static native void setAspectRatio(long camera, float aspectRatio);
}
