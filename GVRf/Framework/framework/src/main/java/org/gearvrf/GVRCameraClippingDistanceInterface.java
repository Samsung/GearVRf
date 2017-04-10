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

/**
 * Implement this interface to provide getters and setters for the near and far clipping planes in your custom GVRCamera class.
 * 
 */
public abstract interface GVRCameraClippingDistanceInterface {
    /**
     * @return Distance from the origin to the near clipping plane for this
     *         camera.
     */
    public abstract float getNearClippingDistance();

    /**
     * Sets the distance from the origin to the near clipping plane for this
     * camera.
     * 
     * @param near
     *            Distance to the near clipping plane.
     */
    public abstract void setNearClippingDistance(float near);

    /**
     * @return Distance from the origin to the far clipping plane for this
     *         camera.
     */
    public abstract float getFarClippingDistance();

    /**
     * Sets the distance from the origin to the far clipping plane for this
     * camera.
     * 
     * @param far
     *            Distance to the far clipping plane.
     */
    public abstract void setFarClippingDistance(float far);
}

