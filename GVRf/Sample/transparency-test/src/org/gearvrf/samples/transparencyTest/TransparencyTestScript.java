package org.gearvrf.samples.transparencyTest;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;

public class TransparencyTestScript extends GVRScript {

    private GVRScene mScene;
    private GVRContext mContext;
    private static final String TAG = "TransparencyTestScript";
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mContext = gvrContext;
        mScene = gvrContext.getNextMainScene();
        mScene.setFrustumCulling(false);

        GVRTextViewSceneObject helloSceneObject = new GVRTextViewSceneObject(gvrContext, gvrContext.getActivity(), "H___________");
        helloSceneObject.setGravity(Gravity.CENTER);
        helloSceneObject.setTextSize(helloSceneObject.getTextSize() * 1.5f);
        helloSceneObject.getTransform().setPosition(0.0f, 0.0f, -2.11f);
        
        // since we didn't mark this one as transparent, it will go in the Geometry bin
        mScene.addSceneObject(helloSceneObject);
        
        // The rest of these will be marked transparent.
        addString("_e__________", -2.01f);
        addString("___________!", -2.10f);
        addString("______W_____", -2.05f);
        addString("___l________", -2.03f);
        addString("_______o____", -2.06f);
        addString("__l_________", -2.02f);
        addString("____o_______", -2.04f);
        addString("_________l__", -2.08f);
        addString("__________d_", -2.09f);
        addString("________r___", -2.07f);

    }
    
    private void addString(String string, float distance) {
        GVRTextViewSceneObject sceneObject = new GVRTextViewSceneObject(mContext, mContext.getActivity(), string);

        sceneObject.setGravity(Gravity.CENTER);
        sceneObject.setTextSize(sceneObject.getTextSize() * 1.5f);
        sceneObject.getTransform().setPosition(0.0f, 0.0f, distance);
        sceneObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        
        mScene.addSceneObject(sceneObject);
    }

    @Override
    public void onStep() {
    }
    
    public void toggleFrustumCulling() {
        mScene.setFrustumCulling(true);
    }

}
