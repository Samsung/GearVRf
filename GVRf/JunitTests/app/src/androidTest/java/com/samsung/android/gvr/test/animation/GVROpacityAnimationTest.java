package com.samsung.android.gvr.test.animation;


import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRAnimation;
import org.gearvrf.animation.GVRInterpolator;
import org.gearvrf.animation.GVROnFinish;
import org.gearvrf.animation.GVROpacityAnimation;
import com.samsung.android.gvr.app.base.misc.ColorShader;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by diego on 2/26/15.
 */
public class GVROpacityAnimationTest extends ActivityInstrumentationGVRf {

    private final String TAG = GVROpacityAnimationTest.class.getSimpleName();

    private static final float DEFAULT_DURATION = 2.5f;
    private static final float DEFAULT_OPACITY = 1.5f;
    private static final float DEFAULT_NEGATIVE = -1.5f;

    public void testConstructorGVROpacityAnimationSceneObject(){
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVRRenderData rd = new GVRRenderData(TestDefaultGVRViewManager.mGVRContext);
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);

        sceneObject.attachRenderData(rd);
        sceneObject.getRenderData().setMaterial(material);
        GVROpacityAnimation opacityAnimation = new GVROpacityAnimation(sceneObject, DEFAULT_DURATION, DEFAULT_OPACITY);

        assertNotNull(opacityAnimation);
    }

    public void testConstructorMaterialDurationOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/20
    public void ignoretestConstructorNullMaterialDurationOpacity() {
        GVRMaterial material = null;
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialNegativeDurationOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_NEGATIVE, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialDurationNegativeOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_NEGATIVE);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialNegativeDurationNegativeOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_NEGATIVE, DEFAULT_NEGATIVE);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialNaNDurationOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, Float.NaN, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialDurationNaNOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, Float.NaN);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialNaNDurationNaNOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, Float.NaN, Float.NaN);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialPositiveInfintyDurationOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, Float.POSITIVE_INFINITY, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialDurationPositiveInfintyOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, Float.POSITIVE_INFINITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialPositiveInfintyDurationPositiveInfintyOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialNegativeInfintyDurationOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, Float.NEGATIVE_INFINITY, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialDurationNegativeInfintyOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, Float.NEGATIVE_INFINITY);
        assertNotNull(opacity);
    }

    public void testConstructorMaterialNegativeInfintyDurationNegativeInfintyOpacity() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        assertNotNull(opacity);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/21
    public void ignoretestConstructorSceneObjectDurationOpacity() {
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVROpacityAnimation opacity = new GVROpacityAnimation(sceneObject, DEFAULT_DURATION, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/21
    public void ignoretestConstructorNullSceneObjectDurationOpacity() {
        GVRSceneObject sceneObject = null;
        GVROpacityAnimation opacity = new GVROpacityAnimation(sceneObject, DEFAULT_DURATION, DEFAULT_OPACITY);
        assertNotNull(opacity);
    }

    // TODO create test which calls animate with a duration of 0f
    public void ignoretestConstructorZeroDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
    }

    // TODO create test which calls animate with a duration of -2f
    public void ignoretestConstructorNegativeDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
    }

    // TODO create test which calls animate with a duration of NaN
    public void ignoretestConstructorNaNDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
    }

    // TODO create test which calls animate with a duration of positive infinity
    public void ignoretestConstructorPositiveInfinityDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
    }

    // TODO create test which calls animate with a duration of negative infinity
    public void ignoretestConstructorNegativeInfinityDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
    }

    public void testRepeatOpacityAnimationValid() {

        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
        opacity.setOnFinish(new GVROnFinish() {
            @Override
            public void finished(GVRAnimation animation) {
                assertNotNull(animation);
            }
        });
    }

    public void testSetInterpolatorValid() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
        opacity.setInterpolator(new GVRInterpolator() {
            @Override
            public float mapRatio(float ratio) {

                if (ratio != 0)
                    assertTrue(true);

                return 0;
            }
        });
    }

    public void testSetReapModeValid() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVROpacityAnimation opacity = new GVROpacityAnimation(material, DEFAULT_DURATION, DEFAULT_OPACITY);
        opacity.setRepeatMode(2);
    }
}
