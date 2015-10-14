package org.gearvrf.sample.LodTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.animation.GVRPositionAnimation;
import org.gearvrf.animation.GVRRepeatMode;
import org.gearvrf.animation.GVRTransformAnimation;
import org.gearvrf.scene_objects.GVRSphereSceneObject;

public class LODTestScript extends GVRScript {
    
    private List<GVRAnimation> animations = new ArrayList<GVRAnimation>();
    private GVRAnimationEngine animationEngine;
    
    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        animationEngine = gvrContext.getAnimationEngine();
        
        GVRScene scene = gvrContext.getNextMainScene(new Runnable() {
            @Override
            public void run() {
                for(GVRAnimation animation : animations) {
                    animation.start(animationEngine);
                }
            }
        });
        
        scene.setFrustumCulling(true);
        
        Future<GVRTexture> redFutureTexture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.red));
        Future<GVRTexture> greenFutureTexture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.green));
        Future<GVRTexture> blueFutureTexture = gvrContext.loadFutureTexture(new GVRAndroidResource(gvrContext, R.drawable.blue));

        GVRSphereSceneObject sphereHighDensity = new GVRSphereSceneObject(gvrContext);
        setupObject(gvrContext, sphereHighDensity, redFutureTexture);
        sphereHighDensity.setLODRange(0.0f, 5.0f);
        scene.addSceneObject(sphereHighDensity);
        
        GVRSphereSceneObject sphereMediumDensity = new GVRSphereSceneObject(gvrContext, 9, 9,
                true, new GVRMaterial(gvrContext));
        setupObject(gvrContext, sphereMediumDensity, greenFutureTexture); 
        sphereMediumDensity.setLODRange(5.0f, 9.0f);
        scene.addSceneObject(sphereMediumDensity);
        
        GVRSphereSceneObject sphereLowDensity = new GVRSphereSceneObject(gvrContext, 6, 6,
                true, new GVRMaterial(gvrContext));
        setupObject(gvrContext, sphereLowDensity, blueFutureTexture);   
        sphereLowDensity.setLODRange(9.0f, Float.MAX_VALUE);
        scene.addSceneObject(sphereLowDensity);
        
    }
    
    private void setupObject(GVRContext gvrContext, GVRSceneObject object, Future<GVRTexture> futureTexture) {
        object.getTransform().setPosition(0,  0,  -3.0f);
        GVRMaterial unlit = new GVRMaterial(gvrContext, GVRShaderType.Texture.ID);
        unlit.setMainTexture(futureTexture);
        object.getRenderData().setMaterial(unlit);
        setupAnimation(object);  
    }
    
    private void setupAnimation(GVRSceneObject object) {
        GVRAnimation animation = new GVRPositionAnimation(object, 2.0f, 0.0f, 0.0f, -10.0f);
        animation.setRepeatMode(GVRRepeatMode.PINGPONG).setRepeatCount(-1);
        animations.add(animation);
    }
    
    @Override
    public void onStep() {
    }
    
}

