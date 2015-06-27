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
        sphereHighDensity.setLODRange(0.0f, -5.0f);
        scene.addSceneObject(sphereHighDensity);
        
        GVRSphereSceneObject sphereMediumDensity = new GVRSphereSceneObject(gvrContext);
        setupObject(gvrContext, sphereMediumDensity, greenFutureTexture); 
        sphereHighDensity.setLODRange(-5.0f, -7.0f);
        scene.addSceneObject(sphereMediumDensity);
        
        GVRSphereSceneObject sphereLowDensity = new GVRSphereSceneObject(gvrContext);
        setupObject(gvrContext, sphereLowDensity, blueFutureTexture);   
        sphereHighDensity.setLODRange(-7.0f, 10.0f);
        scene.addSceneObject(sphereLowDensity);
        
    }
    
    private void setupObject(GVRContext gvrContext, GVRSceneObject object, Future<GVRTexture> futureTexture) {
        object.getTransform().setPosition(0,  0,  -3.0f);
        GVRMaterial unlit = new GVRMaterial(gvrContext, GVRShaderType.Unlit.ID);
        unlit.setMainTexture(futureTexture);
        object.getRenderData().setMaterial(unlit);
        setupAnimation(object);  
    }
    
    private void setupAnimation(GVRSceneObject object) {
        GVRAnimation animation = new TranslationAnimation(object, 2.0f, 0.0f, 0.0f, -10.0f);
        animation.setRepeatMode(GVRRepeatMode.PINGPONG).setRepeatCount(-1);
        animations.add(animation);
    }
    
    @Override
    public void onStep() {
    }
    
    public class TranslationAnimation extends GVRTransformAnimation {
        float startx, starty, startz;
        float deltax, deltay, deltaz;
        GVRTransform transform;
        
        public TranslationAnimation(GVRSceneObject target, float duration, float targetx, float targety, float targetz) {
            super(target, duration);
            
            transform = target.getTransform();
            startx = transform.getPositionX();
            starty = transform.getPositionY();
            startz = transform.getPositionZ();
            deltax = targetx - startx;
            deltay = targety - starty;
            deltaz = targetz - startz;
        }

        @Override
        protected void animate(GVRHybridObject target, float ratio) {
            transform.setPosition(startx + ratio * deltax,
                                  starty + ratio * deltay,
                                  startz + ratio * deltaz);
            
        }
    }
}
