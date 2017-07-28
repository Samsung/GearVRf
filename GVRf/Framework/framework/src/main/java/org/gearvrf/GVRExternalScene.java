package org.gearvrf;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.utility.Log;
import org.gearvrf.GVRContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Component which loads an asset under it's owner.
 * This allows you to reference external assets from your
 * scene more easily and defer their loading.
 * @see GVRAssetLoader
 */
public class GVRExternalScene extends GVRBehavior
{
    private static final String TAG = Log.tag(GVRExternalScene.class);
    static private long TYPE_EXTERNALSCENE = newComponentType(GVRExternalScene.class);
    private String mFilePath;
    private boolean mReplaceScene;
    public final GVRResourceVolume mVolume;

    /**
     * Constructs an external scene component to load the given asset file.
     * @param ctx           GVRContext that owns this component
     * @param filePath      full path to the asset to load
     * @param replaceScene  true to replace the current scene, false to just add the model
     */
    public GVRExternalScene(GVRContext ctx, String filePath, boolean replaceScene)
    {
        super(ctx);
        mType = getComponentType();
        mVolume = new GVRResourceVolume(ctx, filePath);
        mFilePath = filePath;
        mReplaceScene = replaceScene;
    }

    /**
     * Constructs an external scene component to load the given asset file.
     * @param volume        GVRResourceVolume containing the path of the asset.
     * @param replaceScene  true to replace the current scene, false to just add the model
     */
    public GVRExternalScene(GVRResourceVolume volume, boolean replaceScene)
    {
        super(volume.gvrContext);
        mType = getComponentType();
        mVolume = volume;
        mFilePath = volume.getFullPath();
        mReplaceScene = replaceScene;
    }

    static public long getComponentType() { return TYPE_EXTERNALSCENE; }

    /**
     * Determines whether the loaded asset should replace the whole scene or not.
     * @return true to replace scene, false to just load the model into the current scene.
     */
    public boolean replaceScene()
    {
        return mReplaceScene;
    }

    /**
     * Gets the path to the asset to be loaded by this component.
     * If the path begins with "http:" or "https:" it is assumed to
     * be a URL. If it starts with "sd:", it references assets on
     * the SD card. Otherwise assets are assumed to be in the "assets" directory.
     * @return path to external asset
     */
    public String getFilePath()
    {
        return mFilePath;
    }

    /**
     * Get the GVRAnimator containing the animations for the loaded asset.
     * @return GVRAnimator if available, else null
     */
    public GVRAnimator getAnimator()
    {
        return (GVRAnimator) getOwnerObject().getComponent(GVRAnimator.getComponentType());
    }

    /**
     * Get the GVRCameraRig for the main camera of the loaded asset.
     *
     * @return camere rig of main camera or null if not available
     */
    public GVRCameraRig getCameraRig()
    {
        return (GVRCameraRig) getOwnerObject().getComponent(GVRCameraRig.getComponentType());
    }

    /**
     * Loads the asset referenced by the file name
     * under the owner of this component.
     * If this component was constructed to replace the scene with
     * the asset, the scene will contain only the owner of this
     * component upon return. Otherwise, the loaded asset is a
     * child of this component's owner.
     *
     * Loading the asset is performed in a separate thread.
     * This function returns before the asset has finished loading.
     * IAssetEvents are emitted to the event listener on the context.
     * 
     * @param scene scene to add the model to, null is permissible
     * @return always true
     */
    public boolean load(GVRScene scene)
    {
        GVRAssetLoader loader = getGVRContext().getAssetLoader();

        if (scene == null)
        {
            scene = getGVRContext().getMainScene();
        }
        if (mReplaceScene)
        {
            loader.loadScene(getOwnerObject(), mVolume, scene, null);
        }
        else
        {
            loader.loadModel(getOwnerObject(), mVolume, scene);
        }
        return true;
    }

    /**
     * Loads the asset referenced by the file name
     * under the owner of this component.
     * If this component was constructed to replace the scene with
     * the asset, the main scene of the current context
     * will contain only the owner of this
     * component upon return. Otherwise, the loaded asset is a
     * child of this component's owner.
     *
     * Loading the asset is performed in a separate thread.
     * This function returns before the asset has finished loading.
     * IAssetEvents are emitted to the input event handler and
     * to any event listener on the context.
     *
     * @param handler
     *            IAssetEvents handler to process asset loading events
     */
    public void load(IAssetEvents handler)
    {
        GVRAssetLoader loader = getGVRContext().getAssetLoader();

        if (mReplaceScene)
        {
            loader.loadScene(getOwnerObject(), mVolume, getGVRContext().getMainScene(), handler);
        }
        else
        {
            loader.loadModel(mVolume, getOwnerObject(), GVRImportSettings.getRecommendedSettings(), true, handler);
        }
    }
}