package org.gearvrf.jassimp2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRLight;
import org.gearvrf.GVRLightBase;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShaderTemplate;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.ISceneObjectEvents;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;

public class GVRJassimpSceneObject extends GVRModelSceneObject {
    private static final String TAG = GVRJassimpSceneObject.class.getSimpleName();
    protected AiScene scene;
    protected GVRResourceVolume volume;

  public GVRJassimpSceneObject(GVRContext gvrContext, AiScene scene, GVRResourceVolume volume, Hashtable<String, GVRLightBase> lightlist) {
        super(gvrContext);
        this.volume = volume;

        if (scene != null) {
            this.scene = scene;
            recurseAssimpNodes(this, scene.getSceneRoot(GVRJassimpAdapter.sWrapperProvider), lightlist);

            // Animations
            for (AiAnimation aiAnim : scene.getAnimations()) {
                mAnimations.add(GVRJassimpAdapter.get().createAnimation(aiAnim, this));
            }
        }
    
    } 
    
    private void recurseAssimpNodes(
            GVRSceneObject parentSceneObject,
            AiNode node,Hashtable<String, GVRLightBase> lightlist) {
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
            attachLights(lightlist, sceneObject);
            for (AiNode child : node.getChildren()) {
               recurseAssimpNodes(sceneObject, child, lightlist);
                
            }

            // Inform the loaded object after it has been attached to the scene graph
            getGVRContext().getEventManager().sendEvent(
                    sceneObject,
                    ISceneObjectEvents.class,
                    "onLoaded");
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
            AiNode node,
           int index)
            throws IOException {
        AiMesh aiMesh = scene.getMeshes().get(node.getMeshes()[index]);
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
        		GVRJassimpAdapter.get().createMesh(getGVRContext(), aiMesh));

        AiMaterial material = scene.getMaterials().get(aiMesh.getMaterialIndex());
        final GVRMaterial meshMaterial = new GVRMaterial(getGVRContext(), GVRShaderType.BeingGenerated.ID);

        /* Diffuse color */
        AiColor diffuseColor = material.getDiffuseColor(GVRJassimpAdapter.sWrapperProvider);
        meshMaterial.setVec4("diffuse_color",diffuseColor.getRed(),
                diffuseColor.getGreen(), diffuseColor.getBlue(),diffuseColor.getAlpha());

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
        /* Specular Exponent */
        float specularExponent = material.getShininess();        
        meshMaterial.setSpecularExponent(specularExponent);
        
        /* Diffuse Texture */
        loadTextures( material, meshMaterial,  getGVRContext());

 
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
    private void loadTextures(AiMaterial material, final GVRMaterial meshMaterial, final GVRContext context) throws IOException{
        for (final AiTextureType texType : AiTextureType.values()) {
            if(texType != AiTextureType.UNKNOWN ){
            final String texFileName = material.getTextureFile(
                    texType, 0);
            
            if (texFileName != null && !texFileName.isEmpty()) {
                meshMaterial.setTexture(textureMap.get(texType),(GVRTexture)null);
                try {
                    if (volume != null) {
                        GVRAndroidResource resource = volume
                                .openResource(texFileName);

                        TextureCallback callback = new TextureCallback() {
                            @Override
                            public void loaded(GVRTexture texture,
                                    GVRAndroidResource ignored) {                           
                                meshMaterial.setTexture(textureMap.get(texType),texture);
                                Log.i(TAG,
                                         texType + "texture %s loaded and set to material",
                                        texFileName);
                            }

                            @Override
                            public void failed(Throwable t,
                                    GVRAndroidResource androidResource) {
                                Log.e(TAG,
                                        "Error loading " +texType + " texture %s; exception: %s",
                                        texFileName, t.getMessage());
                            }

                            @Override
                            public boolean stillWanted(
                                    GVRAndroidResource androidResource) {
                                return true;
                            }
                        };

                        getGVRContext().loadTexture(callback, resource);
                    }
                } catch (FileNotFoundException file) {
                    Log.e(TAG, "Couldn't find texture: %s", texFileName);
                } catch (IOException e) {
                    Log.e(TAG, "Error in loading texture: %s", texFileName);
                }
            }      
            }
       }
    }

}
