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
import org.gearvrf.scene_objects.GVRConeSceneObject;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRCylinderSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

public class SampleViewManager extends GVRScript {
    private GVRContext mGVRContext;
    private GVRCylinderSceneObject cylinderObject;
    private GVRConeSceneObject coneObject;
    private GVRSphereSceneObject sphereObject;

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
        cylinderObject = new GVRCylinderSceneObject(gvrContext);
        coneObject = new GVRConeSceneObject(gvrContext);
        sphereObject = new GVRSphereSceneObject(gvrContext);

        Future<GVRTexture> futureTexture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.gearvr_logo));
        GVRMaterial material = new GVRMaterial(gvrContext);
        material.setMainTexture(futureTexture);
        cubeObject.getRenderData().setMaterial(material);
        cylinderObject.getRenderData().setMaterial(material);
        coneObject.getRenderData().setMaterial(material);
        sphereObject.getRenderData().setMaterial(material);

        // set the scene object position
        quadObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        cubeObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);
        cylinderObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        coneObject.getTransform().setPosition(0.0f, 0.0f, -3.0f);
        sphereObject.getTransform().setPosition(0.0f, -1.0f, -3.0f);

        GVRAnimation animation = new GVRRotationByAxisAnimation(cylinderObject, 50, -3600, 0, 1, 0);
        animation.setRepeatCount(GVRRepeatMode.REPEATED).setRepeatCount(-1);
        //animation.start(gvrContext.getAnimationEngine());
        cubeObject.getTransform().setRotationByAxis(45.0f, 1.0f, 0.0f, 0.0f);
        
        //cylinderObject.getRenderData().setCullTest(false);
        
        // add the scene object to the scene graph
        //scene.addSceneObject(quadObject);
        //scene.addSceneObject(cubeObject);
        //scene.addSceneObject(cylinderObject);
        //scene.addSceneObject(coneObject);
        scene.addSceneObject(sphereObject);

    }
    
    private float mYAngle = 0;
    private float mXAngle = 0;
    
    public void setYAngle(float angle) {
        mYAngle = angle;
    }
    
    public void setXAngle(float angle) {
        mXAngle = angle;
    }
    

    @Override
    public void onStep() {
        sphereObject.getTransform().setRotationByAxis(mYAngle, 0.0f, 1.0f, 0.0f);
        sphereObject.getTransform().setRotationByAxis(mXAngle, 1.0f, 0.0f, 0.0f);
    }
}
