package com.samsung.android.gvr.test.unit;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREyePointeeHolder;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshEyePointee;
import org.gearvrf.GVRSceneObject;

import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.misc.ColorShader;
import com.samsung.android.gvr.app.base.utils.UtilResource;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;
import com.samsung.android.gvr.test.base.BoundsValues;

import java.security.InvalidParameterException;

/**
 * Created by d.alipio@samsunsg.com; on 1/21/15.
 */
public class GVRMeshTest extends ActivityInstrumentationGVRf {


    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;
    private GVRSceneObject mSceneObject;


    /**
     * Valid create mesh factory.
     */
    public void testCreateMeshFactory() {
        GVRContext gvrContext = TestDefaultGVRViewManager.mGVRContext;
        assertNotNull(gvrContext.createQuad(11, 22));
    }

    public void testTheVerticesPositionOfVertices() {
        GVRContext gvrContext = TestDefaultGVRViewManager.mGVRContext;
        GVRMesh mesh = new GVRMesh(gvrContext);
        float vertices[] = {-0.5f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, 0.5f, 0.0f, 0.5f, -0.5f, 0.0f};
        mesh.setVertices(vertices);
        assertEquals(mesh.getVertices()[0], vertices[0]);
        assertEquals(mesh.getVertices()[1], vertices[1]);
        assertEquals(mesh.getVertices()[2], vertices[2]);
        assertEquals(mesh.getVertices()[3], vertices[3]);
        assertEquals(mesh.getVertices()[4], vertices[4]);
        assertEquals(mesh.getVertices()[5], vertices[5]);
        assertEquals(mesh.getVertices()[6], vertices[6]);
        assertEquals(mesh.getVertices()[7], vertices[7]);
        assertEquals(mesh.getVertices()[8], vertices[8]);
        assertEquals(mesh.getVertices()[9], vertices[9]);
        assertEquals(mesh.getVertices()[10], vertices[10]);
        assertEquals(mesh.getVertices()[11], vertices[11]);
    }


