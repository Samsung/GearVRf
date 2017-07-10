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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.gearvrf.utility.Threads;

/**
 * Holds any number of {@linkplain GVRCollider} instances
 * 
 * Ray casting is computationally expensive. Rather than probing the entire
 * scene graph, GVRF requires you to mark parts of the scene as "pickable" by
 * adding their meshes (or, more cheaply if less precisely, their
 * {@linkplain GVRMesh#getBoundingBox() bounding box}) to a
 * {@link GVRCollider};
 * {@linkplain GVRSceneObject#attachCollider(GVRCollider)}
 * attaching that collider to a {@linkplain GVRSceneObject scene object}; and
 * setting the collider's {@linkplain #setEnable(boolean) enabled flag.}
 *
 * One can use this class to specify multiple colliders for a
 * single {@link GVRSceneObject}.
 *
 */
public class GVRColliderGroup extends GVRCollider {
    private final List<GVRCollider> colliders = new ArrayList<GVRCollider>();

    static GVRCollider lookup(GVRContext gvrContext, long nativePointer) {
        return GVRCollider.lookup(nativePointer);
    }

    /**
     * Constructor
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVRColliderGroup(GVRContext gvrContext) {
        this(gvrContext, NativeColliderGroup.ctor());
    }

    public GVRColliderGroup(GVRContext gvrContext, GVRSceneObject owner) {
        this(gvrContext, NativeColliderGroup.ctor());
        setOwnerObject(owner);
    }
    
    private GVRColliderGroup(GVRContext gvrContext, long nativePointer) {
        super(gvrContext, nativePointer);
    }        
        
    
    /**
     * Is this collider-group enabled?
     * 
     * If this collider-group is disabled, then picking will <b>not</b> occur against
     * its {@link GVRCollider}s.
     * @return true if enabled, false otherwise.
     * @deprecated use GVRComponent.isEnabled()
     */
    public boolean getEnable() {
        return super.isEnabled();
    }


    /**
     * Add a {@link GVRCollider} to this collider-group
     * 
     * @param collider
     *            The {@link GVRCollider} to add
     */
    public void addCollider(GVRCollider collider) {
        colliders.add(collider);
        NativeColliderGroup.addCollider(getNative(), collider.getNative());
    }


    /**
     * Remove a {@link GVRCollider} from this collider-group.
     * 
     * No exception is thrown if the collider is not held by this collider-group.
     *
     * @param collider
     *            The {@link GVRCollider} to remove
     * 
     */
    public void removeCollider(GVRCollider collider) {
        colliders.remove(collider);
        NativeColliderGroup.removeCollider(getNative(),
                collider.getNative());
    }

    /**
     * Get the x, y, z of the point of where the hit occurred in model space
     * 
     * @return Three floats representing the x, y, z hit point.
     * @see #getHit()
     */
    public float[] getHit()
    {
        return NativeColliderGroup.getHit(getNative());
    }

}

class NativeColliderGroup {
    static native long ctor();

    static native float[] getHit(long colliderGroup);
    
    static native void addCollider(long colliderGroup, long collider);

    static native void removeCollider(long colliderGroup, long collider);
}
