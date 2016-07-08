package org.gearvrf.jassimp2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLightBase;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ISceneObjectEvents;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.joml.Quaternionf;

public class GVRJassimpSceneObject extends GVRModelSceneObject {
    private static final String TAG = GVRJassimpSceneObject.class.getSimpleName();
    protected AiScene scene;
    protected GVRResourceVolume volume;

  public GVRJassimpSceneObject(GVRAssetLoader.AssetRequest request, AiScene scene, GVRResourceVolume volume, Hashtable<String, GVRLightBase> lightlist) {
        super(request.getContext());
        this.volume = volume;

        if (scene != null) {
            this.scene = scene;
            recurseAssimpNodes(request, this, scene.getSceneRoot(GVRJassimpAdapter.sWrapperProvider), lightlist);

            // Animations
            for (AiAnimation aiAnim : scene.getAnimations()) {
                mAnimations.add(GVRJassimpAdapter.get().createAnimation(aiAnim, this));
            }
        }    
    } 
    
    private void recurseAssimpNodes(
            GVRAssetLoader.AssetRequest request,
            GVRSceneObject parentSceneObject,
            AiNode node,Hashtable<String, GVRLightBase> lightlist) {
        try {
            final GVRSceneObject sceneObject;
            if (node.getNumMeshes() == 0) {
                sceneObject = GVRJassimpAdapter.get().createSceneObject(getGVRContext(), node);
                parentSceneObject.addChildObject(sceneObject);
            } else if (node.getNumMeshes() == 1) {
                // add the scene object to the scene graph
                sceneObject = createSubSceneObject(request, node, 0);
                parentSceneObject.addChildObject(sceneObject);
            } else {
                sceneObject = GVRJassimpAdapter.get().createSceneObject(getGVRContext(), node);
                parentSceneObject.addChildObject(sceneObject);
                for (int i = 0; i < node.getNumMeshes(); i++) {
                    GVRSceneObject childSceneObject = createSubSceneObject(request, node, i);
                    sceneObject.addChildObject(childSceneObject);
                }
            }

            if (node.getTransform(GVRJassimpAdapter.sWrapperProvider) != null) {
                float[] matrix = node.getTransform(GVRJassimpAdapter.sWrapperProvider);
                sceneObject.getTransform().setModelMatrix(matrix);
            }
            attachLights(lightlist, sceneObject);
            for (AiNode child : node.getChildren()) {
               recurseAssimpNodes(request, sceneObject, child, lightlist);
            }

            getGVRContext().runOnTheFrameworkThread(new Runnable() {
                public void run() {
                    // Inform the loaded object after it has been attached to the scene graph
                    getGVRContext().getEventManager().sendEvent(
                            sceneObject,
                            ISceneObjectEvents.class,
                            "onLoaded");
                }
            });
        } catch (Exception e) {
            // Error while recursing the Scene Graph
            e.printStackTrace();
        }
    }
    
    void attachLights(Hashtable<String, GVRLightBase> lightlist, GVRSceneObject sceneObject){
        String name = sceneObject.getName();
        GVRLightBase light =  lightlist.get(name);
        if (light != null) {
            Quaternionf q = new Quaternionf();
            q.rotationX((float) Math.PI / 2.0f);
            light.setDefaultOrientation(q);
            sceneObject.attachLight(light); 
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
            GVRAssetLoader.AssetRequest assetRequest,
            AiNode node,
           int index)
            throws IOException {
        AiMesh aiMesh = scene.getMeshes().get(node.getMeshes()[index]);
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
        		GVRJassimpAdapter.get().createMesh(getGVRContext(), aiMesh));

        AiMaterial material = scene.getMaterials().get(aiMesh.getMaterialIndex());
        final GVRMaterial meshMaterial = new GVRMaterial(getGVRContext(), GVRShaderType.BeingGenerated.ID);

        /* Diffuse color & Opacity */
        AiColor diffuseColor = material.getDiffuseColor(GVRJassimpAdapter.sWrapperProvider);        /* Opacity */
        float opacity = diffuseColor.getAlpha();
        if (material.getOpacity() > 0) {
            opacity *= material.getOpacity();
        }
        meshMaterial.setVec4("diffuse_color",diffuseColor.getRed(),
                diffuseColor.getGreen(), diffuseColor.getBlue(), opacity);

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

        
        /* Specular Exponent */
        float specularExponent = material.getShininess();        
        meshMaterial.setSpecularExponent(specularExponent);
        
        /* Diffuse Texture */
        loadTextures(assetRequest, material, meshMaterial,  getGVRContext());

 
        GVRSceneObject sceneObject = GVRJassimpAdapter.get().createSceneObject(getGVRContext(), node);
        GVRRenderData sceneObjectRenderData = new GVRRenderData(getGVRContext());
        sceneObjectRenderData.setMesh(futureMesh);

        sceneObjectRenderData.setMaterial(meshMaterial);
        sceneObjectRenderData.setShaderTemplate(GVRPhongShader.class);
        sceneObject.attachRenderData(sceneObjectRenderData);

        return sceneObject;
    }
    private static final Map<AiTextureType, String> textureMap;
    static
    {
        textureMap = new HashMap<AiTextureType, String>();
        textureMap.put(AiTextureType.DIFFUSE,"diffuseTexture");
        textureMap.put(AiTextureType.AMBIENT,"ambientTexture");
        textureMap.put(AiTextureType.NORMALS,"normalTexture");
        textureMap.put(AiTextureType.EMISSIVE,"emissiveTexture");
        textureMap.put(AiTextureType.SPECULAR,"specularTexture");
        textureMap.put(AiTextureType.LIGHTMAP,"lightmapTexture");
        textureMap.put(AiTextureType.OPACITY,"opacityTexture");
    }
    
    private void loadTextures(GVRAssetLoader.AssetRequest assetRequest, AiMaterial material, final GVRMaterial meshMaterial, final GVRContext context) throws IOException{
        for (final AiTextureType texType : AiTextureType.values())
        {
            if(texType != AiTextureType.UNKNOWN)
            {
                final String texFileName = material.getTextureFile(texType, 0);
                if (!"".equals(texFileName))
                {
                    GVRAssetLoader.TextureRequest texRequest = new GVRAssetLoader.MaterialTextureRequest(assetRequest.getContext(), texFileName, meshMaterial, textureMap.get(texType));
                    assetRequest.loadTexture(texRequest);
                }
            }
        }
    }
}
