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
 * A box collider allows a scene object to be picked
 * when a ray penetrates its bounding box.
 * <p>
 * This method of picking performs a ray-box intersection.
 * The center of the collision box is the center of
 * the scene object it is attached to. If no extents are
 * provided, the dimensions of the box are (1, 1, 1).
 *
 * @see GVRPicker
 * @see GVRSphereCollider
 * @see GVRMeshCollider
 * @see GVRSceneObject#attachComponent(GVRComponent)
 */
public class GVRBoxCollider extends GVRCollider {
    public GVRBoxCollider(GVRContext context) {
        super(context, NativeBoxCollider.ctor());
    }

    public void setHalfExtents(float x, float y, float z) {
        NativeBoxCollider.setHalfExtents(getNative(), x, y, z);
    }
}

class NativeBoxCollider {
    static native long ctor();

    static native void setHalfExtents(long jcollider, float x, float y, float z);
}
