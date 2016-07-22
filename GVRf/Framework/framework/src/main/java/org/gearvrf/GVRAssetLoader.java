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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader.FutureResource;
import org.gearvrf.GVRImportSettings;

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
import org.gearvrf.jassimp2.JassimpFileIO;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.GVRByteArray;
import org.gearvrf.utility.Log;
import org.gearvrf.x3d.ShaderSettings;
import org.gearvrf.x3d.X3Dobject;
import org.gearvrf.x3d.X3DparseLights;
import android.content.Context;
import android.content.res.AssetManager;
import org.gearvrf.jassimp.AiColor;
import org.gearvrf.utility.ResourceCacheBase;
import org.gearvrf.utility.ResourceReader;

/**
 * {@link GVRAssetLoader} provides methods for importing 3D models and making them
 * available through instances of {@link GVRAssimpImporter}.
 * <p>
 * Supports importing models from an application's resources (both
 * {@code assets} and {@code res/raw}), from directories on the device's SD
 * card and URLs on the internet that the application has permission to read.
 */
public final class GVRAssetLoader {
    /**
     * Loads textures and listens for texture load events.
     * Raises the "onAssetLoaded" event after all textures have been loaded.
     */
    public static class AssetRequest implements IAssetEvents
    {
        protected final GVRContext        mContext;
        protected final GVRScene          mScene;
        protected final String            mFileName;
        protected final IAssetEvents      mUserHandler;
        protected final GVRResourceVolume mVolume;
        protected GVRSceneObject          mModel = null;
        protected String                  mErrors;
        protected int                     mNumTextures = 0;

        /**
         * Request to load an asset.
         * @param context GVRContext to get asset load events.
         * @param filePath path to file
         */
        public AssetRequest(GVRContext context, String filePath)
        {
            mScene = null;
            mContext = context;
            mNumTextures = 0;
            mFileName = filePath;
            mUserHandler = null;
            mErrors = "";
            mContext.getEventReceiver().addListener(this);
            mVolume = new GVRResourceVolume(mContext, filePath);
            Log.d(TAG, "ASSET: loading %s ...", mFileName);
        }

        /**
         * Request to load an asset and add it to the scene.
         * @param context GVRContext to get asset load events.
         * @param filePath path to file
         * @param scene GVRScene to add the asset to.
         */
        public AssetRequest(GVRContext context, String filePath, GVRScene scene)
        {
            mScene = scene;
            mContext = context;
            mNumTextures = 0;
            mFileName = filePath;
            mUserHandler = null;
            mErrors = "";
            mContext.getEventReceiver().addListener(this);
            mVolume = new GVRResourceVolume(mContext, filePath);
            Log.d(TAG, "ASSET: loading %s ...", mFileName);
        }

        /**
         * Request to load an asset and raise asset events.
         * @param context GVRContext to get asset load events.
         * @param filePath path to file
         * @param userHandler user event handler to get asset events.
         */
        public AssetRequest(GVRContext context, String filePath, IAssetEvents userHandler) {
            mScene = null;
            mContext = context;
            mNumTextures = 0;
            mFileName = filePath;
            mUserHandler = userHandler;
            mErrors = "";
            mContext.getEventReceiver().addListener(this);
            mVolume = new GVRResourceVolume(mContext, filePath);
            if (userHandler != null)
            {
                mContext.getEventReceiver().addListener(userHandler);
            }
            Log.d(TAG, "ASSET: loading %s ...", mFileName);
        }

        public GVRContext getContext()       { return mContext; }
        public GVRResourceVolume getVolume() { return mVolume; }
        public String getBaseName()
        {
        	String fname = getFileName();
            int i = fname.lastIndexOf("/");
            if (i > 0)
            {
                return  fname.substring(i + 1);
            }
            return fname;
        }
        
        public String getFileName()
        {
            if (mFileName.startsWith("sd:"))
            {
                return mFileName.substring(3);
            }
        	return mFileName;
        }

