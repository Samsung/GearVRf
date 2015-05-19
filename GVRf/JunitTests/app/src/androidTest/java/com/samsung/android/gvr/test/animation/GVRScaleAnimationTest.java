package com.samsung.android.gvr.test.animation;

import android.util.Log;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRInterpolator;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import org.gearvrf.animation.GVRScaleAnimation;
import com.samsung.android.gvr.app.base.GVRTestActivity;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

public class GVRScaleAnimationTest extends ActivityInstrumentationGVRf {

    private final String TAG = GVRScaleAnimationTest.class.getSimpleName();

    private final float duration = 1.0f;
    private final float x = 1.0f;
    private final float y = 2.0f;
    private final float z = 3.0f;

    public GVRScaleAnimationTest() {
        super(GVRTestActivity.class);
    }

    public void testConstructorGVRScaleAnimationTransform1(){
        Log.d(TAG, "starting testConstructorGVRScaleAnimationTransform1");

        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRScaleAnimation scaleAnimation = new GVRScaleAnimation(sceneObject.getTransform(), duration, y);

        assertNotNull(scaleAnimation);
    }

    public void testConstructorGVRScaleAnimationSceneObject1(){
        Log.d(TAG, "starting testConstructorGVRScaleAnimationSceneObject1");

        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRScaleAnimation scaleAnimation = new GVRScaleAnimation(sceneObject, duration, y);

        assertNotNull(scaleAnimation);
    }


    public void testSetInvalidRepeatModeAnimation() {

        try {
            GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
            new GVRRotationByAxisAnimation
                    (sceneObject, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f).setRepeatMode(2);

            GVRRotationByAxisWithPivotAnimation animation = new GVRRotationByAxisWithPivotAnimation(sceneObject,
                    1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
            animation.setRepeatMode(4);
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "4 is not a valid repetition type");
        }
    }

    public void testInterpolatorAnimation() {

        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        new GVRRotationByAxisAnimation
                (sceneObject, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f).setRepeatMode(2);

        GVRScaleAnimation animation = new GVRScaleAnimation(sceneObject, 1.0f, 1.0f, 1.0f, 1.0f);
        animation.setInterpolator(new GVRInterpolator() {
            @Override
            public float mapRatio(float ratio) {

                assertNotNull(ratio);

                return 0;
            }
        });
    }

    public void testSetFinishedObject() {
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        new GVRRotationByAxisAnimation
                (sceneObject, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f).setRepeatMode(2);

        GVRScaleAnimation animation = new GVRScaleAnimation(sceneObject, 1.0f, 1.0f, 1.0f, 1.0f);
        animation.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation animation) {
                assertNotNull(animation);
            }
        });
    }

    public void testSetRepeatCount() {
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        new GVRRotationByAxisAnimation
                (sceneObject, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f).setRepeatMode(2);

        GVRScaleAnimation animation = new GVRScaleAnimation(sceneObject, 1.0f, 1.0f, 1.0f, 1.0f);
        animation.setRepeatCount(10);
    }
}
