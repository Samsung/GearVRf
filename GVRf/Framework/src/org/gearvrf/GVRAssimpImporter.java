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
 * Provides access to the {@link GVRMesh meshes} contained in 3D models that
 * have been imported with {@link GVRImporter}.
 */
class GVRAssimpImporter extends GVRHybridObject {
    GVRAssimpImporter(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    /**
     * @return The number of meshes contained in the imported 3D model.
     */
    int getNumberOfMeshes() {
        return NativeAssimpImporter.getNumberOfMeshes(getPtr());
    }

    /**
     * Retrieves a specific mesh from the imported 3D model.
     * 
     * @param index
     *            Index of the mesh to get
     * @return The mesh, encapsulated as a {@link GVRMesh}.
     */
    GVRMesh getMesh(int index) {
        return GVRMesh.factory(getGVRContext(),
                NativeAssimpImporter.getMesh(getPtr(), index));
    }
}

class NativeAssimpImporter {
    static native int getNumberOfMeshes(long assimpImporter);

    static native long getMesh(long assimpImporter, int index);
}
