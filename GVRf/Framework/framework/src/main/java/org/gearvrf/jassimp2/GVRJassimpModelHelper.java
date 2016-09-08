package org.gearvrf.jassimp2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRLightBase;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRPointLight;
import org.gearvrf.GVRResourceVolume;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPhongShader;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSpotLight;
import org.gearvrf.ISceneObjectEvents;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class GVRJassimpModelHelper
{
    private static final String TAG = GVRJassimpModelHelper.class.getSimpleName();
    private AiScene scene;
    private GVRResourceVolume volume;
    private GVRModelSceneObject mModel;
    private GVRContext mContext;
    Hashtable<String, GVRLightBase> mLightList;

    public GVRJassimpModelHelper(GVRAssetLoader.AssetRequest request, GVRModelSceneObject model, AiScene scene, GVRResourceVolume volume)
    {
        List<AiLight> aiLights = scene.getLights();
        GVRSceneObject camera;

        mModel = model;
        mContext = mModel.getGVRContext();
        this.volume = volume;
        mLightList = new Hashtable<String, GVRLightBase>();
        camera = makeCamera(scene);
        if (camera != null)
        {
            mModel.addChildObject(camera);
        }
        importLights(aiLights, mLightList);
        if (scene != null)
        {
            this.scene = scene;
            recurseAssimpNodes(request, mModel, scene.getSceneRoot(GVRJassimpAdapter.sWrapperProvider), mLightList);
            for (AiAnimation aiAnim : scene.getAnimations())
            {
                mModel.getAnimations().add(GVRJassimpAdapter.get().createAnimation(aiAnim, mModel));
            }
        }    
    } 

    private GVRSceneObject makeCamera(AiScene scene)
    {
        List<AiCamera> cameras = scene.getCameras();
        if (cameras.size() == 0)
        {
            return null;
        }
        GVRSceneObject mainCamera = new GVRSceneObject(mContext);
        GVRCameraRig cameraRig = GVRCameraRig.makeInstance(mContext);
        AiCamera aiCam = cameras.get(0);
        AiVector up = (AiVector) aiCam.getUp(Jassimp.BUILTIN);
        AiVector fwd = (AiVector) aiCam.getLookAt(Jassimp.BUILTIN);
        AiVector pos = (AiVector) aiCam.getPosition(Jassimp.BUILTIN);
        Matrix4f mtx = new Matrix4f();

        mtx.setLookAt(pos.getX(), pos.getY(), pos.getZ(),
                      pos.getX() + fwd.getX(), pos.getY() + fwd.getY(), pos.getZ() + fwd.getZ(),
                      up.getX(), up.getY(), up.getZ());
        mainCamera.setName("MainCamera");
        mainCamera.getTransform().setModelMatrix(mtx);
        cameraRig.setNearClippingDistance(aiCam.getClipPlaneNear());
        cameraRig.setFarClippingDistance(aiCam.getClipPlaneFar());
        mainCamera.attachComponent(cameraRig);
        return mainCamera;
    }

    private void recurseAssimpNodes(
            GVRAssetLoader.AssetRequest request,
            GVRSceneObject parentSceneObject,
            AiNode node,Hashtable<String, GVRLightBase> lightlist) {
        try {
            final GVRSceneObject sceneObject;
            if (node.getNumMeshes() == 0) {
                sceneObject = GVRJassimpAdapter.get().createSceneObject(mContext, node);
                parentSceneObject.addChildObject(sceneObject);
            } else if (node.getNumMeshes() == 1) {
                // add the scene object to the scene graph
                sceneObject = createSubSceneObject(request, parentSceneObject, node, 0);
            } else {
                sceneObject = GVRJassimpAdapter.get().createSceneObject(mContext, node);
                parentSceneObject.addChildObject(sceneObject);
                for (int i = 0; i < node.getNumMeshes(); i++) {
                    GVRSceneObject childSceneObject = createSubSceneObject(request, sceneObject, node, i);
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

            mModel.getGVRContext().runOnTheFrameworkThread(new Runnable() {
                public void run() {
                    // Inform the loaded object after it has been attached to the scene graph
                    mContext.getEventManager().sendEvent(
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
            GVRSceneObject parent,
            AiNode node,
            int index)
            throws IOException {
        AiMesh aiMesh = scene.getMeshes().get(node.getMeshes()[index]);
        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
        		GVRJassimpAdapter.get().createMesh(parent.getGVRContext(), aiMesh));
        AiMaterial material = scene.getMaterials().get(aiMesh.getMaterialIndex());
        final GVRMaterial meshMaterial = new GVRMaterial(mContext, GVRShaderType.BeingGenerated.ID);

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
        loadTextures(assetRequest, material, meshMaterial, mContext);

 
        GVRSceneObject sceneObject = GVRJassimpAdapter.get().createSceneObject(mContext, node);
        GVRRenderData sceneObjectRenderData = new GVRRenderData(mContext);
        sceneObjectRenderData.setMesh(futureMesh);

        sceneObjectRenderData.setMaterial(meshMaterial);
        sceneObjectRenderData.setShaderTemplate(GVRPhongShader.class);
        sceneObject.attachRenderData(sceneObjectRenderData);

        parent.addChildObject(sceneObject);
        return sceneObject;
    }

    private static final Map<AiTextureType, String> textureMap;
    static
    {
        textureMap = new HashMap<AiTextureType, String>();
        textureMap.put(AiTextureType.DIFFUSE,"diffuseTexture");
        textureMap.put(AiTextureType.SPECULAR,"specularTexture");
        textureMap.put(AiTextureType.AMBIENT,"ambientTexture");
        textureMap.put(AiTextureType.EMISSIVE,"emissiveTexture");
        textureMap.put(AiTextureType.HEIGHT,"heightTexture");
        textureMap.put(AiTextureType.NORMALS,"normalTexture");
        textureMap.put(AiTextureType.SHININESS,"shininessTexture");
        textureMap.put(AiTextureType.OPACITY,"opacityTexture");
        textureMap.put(AiTextureType.DISPLACEMENT,"displacementTexture");
        textureMap.put(AiTextureType.LIGHTMAP,"lightmapTexture");
        textureMap.put(AiTextureType.REFLECTION,"reflectionTexture");
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

    private void importLights(List<AiLight> lights, Hashtable<String, GVRLightBase> lightlist){
        for(AiLight light: lights){
            AiLightType type = light.getType();
            if(type == AiLightType.DIRECTIONAL){
                GVRDirectLight gvrLight = new GVRDirectLight(mContext);
                setPhongLightProp(gvrLight,light);
                setLightProp(gvrLight, light);
                String name = light.getName();
                lightlist.put(name, gvrLight);
            }
            if(type == AiLightType.POINT){
                GVRPointLight gvrLight = new GVRPointLight(mContext);
                setPhongLightProp(gvrLight,light);
                setLightProp(gvrLight, light);
                String name = light.getName();
                lightlist.put(name, gvrLight);
            }
            if(type == AiLightType.SPOT){
                GVRSpotLight gvrLight = new GVRSpotLight(mContext);
                setPhongLightProp(gvrLight,light);
                setLightProp(gvrLight, light);
                gvrLight.setFloat("inner_cone_angle", (float)Math.cos(light.getAngleInnerCone()));
                gvrLight.setFloat("outer_cone_angle",(float)Math.cos(light.getAngleOuterCone()));
                String name = light.getName();
                lightlist.put(name, gvrLight);
            }
        }

    }

    private void setLightProp(GVRLightBase gvrLight, AiLight assimpLight){
        gvrLight.setFloat("attenuation_constant", assimpLight.getAttenuationConstant());
        gvrLight.setFloat("attenuation_linear", assimpLight.getAttenuationLinear());
        gvrLight.setFloat("attenuation_quadratic", assimpLight.getAttenuationQuadratic());

    }

    private void setPhongLightProp(GVRLightBase gvrLight, AiLight assimpLight){
        org.gearvrf.jassimp2.AiColor ambientCol= assimpLight.getColorAmbient(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp2.AiColor diffuseCol= assimpLight.getColorDiffuse(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp2.AiColor specular = assimpLight.getColorSpecular(GVRJassimpAdapter.sWrapperProvider);
        gvrLight.setVec4("ambient_intensity", ambientCol.getRed(), ambientCol.getGreen(), ambientCol.getBlue(),ambientCol.getAlpha());
        gvrLight.setVec4("diffuse_intensity", diffuseCol.getRed(), diffuseCol.getGreen(),diffuseCol.getBlue(),diffuseCol.getAlpha());
        gvrLight.setVec4("specular_intensity", specular.getRed(),specular.getGreen(),specular.getBlue(), specular.getAlpha());

    }

}
