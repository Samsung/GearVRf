package com.samsung.android.gvr.test.unit;

import org.gearvrf.GVRCustomCamera;
import org.gearvrf.GVRRenderData;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by m.gorll on 2/27/2015.
 */
public class GVRCustomCameraTest extends ActivityInstrumentationGVRf {




    //FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/22
    public void ignoreCreateCustomCamera() {
        GVRCustomCamera leftCustomCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        leftCustomCamera.setProjectionMatrix(2f, 0f, 1f, 0f,
                0f, 2f, 1f, 0f,
                0f, 0f, -1f, -2f,
                0f, 0f, -1f, 0f);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig().attachLeftCamera(leftCustomCamera);
    }

    //FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/32
    public void ignoreCreateCustomCameraRight() {

        GVRCustomCamera rightCustomCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        rightCustomCamera.setProjectionMatrix(2f, 0f, 1f, 0f,
                0f, 2f, 1f, 0f,
                0f, 0f, -1f, -2f,
                0f, 0f, -1f, 0f);
        TestDefaultGVRViewManager.mGVRContext.getMainScene().getMainCameraRig().attachRightCamera(rightCustomCamera);
    }

    public void testSetBackgroundColorInvalid() {

        GVRCustomCamera leftCustomCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        leftCustomCamera.setProjectionMatrix(2f, 0f, 1f, 0f,
                0f, 2f, 1f, 0f,
                0f, 0f, -1f, -2f,
                0f, 0f, -1f, 0f);
        try {
            leftCustomCamera.setBackgroundColor(new Integer("aaa"));
            fail();
        } catch (NumberFormatException e) {
            assertNotNull(e.getMessage());
        }
    }

    public void testSetRenderMask() {

        GVRCustomCamera leftCustomCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        leftCustomCamera.setProjectionMatrix(2f, 0f, 1f, 0f,
                0f, 2f, 1f, 0f,
                0f, 0f, -1f, -2f,
                0f, 0f, -1f, 0f);
        leftCustomCamera.setRenderMask(GVRRenderData.GVRRenderMaskBit.Left);
        assertEquals(leftCustomCamera.getRenderMask(), GVRRenderData.GVRRenderMaskBit.Left);
    }

    //FIXME https://github.com/Samsung/Gear-VR-Hybrid/issues/33
    public void ignoreSetRenderMaskInvalid() {

        GVRCustomCamera leftCustomCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        leftCustomCamera.setProjectionMatrix(2f, 0f, 1f, 0f,
                0f, 2f, 1f, 0f,
                0f, 0f, -1f, -2f,
                0f, 0f, -1f, 0f);

        leftCustomCamera.setRenderMask(365);
        fail();
    }

    public void testSetBackgroundA() {
        GVRCustomCamera customCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        customCamera.setBackgroundColorA(0f);
        assertEquals(customCamera.getBackgroundColorA(), 0f);
    }

    public void testSetBackgroundR() {
        GVRCustomCamera customCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        customCamera.setBackgroundColorA(0f);
        assertEquals(customCamera.getBackgroundColorR(), 0f);
    }

    public void testSetBackgroundG() {
        GVRCustomCamera customCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        customCamera.setBackgroundColorA(0f);
        assertEquals(customCamera.getBackgroundColorG(), 0f);
    }

    public void testSetBackgroundB() {
        GVRCustomCamera customCamera = new GVRCustomCamera(TestDefaultGVRViewManager.mGVRContext);
        customCamera.setBackgroundColorA(0f);
        assertEquals(customCamera.getBackgroundColorB(), 0f);
    }
}
