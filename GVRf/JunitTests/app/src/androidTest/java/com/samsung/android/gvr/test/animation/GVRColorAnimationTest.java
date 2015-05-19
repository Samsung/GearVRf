package com.samsung.android.gvr.test.animation;

import android.util.Log;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.animation.GVRColorAnimation;

import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.utils.UtilResource;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.app.base.misc.ColorShader;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by diego on 2/20/15.
 */
public class GVRColorAnimationTest extends ActivityInstrumentationGVRf {

    private final String TAG = GVRColorAnimationTest.class.getSimpleName();

    private static final float DEFAULT_R = 1f;
    private static final float DEFAULT_G = 1f;
    private static final float DEFAULT_B = 1f;
    private static final float ANIM_DURATION = 1.5f;

    protected GVRSceneObject init(){

        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVRRenderData rd = new GVRRenderData(TestDefaultGVRViewManager.mGVRContext);
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);

        sceneObject.attachRenderData(rd);
        sceneObject.getRenderData().setMaterial(material);

        return sceneObject;

    }

    public void testConstructorGVRColorAnimationSceneObject1(){
        Log.d(TAG, "starting testConstructorGVRColorAnimationSceneObject1");

        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRSceneObject sceneObject = init();
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, 1.0f, rgb);

        assertNotNull(colorAnimation);
    }

    public void testConstructorGVRColorAnimationSceneObject2(){
        Log.d(TAG, "starting testConstructorGVRColorAnimationSceneObject2");

        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRSceneObject sceneObject = init();
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, 1.0f, 135);

        assertNotNull(colorAnimation);
    }

    public void testConstructorMaterialRGB() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/17
    public void ignoretestConstructorNullMaterialRGB() {
        GVRMaterial material = null;
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/19
    public void ignoretestConstructorMaterialRG() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R, DEFAULT_G};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/19
    public void ignoretestConstructorMaterialR() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/19
    public void ignoretestConstructorMaterialNaNRGB() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {Float.NaN, Float.NaN, Float.NaN};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/19
    public void ignoretestConstructorMaterialEmptyRGB() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/19
    public void ignoretestConstructorMaterialNullRGB() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, null);
        assertNotNull(colorAnimation);
    }

    public void testConstructorMaterialColor() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        int androidColor = android.R.color.holo_blue_dark;
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, androidColor);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/17
    public void ignoretestConstructorNullMaterialColor() {
        GVRMaterial material = null;
        int androidColor = android.R.color.holo_blue_dark;
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, androidColor);
        assertNotNull(colorAnimation);
    }

    public void testConstructorMaterialColorNegative() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        int androidColor = -1;
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, androidColor);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorSceneRGB() {
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorNullSceneRGB() {
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRSceneObject sceneObject = null;
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorSceneRG() {
        float[] rgb = {DEFAULT_R, DEFAULT_G};
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorSceneR() {
        float[] rgb = {DEFAULT_R};
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorSceneEmptyRGB() {
        float[] rgb = {};
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorSceneNullRGB() {
        float[] rgb = null;
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, rgb);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorSceneColor() {
        int androidColor = android.R.color.holo_blue_dark;
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, androidColor);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorNullSceneColor() {
        int androidColor = android.R.color.holo_blue_dark;
        GVRSceneObject sceneObject = null;
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, androidColor);
        assertNotNull(colorAnimation);
    }

    // FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/18
    public void ignoretestConstructorSceneColorNegative() {
        int androidColor = -1;
        GVRSceneObject sceneObject = null;
        GVRColorAnimation colorAnimation = new GVRColorAnimation(sceneObject, ANIM_DURATION, androidColor);
        assertNotNull(colorAnimation);
    }

    // TODO create test which calls animate with a duration of 0f
    public void ignoretestConstructorZeroDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
    }

    // TODO create test which calls animate with a duration of -2f
    public void ignoretestConstructorNegativeDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
    }

    // TODO create test which calls animate with a duration of NaN
    public void ignoretestConstructorNaNDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
    }

    // TODO create test which calls animate with a duration of positive infinity
    public void ignoretestConstructorPositiveInfinityDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
    }

    // TODO create test which calls animate with a duration of negative infinity
    public void ignoretestConstructorNegativeInfinityDuration() {
        ColorShader colorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, colorShader.getShaderId());
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        GVRColorAnimation colorAnimation = new GVRColorAnimation(material, ANIM_DURATION, rgb);
    }

    public void ignoreCreateObjectColorAnimationWithSceneObject() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        new GVRColorAnimation(sceneObject, 1.0f, rgb);
    }

    public void ignoreCreateObjectColorAnimationWithColor() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        float[] rgb = {DEFAULT_R, DEFAULT_G, DEFAULT_B};
        new GVRColorAnimation(sceneObject, 1.0f, 83);
    }
}
