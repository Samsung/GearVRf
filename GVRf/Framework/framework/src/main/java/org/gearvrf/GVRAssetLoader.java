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
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;

import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.GVRMaterial.GVRShaderType;
//import org.gearvrf.jassimp.AiColor;
import org.gearvrf.jassimp.AiMaterial;
import org.gearvrf.jassimp.AiNode;
import org.gearvrf.jassimp.AiScene;
import org.gearvrf.jassimp.AiTextureType;
import org.gearvrf.jassimp.GVROldWrapperProvider;
import org.gearvrf.jassimp2.AiLight;
import org.gearvrf.jassimp2.AiLightType;
import org.gearvrf.jassimp2.GVRJassimpAdapter;
import org.gearvrf.jassimp2.GVRJassimpSceneObject;
import org.gearvrf.jassimp2.Jassimp;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.Log;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import org.gearvrf.jassimp.AiColor;

/**
 * {@link GVRAssetLoader} provides methods for importing 3D models and making them
 * available through instances of {@link GVRAssimpImporter}.
 * <p>
 * Supports importing models from an application's resources (both
 * {@code assets} and {@code res/raw}) and from directories on the device's SD
 * card that the application has permission to read.
 */
public final class GVRAssetLoader {
    /**
     * Listens for texture load events and raises the "onAssetLoaded"
     * event after all textures have been loaded.
     */
    static class AssetEventListener implements IAssetEvents
    {
        protected GVRContext        mContext = null;
        protected int               mNumTextures = 0;
        protected GVRScene          mScene = null;
        protected GVRSceneObject    mModel = null;
        protected IAssetEvents      mUserHandler = null;
        protected String            mFileName;
        protected String            mErrors;
        
        public AssetEventListener(GVRContext context, GVRScene scene) {
            mScene = scene;
            mContext = context;
            mNumTextures = 0;
        }
        public void beginLoadTexture()
        {
            ++mNumTextures;
        }
        
        public void beginLoadAsset(String filePath, IAssetEvents userHandler)
        {
            mUserHandler = userHandler;
            mContext.getEventReceiver().addListener(userHandler);
            mFileName = filePath;
            mNumTextures = 0;
            mErrors = "";
        }
        
        public void onModelLoaded(GVRContext context, GVRSceneObject model, String modelFile) {
            mModel = model;
            if (mNumTextures == 0)
            {
                generateLoadEvent();
            }
        }
        
        /**
         * Called when a texture is successfully loaded.
         * @param texture texture that was loaded.
         */
        public void onTextureLoaded(GVRContext context, GVRTexture texture, String texFile)
        {
            if (mNumTextures > 0)
            {
                --mNumTextures;
                if (mNumTextures == 0)
                {
                    generateLoadEvent();
                }
            }           
        }
        
        /**
         * Called when a model cannot be loaded.
         * @param error error message
         */
        public void onModelError(GVRContext context, String error, String modelFile)
        {
            Log.e(TAG, "ERROR: model did not load: %s", error);
            mErrors += "Model " + modelFile + " did not load " + error + "\n";
            mModel = null;
            generateLoadEvent();
        }
        
        /**
         * Called when a texture cannot be loaded.
         * @param error error message
         */
        public void onTextureError(GVRContext context, String error, String texFile)
        {
            Log.e(TAG, "ERROR: cannot load texture %s %s", texFile, error);
            mErrors += "Texture " + texFile + " did not load " + error + "\n";
            if (mNumTextures > 0) {
                --mNumTextures;
                if (mNumTextures == 0)
                {
                    generateLoadEvent();
                }
            }                       
        }

        @Override
        public void onAssetLoaded(GVRContext context, GVRSceneObject model, String filePath, String errors)
        {
            mContext.getEventReceiver().removeListener(this);
        }
        
        private void generateLoadEvent()
        {
            if ((mScene != null) && (mModel != null))
            {
                mScene.addSceneObject(mModel);
            }
            mContext.getEventManager().sendEvent(mContext, IAssetEvents.class,
                    "onAssetLoaded", new Object[] { mContext, mModel, mFileName, mErrors });
            if (mUserHandler != null)
            {
                mContext.getEventReceiver().removeListener(mUserHandler);
                mUserHandler = null;
            }
        }
     }
    protected GVRContext mContext;
    
