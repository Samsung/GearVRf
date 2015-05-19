package com.samsung.android.gvr.test.unit;

import org.gearvrf.GVRCamera;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import com.samsung.android.gvr.app.base.GVRTestActivity;
import com.samsung.android.gvr.app.base.misc.ColorShader;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;
import com.samsung.android.gvr.test.base.BoundsValues;

/**
 * Created by daniel.nog on 1/29/2015.
 */
public class GVRCameraRigTest extends ActivityInstrumentationGVRf {

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    public GVRCameraRigTest() {
        super(GVRTestActivity.class);
    }

    @Override
    public synchronized void setUp() throws Exception {
        super.setUp();
        ColorShader mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        TestDefaultGVRViewManager.mGVRContext.setMainScene(new GVRScene(TestDefaultGVRViewManager.mGVRContext));
    }

    private void init() {
        ColorShader mColorShader = new ColorShader(TestDefaultGVRViewManager.mGVRContext);
        GVRMaterial material = new GVRMaterial(TestDefaultGVRViewManager.mGVRContext, mColorShader.getShaderId());

        material.setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRSceneObject mGVRBoardObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, 1.0f, 1.0f);
        mGVRBoardObject.getRenderData().setMaterial(material);

        mGVRBoardObject.getTransform().setPosition(0.0f, 1.0f, 1.0f);

        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(TestDefaultGVRViewManager.mGVRContext);
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(TestDefaultGVRViewManager.mGVRContext, mGVRBoardObject.getRenderData().getMesh());
        eyePointeeHolder.addPointee(eyePointee);
        mGVRBoardObject.attachEyePointeeHolder(eyePointeeHolder);

        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(mGVRBoardObject);

