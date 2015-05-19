package com.samsung.android.gvr.test.unit;

import android.graphics.Bitmap;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPostEffectShaderManager;
import org.gearvrf.GVRScene;
import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.utils.UtilResource;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import org.gearvrf.periodic.GVRPeriodicEngine;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by santhyago on 2/25/15.
 */
public class GVRContextTest extends ActivityInstrumentationGVRf {
    /**
     * Test if the createQuad method creates a array of chars with 6 positions
     * (3 points = 1 triangle)
     * (6 points = 2 triangles = 1 quadrangle)
     */
    public void testCreateQuad() {
        float width = 1.0f;
        float height = 2.0f;

        /**
         * width  = X;
         * height = y;
         */

        GVRMesh quad = TestDefaultGVRViewManager.mGVRContext.createQuad(width, height);

        /**
         * Tests if the quad has 2 triangles with 2 vertices in common
         */
        assertEquals('\u0000', quad.getTriangles()[0]);
        assertEquals('\u0001', quad.getTriangles()[1]); // [1] is the same as [3]
        assertEquals('\u0002', quad.getTriangles()[2]); // [2] is the same as [5]
        assertEquals('\u0001', quad.getTriangles()[3]); // [3] is the same as [1]
        assertEquals('\u0003', quad.getTriangles()[4]);
        assertEquals('\u0002', quad.getTriangles()[5]); // [5] is the same as [2]

        /**
         * [   0] [   1] [  2]   [-width/2] [ height/2] [ 0.0]
         * [   3] [   4] [  5]   [-width/2] [-height/2] [ 0.0]
         * [   6] [   7] [  8] = [ width/2] [ height/2] [ 0.0]
         * [   9] [  10] [ 11]   [ width/2] [-height/2] [ 0.0]
         */

        float halfWidth = width / 2;
        float halfHeight = height / 2;
        assertEquals(-halfWidth, quad.getVertices()[0]);
        assertEquals(halfHeight, quad.getVertices()[1]);
        assertEquals(0.0f, quad.getVertices()[2]);
        assertEquals(-halfWidth, quad.getVertices()[3]);
        assertEquals(-halfHeight, quad.getVertices()[4]);
        assertEquals(0.0f, quad.getVertices()[5]);
        assertEquals(halfWidth, quad.getVertices()[6]);
        assertEquals(halfHeight, quad.getVertices()[7]);
        assertEquals(0.0f, quad.getVertices()[8]);
        assertEquals(halfWidth, quad.getVertices()[9]);
        assertEquals(-halfHeight, quad.getVertices()[10]);
        assertEquals(0.0f, quad.getVertices()[11]);
    }

    public void testGetMainScene() {
        GVRContext lGVRContext = TestDefaultGVRViewManager.mGVRContext;
        assertNotNull(lGVRContext);
        assertNotNull(lGVRContext.getMainScene());
        assertEquals(new GVRScene(lGVRContext).getClass().getSimpleName(),
                lGVRContext.getMainScene().getClass().getSimpleName());
    }

    public void testGetPeriodicEngine() {
        GVRContext lGVRContext = TestDefaultGVRViewManager.mGVRContext;
        GVRPeriodicEngine periodicEngine = lGVRContext.getPeriodicEngine();
        assertNotNull(periodicEngine);
        assertEquals(periodicEngine.getClass().getSimpleName(), "GVRPeriodicEngine");
    }

    public void testGetPostEffectShaderManager() {
        GVRContext lGVRContext = TestDefaultGVRViewManager.mGVRContext;
        GVRPostEffectShaderManager shaderManager = lGVRContext.getPostEffectShaderManager();

        lGVRContext.getMaterialShaderManager();
        assertNotNull(shaderManager);
    }

    public void testLoadBitmap() {
        GVRContext lGVRContext = TestDefaultGVRViewManager.mGVRContext;
        Bitmap bitmap;
        bitmap = lGVRContext.loadBitmap("texture1.jpg");
        assertEquals(1024, bitmap.getWidth());

        bitmap = lGVRContext.loadBitmap("front.png");
        assertEquals(512, bitmap.getWidth());

        bitmap = lGVRContext.loadBitmap("sample_20140509_l.bmp");
        assertEquals(3695, bitmap.getWidth());
    }



    public void testGetAnimationEngine() {
        GVRContext gVRContext = TestDefaultGVRViewManager.mGVRContext;
        assertNotNull(gVRContext.getAnimationEngine());
    }

    public void testgetMaterialShaderManager() {
        GVRContext gVRContext = TestDefaultGVRViewManager.mGVRContext;
        assertNotNull(gVRContext.getMaterialShaderManager());
    }

//    public void testLoadMeshWhitRaw(){
//        GVRContext gVRContext = TestDefaultGVRViewManager.mGVRContext;
//        gVRContext.loadMesh(R.raw.cinema);
//    }

//    public void testLoadMeshRelativyFileName(){
//        GVRContext gVRContext = TestDefaultGVRViewManager.mGVRContext;
//        gVRContext.loadMesh("cinema.obj");
//    }

    public void testLoadPngFile(){
        GVRContext gVRContext = TestDefaultGVRViewManager.mGVRContext;
        gVRContext.loadBitmap("texture1.jpg");
    }

//    public void testLoadMeshFromAssets() {
//        GVRContext lGVRContext = getActivity().getGVRContext();
//        GVRMesh mesh = lGVRContext.loadMeshFromAssets("pokeball.obj");
//        assertNotNull(mesh);
//        assertTrue(mesh.getTriangles().length > 0);
//    }

//    public void testLoadTexture() {
//        GVRContext lGVRContext = getActivity().getGVRContext();
//        GVRBitmapTexture bitmapTexture = lGVRContext.loadTexture("texture1.jpg");
//        assertNotNull(bitmapTexture);
//    }
}