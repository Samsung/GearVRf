package com.samsung.android.gvr.test.unit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import com.samsung.android.gvr.app.R;
import com.samsung.android.gvr.app.base.utils.UtilResource;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by Douglas on 2/26/15.
 */
public class GVRSceneObjectTest extends ActivityInstrumentationGVRf {

    public void testVerifyIMainSceneIsNull() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext, R.mipmap.cylinder));

        GVRSceneObject screen = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh,
                TestDefaultGVRViewManager.mGVRContext.loadTexture("sample_20140509_l.bmp"));

        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(screen);
        assertNotNull(TestDefaultGVRViewManager.mGVRContext.getMainScene());
    }

    public void testRemoveSceneObject() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject screen = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh,
                TestDefaultGVRViewManager.mGVRContext.loadTexture("sample_20140509_l.bmp"));
        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(screen);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().removeSceneObject(screen);
    }

    public void testAddingSceneObject() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject screen = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh,
                TestDefaultGVRViewManager.mGVRContext.loadTexture("sample_20140509_l.bmp"));
        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(screen);
    }

    public void testIfSceneObjectWasAddedInWholeSceneobjects() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject screen = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh,
                TestDefaultGVRViewManager.mGVRContext.loadTexture("sample_20140509_l.bmp"));
        GVRSceneObject screen1 = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh,
                TestDefaultGVRViewManager.mGVRContext.loadTexture("sample_20140509_l.bmp"));

        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(screen);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(screen1);

        if (TestDefaultGVRViewManager.mGVRContext.getMainScene().getWholeSceneObjects().length >= 2)
            assertTrue(true);
        else
            fail();
    }

    public void testAddGVRSceneValid() {
        GVRScene gvrScene = new GVRScene(TestDefaultGVRViewManager.mGVRContext);
        TestDefaultGVRViewManager.mGVRContext.setMainScene(gvrScene);
        assertNotNull(TestDefaultGVRViewManager.mGVRContext.getMainScene());
    }

    public void testAddGVRSceneNull() {
        TestDefaultGVRViewManager.mGVRContext.setMainScene(null);
        assertNull(TestDefaultGVRViewManager.mGVRContext.getMainScene());
    }

    public void testGetChildrenCount() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        assertEquals(new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh).getChildrenCount(), 0);
    }

    public void testDetachEyePointeeHolder() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh).detachRenderData();
    }

    public void testDetachCameraRig() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh).detachCameraRig();
    }

    public void testDetachCamera() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh).detachCamera();
    }

    public void testDetachRenderData() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh).detachRenderData();
    }


    public void testDetachGetSetName() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        sceneObject.setName("Test");
        assertEquals("Test", sceneObject.getName());
    }

    public void ignoreSceneObjectSetGetNameNull() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        try {
            sceneObject.setName(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals(sceneObject.getName(), null);
        }
    }

    public void testCreateSceneObject() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(sceneObject);
        assertNotNull(sceneObject.children());
    }

    public void testRemoveChildObject() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().addSceneObject(sceneObject);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().removeSceneObject(sceneObject);
    }

    public void testGetCameraRig() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        sceneObject.attachCameraRig(TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig());
        assertNotNull(sceneObject.getCameraRig());
    }

    public void testGetCamera() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        sceneObject.attachCamera(TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig().getLeftCamera());
        assertNotNull(sceneObject.getCamera());
    }

    public void testParent() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        GVRSceneObject sceneObject2 = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        sceneObject.addChildObject(sceneObject2);
        assertNotNull(sceneObject2.getParent());
    }

    public void testAddChildObjectInSceneObject() {

        //scene object
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);

        //bitmap cursor
        Bitmap cursorBitmap = BitmapFactory.decodeResource(TestDefaultGVRViewManager.mGVRContext
                .getContext().getResources(), R.drawable.cursor);

        //children object
        GVRSceneObject cursor = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, 0.05f, 0.5f,
                new GVRBitmapTexture(TestDefaultGVRViewManager.mGVRContext, cursorBitmap));

        cursor.getTransform().setPosition(0.0f, 0.0f, -5.0f);
        sceneObject.addChildObject(cursor);

        assertEquals(cursor.hashCode(), sceneObject.getChildByIndex(0).hashCode());
    }

    public void testRemoveChildObjectInSceneObject() {

        //scene object
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);

        //bitmap cursor
        Bitmap cursorBitmap = BitmapFactory.decodeResource(TestDefaultGVRViewManager.mGVRContext
                .getContext().getResources(), R.drawable.cursor);

        //children object
        GVRSceneObject cursor = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, 0.05f, 0.5f,
                new GVRBitmapTexture(TestDefaultGVRViewManager.mGVRContext, cursorBitmap));

        sceneObject.addChildObject(cursor);
        sceneObject.removeChildObject(cursor);
        try {

            assertNull(sceneObject.getChildByIndex(0));

        } catch (IndexOutOfBoundsException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testGetEyePointeeHolder() {
        GVRMesh mesh = TestDefaultGVRViewManager.mGVRContext.loadMesh(UtilResource.androidResource(TestDefaultGVRViewManager.mGVRContext,R.mipmap.cylinder));
        GVRSceneObject sceneObject = new GVRSceneObject(TestDefaultGVRViewManager.mGVRContext, mesh);
        assertNull(sceneObject.getEyePointeeHolder());
    }

}
