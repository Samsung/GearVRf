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
public class GVRPerspectiveCamera extends GVRCamera {
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
        return NativePerspectiveCamera.getDefaultFovY();
    }

    /**
     * Sets the global default 'y' field of view, in degrees.
     * 
     * @param fovY
     *            The default 'y' field of view, in degrees.
     */
    public static void setDefaultFovY(float fovY) {
        NativePerspectiveCamera.setDefaultFovY(fovY);
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
    public float getNearClippingDistance() {
        return NativePerspectiveCamera.getNearClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the near clipping plane for this
     * camera.
     * 
     * @param near
     *            Distance to the near clipping plane.
     */
    public void setNearClippingDistance(float near) {
        NativePerspectiveCamera.setNearClippingDistance(getPtr(), near);
    }

    /**
     * @return Distance from the origin to the far clipping plane for this
     *         camera.
     */
    public float getFarClippingDistance() {
        return NativePerspectiveCamera.getFarClippingDistance(getPtr());
    }

    /**
     * Sets the distance from the origin to the far clipping plane for this
     * camera.
     * 
     * @param far
     *            Distance to the far clipping plane.
     */
    public void setFarClippingDistance(float far) {
        NativePerspectiveCamera.setFarClippingDistance(getPtr(), far);
    }

    /**
     * @return The 'y' field of view, in degrees, for this camera.
     */
    public float getFovY() {
        return NativePerspectiveCamera.getFovY(getPtr());
    }

    /**
     * Sets the 'y' field of view, in degrees, for this camera.
     * 
     * @param fovY
     *            The 'y' field of view, in degrees.
     */
    public void setFovY(float fovY) {
        NativePerspectiveCamera.setFovY(getPtr(), fovY);
    }

    /**
     * @return The aspect ratio (width/height) for this camera.
     */
    public float getAspectRatio() {
        return NativePerspectiveCamera.getAspectRatio(getPtr());
    }

    /**
     * Sets the aspect ratio (width/height) for this camera.
     * 
     * @param aspectRatio
     *            The aspect ratio.
     */
    public void setAspectRatio(float aspectRatio) {
        NativePerspectiveCamera.setAspectRatio(getPtr(), aspectRatio);
    }
}

class NativePerspectiveCamera {
    public static native long ctor();

    public static native float getDefaultFovY();

    public static native void setDefaultFovY(float fovY);

    public static native float getDefaultAspectRatio();

    public static native void setDefaultAspectRatio(float aspectRatio);

    public static native float getNearClippingDistance(long camera);

    public static native void setNearClippingDistance(long camera, float near);

    public static native float getFarClippingDistance(long camera);

    public static native void setFarClippingDistance(long camera, float far);

    public static native float getFovY(long camera);

    public static native void setFovY(long camera, float fovY);

    public static native float getAspectRatio(long camera);

    public static native void setAspectRatio(long camera, float aspectRatio);
}