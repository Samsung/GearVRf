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

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import org.gearvrf.jassimp.AiMaterial;
import org.gearvrf.jassimp.AiScene;

/**
 * Provides access to the {@link GVRMesh meshes} contained in 3D models that
 * have been imported with {@link GVRAssetLoader}.
 */
class GVRAssimpImporter extends GVRHybridObject {

    GVRAssimpImporter(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    /**
     * @return The number of meshes contained in the imported 3D model.
     */
    int getNumberOfMeshes() {
        return NativeAssimpImporter.getNumberOfMeshes(getNative());
    }

    /**
     * Retrieves a specific mesh from the imported 3D model.
     * 
     * @param index
     *            Index of the mesh to get
     * @return The mesh, encapsulated as a {@link GVRMesh}.
     */
    GVRMesh getMesh(int index) {
        return new GVRMesh(getGVRContext(), NativeAssimpImporter.getMesh(
                getNative(), index));
    }

    /**
     * Retrieves the complete scene from the imported 3D model.
     * 
     * @return The scene, encapsulated as a {@link AiScene}, which is a
     *         component of the Jassimp integration.
     */
    AiScene getAssimpScene() {
        return NativeAssimpImporter.getAssimpScene(getNative());
    }

    /**
     * Retrieves the particular mesh for the given node.
     * 
     * @return The mesh, encapsulated as a {@link GVRMesh}.
     */
    GVRMesh getNodeMesh(String nodeName, int meshIndex) {
        return new GVRMesh(getGVRContext(), NativeAssimpImporter.getNodeMesh(
                getNative(), nodeName, meshIndex));
    }

    /**
     * Retrieves the material for the mesh of the given node..
     * 
     * @return The material, encapsulated as a {@link AiMaterial}.
     */
    AiMaterial getMeshMaterial(String nodeName, int meshIndex) {
        return NativeAssimpImporter.getMeshMaterial(getNative(), nodeName,
                meshIndex);
    }
}

class NativeAssimpImporter {
    static native int getNumberOfMeshes(long assimpImporter);

    static native long getMesh(long assimpImporter, int index);

    static native AiScene getAssimpScene(long assimpImporter);

    static native long getNodeMesh(long assimpImporter, String nodeName,
            int meshIndex);

    static native AiMaterial getMeshMaterial(long assimpImporter,
            String nodeName, int meshIndex);
}
