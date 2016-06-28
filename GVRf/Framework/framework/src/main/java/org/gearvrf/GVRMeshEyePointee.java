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
 * An actual eye pointee.
 * 
 * A {@link GVRCollider} is something that is being pointed at by a picking
 * ray. {@linkplain GVRColliders Colliders} are attached to
 * {@link GVRSceneObject scene objects.} The {@link GVRPicker} will return a
 * {@code GVRCollider[]}: you use
 * {@link GVRCollider#getOwnerObject()} to retrieve the scene object.
 * 
 * <p>
 * A MeshCollider holds the {@link GVRMesh} that the picking ray will be
 * tested against.
 * @deprecated use GVRMeshCollider
 */
public class GVRMeshEyePointee extends GVRMeshCollider {
    /**
     * Base constructor.
     * 
     * When the mesh is complicated, it will be cheaper - though less accurate -
     * to use {@link GVRMesh#getBoundingBox()} instead of the raw mesh.
     * 
     * @param gvrContext
     *            The {@link GVRContext} used by the app.
     * 
     * @param mesh
     *            The {@link GVRMesh} that the picking ray will test against.
     */
    public GVRMeshEyePointee(GVRContext gvrContext, GVRMesh mesh) {
        super(gvrContext, mesh);
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
    public GVRMeshEyePointee(GVRMesh mesh) {
        this(mesh.getGVRContext(), mesh);
    }

    /**
     * Constructor that can use the mesh's bounding box.
     * 
     * When the mesh is complicated, it will be cheaper - though less accurate -
     * to use {@link GVRMesh#getBoundingBox()} instead of the raw mesh.
     * 
     * @param mesh
     *            The {@link GVRMesh} that the picking ray will test against.
     * @param useBoundingBox
     *            When {@code true}, will use {@link GVRMesh#getBoundingBox()
     *            mesh.getBoundingBox()}; when {@code false} will use
     *            {@code mesh} directly.
     */
    /*
     * TODO How much accuracy do we lose with bounding boxes?
     * 
     * Would it make sense for the useBoundingBox parameter to be a tri-state
     * enum: mesh, box, box-then-mesh?
     */
    public GVRMeshEyePointee(GVRMesh mesh, boolean useBoundingBox) {
        this(mesh.getGVRContext(), useBoundingBox ? mesh.getBoundingBox()
                : mesh);
    }
}