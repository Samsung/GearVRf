package org.gearvrf.sample.sceneobjects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;

public class SampleViewManager extends GVRScript {
    private GVRContext mGVRContext;

    @Override
    public void onInit(GVRContext gvrContext) {

        // save context for possible use in onStep(), even though that's empty
        // in this sample
        mGVRContext = gvrContext;

        GVRScene scene = gvrContext.getNextMainScene();

        // set background color
        GVRCameraRig mainCameraRig = scene.getMainCameraRig();
        //mainCameraRig.getLeftCamera().setBackgroundColor(Color.WHITE);
        //mainCameraRig.getRightCamera().setBackgroundColor(Color.WHITE);

        // load texture
        GVRTexture texture = gvrContext.loadTexture(new GVRAndroidResource(
                mGVRContext, R.drawable.gearvr_logo));

        // create a scene object (this constructor creates a rectangular scene
        // object that uses the standard 'unlit' shader)
        GVRSceneObject quadObject = new GVRSceneObject(gvrContext, 4.0f, 2.0f, texture);
        GVRCubeSceneObject cubeObject = new GVRCubeSceneObject(gvrContext);
        GVRCylinderSceneObject cylinderObject = new GVRCylinderSceneObject(gvrContext);
        
        Future<GVRTexture> futureTexture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.gearvr_logo));
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTexture);
        cubeObject.getRenderData().setMaterial(material);
        cylinderObject.getRenderData().setMaterial(material);
        
        // set the scene object position
        quadObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        cubeObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);
        cylinderObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);
        
        GVRAnimation animation = new GVRRotationByAxisAnimation(cubeObject, 200, 360, 1, 0, 0);
        animation.setRepeatCount(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        animation.start(gvrContext.getAnimationEngine());
        cubeObject.getTransform().setRotationByAxis(45.0f, 1.0f, 0.0f, 0.0f);
        cylinderObject.getTransform().setRotationByAxis(45.0f, 1.0f, 0.0f, 0.0f);
        
        // add the scene object to the scene graph
        //scene.addSceneObject(quadObject);
        //scene.addSceneObject(cubeObject);
        scene.addSceneObject(cylinderObject);
    }
    

    @Override
    public void onStep() {
    }
}