        /**
         * Load a texture asynchronously with a callback.
         * @param request callback that indicates which texture to load
         */
        public void loadTexture(TextureRequest request)
        {
            ++mNumTextures;
            try
            {
                GVRAndroidResource resource = mVolume.openResource(request.TextureFile);
                mContext.loadTexture(request, resource);
            }
            catch (IOException ex)
            {
                onTextureError(mContext, ex.getMessage(), request.TextureFile);
            }
        }

        /**
         * Load a future texture asynchronously with a callback.
         * @param request callback that indicates which texture to load
         */
        public Future<GVRTexture> loadFutureTexture(TextureRequest request)
        {
            ++mNumTextures;
            try
            {
                GVRAndroidResource resource = mVolume.openResource(request.TextureFile);
                FutureResource<GVRTexture> result = new FutureResource<GVRTexture>(resource);
                mContext.loadTexture(request, resource);
                return result;
            }
            catch (IOException ex)
            {
                onTextureError(mContext, ex.getMessage(), request.TextureFile);
            }
            return null;
         }

        /**
         * Called when a model is successfully loaded.
         * @param context   GVRContext which loaded the model
         * @param model     root node of model hierarchy that was loaded
         * @param modelFile filename of model loaded
         */
        public void onModelLoaded(GVRContext context, GVRSceneObject model, String modelFile) {
            mModel = model;
            Log.d(TAG, "ASSET: successfully loaded model %s", modelFile);
            if (mNumTextures == 0)
            {
                generateLoadEvent();
            }
        }

        /**
         * Called when a texture is successfully loaded.
         * @param context GVRContext which loaded the texture
         * @param texture texture that was loaded
         * @param texFile filename of texture loaded
         */
        public void onTextureLoaded(GVRContext context, GVRTexture texture, String texFile)
        {
            if (mNumTextures > 0)
            {
                --mNumTextures;
                if ((mNumTextures == 0) && (mModel != null))
                {
                    generateLoadEvent();
                }
            }
        }

        /**
         * Called when a model cannot be loaded.
         * @param context GVRContext which loaded the texture
         * @param error error message
         * @param modelFile filename of model loaded
         */
        public void onModelError(GVRContext context, String error, String modelFile)
        {
            Log.e(TAG, "ASSET: ERROR: model %s did not load %s", modelFile, error);
            mErrors += "Model " + modelFile + " did not load " + error + "\n";
            mModel = null;
            generateLoadEvent();
        }

        /**
         * Called when a texture cannot be loaded.
         * @param context GVRContext which loaded the texture
         * @param error error message
          * @param texFile filename of texture loaded
        */
        public void onTextureError(GVRContext context, String error, String texFile)
        {
            Log.e(TAG, "ASSET: ERROR: texture did %s not load %s", texFile, error);
            mErrors += "Texture " + texFile + " did not load " + error + "\n";
            if (mNumTextures > 0)
            {
                --mNumTextures;
                if ((mNumTextures == 0) && (mModel != null))
                {
                    generateLoadEvent();
                }
            }
        }

        /**
         * Called when the model and all of its textures have loaded.
         * @param context GVRContext which loaded the texture
         * @param model model that was loaded (will be null if model failed to load)
         * @param error error messages (will be null if no errors)
         * @param modelFile filename of model loaded
         */
        @Override
        public void onAssetLoaded(GVRContext context, GVRSceneObject model, String modelFile, String errors)
        {
            mContext.getEventReceiver().removeListener(this);
        }

        private void generateLoadEvent()
        {
            String errors = !"".equals(mErrors) ? mErrors : null;
            if ((mScene != null) && (mModel != null) && (errors == null))
            {
                Log.d(TAG, "ASSET: asset %s added to scene", mFileName);
                mScene.addSceneObject(mModel);
            }
            mContext.getEventManager().sendEvent(mContext, IAssetEvents.class,
                    "onAssetLoaded", new Object[] { mContext, mModel, mFileName, errors });
            if (mUserHandler != null)
            {
                mContext.getEventReceiver().removeListener(mUserHandler);
            }
        }
     }

    /**
     * Texture load callback the generates asset events.
     */
    public static class TextureRequest implements TextureCallback
    {
        public final String TextureFile;
        protected final GVRContext mContext;

        public TextureRequest(GVRContext context, String texFile)
        {
            mContext = context;
            TextureFile = texFile;
        }

