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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.UUID;

import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.jassimp.AiColor;
import org.gearvrf.jassimp.AiMaterial;
import org.gearvrf.jassimp.AiNode;
import org.gearvrf.jassimp.AiScene;
import org.gearvrf.jassimp.AiTextureType;
import org.gearvrf.jassimp.GVROldWrapperProvider;
import org.gearvrf.jassimp2.GVRJassimpAdapter;
import org.gearvrf.jassimp2.GVRJassimpSceneObject;
import org.gearvrf.jassimp2.Jassimp;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.Log;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

/**
 * {@link GVRImporter} provides methods for importing 3D models and making them
 * available through instances of {@link GVRAssimpImporter}.
 * <p>
 * Supports importing models from an application's resources (both
 * {@code assets} and {@code res/raw}) and from directories on the device's SD
 * card that the application has permission to read.
 */
final class GVRImporter {
    private GVRImporter() {
    }

    /**
     * Imports a 3D model from the specified file in the application's
     * {@code asset} directory.
     * 
     * @param gvrContext
     *            Context to import file from.
     * @param filename
     *            Name of the file to import.
     * @return An instance of {@link GVRAssimpImporter} or {@code null} if the
     *         file does not exist (or cannot be read)
     */
    static GVRAssimpImporter readFileFromAssets(GVRContext gvrContext,
            String filename, EnumSet<GVRImportSettings> settings) { 
        long nativeValue = NativeImporter.readFileFromAssets(gvrContext
                .getContext().getAssets(), filename, GVRImportSettings.getAssimpImportFlags(settings));
        return nativeValue == 0 ? null : new GVRAssimpImporter(gvrContext,
                nativeValue);
    }

    static GVRAssimpImporter readFileFromResources(GVRContext gvrContext,
            int resourceId, EnumSet<GVRImportSettings> settings) {
        return readFileFromResources(gvrContext, new GVRAndroidResource(
                gvrContext, resourceId), settings);
    }

