package org.gearvrf.jassimp2;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;

public class GVRJassimpSceneObject extends GVRModelSceneObject {
    private static final String TAG = GVRJassimpSceneObject.class.getSimpleName();
    protected AiScene scene;
    protected GVRResourceVolume volume;

    public GVRJassimpSceneObject(GVRContext gvrContext, AiScene scene, GVRResourceVolume volume) {
        super(gvrContext);
        this.volume = volume;

        if (scene != null) {
            this.scene = scene;
            recurseAssimpNodes(this, scene.getSceneRoot(GVRJassimpAdapter.sWrapperProvider));

            // Animations
            for (AiAnimation aiAnim : scene.getAnimations()) {
                mAnimations.add(GVRJassimpAdapter.get().createAnimation(aiAnim, this));
            }
        }
    }

    private void recurseAssimpNodes(
            GVRSceneObject parentSceneObject,
            AiNode node) {
        try {
            GVRSceneObject sceneObject = null;
            if (node.getNumMeshes() == 0) {
                sceneObject = GVRJassimpAdapter.get().createSceneObject(getGVRContext(), node);
                parentSceneObject.addChildObject(sceneObject);
            } else if (node.getNumMeshes() == 1) {
                // add the scene object to the scene graph
                sceneObject = createSubSceneObject(node, 0);
                parentSceneObject.addChildObject(sceneObject);
            } else {
                sceneObject = GVRJassimpAdapter.get().createSceneObject(getGVRContext(), node);
                parentSceneObject.addChildObject(sceneObject);
                for (int i = 0; i < node.getNumMeshes(); i++) {
                    GVRSceneObject childSceneObject = createSubSceneObject(node, i);
                    sceneObject.addChildObject(childSceneObject);
                }
            }

            if (node.getTransform(GVRJassimpAdapter.sWrapperProvider) != null) {
            	float[] matrix = node.getTransform(GVRJassimpAdapter.sWrapperProvider);
                sceneObject.getTransform().setModelMatrix(matrix);
            }

            for (AiNode child : node.getChildren()) {
                recurseAssimpNodes(sceneObject, child);
            }

            // Inform the loaded object after it has been attached to the scene graph
            sceneObject.onLoaded();
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
     * @param assetRelativeFilename
     *            A filename, relative to the {@code assets} directory. The file
     *            can be in a sub-directory of the {@code assets} directory:
     *            {@code "foo/bar.png"} will open the file
     *            {@code assets/foo/bar.png}
     * 
     * @param node
     *            A reference to the AiNode for which we want to recurse all its
     *            children and meshes.
     * 
     * @param index
     *            The index of the mesh in the array of meshes for that node.
     * 
     * @param wrapperProvider
     *            AiWrapperProvider for unwrapping Jassimp properties.
     * 
     * @return The new {@link GVRSceneObject} with the mesh at the index
     *         {@link index} for the node {@link node}
     * 
     * @throws IOException
     *             File does not exist or cannot be read
     */
    private GVRSceneObject createSubSceneObject(
            AiNode node,
            int index)
            throws IOException {
        AiMesh aiMesh = scene.getMeshes().get(node.getMeshes()[index]);
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
        		GVRJassimpAdapter.get().createMesh(getGVRContext(), aiMesh));

        AiMaterial material = scene.getMaterials().get(aiMesh.getMaterialIndex());
        final GVRMaterial meshMaterial = new GVRMaterial(getGVRContext(), GVRShaderType.Assimp.ID);

        /* Feature set */
        int assimpFeatureSet = 0x00000000;

        /* Diffuse color */
        AiColor diffuseColor = material.getDiffuseColor(GVRJassimpAdapter.sWrapperProvider);
        meshMaterial.setDiffuseColor(diffuseColor.getRed(),
                diffuseColor.getGreen(), diffuseColor.getBlue(),
                diffuseColor.getAlpha());

        /* Specular color */
        AiColor specularColor = material.getSpecularColor(GVRJassimpAdapter.sWrapperProvider);
        meshMaterial.setSpecularColor(specularColor.getRed(),
                specularColor.getGreen(), specularColor.getBlue(),
                specularColor.getAlpha());

        /* Ambient color */
        AiColor ambientColor = material.getAmbientColor(GVRJassimpAdapter.sWrapperProvider);
        meshMaterial.setAmbientColor(ambientColor.getRed(),
                ambientColor.getGreen(), ambientColor.getBlue(),
                ambientColor.getAlpha());

        /* Emissive color */
        AiColor emissiveColor = material.getEmissiveColor(GVRJassimpAdapter.sWrapperProvider);
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
            try {
                if (volume != null) {
                    GVRAndroidResource resource = volume
                            .openResource(texDiffuseFileName);

                    TextureCallback callback = new TextureCallback() {
                        @Override
                        public void loaded(GVRTexture texture,
                                GVRAndroidResource ignored) {
                            meshMaterial.setMainTexture(texture);
                            Log.i(TAG,
                                    "diffuse texture %s loaded and set to material",
                                    texDiffuseFileName);
                        }

                        @Override
                        public void failed(Throwable t,
                                GVRAndroidResource androidResource) {
                            Log.e(TAG,
                                    "Error loading diffuse texture %s; exception: %s",
                                    texDiffuseFileName, t.getMessage());
                        }

                        @Override
                        public boolean stillWanted(
                                GVRAndroidResource androidResource) {
                            return true;
                        }
                    };

                    getGVRContext().loadTexture(callback, resource);
                }
                assimpFeatureSet = GVRShaderType.Assimp.setBit(
                        assimpFeatureSet,
                        GVRShaderType.Assimp.AS_DIFFUSE_TEXTURE);
            } catch (FileNotFoundException file) {
                Log.e(TAG, "Couldn't find diffuse texture: %s", texDiffuseFileName);
            } catch (IOException e) {
                Log.e(TAG, "Error in loading texture: %s", texDiffuseFileName);
            }
        }
 
        /* Skinning */
        if (aiMesh.hasBones()) {
            assimpFeatureSet = GVRShaderType.Assimp.setBit(
                    assimpFeatureSet,
                    GVRShaderType.Assimp.AS_SKINNING);
        }

        /* Apply feature set to the material */
        meshMaterial.setShaderFeatureSet(assimpFeatureSet);

        GVRSceneObject sceneObject = GVRJassimpAdapter.get().createSceneObject(getGVRContext(), node);
        GVRRenderData sceneObjectRenderData = new GVRRenderData(getGVRContext());
        sceneObjectRenderData.setMesh(futureMesh);
        sceneObjectRenderData.setMaterial(meshMaterial);
        sceneObject.attachRenderData(sceneObjectRenderData);

        return sceneObject;
    }
}