        public void loaded(GVRTexture texture, GVRAndroidResource ignored)
        {
            mContext.getEventManager().sendEvent(mContext,
                    IAssetEvents.class,
                    "onTextureLoaded", new Object[] { mContext, texture, TextureFile });
        }

        @Override
        public void failed(Throwable t, GVRAndroidResource androidResource)
        {
            mContext.getEventManager().sendEvent(mContext,
                    IAssetEvents.class,
                    "onTextureError", new Object[] { mContext, t.getMessage(), TextureFile });
        }

        @Override
        public boolean stillWanted(GVRAndroidResource androidResource)
        {
            return true;
        }
    }

    /**
     * Texture load callback that binds the texture to the material.
     */
    public static class MaterialTextureRequest extends TextureRequest
    {
        public final GVRMaterial Material;
        public final String TextureName;

        public MaterialTextureRequest(GVRContext context, String texFile)
        {
        	super(context, texFile);
            Material = null;
            TextureName = null;
        }

        public MaterialTextureRequest(GVRContext context, String texFile, GVRMaterial material, String textureName)
        {
        	super(context, texFile);
            Material = material;
            TextureName = textureName;
            if (Material != null)
            {
                Material.setTexture(textureName, (GVRTexture) null);
            }
        }

        public void loaded(GVRTexture texture, GVRAndroidResource ignored)
        {
            if (Material != null)
            {
                Material.setTexture(TextureName, texture);
            }
            super.loaded(texture,  ignored);
        }
    }