    public GVRAssetLoader(GVRContext context) {
        mContext = context;
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
    GVRAssimpImporter readFileFromAssets(GVRContext gvrContext,
            String filename, EnumSet<GVRImportSettings> settings) { 
        long nativeValue = NativeImporter.readFileFromAssets(gvrContext
                .getContext().getAssets(), filename, GVRImportSettings.getAssimpImportFlags(settings));
        return nativeValue == 0 ? null : new GVRAssimpImporter(gvrContext,
                nativeValue);
    }

    GVRAssimpImporter readFileFromResources(GVRContext gvrContext,
            int resourceId, EnumSet<GVRImportSettings> settings) {
        return readFileFromResources(gvrContext, new GVRAndroidResource(
                gvrContext, resourceId), settings);
    }

    /** @since 1.6.2 */
    GVRAssimpImporter readFileFromResources(GVRContext gvrContext,
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
    GVRAssimpImporter readFileFromSDCard(GVRContext gvrContext,
            String filename, EnumSet<GVRImportSettings> settings) {
        long nativeValue = NativeImporter.readFileFromSDCard(filename, GVRImportSettings.getAssimpImportFlags(settings));
        return new GVRAssimpImporter(gvrContext, nativeValue);
    }

    /**
     * Loads a scene object {@link GVRModelSceneObject} from
     * a 3D model and adds it to the scene.
     *
     * @param assetFile
     *            A filename, relative to the root of the volume.
     *
     * @param volumeType
     *            Where the asset is located (SD card, assets directory or network)
     *            
     * @param scene
     *            If present, this asset loader will wait until all of the textures have been
     *            loaded and then adds the model to the scene.
     *            
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException 
     *
     */
    public GVRModelSceneObject loadModel(String filePath, GVRResourceVolume.VolumeType volumeType,
            GVRScene scene) throws IOException
    {
        if (scene != null)
        {
            AssetEventListener assetListener = new AssetEventListener(mContext, scene);
            mContext.getEventReceiver().addListener(assetListener);
            assetListener.beginLoadAsset(filePath, null);
        }
        return loadJassimpModel(filePath, volumeType, GVRImportSettings.getRecommendedSettings(), false);
    }
    
    /**
     * Loads a scene object {@link GVRModelSceneObject} from
     * a 3D model and adds it to the scene.
     *
     * @param assetFile
     *            A filename, relative to the root of the volume.
     *
     * @param volumeType
     *            Where the asset is located (SD card, assets directory or network)
     *            
     * @param settings
     *            Additional import {@link GVRImportSettings settings}
     *
     * @param cacheEnabled
     *            If true, add the loaded model to the in-memory cache.
     *
     * @param scene
     *            If present, this asset loader will wait until all of the textures have been
     *            loaded and then adds the model to the scene.
     *            
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException 
     *
     */
    public GVRModelSceneObject loadModel(String filePath, GVRResourceVolume.VolumeType volumeType,
            EnumSet<GVRImportSettings> settings,
            boolean cacheEnabled,
            GVRScene scene) throws IOException
    {
        if (scene != null)
        {
            AssetEventListener assetListener = new AssetEventListener(mContext, scene);
            mContext.getEventReceiver().addListener(assetListener);
            assetListener.beginLoadAsset(filePath, null);
        }
        return loadJassimpModel(filePath, volumeType, settings, cacheEnabled);
    }

    /**
     * Loads a scene object {@link GVRModelSceneObject} from a 3D model.
     *
     * @param assetFile
     *            A filename, relative to the root of the volume.
     *
     * @param volumeType
     *            Where the asset is located (SD card, assets directory or network)
     *            
     * @param settings
     *            Additional import {@link GVRImportSettings settings}
     *
     * @param cacheEnabled
     *            If true, add the loaded model to the in-memory cache.
     *            
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException 
     *
     */
    private GVRModelSceneObject loadJassimpModel(String filePath, GVRResourceVolume.VolumeType volumeType,
            EnumSet<GVRImportSettings> settings, boolean cacheEnabled) throws IOException
    {
        Jassimp.setWrapperProvider(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp2.AiScene assimpScene = null;
        try
        {
            switch (volumeType)
            {
                case ANDROID_ASSETS:
                assimpScene = Jassimp.importAssetFile(filePath,
                        GVRJassimpAdapter.get().toJassimpSettings(settings),
                        mContext.getContext().getAssets());
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
                File tmpFile = downloadFile(mContext.getActivity(), filePath);
                if (tmpFile != null) {
                    assimpScene = Jassimp.importFile(tmpFile.getAbsolutePath(),
                            GVRJassimpAdapter.get().toJassimpSettings(settings));
                    tmpFile.delete();
                }
                break;
            }
        }
        catch (IOException ex)
        {
            assimpScene = null;
            mContext.getEventManager().sendEvent(this,
                    IAssetEvents.class,
                    "onModelError", new Object[] { mContext, ex.getMessage(), filePath });
            throw ex;
       }

        if (assimpScene == null) {
            String errmsg = "Cannot load model from path " + filePath + " from " + volumeType;
            mContext.getEventManager().sendEvent(this,
                    IAssetEvents.class,
                    "onModelError", new Object[] { mContext, errmsg, filePath });
            throw new IOException(errmsg);
        }
        
        Log.d(TAG, "start creating jassimp model %s", filePath);

        List<AiLight> lights = assimpScene.getLights();
        Hashtable<String, GVRLightBase> lightlist = new Hashtable<String, GVRLightBase>();
        importLights(lights, lightlist);

        GVRJassimpSceneObject sceneOb = new GVRJassimpSceneObject(mContext, assimpScene,
                new GVRResourceVolume(mContext, volumeType,
                        FileNameUtils.getParentDirectory(filePath),
                        cacheEnabled), lightlist);
        mContext.getEventManager().sendEvent(this,
                IAssetEvents.class,
                "onModelLoaded", new Object[] { mContext, (GVRSceneObject) sceneOb, filePath });
        return sceneOb;
    }
    
    protected void importLights(List<AiLight> lights,Hashtable<String, GVRLightBase> lightlist){
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

    protected void setLightProp(GVRLightBase gvrLight, AiLight assimpLight){
        gvrLight.setFloat("attenuation_constant", assimpLight.getAttenuationConstant());
        gvrLight.setFloat("attenuation_linear", assimpLight.getAttenuationLinear());
        gvrLight.setFloat("attenuation_quadratic", assimpLight.getAttenuationQuadratic());

    }

    protected void setPhongLightProp(GVRLightBase gvrLight, AiLight assimpLight){
        org.gearvrf.jassimp2.AiColor ambientCol= assimpLight.getColorAmbient(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp2.AiColor diffuseCol= assimpLight.getColorDiffuse(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp2.AiColor specular = assimpLight.getColorSpecular(GVRJassimpAdapter.sWrapperProvider);       
        gvrLight.setVec4("ambient_intensity", ambientCol.getRed(), ambientCol.getGreen(), ambientCol.getBlue(),ambientCol.getAlpha());
        gvrLight.setVec4("diffuse_intensity", diffuseCol.getRed(), diffuseCol.getGreen(),diffuseCol.getBlue(),diffuseCol.getAlpha());
        gvrLight.setVec4("specular_intensity", specular.getRed(),specular.getGreen(),specular.getBlue(), specular.getAlpha());

    }

    public static File downloadFile(Context context, String urlString) {
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

    protected GVRSceneObject getAssimpModel(final GVRContext context, String assetRelativeFilename,
            EnumSet<GVRImportSettings> settings) throws IOException {

        GVRAssimpImporter assimpImporter = readFileFromResources(
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
    private void recurseAssimpNodes(
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
    private GVRSceneObject createSceneObject(
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
                    context.getEventManager().sendEvent(GVRAssetLoader.this,
                            IAssetEvents.class,
                            "onTextureLoaded", new Object[] { context, texture, texDiffuseFileName });
                }

                @Override
                public void failed(Throwable t, GVRAndroidResource androidResource) {
                    context.getEventManager().sendEvent(GVRAssetLoader.this,
                            IAssetEvents.class,
                            "onTextureError", new Object[] { context, t.getMessage(), texDiffuseFileName });
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
    private AiMaterial getMeshMaterial(GVRAssimpImporter assimpImporter,
            String nodeName, int meshIndex) {
        return assimpImporter.getMeshMaterial(nodeName, meshIndex);
    }

    /**
     * State-less, should be fine having one instance
     */
    private final static GVROldWrapperProvider sWrapperProvider = new GVROldWrapperProvider();

    private final static String TAG = "GVRAssetLoader";

}

class NativeImporter {
    static native long readFileFromAssets(AssetManager assetManager,
            String filename, int settings);

    static native long readFileFromSDCard(String filename, int settings);

    static native long readFromByteArray(byte[] bytes, String filename, int settings);
}
