package com.samsung.android.gvr.test.animation;

import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRInterpolator;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVRRotationByAxisAnimation;
import org.gearvrf.animation.GVRRotationByAxisWithPivotAnimation;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;


/**
 * Created by Douglas on 2/28/15.
 */
public class GVRRotationByAxisWithPivotAnimationTest extends ActivityInstrumentationGVRf {

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

        GVRRotationByAxisWithPivotAnimation animation = new GVRRotationByAxisWithPivotAnimation(sceneObject,
                1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
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

        GVRRotationByAxisWithPivotAnimation animation = new GVRRotationByAxisWithPivotAnimation(sceneObject,
                1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
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

        GVRRotationByAxisWithPivotAnimation animation = new GVRRotationByAxisWithPivotAnimation(sceneObject,
                1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        animation.setRepeatCount(10);
    }
}
