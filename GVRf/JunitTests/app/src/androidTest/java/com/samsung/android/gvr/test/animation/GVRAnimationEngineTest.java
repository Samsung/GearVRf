package com.samsung.android.gvr.test.animation;

import org.gearvrf.GVRHybridObject;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRAnimationEngine;
import com.samsung.android.gvr.app.base.GVRTestActivity;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

import java.lang.Override;

/**
 * Created by diego on 2/20/15.
 */
public class GVRAnimationEngineTest extends ActivityInstrumentationGVRf {

    private static final float ANIM_DURATION = 1.5f;

    public GVRAnimationEngineTest() {super(GVRTestActivity.class);}

    public void testGetInstance() {
        GVRAnimationEngine animationEngine = TestDefaultGVRViewManager.mGVRContext.getAnimationEngine();
        assertNotNull(animationEngine);
    }

    // TODO make a test that calls animate() to show start() working
    public void ignoretestStart() {
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRAnimation animation = new GVRAnimation(sceneObject, ANIM_DURATION) {
            @Override
            protected void animate(GVRHybridObject gvrHybridObject, float v) {}
        };

        GVRAnimationEngine animationEngine = TestDefaultGVRViewManager.mGVRContext.getAnimationEngine();
        animation = animationEngine.start(animation);
        assertNotNull(animationEngine);
    }

    // TODO make a test that calls animate() to show start() working with null
    public void ignoretestStartNull() {
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRAnimation animation = new GVRAnimation(sceneObject, ANIM_DURATION) {
            @Override
            protected void animate(GVRHybridObject gvrHybridObject, float v) {}
        };

        GVRAnimationEngine animationEngine = TestDefaultGVRViewManager.mGVRContext.getAnimationEngine();
        animation = animationEngine.start(null);
        assertNotNull(animationEngine);
    }

    // TODO make a test that shows start() and stop() working by checking animation state
    public void ignoretestStartStopAnimation() {
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRAnimation animation = new GVRAnimation(sceneObject, ANIM_DURATION) {
            @Override
            protected void animate(GVRHybridObject gvrHybridObject, float v) {}
        };

        GVRAnimationEngine animationEngine = TestDefaultGVRViewManager.mGVRContext.getAnimationEngine();
        animationEngine.stop(null);
        assertNotNull(animationEngine);
    }

    public void testStopAnimation() {
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRAnimation animation = new GVRAnimation(sceneObject, ANIM_DURATION) {
            @Override
            protected void animate(GVRHybridObject gvrHybridObject, float v) {}
        };

        GVRAnimationEngine animationEngine = TestDefaultGVRViewManager.mGVRContext.getAnimationEngine();
        animationEngine.stop(animation);
        assertNotNull(animationEngine);
    }

    public void testStopNull() {
        GVRAnimationEngine animationEngine = TestDefaultGVRViewManager.mGVRContext.getAnimationEngine();
        animationEngine.stop(null);
        assertNotNull(animationEngine);
    }
}
