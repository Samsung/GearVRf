package com.samsung.android.gvr.app.base.viewmanager;


import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import com.samsung.android.gvr.app.base.misc.ColorShader;

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by santhyago on 1/15/15.
 */
public abstract class BaseViewManager extends GVRScript {
    protected static final float UNPICKED_COLOR_R = 0.7f;
    protected static final float UNPICKED_COLOR_G = 0.7f;
    protected static final float UNPICKED_COLOR_B = 0.7f;
    protected static final float UNPICKED_COLOR_A = 1.0f;
    protected static final float PICKED_COLOR_R = 1.0f;
    protected static final float PICKED_COLOR_G = 0.0f;
    protected static final float PICKED_COLOR_B = 0.0f;
    protected static final float PICKED_COLOR_A = 1.0f;

    protected GVRContext mGVRContext = null;
    protected ColorShader mColorShader = null;
    protected Vector<GVRSceneObject> mObjects = new Vector<GVRSceneObject>();
//    protected GVRContextListener listener;
    protected LinkedBlockingQueue<Runnable> runnableTests = new LinkedBlockingQueue<>();

    protected float OBJECT_ROT = 0.0f;

    public void addRunnableTests(Runnable test) {
        runnableTests.add(test);
    }

    @Override
    public void onStep() {
        Runnable runTest;
        while ((runTest = runnableTests.poll()) != null)
            runTest.run();

        GVRSceneObject objectExt = null;

        for(GVRSceneObject object : mObjects)
        {
            object.getRenderData().getMaterial().setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        }
        for(GVREyePointeeHolder eph : GVRPicker.pickScene(mGVRContext.getMainScene()))
        {
            for(GVRSceneObject object : mObjects)
            {
                if(eph.getOwnerObject().equals(object))
                {
                    object.getRenderData().getMaterial().setVec4(ColorShader.COLOR_KEY, PICKED_COLOR_R, PICKED_COLOR_G, PICKED_COLOR_B, PICKED_COLOR_A);
                    objectExt = object;
                    break;
                }
            }
        }
        OBJECT_ROT += -1.0f;
        for (int i = 0; i < OBJECT_ROT; i++) objectExt.getTransform().rotateByAxis(OBJECT_ROT, 0f, 1.0f, 0f);
    }

    protected GVRSceneObject getColorBoard(float width, float height)
    {
        GVRMaterial material = new GVRMaterial(mGVRContext, mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(mGVRContext, width, height);
        board.getRenderData().setMaterial(material);

        return board;
    }



    protected void attachDefaultEyePointee(GVRSceneObject sceneObject)
    {
        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(mGVRContext);
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(mGVRContext, sceneObject.getRenderData().getMesh());
        eyePointeeHolder.addPointee(eyePointee);
        sceneObject.attachEyePointeeHolder(eyePointeeHolder);
    }

    protected void attachBoundingBoxEyePointee(GVRSceneObject sceneObject)
    {
        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(mGVRContext);
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(mGVRContext, sceneObject.getRenderData().getMesh().getBoundingBox());
        eyePointeeHolder.addPointee(eyePointee);
        sceneObject.attachEyePointeeHolder(eyePointeeHolder);
    }

    public GVRContext getGVRContext() {
        return mGVRContext;
    }

//    public void setGVRContextListener(GVRContextListener contextListener) {
//        listener = contextListener;
//    }
}

