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
 * A {@link GVREyePointee} is something that is being pointed at by a picking
 * ray. {@linkplain GVREyePointee Eye pointees} are held by
 * {@linkplain GVREyePointeeHolder eye pointee holders,} which are attached to
 * {@link GVRSceneObject scene objects.} The {@link GVRPicker} will return a
 * {@code GVREyePointeeHolder[]}: you use
 * {@link GVREyePointeeHolder#getOwnerObject()} to retrieve the scene object.
 * 
 * <p>
 * A MeshEyePointee holds the {@link GVRMesh} that the picking ray will be
 * tested against.
 */
public class GVRMeshEyePointee extends GVREyePointee {
    /**
     * Constructor.
     * 
     * When the mesh is complicated, it will be cheaper - though less accurate -
     * to use {@link GVRMesh#getBoundingBox()} instead of the raw mesh.
     * 
     * @param gvrContext
     *            The {@link GVRContext} used by the app.
     * 
     * @param mesh
     *            The {@link GVRMesh} that the picking ray will test against.
     * 
     */
    public GVRMeshEyePointee(GVRContext gvrContext, GVRMesh mesh) {
        super(gvrContext, NativeMeshEyePointee.ctor(mesh.getPtr()));
    }

    /**
     * Retrieve the mesh that is held by this GVRMeshEyePointee
     * 
     * @return the {@link GVRMesh}
     * 
     */
    public GVRMesh getMesh() {
        return GVRMesh.factory(getGVRContext(),
                NativeMeshEyePointee.getMesh(getPtr()));
    }

    /**
     * Set the mesh to be tested against.
     * 
     * @param mesh
     *            The {@link GVRMesh} that the picking ray will test against.
     * 
     */
    public void setMesh(GVRMesh mesh) {
        NativeMeshEyePointee.setMesh(getPtr(), mesh.getPtr());
    }
}

class NativeMeshEyePointee {
    public static native long ctor(long mesh);

    public static native long getMesh(long meshEyePointee);

    public static native void setMesh(long meshEyePointee, long mesh);
}
