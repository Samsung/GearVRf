package com.samsung.android.gvr.test.unit;

import android.view.Window;
import android.view.WindowManager;

import org.gearvrf.GVRScene;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by santhyago on 2/23/15.
 */
public class GVRActivityTest extends ActivityInstrumentationGVRf {
    /**
     * Wait a connection to pass null values.
     */
    public void ignoreSetScriptInActivityNull() {
        try {
            getActivity().setScript(null, null);
            assertTrue(false);
        } catch (NullPointerException e) {
            e.printStackTrace();
            assertTrue(true);
        }
    }

    /**
     * Uses the setScript method with valid parameters
     * TODO-Native Crash
     */
    public void ignoreSetScriptInActivityValid() {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().setScript(new TestDefaultGVRViewManager(), "gvr_s5.xml");
                }
            });
        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }
    }

    /**
     * Create a Activity with title bar.
     * TODO-Native Crash
     */
    public void ignoreSetScriptWithTitleBar() {

        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().requestWindowFeature(Window.FEATURE_ACTION_BAR);
                    getActivity().setScript(new TestDefaultGVRViewManager(), "gvr_s5.xml");
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        getInstrumentation().waitForIdleSync();
    }

    /**
     * Create an activity (GVRActivity) with width and height with WRAP_CONTENT.
     * This change freeze the smartphone.
     * <p/>
     * TODO-Native Crash
     */
    public void ignoreSetLayoutParamWrapContentInGVRActivity() {
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getActivity().getWindow().setFlags(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
                    getActivity().setScript(new TestDefaultGVRViewManager(), "gvr_s5.xml");
                }
            });
        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }
        getInstrumentation().waitForIdleSync();
    }

    /**
     * Test if the getFrameTime is greater than 0
     */
    @Test
    public void ignoreGetFrameTimeFromGVRContext() {
        float frameTime = TestDefaultGVRViewManager.mGVRContext.getFrameTime();
        assertTrue(frameTime > 0);
    }


    /**
     * Test if the return from getMainScene is not null
     */
    @Test
    public void testGetMainSceneFromGVRContextNotNull() {
        assertNotNull(TestDefaultGVRViewManager.mGVRContext.getMainScene());
    }

    /**
     * Test if the return from getMainScene is a GVRScene object
     */
    @Test
    public void testGetMainSceneFromGVRContext() {
        final GVRScene lGVRScene = new GVRScene(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(lGVRScene.getClass().getName(), TestDefaultGVRViewManager.mGVRContext.getMainScene().getClass()
                .getName());
    }

    /**
     * When try set mainScene null.
     * <p/>
     * TODO-Native Crash
     */
    @Test(expected = NullPointerException.class)
    @Ignore
    public void testSetGVRMainScene() {
        TestDefaultGVRViewManager.mGVRContext.setMainScene(null);
    }
}
