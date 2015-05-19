package com.samsung.android.gvr.test.unit;

import android.os.Debug;
import android.util.Log;

import org.gearvrf.GVRCamera;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRPostEffect;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRSceneObject;
import com.samsung.android.gvr.app.base.misc.CustomPostEffectShaderManager;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * @author Marcos Gorll
 */
public class GVRPerspectiveCameraTest extends ActivityInstrumentationGVRf {

    private final String TAG = GVRPerspectiveCameraTest.class.getSimpleName();

    public void testCreatePerspectiveCamera() {
        Log.d(TAG, "testCreatePerspectiveCamera");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(TestDefaultGVRViewManager.mGVRContext, camera.getGVRContext());
    }


    //FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/56
    public void ignoretestAttachCamera() {
        Log.d(TAG, "testAttachCamera");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig().attachRightCamera(camera);
    }

    public void testGetNearClippingDistance() {
        Log.d(TAG, "testGetNearClippingDistance");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(0.1f, camera.getNearClippingDistance(), 0.01f);
    }

    public void testGetFarClippingDistance() {
        Log.d(TAG, "testGetNearClippingDistance");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(1000.0f, camera.getFarClippingDistance(), 0.01f);
    }

    public void testGetFovY() {
        Log.d(TAG, "testGetFovY");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(90.0f, camera.getFovY(), 0.01f);
    }

    public void testGetAspectRatio() {
        Log.d(TAG, "testGetAspectRatio");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(1.0f, camera.getAspectRatio(), 0.01f);
    }

    public void testGetDefaultFovY() {
        Log.d(TAG, "testGetDefaultFovY");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(90.0f, camera.getDefaultFovY(), 0.01f);
    }

    public void testGetDefaultAspectRatio() {
        Log.d(TAG, "testGetDefaultAspectRatio");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(1.0f, camera.getDefaultAspectRatio(), 0.01f);
    }

    public void testSetNearClippingDistance() {
        Log.d(TAG, "testSetNearClippingDistance");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        float distance = 1.5f;
        camera.setNearClippingDistance(distance);
        assertEquals(distance, camera.getNearClippingDistance(), 0.01f);
    }

    public void testSetFarClippingDistance() {
        Log.d(TAG, "testSetFarClippingDistance");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        float distance = 90.5f;
        camera.setFarClippingDistance(distance);
        assertEquals(distance, camera.getFarClippingDistance(), 0.01f);
    }

    public void testSetFovY() {
        Log.d(TAG, "testSetFovY");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        float value = 0.5f;
        camera.setFovY(value);
        assertEquals(value, camera.getFovY(), 0.01f);
    }

    public void testSetAspectRatio() {
        Log.d(TAG, "testSetAspectRatio");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        float value = 0.5f;
        camera.setAspectRatio(value);
        assertEquals(value, camera.getAspectRatio(), 0.01f);
    }

    public void testSetDefaultFovY() {
        Log.d(TAG, "testSetDefaultFovY");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        float value = 2.5f;
        camera.setDefaultFovY(value);
        assertEquals(value, camera.getDefaultFovY(), 0.01f);
    }

    public void testSetDefaultAspectRatio() {
        Log.d(TAG, "testSetDefaultAspectRatio");
        GVRPerspectiveCamera camera = new GVRPerspectiveCamera(TestDefaultGVRViewManager.mGVRContext);
        float value = 1.5f;
        camera.setDefaultAspectRatio(value);
        assertEquals(value, camera.getDefaultAspectRatio(), 0.01f);
    }

}