    /** @since 1.6.2 */
    static GVRAssimpImporter readFileFromResources(GVRContext gvrContext,
            GVRAndroidResource resource, EnumSet<GVRImportSettings> settings) {
        try {
            byte[] bytes;
            InputStream stream = resource.getStream();
            try {
                bytes = new byte[stream.available()];
                stream.read(bytes);
            } finally {
                resource.closeStream();
            }
            String resourceFilename = resource.getResourceFilename();
            if (resourceFilename == null) {
                resourceFilename = ""; // Passing null causes JNI exception.
            }
            long nativeValue = NativeImporter.readFromByteArray(bytes,
                    resourceFilename, GVRImportSettings.getAssimpImportFlags(settings));
            return new GVRAssimpImporter(gvrContext, nativeValue);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Imports a 3D model from a file on the device's SD card. The application
     * must have read permission for the directory containing the file.
     * 
     * Does not check that file exists and is readable by this process: the only
     * public caller does that check.
     * 
     * @param gvrContext
     *            Context to import file from.
     * @param filename
     *            Name of the file to import.
     * @return An instance of {@link GVRAssimpImporter}.
     */
    static GVRAssimpImporter readFileFromSDCard(GVRContext gvrContext,
            String filename, EnumSet<GVRImportSettings> settings) {
        long nativeValue = NativeImporter.readFileFromSDCard(filename, GVRImportSettings.getAssimpImportFlags(settings));
        return new GVRAssimpImporter(gvrContext, nativeValue);
    }

    static GVRModelSceneObject loadJassimpModel(final GVRContext context,
            String filePath, GVRResourceVolume.VolumeType volumeType,
            EnumSet<GVRImportSettings> settings) throws IOException {
        return loadJassimpModel(context, filePath, volumeType, settings, false);
    }

    static GVRModelSceneObject loadJassimpModel(final GVRContext context,
            String filePath, GVRResourceVolume.VolumeType volumeType,
            EnumSet<GVRImportSettings> settings, boolean cacheEnabled)
                    throws IOException {

        Jassimp.setWrapperProvider(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp2.AiScene assimpScene = null;

        switch (volumeType) {
        case ANDROID_ASSETS:
            assimpScene = Jassimp.importAssetFile(filePath,
                    GVRJassimpAdapter.get().toJassimpSettings(settings),
                    context.getContext().getAssets());
            break;

        case ANDROID_SDCARD:
            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            assimpScene = Jassimp.importFile(sdPath + File.separator + filePath,
                    GVRJassimpAdapter.get().toJassimpSettings(settings));
            break;

        case LINUX_FILESYSTEM:
            assimpScene = Jassimp.importFile(filePath,
                    GVRJassimpAdapter.get().toJassimpSettings(settings));
            break;

        case NETWORK:
            // filePath is a URL in this case
            File tmpFile = downloadFile(context.getActivity(), filePath);
            if (tmpFile != null) {
                assimpScene = Jassimp.importFile(tmpFile.getAbsolutePath(),
                        GVRJassimpAdapter.get().toJassimpSettings(settings));
                tmpFile.delete();
            }
            break;
        }

        if (assimpScene == null) {
            throw new IOException("Cannot load a model from path " + filePath +
                    " from " + volumeType);
        }

        return new GVRJassimpSceneObject(context, assimpScene,
                new GVRResourceVolume(context, volumeType,
                        FileNameUtils.getParentDirectory(filePath),
                        cacheEnabled));
    }

    static File downloadFile(Context context, String urlString) {
        URL url = null;
        try {
            url = new URL(urlString);
        } catch (IOException e) {
            Log.e(TAG, "URL error: ", urlString);
            return null;
        }

        String directoryPath = context.getCacheDir().getAbsolutePath();
        // add a uuid value for the url to prevent aliasing from files sharing
        // same name inside one given app
        String outputFilename = directoryPath + File.separator
                + UUID.nameUUIDFromBytes(urlString.getBytes()).toString()
                + FileNameUtils.getURLFilename(urlString);

        Log.d(TAG, "URL filename: %s", outputFilename);
        
        File localCopy = new File(outputFilename);
        if (localCopy.exists()) {
            return localCopy;
        }

        InputStream input = null;
        // Output stream to write file
        OutputStream output = null;

        try {
            input = new BufferedInputStream(url.openStream(), 8192);
            output = new FileOutputStream(outputFilename);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to download: ", urlString);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }

            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }

        return new File(outputFilename);
    }

    static GVRSceneObject getAssimpModel(final GVRContext context, String assetRelativeFilename,
            EnumSet<GVRImportSettings> settings) throws IOException {

        GVRAssimpImporter assimpImporter = GVRImporter.readFileFromResources(
                context, new GVRAndroidResource(context, assetRelativeFilename),
                settings);

        GVRSceneObject wholeSceneObject = new GVRSceneObject(context);

        AiScene assimpScene = assimpImporter.getAssimpScene();

        AiNode rootNode = assimpScene.getSceneRoot(sWrapperProvider);

        // Recurse through the entire hierarchy to attache all the meshes as
        // Scene Object
        recurseAssimpNodes(context, assimpImporter, assetRelativeFilename, wholeSceneObject,
                rootNode, sWrapperProvider);

        return wholeSceneObject;
    }

    /**
     * Helper method to recurse through all the assimp nodes and get all their
     * meshes that can be used to create {@link GVRSceneObject} to be attached
     * to the set of complete scene objects for the assimp model.
     * 
     * @param assetRelativeFilename A filename, relative to the {@code assets}
     *            directory. The file can be in a sub-directory of the
     *            {@code assets} directory: {@code "foo/bar.png"} will open the
     *            file {@code assets/foo/bar.png}
     * @param wholeSceneObject A reference of the {@link GVRSceneObject}, to
     *            which all other scene objects are attached.
     * @param node A reference to the AiNode for which we want to recurse all
     *            its children and meshes.
     * @param sWrapperProvider AiWrapperProvider for unwrapping Jassimp
     *            properties.
     */
    @SuppressWarnings("resource")
    private static void recurseAssimpNodes(
            final GVRContext context,
            GVRAssimpImporter assimpImporter,
            String assetRelativeFilename,
            GVRSceneObject parentSceneObject,
            AiNode node,
            GVROldWrapperProvider wrapperProvider) {
        try {
            GVRSceneObject newParentSceneObject = new GVRSceneObject(context);

            if (node.getNumMeshes() == 0) {
                parentSceneObject.addChildObject(newParentSceneObject);
                parentSceneObject = newParentSceneObject;
            } else if (node.getNumMeshes() == 1) {
                // add the scene object to the scene graph
                GVRSceneObject sceneObject = createSceneObject(
                        context, assimpImporter, assetRelativeFilename, node, 0, wrapperProvider);
                parentSceneObject.addChildObject(sceneObject);
                parentSceneObject = sceneObject;
            } else {
                for (int i = 0; i < node.getNumMeshes(); i++) {
                    GVRSceneObject sceneObject = createSceneObject(
                            context, assimpImporter, assetRelativeFilename, node, i, wrapperProvider);
                    newParentSceneObject.addChildObject(sceneObject);
                }
                parentSceneObject.addChildObject(newParentSceneObject);
                parentSceneObject = newParentSceneObject;
            }

            if (node.getTransform(wrapperProvider) != null) {
                parentSceneObject.getTransform().setModelMatrix(
                        GVROldWrapperProvider
                                .transpose(node.getTransform(wrapperProvider).toByteBuffer()));
            }

            for (int i = 0; i < node.getNumChildren(); i++) {
                recurseAssimpNodes(context, assimpImporter, assetRelativeFilename,
                        parentSceneObject, node.getChildren().get(i),
                        wrapperProvider);
            }
        } catch (Exception e) {
            // Error while recursing the Scene Graph
            e.printStackTrace();
        }
    }

    /**
     * Helper method to create a new {@link GVRSceneObject} with the mesh at the
     * index {@link index} of the node mesh array with a color or texture
     * material.
     * 
     * @param assetRelativeFilename A filename, relative to the {@code assets}
     *            directory. The file can be in a sub-directory of the
     *            {@code assets} directory: {@code "foo/bar.png"} will open the
     *            file {@code assets/foo/bar.png}
     * @param node A reference to the AiNode for which we want to recurse all
     *            its children and meshes.
     * @param index The index of the mesh in the array of meshes for that node.
     * @param sWrapperProvider AiWrapperProvider for unwrapping Jassimp
     *            properties.
     * @return The new {@link GVRSceneObject} with the mesh at the index
     *         {@link index} for the node {@link node}
     * @throws IOException File does not exist or cannot be read
     */
    private static GVRSceneObject createSceneObject(
            final GVRContext context,
            GVRAssimpImporter assimpImporter,
            String assetRelativeFilename,
            AiNode node,
            int index,
            GVROldWrapperProvider wrapperProvider)
                    throws IOException {

        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                context.getNodeMesh(assimpImporter, node.getName(), index));

        AiMaterial material = getMeshMaterial(assimpImporter, node.getName(), index);

        final GVRMaterial meshMaterial = new GVRMaterial(context,
                GVRShaderType.Assimp.ID);

        /* Feature set */
        int assimpFeatureSet = 0x00000000;

        /* Diffuse color */
        AiColor diffuseColor = material.getDiffuseColor(wrapperProvider);
        meshMaterial.setDiffuseColor(diffuseColor.getRed(),
                diffuseColor.getGreen(), diffuseColor.getBlue(),
                diffuseColor.getAlpha());

        /* Specular color */
        AiColor specularColor = material.getSpecularColor(wrapperProvider);
        meshMaterial.setSpecularColor(specularColor.getRed(),
                specularColor.getGreen(), specularColor.getBlue(),
                specularColor.getAlpha());

        /* Ambient color */
        AiColor ambientColor = material.getAmbientColor(wrapperProvider);
        meshMaterial.setAmbientColor(ambientColor.getRed(),
                ambientColor.getGreen(), ambientColor.getBlue(),
                ambientColor.getAlpha());

        /* Emissive color */
        AiColor emissiveColor = material.getEmissiveColor(wrapperProvider);
        meshMaterial.setVec4("emissive_color", emissiveColor.getRed(),
                emissiveColor.getGreen(), emissiveColor.getBlue(),
                emissiveColor.getAlpha());

        /* Opacity */
        float opacity = material.getOpacity();
        meshMaterial.setOpacity(opacity);

        /* Diffuse Texture */
        final String texDiffuseFileName = material.getTextureFile(
                AiTextureType.DIFFUSE, 0);
        if (texDiffuseFileName != null && !texDiffuseFileName.isEmpty()) {
            context.loadTexture(new TextureCallback() {
                @Override
                public void loaded(GVRTexture texture, GVRAndroidResource ignored) {
                    meshMaterial.setMainTexture(texture);
                    final int features = GVRShaderType.Assimp.setBit(
                            meshMaterial.getShaderFeatureSet(),
                            GVRShaderType.Assimp.AS_DIFFUSE_TEXTURE);
                    meshMaterial.setShaderFeatureSet(features);
                }

                @Override
                public void failed(Throwable t, GVRAndroidResource androidResource) {
                    Log.e(TAG, "Error loading diffuse texture %s; exception: %s",
                            texDiffuseFileName, t.getMessage());
                }

                @Override
                public boolean stillWanted(GVRAndroidResource androidResource) {
                    return true;
                }
            }, new GVRAndroidResource(context, texDiffuseFileName));
        }

        /* Apply feature set to the material */
        meshMaterial.setShaderFeatureSet(assimpFeatureSet);

        GVRSceneObject sceneObject = new GVRSceneObject(context);
        sceneObject.setName(node.getName());
        GVRRenderData sceneObjectRenderData = new GVRRenderData(context);
        sceneObjectRenderData.setMesh(futureMesh);
        sceneObjectRenderData.setMaterial(meshMaterial);
        sceneObject.attachRenderData(sceneObjectRenderData);
        return sceneObject;
    }

    /**
     * Retrieves the material for the mesh of the given node..
     * 
     * @return The material, encapsulated as a {@link AiMaterial}.
     */
    static AiMaterial getMeshMaterial(GVRAssimpImporter assimpImporter,
            String nodeName, int meshIndex) {
        return assimpImporter.getMeshMaterial(nodeName, meshIndex);
    }

    /**
     * State-less, should be fine having one instance
     */
    private final static GVROldWrapperProvider sWrapperProvider = new GVROldWrapperProvider();

    private final static String TAG = "GVRImporter";
}

class NativeImporter {
    static native long readFileFromAssets(AssetManager assetManager,
            String filename, int settings);

    static native long readFileFromSDCard(String filename, int settings);

    static native long readFromByteArray(byte[] bytes, String filename, int settings);
}