        // GVRSceneObject mGVRSceneObject = mGVRBoardObject;
    }

    /**
     * GVRCameraRig - Test setCameraSeparationDistance
     */
    public void testSetCameraSeparationDistance() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        float distance = 1000f;
        gvrCameraRig.setCameraSeparationDistance(distance);
        //TestDefaultViewManager.mGVRContext.getMainScene().setMainCameraRig(gvrCameraRig);
        assertEquals(distance, gvrCameraRig.getCameraSeparationDistance());
    }

    /**
     * GVRCameraRig - Test setDefaultCameraSeparationDistance
     */
    public void testSetDefaultCameraSeparationDistance() {
        init();
        float distance = 1000f;
        GVRCameraRig.setDefaultCameraSeparationDistance(distance);
        assertEquals(distance, GVRCameraRig.getDefaultCameraSeparationDistance());
    }

    /**
     * GVRCameraRig - Test bound of getCameraSeparationDistance
     */
    public void testGetCameraSeparationDistanceBound() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        int size = BoundsValues.getFloatList().size();

        for (int i = 0; i < size; i++) {
            gvrCameraRig.setCameraSeparationDistance(BoundsValues.getFloatList().get(i));
            assertEquals(BoundsValues.getFloatList().get(i), gvrCameraRig.getCameraSeparationDistance());
        }
    }

    /**
     * GVRCameraRig - Test bound of setDefaultCameraSeparationDistance
     */
    public void testSetDefaultCameraSeparationDistanceBound() {
        init();
        int size = BoundsValues.getFloatList().size();
        for (int i = 0; i < size; i++) {
            GVRCameraRig.setDefaultCameraSeparationDistance(BoundsValues.getFloatList().get(i));
            assertEquals(BoundsValues.getFloatList().get(i), GVRCameraRig.getDefaultCameraSeparationDistance());
        }
    }

    /**
     * GVRCameraRig - Test if attachLeftCamera and getLeftCamera works
     */
    public void testAttachLeftCameraAndGetLeftCamera() {
        //TODO ver se este teste não quebra os outros, pois está mudando seriamente a estrutra interna

        init();

        GVRCamera leftCamera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        leftCamera.setRenderMask(GVRRenderData.GVRRenderMaskBit.Left);
        GVRCamera rightCamera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        rightCamera.setRenderMask(GVRRenderData.GVRRenderMaskBit.Right);
        GVRSceneObject leftCameraObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        leftCameraObject.attachCamera(leftCamera);
        GVRSceneObject rightCameraObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        rightCameraObject.attachCamera(rightCamera);
        GVRSceneObject cameraRigObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRCameraRig cameraRig = new GVRCameraRig(TestDefaultGVRViewManager.mGVRContext);
        cameraRig.attachLeftCamera(leftCamera);
        cameraRig.attachRightCamera(rightCamera);
        cameraRigObject.attachCameraRig(cameraRig);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(cameraRigObject);
        cameraRigObject.addChildObject(leftCameraObject);
        cameraRigObject.addChildObject(rightCameraObject);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().setMainCameraRig(cameraRig);

        assertEquals(TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig().getLeftCamera(), leftCamera);
        assertEquals(TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig().getRightCamera(), rightCamera);
    }

    /**
     * GVRCameraRig - Test getCameraRigType
     */
    public void testGetCameraRigType() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        assertTrue(!Float.isNaN(gvrCameraRig.getCameraRigType()));
    }

    /**
     * GVRCameraRig - Test setCameraRigType
     */
    public void testSetCameraRigType() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        int type = gvrCameraRig.getCameraRigType();
        try {
            gvrCameraRig.setCameraRigType(GVRCameraRig.GVRCameraRigType.Free.ID);
            assertEquals(GVRCameraRig.GVRCameraRigType.Free.ID, gvrCameraRig.getCameraRigType());
        } finally {
            gvrCameraRig.setCameraRigType(type);
        }
    }

    /**
     * GVRCameraRig - Test reset
     */
    public void testReset() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.reset();
        //teste completamente sem sentido. Apenas para passar pelo método.
        //Esse tipo de teste começa a indicar que temos um problema na semântica da API
    }


    /**
     * GVRCameraRig - Test resetYaw
     */
    public void testResetYaw() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.resetYaw();
        //teste completamente sem sentido. Apenas para passar pelo método.
        //Esse tipo de teste começa a indicar que temos um problema na semântica da API
    }

    /**
     * GVRCameraRig - Test resetYawPitch
     */
    public void testResetYawPitch() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.resetYawPitch();
        //teste completamente sem sentido. Apenas para passar pelo método.
        //Esse tipo de teste começa a indicar que temos um problema na semântica da API
    }

    /**
     * GVRCameraRig - Test getLookAt
     */
    public void testGetLookAt() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        float[] floats = gvrCameraRig.getLookAt();
        assertNotNull(floats);
        assertEquals(3, floats.length);
    }


    /**
     * GVRCameraRig - Test getFloat
     */
    public void testGetFloat() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.setFloat("ratio_r", 1.0f);
        assertTrue(!Float.isNaN(gvrCameraRig.getFloat("ratio_r")));
    }

    /**
     * GVRCameraRig - Test Bounds of  setFloat
     */
    public void testGetFloatBounds() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        int size = BoundsValues.getFloatList().size();
        for (int i = 0; i < size; i++) {
            gvrCameraRig.setFloat("ratio_r", BoundsValues.getFloatList().get(i));
            assertEquals(BoundsValues.getFloatList().get(i), gvrCameraRig.getFloat("ratio_r"));
        }
    }

    /**
     * GVRCameraRig - Test setFloat
     */
    public void testSetFloat() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        float value = 1.0f;
        gvrCameraRig.setFloat("ratio_r", value);
        assertEquals(value, gvrCameraRig.getFloat("ratio_r"));
    }

    /**
     * Just test setVec3 from GVRPostEffectData object from lib
     */
    public void testSetVec3() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        float x = 0.393f;
        float y = 0.769f;
        float z = 0.189f;
        gvrCameraRig.setVec3("ratio_r", x, y, z);
        float[] ratioR = gvrCameraRig.getVec3("ratio_r");
        assertNotNull(ratioR);
        assertEquals(3, ratioR.length);
        assertEquals(x, ratioR[0]);
        assertEquals(y, ratioR[1]);
        assertEquals(z, ratioR[2]);
    }

    /**
     * Just test Bounds of setVec3 from GVRPostEffectData object from lib
     */
    public void testSetVec3Bounds() {
        //TODO esse parece estar dando problema
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        int size = BoundsValues.getFloatList().size();
        for (int i = 0; i < size; i++) {
            float value = BoundsValues.getFloatList().get(i);
            gvrCameraRig.setVec3("ratio_r", value, value, value);
            float[] ratioR = gvrCameraRig.getVec3("ratio_r");
            assertNotNull(ratioR);
            assertEquals(3, ratioR.length);
            assertEquals(value, ratioR[0]);
            assertEquals(value, ratioR[1]);
            assertEquals(value, ratioR[2]);
        }
    }

    /**
     * Just test setVec2 from GVRPostEffectData object from lib
     */
    public void testSetVec2() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        float x = 0.393f;
        float y = 0.769f;
        gvrCameraRig.setVec2("ratio_r", 0.393f, 0.769f);
        float[] ratioR = gvrCameraRig.getVec2("ratio_r");
        assertNotNull(ratioR);
        assertEquals(2, ratioR.length);
        assertEquals(x, ratioR[0]);
        assertEquals(y, ratioR[1]);
    }

    public void testSetVec2Bounds() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        int size = BoundsValues.getFloatList().size();
        for (int i = 0; i < size; i++) {
            float value = BoundsValues.getFloatList().get(i);
            gvrCameraRig.setVec2("ratio_r", value, value);
            float[] ratioR = gvrCameraRig.getVec2("ratio_r");
            assertNotNull(ratioR);
            assertEquals(2, ratioR.length);
            assertEquals(value, ratioR[0]);
            assertEquals(value, ratioR[1]);
        }
    }

    /**
     * Just test setVec4 from GVRPostEffectData object from lib
     */
    public void testSetVec4() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        float x = 0.393f;
        float y = 0.769f;
        float z = 0.189f;
        float w = 0.065f;
        gvrCameraRig.setVec4("ratio_r", x, y, z, w);
        float[] ratioR = gvrCameraRig.getVec4("ratio_r");
        assertNotNull(ratioR);
        assertEquals(4, ratioR.length);
        assertEquals(x, ratioR[0]);
        assertEquals(y, ratioR[1]);
        assertEquals(z, ratioR[2]);
        assertEquals(w, ratioR[3]);
    }

    /**
     * Just test Bounds of setVec4 from GVRPostEffectData object from lib
     */
    public void testSetVec4Bounds() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();

        int size = BoundsValues.getFloatList().size();
        for (int i = 0; i < size; i++) {
            float value = BoundsValues.getFloatList().get(i);
            gvrCameraRig.setVec4("ratio_r", value, value, value, value);
            float[] ratioR = gvrCameraRig.getVec4("ratio_r");
            assertNotNull(ratioR);
            assertEquals(4, ratioR.length);
            assertEquals(value, ratioR[0]);
            assertEquals(value, ratioR[1]);
            assertEquals(value, ratioR[2]);
            assertEquals(value, ratioR[3]);
        }
    }

    /**
     * Just test getOwnerObject from GVRPostEffectData object from lib
     */
    public void testGetOwnerObject() {
        init();
        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        assertEquals(GVRSceneObject.class.getName(), gvrCameraRig.getOwnerObject().getClass().getName());
    }

    public void testCameraRigFloatNull() {
        try {

            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setFloat("ratio_r", BoundsValues.getFloatNull());
            fail();
        } catch (NullPointerException e) {
            //esperado
        }
    }

    public void testCameraRigVecNull() {
        try {

            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setVec2("ratio_r", BoundsValues.getFloatNull(), BoundsValues.getFloatNull());
            fail();
        } catch (NullPointerException e) {
            //esperado
        }
    }

    public void testCameraRigTypeNull() {
        try {

            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setCameraRigType(BoundsValues.getIntegerNull());
            fail();
        } catch (NullPointerException e) {
            //esperado
        }
    }

    public void testCameraRigSeparationNull() {
        try {

            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setCameraSeparationDistance(BoundsValues.getFloatNull());
            fail();
        } catch (NullPointerException e) {
            //esperado
        }
    }

    public void testVec2Null() {
        try {

            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setVec2("ratio_r", BoundsValues.getFloatNull(), BoundsValues.getFloatNull());
            fail();
        } catch (NullPointerException e) {
            //esperado
        }
    }

    public void testVec3Null() {
        try {

            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setVec3("ratio_r", BoundsValues.getFloatNull(), BoundsValues.getFloatNull(),
                    BoundsValues.getFloatNull());
            fail();
        } catch (NullPointerException e) {
            //esperado
        }
    }

    public void testVec4Null() {
        try {

            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setVec4("ratio_r", BoundsValues.getFloatNull(), BoundsValues.getFloatNull(),
                    BoundsValues.getFloatNull(), BoundsValues.getFloatNull());
            fail();
        } catch (NullPointerException e) {
            //esperado
        }
    }

    public void testSetCameraRigTypeFree() {

        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.setCameraRigType(GVRCameraRig.GVRCameraRigType.Free.ID);
        assertEquals(gvrCameraRig.getCameraRigType(), GVRCameraRig.GVRCameraRigType.Free.ID);
    }

    public void testSetCameraRigTypeFreeze() {

        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.setCameraRigType(GVRCameraRig.GVRCameraRigType.Freeze.ID);
        assertEquals(gvrCameraRig.getCameraRigType(), GVRCameraRig.GVRCameraRigType.Freeze.ID);
    }

    public void testSetCameraRigTypeOrbitPivot() {

        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.setCameraRigType(GVRCameraRig.GVRCameraRigType.OrbitPivot.ID);
        assertEquals(gvrCameraRig.getCameraRigType(), GVRCameraRig.GVRCameraRigType.OrbitPivot.ID);
    }

    public void testSetCameraRigTypeRollFreeze() {

        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.setCameraRigType(GVRCameraRig.GVRCameraRigType.RollFreeze.ID);
        assertEquals(gvrCameraRig.getCameraRigType(), GVRCameraRig.GVRCameraRigType.RollFreeze.ID);
    }

    public void testSetCameraRigTypeYawOnly() {

        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.setCameraRigType(GVRCameraRig.GVRCameraRigType.YawOnly.ID);
        assertEquals(gvrCameraRig.getCameraRigType(), GVRCameraRig.GVRCameraRigType.YawOnly.ID);
    }

    public void testInvalidCameraRigType() {

        GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
        gvrCameraRig.setCameraRigType(404);
        fail();
    }

    public void testExpectedAnIllegalArgumentException() {
        try {


            GVRCameraRig gvrCameraRig = TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig();
            gvrCameraRig.setCameraRigType(404);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "IllegalArgumentException for cameraRigType. Types supported: " +
                    "GVRCameraRig.GVRCameraRigType.Free, " +
                    "GVRCameraRig.GVRCameraRigType.Freeze, " +
                    "GVRCameraRig.GVRCameraRigType.OrbitPivot" +
                    "GVRCameraRig.GVRCameraRigType.RollFreeze" +
                    "GVRCameraRig.GVRCameraRigType.YawOnly");
        }
    }


    /**
     * GVRCameraRig - Test getFloat
     * TODO-Native Crashd
     */

    public void ignoregetFloat1() {
        init();
        GVRCamera leftCamera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        leftCamera.setRenderMask(GVRRenderData.GVRRenderMaskBit.Left);
        GVRCamera rightCamera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        rightCamera.setRenderMask(GVRRenderData.GVRRenderMaskBit.Right);
        rightCamera.setBackgroundColorA(12412.1f);
        GVRSceneObject leftCameraObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        leftCameraObject.attachCamera(leftCamera);
        GVRSceneObject rightCameraObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        rightCameraObject.attachCamera(rightCamera);
        GVRSceneObject cameraRigObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext);
        GVRCameraRig cameraRig = new GVRCameraRig(TestDefaultGVRViewManager.mGVRContext);
        cameraRig.attachLeftCamera(leftCamera);
        cameraRig.attachRightCamera(rightCamera);
        cameraRigObject.attachCameraRig(cameraRig);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(cameraRigObject);
        cameraRigObject.addChildObject(leftCameraObject);
        cameraRigObject.addChildObject(rightCameraObject);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().setMainCameraRig(cameraRig);

        assertTrue(!Float.isNaN(TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig().getFloat("ratio_r")));

    }
}
