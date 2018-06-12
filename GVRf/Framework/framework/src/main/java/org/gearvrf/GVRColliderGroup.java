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
import java.util.Iterator;
import java.util.List;

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
 */
public class GVRColliderGroup extends GVRCollider implements GVRComponent.IComponentGroup<GVRCollider>
{
    GVRComponent.Group<GVRCollider> mGroup = new GVRComponent.Group<GVRCollider>();

    /**
     * Constructor
     * 
     * @param gvrContext Current {@link GVRContext}
     */
    public GVRColliderGroup(GVRContext gvrContext)
    {
        super(gvrContext, NativeColliderGroup.ctor());
    }

    /**
     * Add a {@link GVRCollider} to this collider-group
     * 
     * @param collider The {@link GVRCollider} to add
     */
    public void addCollider(GVRCollider collider)
    {
        mGroup.addChild(collider);
        NativeComponent.addChildComponent(getNative(), collider.getNative());
    }

    public void addChildComponent(GVRCollider collider)
    {
        mGroup.addChild(collider);
        NativeComponent.addChildComponent(getNative(), collider.getNative());
    }

    public int getSize()
    {
        return mGroup.getSize();
    }

    public GVRCollider getChildAt(int index)
    {
        return mGroup.getChildAt(index);
    }

    public Iterator<GVRCollider> iterator()
    {
        return mGroup.iterator();
    }

    /**
     * Remove a {@link GVRCollider} from this collider-group.
     * 
     * No exception is thrown if the collider is not held by this collider-group.
     *
     * @param collider The {@link GVRCollider} to remove
     * 
     */
    public void removeCollider(GVRCollider collider)
    {
        mGroup.removeChild(collider);
        NativeComponent.removeChildComponent(getNative(), collider.getNative());
    }

    public void removeChildComponent(GVRCollider collider)
    {
        mGroup.removeChild(collider);
        NativeComponent.removeChildComponent(getNative(), collider.getNative());
    }
}

class NativeColliderGroup
{
    static native long ctor();
}