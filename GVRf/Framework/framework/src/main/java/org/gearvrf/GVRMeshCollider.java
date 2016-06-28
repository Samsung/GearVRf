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
 * Represents collision geometry that is a mesh.
 * 
 * A {@link GVRCollider} is something that is being pointed at by a picking
 * ray. {@linkplain GVRCollider Colliders} are attached to
 * {@link GVRSceneObject scene objects.} The {@link GVRPicker} will return an
 * array of GVRColliders: you use {@link GVRCollider#getOwnerObject()} to retrieve the scene object.
 * 
 * <p>
 * A MeshCollider holds the {@link GVRMesh} that the picking ray will be
 * tested against. If no mesh is specified, it will use the mesh
 * attached to the GVRSceneObject that owns it.
 * 
 * You do not need to wait for the mesh to load before attaching the collider.
 * If the scene object that owns the mesh collider does not have a mesh and
 * the mesh collider doesn't have one, the scene object will not be pickable.
 */
public class GVRMeshCollider extends GVREyePointee {
    private GVRMesh mMesh;

    /**
     * Constructor to make mesh collider and attach a mesh.
     * 
     * When the mesh is complicated, it will be cheaper - though less accurate -
     * to use the bounding box instead of the mesh.
     * 
     * @param gvrContext
     *            The {@link GVRContext} used by the app.
     * 
     * @param mesh
     *            The {@link GVRMesh} that the picking ray will test against.
     */
    public GVRMeshCollider(GVRContext gvrContext, GVRMesh mesh) {
        super(gvrContext, NativeMeshCollider.ctorMesh(mesh.getNative()));
        mMesh = mesh;
    }
    
    /**
     * Constructor to make mesh collider without a mesh.
     * 
     * The collider will use the mesh attached to the
     * scene object that owns it. If there is no mesh
     * on that scene object, the collider will never be picked.
     * 
     * Your application does not have to wait for the mesh to load
     * before attaching a collider - it will become pickable
     * when the mesh becomes available.
     * 
     * @param gvrContext
     *            The {@link GVRContext} used by the app.
     * @param useMeshBounds
     *            If true, the mesh bounding box is used instead of the mesh.
     */
    public GVRMeshCollider(GVRContext gvrContext, boolean useMeshBounds) {
        super(gvrContext, NativeMeshCollider.ctor(useMeshBounds));
    }

    /**
     * Simple constructor.
     * 
     * When the mesh is complicated, it will be cheaper - though less accurate -
     * to use {@link GVRMesh#getBoundingBox()} instead of the raw mesh.
     * 
     * @param mesh
     *            The {@link GVRMesh} that the picking ray will test against.
     */
    public GVRMeshCollider(GVRMesh mesh) {
        this(mesh.getGVRContext(), mesh);
    }

    /**
     * Retrieve the mesh that is held by this GVRMeshEyePointee
     * 
     * @return the {@link GVRMesh}
     * 
     */
    public GVRMesh getMesh() {
        return mMesh;
    }

    /**
     * Set the mesh to be tested against.
     * 
     * @param mesh
     *            The {@link GVRMesh} that the picking ray will test against.
     * 
     */
    public void setMesh(GVRMesh mesh) {
        mMesh = mesh;
        NativeMeshCollider.setMesh(getNative(), mesh.getNative());
    }
}

class NativeMeshCollider {
    static native long ctorMesh(long mesh);

    static native long ctor(boolean useMeshBounds);

    static native void setMesh(long meshEyePointee, long mesh);
}
