package com.samsung.android.gvr.test.unit;

import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import com.samsung.android.gvr.app.base.misc.ColorShader;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.app.base.misc.ReflectionShader;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

import java.text.DecimalFormat;

/**
 * Created by santhyago on 2/27/15.
 */
public class GVRMaterialTest extends ActivityInstrumentationGVRf {
    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    private ColorShader mColorShader = null;
    private float mWidth = 1.0f;
    private float mHeight = 1.0f;


    public void testGetTexture() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);

        GVRTexture m360 = TestDefaultGVRViewManager.mGVRContext.loadTexture("env.jpg");
        //Testing GVRMaterial constructor using GVRContext and a Long parameter
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setTexture(ReflectionShader.TEXTURE_KEY, m360);
        material.setVec4(ColorShader.COLOR_KEY,
                UNPICKED_COLOR_R,
                UNPICKED_COLOR_G,
                UNPICKED_COLOR_B,
                UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mWidth, mHeight);
        board.getRenderData().setMaterial(material);

        assertEquals(m360, material.getTexture(ReflectionShader.TEXTURE_KEY));
        assertNull(material.getTexture("0"));
    }

    public void testSetColorInt() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY,
                UNPICKED_COLOR_R,
                UNPICKED_COLOR_G,
                UNPICKED_COLOR_B,
                UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mWidth, mHeight);
        board.getRenderData().setMaterial(material);

        DecimalFormat fourPlaces = new DecimalFormat("#.####");
        material.setColor(100000);

        float fourPlacesDecimal = Float.valueOf(fourPlaces.format(material.getColor()[0]));
        assertEquals(0.0039f, fourPlacesDecimal);
    }

    public void ignoreGetVec2WithAbsentKey() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        assertNull(material.getVec2("radio_r"));
    }

    public void ignoreGetVec3WithAbsentKey() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        assertNull(material.getVec3("radio_r"));
    }

    public void ignoreGetVec4WithAbsentKey() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        assertNull(material.getVec4("radio_r"));
    }

    public void testGetVec4() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY,
                UNPICKED_COLOR_R,
                UNPICKED_COLOR_G,
                UNPICKED_COLOR_B,
                UNPICKED_COLOR_A);
        material.getVec4(ColorShader.COLOR_KEY);
    }

    public void testGetVec3() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setVec3(ColorShader.COLOR_KEY,
                UNPICKED_COLOR_R,
                UNPICKED_COLOR_G,
                UNPICKED_COLOR_B);
        material.getVec3(ColorShader.COLOR_KEY);
    }

    public void testGetVec2() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setVec2(ColorShader.COLOR_KEY,
                UNPICKED_COLOR_R,
                UNPICKED_COLOR_G);
        assertNotNull(material.getVec2(ColorShader.COLOR_KEY));
    }

    public void testSetMat4Material() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setMat4("radio_r", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void testSetColorMaterial() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        float[] rgb = {0.0f, 0.0f, 0.0f};
        material.setColor(rgb[0], rgb[1], rgb[0]);
        assertNotNull(material.getColor());
    }

    public void testGetRGBColor() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        float[] rgb = {0.0f, 0.0f, 0.0f};
        material.setColor(rgb[0], rgb[1], rgb[0]);
        assertNotNull(material.getRgbColor());
    }

    public void testGetOpacity() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        material.setOpacity(1.0f);
        assertEquals(material.getOpacity(), 1.0f, 0);
    }

    public void testSetMainTexture() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());
        GVRTexture m360 = TestDefaultGVRViewManager.mGVRContext.loadTexture("sea_env.jpg");
        material.setMainTexture(m360);
        assertNotNull(material.getMainTexture());
    }

    public void testGetShaderType() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());

        material.setShaderType(GVRMaterial.GVRShaderType.OESHorizontalStereo.ID);
        assertEquals(GVRMaterial.GVRShaderType.OESHorizontalStereo.ID, material.getShaderType());
    }

    public void testSetColorShaderNull() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);

        try {
            new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, null);
            fail();

        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    public void testSetContextNull() {
        mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        try {
            new GVRMaterial(null, mColorShader.getShaderId());
            fail();
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }
}