    private GVRSceneObject getColorBoard(float width, float height) {

        GVRContext gvrContext = TestDefaultGVRViewManager.mGVRContext;
        ColorShader mColorShader = new ColorShader(gvrContext);
        GVRMaterial material = new GVRMaterial(gvrContext, mColorShader.getShaderId());
        material.setVec4(ColorShader.COLOR_KEY, UNPICKED_COLOR_R, UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRSceneObject board = new GVRSceneObject(gvrContext, width, height);
        board.getRenderData().setMaterial(material);

        return board;
    }

    public void testAttachEyePointee() {
        mSceneObject = getColorBoard(1.0f, 1.0f);
        mSceneObject.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        GVRSceneObject object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        GVRContext gvrContext = TestDefaultGVRViewManager.mGVRContext;
        GVREyePointeeHolder eyePointeeHolder = new GVREyePointeeHolder(gvrContext);
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(gvrContext, mSceneObject.getRenderData().getMesh());
        eyePointeeHolder.addPointee(eyePointee);
        mSceneObject.attachEyePointeeHolder(eyePointeeHolder);
    }

    public void testGetGVRMeshOfEyePointee() {
        mSceneObject = getColorBoard(1.0f, 1.0f);
        mSceneObject.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        GVRSceneObject object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        GVRContext gvrContext = TestDefaultGVRViewManager.mGVRContext;
        GVRMeshEyePointee eyePointee = new GVRMeshEyePointee(gvrContext, mSceneObject.getRenderData().getMesh());
        assertNotNull(eyePointee.getMesh());
    }


    public void testGetOwnerObjectScene() {
        mSceneObject = getColorBoard(1.0f, 1.0f);
        mSceneObject.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        GVRSceneObject object = getColorBoard(1.0f, 1.0f);
        object.getTransform().setPosition(0.0f, 3.0f, -5.0f);
        assertNotNull(mSceneObject);
    }


    public void testGetNormalGVRMesh() {
        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext, R.mipmap.cylinder));
        assertNotNull(gvrMesh.getNormals());
    }


    /**
     * Try set object empty in @gvrMesh.setNormals
     */
    public void testRetrievePositionEmptyArrayInNormalsMesh() {
        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));

        try {
            gvrMesh.setNormals(BoundsValues.getArrayFloatEmpty());
            fail();

        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }

    }

    /**
     * Try set object empty in @gvrMesh.setFloatVector
     */
    public void testRetrievePositionEmptyArrayInVector() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));

        try {
            gvrMesh.setFloatVector("ratio_r", BoundsValues.getArrayFloatEmpty());
            fail();
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Try set object empty in @gvrMesh.setTexCoords
     */
    public void testRetrievePositionEmptyArrayInTextCoords() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));

        try {
            gvrMesh.setTexCoords(BoundsValues.getArrayFloatEmpty());
            fail();
        } catch (Exception e) {
            assertNotNull(e.getMessage());
        }
    }

    /**
     * Try set object empty in @gvrMesh.setVec2Vector
     * TODO-Native Crash
     */

    public void ignoreRetrievePositionEmptyArrayInVec2() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));

        try {
            gvrMesh.setVec2Vector("ratio_", BoundsValues.getArrayFloatEmpty());
            assertNotNull(gvrMesh.getVec2Vector("ratio_r")[0]);
        } catch (Exception e) {
            fail();
        }

    }

    /**
     * Try set object empty in @gvrMesh.setVec3Vector
     * TODO-Native Crash
     */
    public void ignoreRetrievePositionEmptyArrayInVec3() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));

        try {
            gvrMesh.setVec3Vector("ratio_", BoundsValues.getArrayFloatEmpty());
            assertNull(gvrMesh.getVec3Vector("ratio_r")[0]);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Try set object empty in @gvrMesh.setVec4Vector
     */
    public void ignoreRetrievePositionEmptyArrayInVec4() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));

        try {
            gvrMesh.setVec4Vector("ratio_", BoundsValues.getArrayFloatEmpty());
            assertNotNull(gvrMesh.getVec4Vector("ratio_r")[0]);
        } catch (Exception e) {
            fail();
        }
    }

    /**
     * Try set object empty in @gvrMesh.setVertices
     * TODO-Native Crashd
     */
    public void ignoreRetrievePositionEmptyArrayInVertices() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));

        try {
            gvrMesh.setVertices(BoundsValues.getArrayFloatEmpty());
            assertNotNull(gvrMesh.getVec4Vector("ratio_r")[0]);
        } catch (Exception e) {
            fail();
        }

    }

    /**
     * Create a array with six position in vec4.
     * In documentation we have:
     * <p/>
     * Bind an array of three-component float vectors to the shader attribute key.
     */
    public void testCreateArrayForVec4With6Position() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        float[] vec4 = {-0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f};

        try {
            gvrMesh.setVec4Vector("ratio_r", vec4);
            assertNotNull(gvrMesh.getVec4Vector("ratio_r"));
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "setVec4Vector method support only three position array");
        }
    }


    /**
     * Create a array with six position in vec3.
     * In documentation we have:
     * <p/>
     * <p/>
     * Bind an array of three-component float vectors to the shader attribute key.
     */
    public void ignoreCreateArrayForVec3With6Position() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        float[] vec3 = {-0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f};

        try {
            gvrMesh.setVec3Vector("ratio_r", vec3);
            assertNotNull(gvrMesh.getVec4Vector("ratio_r"));
            fail();
        } catch (InvalidParameterException e) {
            assertEquals(e.getMessage(), "setVec3Vector method support only three position array");
        }
    }

    /**
     * Create a array with six position in vec2.
     * In documentation we have:
     * <p/>
     * Bind an array of two-component float vectors to the shader attribute key.
     */
    public void ignoreCreateArrayForVec2With6Position() {

        GVRMesh gvrMesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        float[] vec2 = {-0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f};

        try {
            gvrMesh.setVec3Vector("ratio_r", vec2);
            assertNotNull(gvrMesh.getVec4Vector("ratio_r"));
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "setVec2Vector method support only three position array");
        }
    }
}
