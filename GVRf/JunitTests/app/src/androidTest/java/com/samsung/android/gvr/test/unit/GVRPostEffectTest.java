package com.samsung.android.gvr.test.unit;

import org.gearvrf.GVRContext;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by d.schettini on 3/4/2015.
 */
public class GVRPostEffectTest  {
    private final String TAG = GVRPostEffectTest.class.getSimpleName();

    private static final float DEFAULT_R = 1f;
    private static final float DEFAULT_G = 1f;
    private static final float DEFAULT_B = 1f;
    private static final float ANIM_DURATION = 1.5f;

//    private CustomPostEffectShaderManager shaderManager;
//
//    @Override
//    protected void setUp() throws Exception {
//        super.setUp();
//        mGVRContext = SampleViewManager.mGVRContext;
//        shaderManager = new CustomPostEffectShaderManager(mGVRContext);
//    }
//
//    public void testPostEffectConstructor(){
//        Log.d(TAG, "Starting testPostEffectConstructor!");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        assertNotNull(postEffect);
//
//    }
//
//    public void testSetFloat(){
//        Log.d(TAG, "Starting testSetFloat");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        float value = 1.5f;
//        String key = "ratio_r";
//        postEffect.setFloat(key, value);
//        assertEquals(value, postEffect.getFloat(key));
//
//    }
//
//    public void testFloatBounds(){
//        Log.d(TAG, "Starting testFloatBounds");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        String key = "ratio_r";
//
//        int size = BoundsValues.getFloatList().size();
//        for (int i = 0; i<size; i++){
//            postEffect.setFloat(key, BoundsValues.getFloatList().get(i));
//            assertEquals(BoundsValues.getFloatList().get(i), postEffect.getFloat(key));
//        }
//
//    }
//
//    public void testSizeVec3(){
//        Log.d(TAG, "Starting testSizeVec3");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        float[] value = {0.395f, 0.789f, 0.200f};
//        String key = "ratio_r";
//        postEffect.setVec3(key, value[0], value[1], value[2]);
//        float[] ratioR = postEffect.getVec3(key);
//
//        assertEquals(value.length, ratioR.length);
//
//    }
//
//
//    public void testSetVec3(){
//        Log.d(TAG, "Starting testSetVec3");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        float[] value = {0.395f, 0.789f, 0.200f};
//        String key = "ratio_r";
//        postEffect.setVec3(key, value[0], value[1], value[2]);
//        float[] ratioR = postEffect.getVec3(key);
//
//        for(int i=0; i<value.length; i++)
//            assertEquals(value[i], ratioR[i]);
//
//
//    }
//
//    public void testSizeVec2(){
//        Log.d(TAG, "Starting testSizeVec2");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        float[] value = {0.395f, 0.789f};
//        String key = "ratio_r";
//        postEffect.setVec2(key, value[0], value[1]);
//        float[] ratioR = postEffect.getVec2(key);
//
//        assertEquals(value.length, ratioR.length);
//
//    }
//
//
//    public void testSetVec2(){
//        Log.d(TAG, "Starting testSetVec2");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        float[] value = {0.395f, 0.789f};
//        String key = "ratio_r";
//        postEffect.setVec2(key, value[0], value[1]);
//        float[] ratioR = postEffect.getVec2(key);
//
//        for(int i=0; i<value.length; i++)
//            assertEquals(value[i], ratioR[i]);
//
//
//    }
//
//    public void testSizeVec4(){
//        Log.d(TAG, "Starting testSizeVec4");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        float[] value = {0.395f, 0.789f, 0.200f, 0.070f};
//        String key = "ratio_r";
//        postEffect.setVec4(key, value[0], value[1], value[2], value[3]);
//        float[] ratioR = postEffect.getVec4(key);
//
//        assertEquals(value.length, ratioR.length);
//
//    }
//
//
//    public void testSetVec4(){
//        Log.d(TAG, "Starting testSetVec4");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//
//        float[] value = {0.395f, 0.789f, 0.200f, 0.070f};
//        String key = "ratio_r";
//        postEffect.setVec4(key, value[0], value[1], value[2], value[3]);
//        float[] ratioR = postEffect.getVec4(key);
//
//        for(int i=0; i<value.length; i++)
//            assertEquals(value[i], ratioR[i]);
//
//
//    }
//
//    public void testTextureNull(){
//        Log.d(TAG, "Starting testTextureNull");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//        GVRTexture texture = mGVRContext.loadTexture("sea_env.jpg");
//        String key = "texture";
//
//        postEffect.setTexture(key, texture);
//
//        assertNotNull(postEffect.getTexture(key));
//
//    }
//
//
//    public void testGetTexture(){
//        Log.d(TAG, "Starting testGetTexture");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//        GVRTexture texture = mGVRContext.loadTexture("sea_env.jpg");
//        String key = "texture";
//
//        postEffect.setTexture(key, texture);
//
//        assertEquals(texture, postEffect.getTexture(key));
//
//
//    }
//
//    public void testMainTextureNull(){
//        Log.d(TAG, "Starting testMainTextureNull");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//        GVRTexture texture = mGVRContext.loadTexture("sea_env.jpg");
//
//        postEffect.setMainTexture(texture);
//
//        assertNotNull(postEffect.getMainTexture());
//
//    }
//
//
//    public void testGetMainTexture(){
//        Log.d(TAG, "Starting testGetTexture");
//
//        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
//        GVRTexture texture = mGVRContext.loadTexture("sea_env.jpg");
//
//        postEffect.setMainTexture(texture);
//
//        assertEquals(texture, postEffect.getMainTexture());
//
//    }

    /*
    public void testShaderTypeNull(){
        Log.d(TAG, "Starting testShaderTypeNull");

        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());

        postEffect.setShaderType();

        postEffect.setMainTexture(texture);

        assertNotNull(postEffect.getMainTexture());

    }


    public void testGetMainTexture(){
        Log.d(TAG, "Starting testGetTexture");

        GVRPostEffect postEffect = new GVRPostEffect(mGVRContext, shaderManager.getShaderId());
        GVRTexture texture = mGVRContext.loadTexture("sea_env.jpg");

        postEffect.setMainTexture(texture);

        assertEquals(texture, postEffect.getMainTexture());

    }
    */



}
