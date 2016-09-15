package org.gearvrf;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimator;
import org.gearvrf.utility.Log;
import org.gearvrf.GVRContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GVRExternalScene extends GVRBehavior
{
    private static final String TAG = Log.tag(GVRExternalScene.class);
    static private long TYPE_EXTERNALSCENE = newComponentType(GVRExternalScene.class);
    private String mFilePath;
    private boolean mReplaceScene;

    public GVRExternalScene(GVRContext ctx, String filePath, boolean replaceScene)
    {
        super(ctx);
        mType = getComponentType();
        mFilePath = filePath;
        mReplaceScene = replaceScene;
    }

    static public long getComponentType() { return TYPE_EXTERNALSCENE; }

    public boolean replaceScene()
    {
        return mReplaceScene;
    }

    public String getFilePath()
    {
        return mFilePath;
    }

    public GVRAnimator getAnimator()
    {
        return (GVRAnimator) getOwnerObject().getComponent(GVRAnimator.getComponentType());
    }

    public GVRCameraRig getCameraRig()
    {
        return (GVRCameraRig) getOwnerObject().getComponent(GVRCameraRig.getComponentType());
    }

    public boolean load(GVRScene scene)
    {
        GVRAssetLoader loader = getGVRContext().getAssetLoader();

        if (scene == null)
        {
            scene = getGVRContext().getMainScene();
        }
        try
        {
            if (mReplaceScene)
            {
                loader.loadScene(getOwnerObject(), mFilePath, scene);
            }
            else
            {
                loader.loadModel(getOwnerObject(), mFilePath, scene);
            }
            return true;
        }
        catch (IOException ex)
        {
            return false;
        }
    }
}