    protected GVRContext mContext;
    public GVRAssetLoader(GVRContext context) {
        mContext = context;
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


    // IO Handler for Jassimp
    static class ResourceVolumeIO implements JassimpFileIO {
        private GVRResourceVolume volume;

        ResourceVolumeIO(GVRResourceVolume volume) {
            this.volume = volume;
        }

        @Override
        public byte[] read(String path) {
            GVRAndroidResource resource = null;
            try {
                resource = volume.openResource(path);
                InputStream stream = resource.getStream();
                if (stream == null) {
                    return null;
                }
                byte data[] = ResourceReader.readStream(stream);
                return data;
            } catch (IOException e) {
                return null;
            } finally {
                if (resource != null) {
                    resource.closeStream();
                }
            }
        }

        protected GVRResourceVolume getResourceVolume() {
            return volume;
        }
    };

    static class CachedVolumeIO implements JassimpFileIO {
        protected ResourceVolumeIO uncachedIO;
        protected ResourceCacheBase<GVRByteArray> cache;

        public CachedVolumeIO(ResourceVolumeIO uncachedIO) {
            this.uncachedIO = uncachedIO;
            cache = new ResourceCacheBase<GVRByteArray>();
        }

        @Override
        public byte[] read(String path) {
            try {
                GVRAndroidResource resource = uncachedIO.getResourceVolume().openResource(path);
                GVRByteArray byteArray = cache.get(resource);
                if (byteArray == null) {
                    resource.closeStream(); // needed to avoid hanging
                    byteArray = GVRByteArray.wrap(uncachedIO.read(path));
                    cache.put(resource, byteArray);
                }
                return byteArray.getByteArray();
            } catch (IOException e) {
                return null;
            }
        }
    }

    /**
     * Loads a scene object {@link GVRModelSceneObject} from
     * a 3D model and adds it to the scene.
     *
     * @param assetFile
     *            A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
     *
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException
     *
     */
    public GVRModelSceneObject loadModel(final String filePath) throws IOException {
        return loadModel(filePath, (GVRScene)null);
    }

    /**
     * Loads a scene object {@link GVRModelSceneObject} from
     * a 3D model and adds it to the scene.
     *
     * @param filePath
     *            A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
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
    public GVRModelSceneObject loadModel(String filePath, GVRScene scene) throws IOException
    {
        AssetRequest assetRequest = new AssetRequest(mContext, filePath, scene);
        String ext = filePath.substring(filePath.length() - 3).toLowerCase();
        if (ext.equals("x3d"))
            return loadX3DModel(assetRequest, GVRImportSettings.getRecommendedSettings(), false);
        else
            return loadJassimpModel(assetRequest, GVRImportSettings.getRecommendedSettings(), false);
    }

    /**
     * Loads a scene object {@link GVRModelSceneObject} from
     * a 3D model and raises asset events to a handler.
     *
     * @param filePath
     *            A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
     *
     * @param handler
     *            IAssetEvents handler to process asset loading events
     *            
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException 
     *
     */
    public GVRModelSceneObject loadModel(String filePath, IAssetEvents handler) throws IOException
    {
        AssetRequest assetRequest = new AssetRequest(mContext, filePath, handler);

        String ext = filePath.substring(filePath.length() - 3).toLowerCase();
        if (ext.equals("x3d"))
            return loadX3DModel(assetRequest, GVRImportSettings.getRecommendedSettings(), false);
        else
            return loadJassimpModel(assetRequest, GVRImportSettings.getRecommendedSettings(), false);
    }
    
    
    /**
     * Loads a scene object {@link GVRModelSceneObject} from
     * a 3D model and raises asset events to a handler.
     *
     * @param filePath
     *            A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
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
    public GVRModelSceneObject loadModel(String filePath,
            EnumSet<GVRImportSettings> settings,
            boolean cacheEnabled,
            GVRScene scene) throws IOException
    {
        AssetRequest assetRequest = new AssetRequest(mContext, filePath, scene);
        String ext = filePath.substring(filePath.length() - 3).toLowerCase();

		if (ext.equals("x3d"))
		    return loadX3DModel(assetRequest, GVRImportSettings.getRecommendedSettings(), false);
		else
		    return loadJassimpModel(assetRequest, GVRImportSettings.getRecommendedSettings(), false);
    }


    /**
     * Loads a scene object {@link GVRModelSceneObject} from a 3D model.
     *
     * @param filePath
     *            A filename, relative to the root of the volume.
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
    private GVRModelSceneObject loadJassimpModel(AssetRequest request,
            EnumSet<GVRImportSettings> settings, boolean cacheEnabled) throws IOException
    {
        Jassimp.setWrapperProvider(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp2.AiScene assimpScene = null;
        String filePath = request.getBaseName();
        GVRResourceVolume volume = request.getVolume();
        try
        {
            assimpScene = Jassimp.importFileEx(FileNameUtils.getFilename(filePath),
                    GVRJassimpAdapter.get().toJassimpSettings(settings),
                    new CachedVolumeIO(new ResourceVolumeIO(volume)));
        }
        catch (IOException ex)
        {
            assimpScene = null;
            mContext.getEventManager().sendEvent(mContext,
                    IAssetEvents.class,
                    "onModelError", new Object[] { mContext, ex.getMessage(), filePath });
            throw ex;
       }

        if (assimpScene == null) {
            String errmsg = "Cannot load model from path " + filePath;
            mContext.getEventManager().sendEvent(mContext,
                    IAssetEvents.class,
                    "onModelError", new Object[] { mContext, errmsg, filePath });
            throw new IOException(errmsg);
        }        
        List<AiLight> lights = assimpScene.getLights();
        Hashtable<String, GVRLightBase> lightlist = new Hashtable<String, GVRLightBase>();
        importLights(lights, lightlist);

        GVRJassimpSceneObject sceneObj = new GVRJassimpSceneObject(request, assimpScene, volume, lightlist);
        mContext.getEventManager().sendEvent(mContext,
                IAssetEvents.class,
                "onModelLoaded", new Object[] { mContext, (GVRSceneObject) sceneObj, filePath });
         return sceneObj;
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
        GVRAssetLoader.AssetRequest request = new GVRAssetLoader.AssetRequest(context, assetRelativeFilename);
        recurseAssimpNodes(request, assimpImporter, assetRelativeFilename, wholeSceneObject,
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
     * @param parentSceneObject A reference of the {@link GVRSceneObject}, to
     *            which all other scene objects are attached.
     * @param node A reference to the AiNode for which we want to recurse all
     *            its children and meshes.
     * @param wrapperProvider AiWrapperProvider for unwrapping Jassimp
     *            properties.
     */
    @SuppressWarnings("resource")
    private void recurseAssimpNodes(
            GVRAssetLoader.AssetRequest request,
            GVRAssimpImporter assimpImporter,
            String assetRelativeFilename,
            GVRSceneObject parentSceneObject,
            AiNode node,
            GVROldWrapperProvider wrapperProvider) {
        GVRContext context = mContext;
        try {
            GVRSceneObject newParentSceneObject = new GVRSceneObject(context);

            if (node.getNumMeshes() == 0) {
                parentSceneObject.addChildObject(newParentSceneObject);
                parentSceneObject = newParentSceneObject;
            } else if (node.getNumMeshes() == 1) {
                // add the scene object to the scene graph
                GVRSceneObject sceneObject = createSceneObject(request,
                        assimpImporter, assetRelativeFilename, node, 0, wrapperProvider);
                parentSceneObject.addChildObject(sceneObject);
                parentSceneObject = sceneObject;
            } else {
                for (int i = 0; i < node.getNumMeshes(); i++) {
                    GVRSceneObject sceneObject = createSceneObject(request,
                            assimpImporter, assetRelativeFilename, node, i, wrapperProvider);
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
                recurseAssimpNodes(request, assimpImporter, assetRelativeFilename,
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
     * @param wrapperProvider AiWrapperProvider for unwrapping Jassimp
     *            properties.
     * @return The new {@link GVRSceneObject} with the mesh at the index
     *         {@link index} for the node {@link node}
     * @throws IOException File does not exist or cannot be read
     */
    private GVRSceneObject createSceneObject(
            GVRAssetLoader.AssetRequest assetRequest,
            GVRAssimpImporter assimpImporter,
            String assetRelativeFilename,
            AiNode node,
            int index,
            GVROldWrapperProvider wrapperProvider)
                    throws IOException {
        final GVRContext context = assetRequest.getContext();
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
            assetRequest.loadTexture(new MaterialTextureRequest(mContext, texDiffuseFileName, meshMaterial, "diffuseTexture"));
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

    GVRModelSceneObject loadX3DModel(GVRAssetLoader.AssetRequest assetRequest,
            EnumSet<GVRImportSettings> settings, boolean cacheEnabled)
                    throws IOException {
        GVRModelSceneObject root = new GVRModelSceneObject(mContext);
        GVRResourceVolume volume = assetRequest.getVolume();
        InputStream inputStream = null;
        String fileName = assetRequest.getBaseName();
        GVRAndroidResource resource = volume.openResource(fileName);

        org.gearvrf.x3d.X3Dobject x3dObject = new org.gearvrf.x3d.X3Dobject(assetRequest, root);
        try {
             ShaderSettings shaderSettings = new ShaderSettings(new GVRMaterial(mContext));
             if (!X3Dobject.UNIVERSAL_LIGHTS) {
                X3DparseLights x3dParseLights = new X3DparseLights(mContext, root);
                inputStream = resource.getStream();
                if (inputStream == null) {
                	throw new FileNotFoundException(fileName + " not found");
                }
                Log.d(TAG, "Parse: " + fileName);
                x3dParseLights.Parse(inputStream, shaderSettings);
                inputStream.close();
              }
              inputStream = resource.getStream();
              if (inputStream == null) {
              	throw new FileNotFoundException(fileName + " not found");
              }
              x3dObject.Parse(inputStream, shaderSettings);
              inputStream.close();
              mContext.getEventManager().sendEvent(mContext,
                                                   IAssetEvents.class,
                                                   "onModelLoaded", new Object[] { mContext, (GVRSceneObject) root, fileName });
        }
        catch (FileNotFoundException e) {
          mContext.getEventManager().sendEvent(mContext,
                                               IAssetEvents.class,
                                               "onModelError", new Object[] { mContext, e.getMessage(), fileName });
        }
        catch (IOException e1) {
          mContext.getEventManager().sendEvent(mContext,
                                               IAssetEvents.class,
                                               "onModelError", new Object[] { mContext, e1.getMessage(), fileName });
        }
        catch (Exception e2) {
          mContext.getEventManager().sendEvent(mContext,
                                               IAssetEvents.class,
                                               "onModelError", new Object[] { mContext, e2.getMessage(), fileName });
          e2.printStackTrace();
        }
        return root;
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

