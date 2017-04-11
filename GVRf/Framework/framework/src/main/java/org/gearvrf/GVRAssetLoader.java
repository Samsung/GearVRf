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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.gearvrf.GVRAndroidResource.TextureCallback;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader;
import org.gearvrf.asynchronous.GVRAsynchronousResourceLoader.FutureResource;
import org.gearvrf.asynchronous.GVRCompressedTexture;
import org.gearvrf.asynchronous.GVRCompressedTextureLoader;
import org.gearvrf.jassimp.AiTexture;
import org.gearvrf.jassimp.Jassimp;
import org.gearvrf.jassimp.JassimpFileIO;
import org.gearvrf.scene_objects.GVRModelSceneObject;
import org.gearvrf.utility.FileNameUtils;
import org.gearvrf.utility.GVRByteArray;
import org.gearvrf.utility.Log;
import org.gearvrf.utility.ResourceCache;
import org.gearvrf.utility.ResourceCacheBase;
import org.gearvrf.utility.ResourceReader;
import org.gearvrf.utility.Threads;
import org.gearvrf.x3d.ShaderSettings;
import org.gearvrf.x3d.X3Dobject;
import org.gearvrf.x3d.X3DparseLights;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

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
     * The priority used by
     * {@link #loadTexture(GVRAndroidResource, GVRAndroidResource.TextureCallback)}
     */
    public static final int DEFAULT_PRIORITY = 0;

    /**
     * The default texture parameter instance for overloading texture methods
     *
     */
    private final GVRTextureParameters mDefaultTextureParameters;

    /**
     * Loads textures and listens for texture load events.
     * Raises the "onAssetLoaded" event after all textures have been loaded.
     * This listener is NOT attached to the event manager. It is explicitly
     * called by GVRAssetLoader to get around the restriction that GVRContext
     * can only have a single listener for asset events.
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
        protected Integer                 mNumTextures;
        protected boolean                 mReplaceScene = false;

        /**
         * Request to load an asset.
         * @param context GVRContext to get asset load events.
         * @param fileVolume GVRResourceVolume containing path to file
         */
        public AssetRequest(GVRContext context, GVRResourceVolume fileVolume)
        {
            mScene = null;
            mContext = context;
            mNumTextures = 0;
            mFileName = fileVolume.getFileName();
            mUserHandler = null;
            mErrors = "";
            mVolume = fileVolume;
            Log.d(TAG, "ASSET: loading %s ...", mFileName);
        }

        /**
         * Request to load an asset.
         * @param context GVRContext to get asset load events.
         * @param resource GVRAndroidResource describing the file to load.
         */
        public AssetRequest(GVRContext context, GVRAndroidResource resource, GVRScene scene)
        {
            mScene = scene;
            mContext = context;
            mNumTextures = 0;
            mFileName = resource.getResourceFilename();
            mUserHandler = null;
            mErrors = "";
            mVolume = new GVRResourceVolume(mContext, mFileName);
            mVolume.addResource(resource);
            Log.d(TAG, "ASSET: loading %s ...", mFileName);
        }

        /**
         * Request to load an asset and add it to the scene.
         * @param model GVRSceneObject to be the root of the loaded asset.
         * @param fileVolume GVRResourceVolume containing path to file
         * @param scene GVRScene to add the asset to.
         * @param userHandler user event handler to get asset events.
         * @param replaceScene true to replace entire scene with model, false to add model to scene
         */
        public AssetRequest(GVRSceneObject model, GVRResourceVolume fileVolume, GVRScene scene, IAssetEvents userHandler, boolean replaceScene)
        {
            mScene = scene;
            mContext = model.getGVRContext();
            mNumTextures = 0;
            mFileName = fileVolume.getFileName();
            mUserHandler = userHandler;
            mModel = null;
            mErrors = "";
            mReplaceScene = replaceScene;
            mVolume = fileVolume;
            Log.d(TAG, "ASSET: loading %s ...", mFileName);
        }


        public GVRContext getContext()       { return mContext; }
        public boolean replaceScene()        { return mReplaceScene; }
        public GVRResourceVolume getVolume() { return mVolume; }
        public String getBaseName()
        {
        	String fname = mVolume.getFileName();
            int i = fname.lastIndexOf("/");
            if (i > 0)
            {
                return  fname.substring(i + 1);
            }
            return fname;
        }

        /**
         * Load a texture asynchronously with a callback.
         * @param request callback that indicates which texture to load
         */
        public void loadTexture(TextureRequest request)
        {
            synchronized (mNumTextures)
            {
                ++mNumTextures;
                Log.d(TAG, "ASSET: loadTexture %s %d", request.TextureFile, mNumTextures);
                try
                {
                    GVRAndroidResource resource = mVolume.openResource(request.TextureFile);
                    mContext.getAssetLoader().loadTexture(resource, request);
                }
                catch (IOException ex)
                {
                    request.loaded(getDefaultTexture(mContext), null);
                    onTextureError(mContext, ex.getMessage(), request.TextureFile);
                }
            }
        }

        /**
         * Load a future texture asynchronously with a callback.
         * @param request callback that indicates which texture to load
         */
        public Future<GVRTexture> loadFutureTexture(TextureRequest request)
        {
            synchronized (mNumTextures)
            {
                ++mNumTextures;
                Log.d(TAG, "ASSET: loadFutureTexture %s %d", request.TextureFile, mNumTextures);
            }
            try
            {
                GVRAndroidResource resource = mVolume.openResource(request.TextureFile);
                FutureResource<GVRTexture> result = new FutureResource<GVRTexture>(resource);
                mContext.getAssetLoader().loadTexture(resource, request);
                return result;
            }
            catch (IOException ex)
            {
                request.loaded(getDefaultTexture(mContext), null);
                onTextureError(mContext, ex.getMessage(), request.TextureFile);
            }
            return null;
         }

        /**
         * Load an embedded RGBA texture from the JASSIMP AiScene.
         * An embedded texture is represented as an AiTexture object in Java.
         * The AiTexture contains the pixel data for the bitmap.
         *
         * @param request TextureRequest for the embedded texture reference.
         *                The filename inside starts with '*' followed
         *                by an integer texture index into AiScene embedded textures
         * @param aitex   Assimp texture containing the pixel data
         * @return GVRTexture made from embedded texture
         */
        public GVRTexture loadEmbeddedTexture(final TextureRequest request, final AiTexture aitex, final GVRTextureParameters texParams)
        {
            GVRAndroidResource resource = null;
            GVRBitmapTexture bmapTex;

            Log.d(TAG, "ASSET: loadEmbeddedTexture %s %d", request.TextureFile, mNumTextures);
            try
            {
                resource = new GVRAndroidResource(request.TextureFile);
            }
            catch (IOException ex)
            {
                request.failed(ex, resource);
            }
            Map<String, GVRTexture> texCache = GVRAssetLoader.getEmbeddedTextureCache();
            synchronized (texCache)
            {
                GVRTexture tex = texCache.get(request.TextureFile);
                if (tex != null)
                {
                    Log.d(TAG, "ASSET: loadEmbeddedTexture found %s", resource.getResourceFilename());
                    return tex;
                }
                synchronized (mNumTextures)
                {
                    ++mNumTextures;
                }
                Bitmap bmap;
                if (aitex.getHeight() == 0)
                {
                    ByteArrayInputStream input = new ByteArrayInputStream(aitex.getByteData());
                    bmap = BitmapFactory.decodeStream(input);
                }
                else
                {
                    bmap = Bitmap.createBitmap(aitex.getWidth(), aitex.getHeight(), Bitmap.Config.ARGB_8888);
                    bmap.setPixels(aitex.getIntData(), 0, aitex.getWidth(), 0, 0, aitex.getWidth(), aitex.getHeight());
                }
                bmapTex = new GVRBitmapTexture(mContext, bmap, texParams);
                Log.d(TAG, "ASSET: loadEmbeddedTexture saved %s", resource.getResourceFilename());
                texCache.put(request.TextureFile, bmapTex);
            }
            request.loaded(bmapTex, resource);
            return bmapTex;
        }

        /**
         * Called when a model is successfully loaded.
         * @param context   GVRContext which loaded the model
         * @param model     root node of model hierarchy that was loaded
         * @param modelFile filename of model loaded
         */
        public void onModelLoaded(GVRContext context, GVRSceneObject model, String modelFile) {
            mModel = model;
            Log.d(TAG, "ASSET: successfully loaded model %s %d", modelFile, mNumTextures);
            if (mUserHandler != null)
            {
                mUserHandler.onModelLoaded(context, model, modelFile);
            }
            mContext.getEventManager().sendEvent(mContext,
                    IAssetEvents.class,
                    "onModelLoaded", new Object[]{mContext, model, modelFile});
            if (mNumTextures == 0)
            {
                generateLoadEvent();
            }
            else
            {
                Log.d(TAG, "ASSET: %s has %d outstanding textures", modelFile, mNumTextures);
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
            if (mUserHandler != null)
            {
                mUserHandler.onTextureLoaded(context, texture, texFile);
            }
            mContext.getEventManager().sendEvent(mContext, IAssetEvents.class,
                                                 "onTextureLoaded", new Object[] { mContext, texture, texFile });
            synchronized (mNumTextures)
            {
                Log.e(TAG, "ASSET: successfully loaded texture %s %d", texFile, mNumTextures);
                if (mNumTextures >= 1)
                {
                    --mNumTextures;
                    if (mNumTextures != 0)
                    {
                        return;
                    }
                } else
                {
                    return;
                }
            }
            if (mModel != null)
            {
                generateLoadEvent();
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
            if (mUserHandler != null)
            {
                mUserHandler.onModelError(context, error, modelFile);
            }
            mContext.getEventManager().sendEvent(mContext,
                    IAssetEvents.class,
                    "onModelError", new Object[] { mContext, error, modelFile });
            mErrors += error + "\n";
            mModel = null;
            mNumTextures = 0;
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
            mErrors += error + "\n";
            if (mUserHandler != null)
            {
                mUserHandler.onTextureError(context, error, texFile);
            }
            mContext.getEventManager().sendEvent(mContext, IAssetEvents.class,
                                                 "onTextureError", new Object[] { mContext, error, texFile });
            synchronized (mNumTextures)
            {
                if (mNumTextures >= 1)
                {
                    --mNumTextures;
                    if (mNumTextures != 0)
                    {
                        return;
                    }
                } else
                {
                    return;
                }
            }
            if (mModel != null)
            {
                generateLoadEvent();
            }
        }

        /**
         * Called when the model and all of its textures have loaded.
         * @param context GVRContext which loaded the texture
         * @param model model that was loaded (will be null if model failed to load)
         * @param errors error messages (will be null if no errors)
         * @param modelFile filename of model loaded
         */
        @Override
        public void onAssetLoaded(GVRContext context, GVRSceneObject model, String modelFile, String errors)
        {
            if (mUserHandler != null)
            {
                mUserHandler.onAssetLoaded(context, model, modelFile, errors);
            }
            mContext.getEventManager().sendEvent(mContext, IAssetEvents.class,
                                                 "onAssetLoaded", new Object[] { mContext, mModel, mFileName, errors });
        }

        /**
         * Generate the onAssetLoaded event.
         * Add the model to the scene and start animations.
         */
        private void generateLoadEvent()
        {
            String errors = !"".equals(mErrors) ? mErrors : null;
            if (mModel != null)
            {
                if ((mScene != null))
                {
                    /*
                     * If the model does not already have a parent,
                     * add it to the scene.
                     */
                    if (mModel.getParent() == null)
                    {
                        Log.d(TAG, "ASSET: asset %s added to scene", mFileName);
                        if (mReplaceScene)
                        {
                            GVRSceneObject mainCam = mModel.getSceneObjectByName("MainCamera");
                            GVRCameraRig modelCam =
                                    (mainCam != null) ? mainCam.getCameraRig() : null;

                            mScene.clear();
                            if (modelCam != null)
                            {
                                GVRCameraRig sceneCam = mScene.getMainCameraRig();
                                sceneCam.getTransform().setModelMatrix(
                                        mainCam.getTransform().getLocalModelMatrix());
                                sceneCam.setNearClippingDistance(
                                        modelCam.getNearClippingDistance());
                                sceneCam.setFarClippingDistance(modelCam.getFarClippingDistance());
                                sceneCam.setCameraRigType(modelCam.getCameraRigType());
                            }
                        }
                        mScene.addSceneObject(mModel);
                    }
                    /*
                     * Bind appropriate shaders in case asset contains lights
                     */
                    else
                    {
                        mScene.bindShaders(mModel);
                    }
                }
                /*
                 * If the model has animations, start them now.
                 */
                GVRAnimator animator = (GVRAnimator) mModel.getComponent(GVRAnimator.getComponentType());
                if ((animator != null) && animator.autoStart())
                {
                    animator.start();
                }
            }
            onAssetLoaded(mContext, mModel, mFileName, errors);
        }
     }

    /**
     * Texture load callback the generates asset events.
     */
    public static class TextureRequest implements TextureCallback
    {
        public final String TextureFile;
        protected GVRTextureParameters mTexParams;
        protected AssetRequest mAssetRequest;
        private boolean loadFinished;   // in case loaded(), failed() called more than once

        public TextureRequest(AssetRequest assetRequest, String texFile, final GVRTextureParameters texParams)
        {
            mAssetRequest = assetRequest;
            TextureFile = texFile;
            mTexParams = texParams;
            loadFinished = false;
        }

        public void loaded(final GVRTexture texture, GVRAndroidResource resource)
        {
            texture.getGVRContext().runOnGlThread(new Runnable()
            {
                public void run()
                {
                    texture.updateTextureParameters(mTexParams);
                }
            });
            if (!loadFinished)
            {
                loadFinished = true;
                mAssetRequest.onTextureLoaded(texture.getGVRContext(), texture, TextureFile);
            }
        }

        @Override
        public void failed(Throwable t, GVRAndroidResource androidResource)
        {
            if (!loadFinished)
            {
                loadFinished = true;
                mAssetRequest.onTextureError(mAssetRequest.getContext(), t.getMessage(), TextureFile);
                loaded(getDefaultTexture(mAssetRequest.getContext()), null);
            }
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

        public MaterialTextureRequest(AssetRequest assetRequest, String texFile, GVRMaterial material, String textureName,
                                      final GVRTextureParameters texParams)
        {
        	super(assetRequest, texFile, texParams);
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
    protected static ResourceCache<GVRTexture> mTextureCache = new ResourceCache<GVRTexture>();
    protected static HashMap<String, GVRTexture> mEmbeddedCache = new HashMap<String, GVRTexture>();

    protected static GVRTexture mDefaultTexture = null;

    /**
     * When the application is restarted we recreate the texture cache
     * since all of the GL textures have been deleted.
     */
    static
    {
        GVRContext.addResetOnRestartHandler(new Runnable() {

            @Override
            public void run() {
                mTextureCache = new ResourceCache<GVRTexture>();
                mEmbeddedCache = new HashMap<String, GVRTexture>();
                mDefaultTexture = null;
            }
        });
    }

    /**
     * Construct an instance of the asset loader
     * @param context GVRContext to get asset load events
     */
    public GVRAssetLoader(GVRContext context)
    {
        mContext = context;
        mDefaultTextureParameters = new GVRTextureParameters(context);
    }

    /**
     * Get the embedded texture cache.
     * This is an internal routine used during asset loading for processing
     * embedded textures.
     * @return embedded texture cache
     */
    static Map<String, GVRTexture> getEmbeddedTextureCache()
    {
        return mEmbeddedCache;
    }

    private static GVRTexture getDefaultTexture(GVRContext ctx)
    {
        if (mDefaultTexture == null)
        {
            GVRAndroidResource r = new GVRAndroidResource(ctx, R.drawable.white_texture);
            mDefaultTexture = ctx.getAssetLoader().loadTexture(r);
        }
        return mDefaultTexture;
    }

    /**
     * Loads file placed in the assets folder, as a {@link GVRBitmapTexture}
     * with the user provided texture parameters.
     *
     * <p>
     * Note that this method may take hundreds of milliseconds to return: unless
     * the texture is quite tiny, you probably don't want to call this directly
     * from your {@link GVRMain#onStep() onStep()} callback as that is called
     * once per frame, and a long call will cause you to miss frames. For large
     * images, you should use
     * {@link #loadTexture(GVRAndroidResource, GVRAndroidResource.TextureCallback)}.
     * <p>
     * This method automatically scales large images to fit the GPU's
     * restrictions and to avoid {@linkplain OutOfMemoryError out of memory
     * errors.}
     *
     * @param resource
     *            Basically, a stream containing a bitmap texture. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * @param textureParameters
     *            The texture parameter object which has all the values that
     *            were provided by the user for texture enhancement. The
     *            {@link GVRTextureParameters} class has methods to set all the
     *            texture filters and wrap states. If this parameter is nullo,
     *            default texture parameters are used.
     * @return The file as a texture, or {@code null} if the file can not be
     *         decoded into a Bitmap.
     * @see GVRAssetLoader#getDefaultTextureParameters
     */
    public GVRTexture loadTexture(GVRAndroidResource resource,
                                  GVRTextureParameters textureParameters)
    {
        GVRBitmapTexture bmapTex = null;
        synchronized (mTextureCache)
        {
            GVRTexture texture = mTextureCache.get(resource);
            if (texture != null)
            {
                return texture;
            }
            bmapTex = new GVRBitmapTexture(mContext, (Bitmap) null, textureParameters);
            mTextureCache.put(resource, bmapTex);
        }
        try
        {
            Bitmap bitmap = GVRAsynchronousResourceLoader.decodeStream(resource.getStream(), false);
            resource.closeStream();
            if (bitmap != null)
            {
                bmapTex.update(bitmap);
            }
        }
        catch (IOException ex)
        {
            return null;
        }
        return bmapTex;
    }

    public GVRTexture loadTexture(GVRAndroidResource resource)
    {
        return loadTexture(resource, mDefaultTextureParameters);
    }


    /**
     * Loads a texture asynchronously.
     *
     * This method can detect whether the resource file holds a compressed
     * texture (GVRF currently supports ASTC, ETC2, and KTX formats:
     * applications can add new formats by implementing
     * {@link GVRCompressedTextureLoader}): if the file is not a compressed
     * texture, it is loaded as a normal, bitmapped texture. This format
     * detection adds very little to the cost of loading even a compressed
     * texture, and it makes your life a lot easier: you can replace, say,
     * {@code res/raw/resource.png} with {@code res/raw/resource.etc2} without
     * having to change any code.
     *
     * @param callback
     *            Before loading, GVRF may call
     *            {@link GVRAndroidResource.TextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} several times (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     *
     *            Successful loads will call
     *            {@link GVRAndroidResource.Callback#loaded(GVRHybridObject, GVRAndroidResource)
     *            loaded()} on the GL thread;
     *
     *            any errors will call
     *            {@link GVRAndroidResource.TextureCallback#failed(Throwable, GVRAndroidResource)
     *            failed()}, with no promises about threading.
     *
     *            <p>
     *            This method uses a throttler to avoid overloading the system.
     *            If the throttler has threads available, it will run this
     *            request immediately. Otherwise, it will enqueue the request,
     *            and call
     *            {@link GVRAndroidResource.TextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} at least once (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     *
     *            <p>
     *            Use {@link #loadFutureTexture(GVRAndroidResource)} to avoid
     *            having to implement a callback.
     * @param resource
     *            Basically, a stream containing a texture file. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     * @param texparams
     *            GVRTextureParameters object containing texture sampler attributes.
     * @param priority
     *            This request's priority. Please see the notes on asynchronous
     *            priorities in the <a href="package-summary.html#async">package
     *            description</a>. Also, please note priorities only apply to
     *            uncompressed textures (standard Android bitmap files, which
     *            can take hundreds of milliseconds to load): compressed
     *            textures load so quickly that they are not run through the
     *            request scheduler.
     * @param quality
     *            The compressed texture {@link GVRCompressedTexture#mQuality
     *            quality} parameter: should be one of
     *            {@link GVRCompressedTexture#SPEED},
     *            {@link GVRCompressedTexture#BALANCED}, or
     *            {@link GVRCompressedTexture#QUALITY}, but other values are
     *            'clamped' to one of the recognized values. Please note that
     *            this (currently) only applies to compressed textures; normal
     *            {@linkplain GVRBitmapTexture bitmapped textures} don't take a
     *            quality parameter.
     */
    public void loadTexture(GVRAndroidResource resource, TextureCallback callback, GVRTextureParameters texparams, int priority, int quality)
    {
        if (texparams == null)
        {
            texparams = mDefaultTextureParameters;
        }
        GVRAsynchronousResourceLoader.loadTexture(mContext, mTextureCache,
                callback, resource, texparams, priority, quality);
    }

    /**
     * Loads a texture asynchronously with default priority and quality.
     *
     * This method can detect whether the resource file holds a compressed
     * texture (GVRF currently supports ASTC, ETC2, and KTX formats:
     * applications can add new formats by implementing
     * {@link GVRCompressedTextureLoader}): if the file is not a compressed
     * texture, it is loaded as a normal, bitmapped texture. This format
     * detection adds very little to the cost of loading even a compressed
     * texture, and it makes your life a lot easier: you can replace, say,
     * {@code res/raw/resource.png} with {@code res/raw/resource.etc2} without
     * having to change any code.
     *
     * @param callback
     *            Before loading, GVRF may call
     *            {@link GVRAndroidResource.TextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} several times (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     *
     *            Successful loads will call
     *            {@link GVRAndroidResource.Callback#loaded(GVRHybridObject, GVRAndroidResource)
     *            loaded()} on the GL thread;
     *
     *            any errors will call
     *            {@link GVRAndroidResource.TextureCallback#failed(Throwable, GVRAndroidResource)
     *            failed()}, with no promises about threading.
     *
     *            <p>
     *            This method uses a throttler to avoid overloading the system.
     *            If the throttler has threads available, it will run this
     *            request immediately. Otherwise, it will enqueue the request,
     *            and call
     *            {@link GVRAndroidResource.TextureCallback#stillWanted(GVRAndroidResource)
     *            stillWanted()} at least once (on a background thread) to give
     *            you a chance to abort a 'stale' load.
     *
     *            <p>
     *            Use {@link #loadFutureTexture(GVRAndroidResource)} to avoid
     *            having to implement a callback.
     * @param resource
     *            Basically, a stream containing a texture file. The
     *            {@link GVRAndroidResource} class has six constructors to
     *            handle a wide variety of Android resource types. Taking a
     *            {@code GVRAndroidResource} here eliminates six overloads.
     */
    public void loadTexture(GVRAndroidResource resource, TextureCallback callback)
    {
        GVRAsynchronousResourceLoader.loadTexture(mContext, mTextureCache,
                callback, resource, mDefaultTextureParameters, DEFAULT_PRIORITY, GVRCompressedTexture.BALANCED);
    }

    public Future<GVRTexture> loadFutureTexture(GVRAndroidResource resource,
                                                int priority, int quality)
    {
        return GVRAsynchronousResourceLoader.loadFutureTexture(mContext,
                mTextureCache, resource, priority, quality);
    }

    public Future<GVRTexture> loadFutureTexture(GVRAndroidResource resource)
    {
        return GVRAsynchronousResourceLoader.loadFutureTexture(mContext,
                mTextureCache, resource, GVRAssetLoader.DEFAULT_PRIORITY, GVRCompressedTexture.BALANCED);
    }

    public Future<GVRTexture> loadFutureCubemapTexture(GVRAndroidResource resource)
    {
        return GVRAsynchronousResourceLoader.loadFutureCubemapTexture(mContext,
                mTextureCache, resource, DEFAULT_PRIORITY,
                GVRCubemapTexture.faceIndexMap);
    }

    public Future<GVRTexture> loadFutureCompressedCubemapTexture(GVRAndroidResource resource)
    {
        return GVRAsynchronousResourceLoader.loadFutureCompressedCubemapTexture(mContext,
                mTextureCache, resource, DEFAULT_PRIORITY,
                GVRCubemapTexture.faceIndexMap);
    }

    /** @since 1.6.2 */
    GVRAssimpImporter readFileFromResources(GVRContext gvrContext, GVRAndroidResource resource,
                                            EnumSet<GVRImportSettings> settings) throws IOException {
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
            } catch (Exception e) {
                Log.e("GVRAssetLoader", path + " exception loading asset from " + e.getMessage());
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
                    if (resource.getResourceType() != GVRAndroidResource.ResourceType.INPUT_STREAM) {
                        resource.closeStream(); // needed to avoid hanging
                    }
                    byte[] data = uncachedIO.read(path);
                    if (data == null) {
                        return null;
                    }
                    byteArray = GVRByteArray.wrap(data);
                    cache.put(resource, byteArray);
                }
                return byteArray.getByteArray();
            } catch (IOException e) {
                Log.e("GVRAssetLoader", path + " exception loading asset from " + e.getMessage());
                return null;
            }
        }
    }

    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model.
     * The model is not added to the current scene.
     * IAssetEvents are emitted to the event listener attached to the context.
     * This function blocks the current thread while loading the model
     * but loads the textures asynchronously in the background.
     *
     * @param filePath
     *            A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
     *
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException if model file cannot be opened
     *
     */
    public GVRModelSceneObject loadModel(final String filePath) throws IOException
    {
        return loadModel(filePath, (GVRScene) null);
    }

    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model
     * and adds it to the specified scene.
     * IAssetEvents are emitted to event listeners attached to the context.
     * This function blocks the current thread while loading the model
     * but loads the textures asynchronously in the background.
     *
     * @param filePath
     *            A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
     *
     * @param scene
     *            If present, this asset loader will wait until all of the textures have been
     *            loaded and then it will add the model to the scene.
     *            
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException 
     *
     */
    public GVRModelSceneObject loadModel(final String filePath, final GVRScene scene) throws IOException
    {
        GVRModelSceneObject model = new GVRModelSceneObject(mContext);
        AssetRequest assetRequest = new AssetRequest(model, new GVRResourceVolume(mContext, filePath), scene, null, false);
        String ext = filePath.substring(filePath.length() - 3).toLowerCase();

        model.setName(assetRequest.getBaseName());
        if (ext.equals("x3d"))
            loadX3DModel(assetRequest, model, GVRImportSettings.getRecommendedSettings());
        else
            loadJassimpModel(assetRequest, model, GVRImportSettings.getRecommendedSettings(), true, null);
        return model;
    }

    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model
     * replaces the current scene with it.
     * IAssetEvents are emitted to event listeners attached to the context.
     * This function blocks the current thread while loading the model
     * but loads the textures asynchronously in the background.
     *
     * @param filePath
     *            A filename, relative to the root of the volume.
     *            If the filename starts with "sd:" the file is assumed to reside on the SD Card.
     *            If the filename starts with "http:" or "https:" it is assumed to be a URL.
     *            Otherwise the file is assumed to be relative to the "assets" directory.
     *
     * @param scene
     *            Scene to be replaced with the model.
     *
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException
     *
     */
    public GVRModelSceneObject loadScene(final String filePath, final GVRScene scene) throws IOException
    {
        GVRModelSceneObject model = new GVRModelSceneObject(mContext);
        AssetRequest assetRequest = new AssetRequest(model, new GVRResourceVolume(mContext, filePath), scene, null, true);
        String ext = filePath.substring(filePath.length() - 3).toLowerCase();

        model.setName(assetRequest.getBaseName());
        if (ext.equals("x3d"))
            loadX3DModel(assetRequest, model, GVRImportSettings.getRecommendedSettings());
        else
            loadJassimpModel(assetRequest, model, GVRImportSettings.getRecommendedSettings(), true, scene);
        return model;
    }

    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model
     * replaces the current scene with it.
     * This function loads the model and its textures asynchronously in the background
     * and will return before the model is loaded.
     * IAssetEvents are emitted to event listeners attached to the context.
     *
     * @param model
     *          Scene object to become the root of the loaded model.
     *          This scene object will be named with the base filename of the loaded asset.
     * @param volume
     *            A GVRResourceVolume based on the asset path to load.
     *            This volume will be used as the base for loading textures
     *            and other models contained within the model.
     *            You can subclass GVRResourceVolume to provide custom IO.
     * @param scene
     *            Scene to be replaced with the model.
     * @param handler
     *            IAssetEvents handler to process asset loading events
     */
    public void loadScene(final GVRSceneObject model, final GVRResourceVolume volume, final GVRScene scene, final IAssetEvents handler)
    {
        Threads.spawn(new Runnable()
        {
            public void run()
            {
                AssetRequest assetRequest = new AssetRequest(model, volume, scene, handler, true);
                String filePath = volume.getFileName();
                String ext = filePath.substring(filePath.length() - 3).toLowerCase();

                model.setName(assetRequest.getBaseName());
                try
                {
                    if (ext.equals("x3d"))
                        loadX3DModel(assetRequest, model,
                                     GVRImportSettings.getRecommendedSettings());
                    else
                        loadJassimpModel(assetRequest, model,
                                         GVRImportSettings.getRecommendedSettings(), true, scene);
                }
                catch (IOException ex)
                {
                    // onModelError is generated in this case
                }
            }
        });
    }

    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model
     * on the volume provided and adds it to the specified scene.
     * IAssetEvents are emitted to event listeners attached to the context.
     * This function loads the model and its textures asynchronously in the background
     * and will return before the model is loaded.
     * IAssetEvents are emitted to event listeners attached to the context.
     * The resource volume may reference res/raw in which case all textures
     * and other referenced assets must also come from res/raw. The asset loader
     * cannot loadtextures from the drawable directory.
     *
     * @param model
     *            A GVRSceneObject to become the root of the loaded model.
     * @param volume
     *            A GVRResourceVolume based on the asset path to load.
     *            This volume will be used as the base for loading textures
     *            and other models contained within the model.
     *            You can subclass GVRResourceVolume to provide custom IO.
     * @param scene
     *            If present, this asset loader will wait until all of the textures have been
     *            loaded and then it will add the model to the scene.
     *
     *
     */
    public void loadModel(final GVRSceneObject model, final GVRResourceVolume volume, final GVRScene scene)
    {
        Threads.spawn(new Runnable()
        {
            public void run()
            {
                String filePath = volume.getFileName();
                AssetRequest assetRequest = new AssetRequest(model, volume, scene, null, false);
                String ext = filePath.substring(filePath.length() - 3).toLowerCase();

                model.setName(assetRequest.getBaseName());
                try
                {
                    if (ext.equals("x3d"))
                        loadX3DModel(assetRequest, model,
                                     GVRImportSettings.getRecommendedSettings());
                    else
                        loadJassimpModel(assetRequest, model,
                                         GVRImportSettings.getRecommendedSettings(), true, null);
                }
                catch (IOException ex)
                {
                    // onModelError is generated in this case.
                }
            }
        });
     }

    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model.
     * The model is not added to the current scene.
     * IAssetEvents are emitted to the event handler supplied first and then to
     * the event listener attached to the context.
     * This function blocks the current thread while loading the model
     * but loads the textures asynchronously in the background.
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
        GVRModelSceneObject model = new GVRModelSceneObject(mContext);
        GVRResourceVolume   volume = new GVRResourceVolume(mContext, filePath);
        AssetRequest assetRequest = new AssetRequest(model, volume, null, handler, false);
        String ext = filePath.substring(filePath.length() - 3).toLowerCase();

        model.setName(assetRequest.getBaseName());
        if (ext.equals("x3d"))
            loadX3DModel(assetRequest, model, GVRImportSettings.getRecommendedSettings());
        else
            loadJassimpModel(assetRequest, model, GVRImportSettings.getRecommendedSettings(), true, null);
        return model;
    }
    
    
    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model.
     * The model is not added to the current scene.
     * IAssetEvents are emitted to the event handler supplied first and then to
     * the event listener attached to the context.
     * This function blocks the current thread while loading the model
     * but loads the textures asynchronously in the background.
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
        String ext = filePath.substring(filePath.length() - 3).toLowerCase();
        GVRModelSceneObject model = new GVRModelSceneObject(mContext);
        AssetRequest assetRequest = new AssetRequest(model, new GVRResourceVolume(mContext, filePath), scene, null, false);
        model.setName(assetRequest.getBaseName());

		if (ext.equals("x3d"))
		    loadX3DModel(assetRequest, model, settings);
		else
		    loadJassimpModel(assetRequest, model, settings, cacheEnabled, null);
        return model;
    }

    /**
     * Loads a hierarchy of scene objects {@link GVRSceneObject} from a 3D model
     * inside an Android resource.
     * cannot load textures from the drawable directory.
     * The model is not added to the current scene.
     * IAssetEvents are emitted to the event handler supplied first and then to
     * the event listener attached to the context.
     * This function blocks the current thread while loading the model
     * but loads the textures asynchronously in the background.
     * The resource may be from res/raw in which case all textures
     * and other referenced assets must also come from res/raw. The asset loader
     * cannot load textures referenced from within 3D models from the drawable directory.
     *
     * @param resource
     *            GVRAndroidResource describing the asset. If it is a resource ID,
     *            the file it references must have a valid extension because the
     *            extension is used to determine what type of 3D file it is.
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
    public GVRModelSceneObject loadModel(GVRAndroidResource resource,
                                         EnumSet<GVRImportSettings> settings,
                                         boolean cacheEnabled,
                                         GVRScene scene) throws IOException
    {
        String filePath = resource.getResourceFilename();
        String ext = filePath.substring(filePath.length() - 3).toLowerCase();
        GVRModelSceneObject model = new GVRModelSceneObject(mContext);
        GVRResourceVolume volume = new GVRResourceVolume(mContext, resource);
        AssetRequest assetRequest = new AssetRequest(model, volume, scene, null, false);

        model.setName(assetRequest.getBaseName());
        if (ext.equals("x3d"))
            loadX3DModel(assetRequest, model, settings);
        else
            loadJassimpModel(assetRequest, model, settings, cacheEnabled, null);
        return model;
    }

    /**
     * Loads a scene object {@link GVRSceneObject} from
     * a 3D model and raises asset events to a handler.
     *
     * @param fileVolume
     *            GVRResourceVolume with the path to the model to load.
     *            The filename is relative to the root of this volume.
     *            The volume will be used to load models referenced by this model.
     *
     * @param model
     *            {@link GVRModelSceneObject} that is the root of the hierarchy generated
     *            by loading the 3D model.
     *
     * @param settings
     *            Additional import {@link GVRImportSettings settings}
     *
     * @param cacheEnabled
     *            If true, add the loaded model to the in-memory cache.
     *
     * @param handler
     *            IAssetEvents handler to process asset loading events
     */
    public void loadModel(final GVRResourceVolume fileVolume,
                          final GVRSceneObject model,
                          final EnumSet<GVRImportSettings> settings,
                          final boolean cacheEnabled,
                          final IAssetEvents handler)
    {
        Threads.spawn(new Runnable()
        {
            public void run()
            {
                String filePath = fileVolume.getFileName();
                String ext = filePath.substring(filePath.length() - 3).toLowerCase();
                AssetRequest assetRequest = new AssetRequest(model, fileVolume, null, handler, false);
                model.setName(assetRequest.getBaseName());

                try
                {
                    if (ext.equals("x3d"))
                        loadX3DModel(assetRequest, model, settings);
                    else
                        loadJassimpModel(assetRequest, model, settings, cacheEnabled, null);
                }
                catch (IOException ex)
                {
                    // onModelError is generated in this case
                }
            }
        });
     }

    /**
     * Loads a scene object {@link GVRSceneObject} from a 3D model.
     *
     * @param request
     *            AssetRequest with the filename, relative to the root of the volume.
     * @param model
     *            GVRModelSceneObject that is the root of the loaded asset
     * @param settings
     *            Additional import {@link GVRImportSettings settings}
     *
     * @param cacheEnabled
     *            If true, add the loaded model to the in-memory cache.
     * @return scene
     *            If not null, replace the current scene with the model.
     * @return A {@link GVRModelSceneObject} that contains the meshes with textures and bones
     * and animations.
     * @throws IOException 
     *
     */
    private GVRSceneObject loadJassimpModel(AssetRequest request, GVRSceneObject model,
            EnumSet<GVRImportSettings> settings, boolean cacheEnabled, GVRScene scene) throws IOException
    {
        Jassimp.setWrapperProvider(GVRJassimpAdapter.sWrapperProvider);
        org.gearvrf.jassimp.AiScene assimpScene = null;
        String filePath = request.getBaseName();
        GVRJassimpAdapter jassimpAdapter = new GVRJassimpAdapter(this, filePath);

        model.setName(filePath);
        GVRResourceVolume volume = request.getVolume();
        try
        {
            assimpScene = Jassimp.importFileEx(FileNameUtils.getFilename(filePath),
                    jassimpAdapter.toJassimpSettings(settings),
                    new CachedVolumeIO(new ResourceVolumeIO(volume)));
        }
        catch (IOException ex)
        {
            assimpScene = null;
            request.onModelError(mContext, ex.getMessage(), filePath);
            throw ex;
        }

        if (assimpScene == null)
        {
            String errmsg = "Cannot load model from path " + filePath;
            request.onModelError(mContext, errmsg, filePath);
            throw new IOException(errmsg);
        }
        jassimpAdapter.processScene(request, model, assimpScene, volume);
        request.onModelLoaded(mContext, model, filePath);
        return model;
    }
    

    GVRSceneObject loadX3DModel(GVRAssetLoader.AssetRequest assetRequest,
            GVRSceneObject root, EnumSet<GVRImportSettings> settings) throws IOException
    {
        GVRResourceVolume volume = assetRequest.getVolume();
        InputStream inputStream = null;
        String fileName = assetRequest.getBaseName();
        GVRAndroidResource resource = volume.openResource(fileName);

        root.setName(fileName);
        org.gearvrf.x3d.X3Dobject x3dObject = new org.gearvrf.x3d.X3Dobject(assetRequest, root);
        try
        {
            ShaderSettings shaderSettings = new ShaderSettings(new GVRMaterial(mContext));
            if (!X3Dobject.UNIVERSAL_LIGHTS)
            {
                X3DparseLights x3dParseLights = new X3DparseLights(mContext, root);
                inputStream = resource.getStream();
                if (inputStream == null)
                {
                    throw new FileNotFoundException(fileName + " not found");
                }
                Log.d(TAG, "Parse: " + fileName);
                x3dParseLights.Parse(inputStream, shaderSettings);
                inputStream.close();
            }
            inputStream = resource.getStream();
            if (inputStream == null)
            {
              	throw new FileNotFoundException(fileName + " not found");
            }
            x3dObject.Parse(inputStream, shaderSettings);
            inputStream.close();
            assetRequest.onModelLoaded(mContext, root, fileName);
        }
        catch (Exception ex)
        {
            assetRequest.onModelError(mContext, ex.getMessage(), fileName);
            throw ex;
        }
        return root;
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

    GVRTextureParameters getDefaultTextureParameters() {
        return mDefaultTextureParameters;
    }

     /**
     * State-less, should be fine having one instance
     */
    private final static String TAG = "GVRAssetLoader";

}

class NativeImporter {
    static native long readFromByteArray(byte[] bytes, String filename, int settings);
}

