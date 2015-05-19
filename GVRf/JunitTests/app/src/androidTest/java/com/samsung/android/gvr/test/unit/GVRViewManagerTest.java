package com.samsung.android.gvr.test.unit;


import android.util.Log;

import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRScene;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;
import com.samsung.android.gvr.test.base.BoundsValues;

import java.security.InvalidParameterException;

/**
 * Created by danielnogueira on 2/11/15.
 */
public class GVRViewManagerTest extends ActivityInstrumentationGVRf {


    public void testGVRViewManagerKeyDownNull() {
        try {
            TestDefaultGVRViewManager.mGVRContext.isKeyDown(BoundsValues.getIntegerNull());
            fail();
        } catch (NullPointerException e) {
            assertTrue(true);
        }
    }

    public void testGVRViewLoadBitmapEmpty() {
        try {
            TestDefaultGVRViewManager.mGVRContext.loadBitmap("");
            fail();
        } catch (InvalidParameterException e) {
        }
    }

    public void testGVRViewLoadBitmapNull() {
        try {
            TestDefaultGVRViewManager.mGVRContext.loadBitmap(null);
            fail();
        } catch (NullPointerException e) {
            assertTrue(true);
        }

    }

    public void testGVRViewLoadBitmapPngInvalid() {
        try {
            TestDefaultGVRViewManager.mGVRContext.loadBitmap("invalid.png");
            fail();
        } catch (InvalidParameterException e) {
            assertTrue(true);
        }
    }

    public void testGVRViewLoadValidBitmap() {
        try {
            TestDefaultGVRViewManager.mGVRContext.loadBitmap("front.png");
            assertTrue(true);
        } catch (InvalidParameterException e) {
            fail();
        }
    }

    public void testGVRViewLoadBitmapNoPngFile() {
        try {
            TestDefaultGVRViewManager.mGVRContext.loadBitmap("bunny.obj");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testGVRViewLoadBitmapCorruptedFile() {

        try {
            TestDefaultGVRViewManager.mGVRContext.loadBitmap("corrupted.png");
            fail();
        } catch (InvalidParameterException e) {
        }
    }


    /**
     * Just pass null parameters to take registerDrawFrameListener
     * TODO-Native Crashd
     */
    public void testDrawFrameListenerNullParameter() {


        try {
            TestDefaultGVRViewManager.mGVRContext.registerDrawFrameListener(null);
            fail();
        } catch (NullPointerException e) {
        }

    }


    /**
     * Just test unregisterDrawFrameListener
     */
    public void testUnregisterDrawFrameListenerWithouRegister() {

        try {

            GVRDrawFrameListener gvrDrawFrameListener = new GVRDrawFrameListener() {
                @Override
                public void onDrawFrame(float frameTime) {
                    Log.d(GVRViewManagerTest.class.getSimpleName(), "onDrawFrame time :" + frameTime);

                }
            };
            TestDefaultGVRViewManager.mGVRContext.unregisterDrawFrameListener(gvrDrawFrameListener);
            assertTrue(true);

        } catch (Exception e) {
            fail();
        }

    }

    /**
     * Just test unregisterDrwaFrameListener WARNING THIS TEST NEED TO BE DONE BEFORE testregisterDrawFrameListener
     */
    public void testUnregisterDrawFrameListener() {

        try {

            GVRDrawFrameListener gvrDrawFrameListener = new GVRDrawFrameListener() {
                @Override
                public void onDrawFrame(float frameTime) {
                    Log.d(GVRViewManagerTest.class.getSimpleName(), "onDrawFrame time :" + frameTime);

                }
            };
            TestDefaultGVRViewManager.mGVRContext.registerDrawFrameListener(gvrDrawFrameListener);
            TestDefaultGVRViewManager.mGVRContext.unregisterDrawFrameListener(gvrDrawFrameListener);
            assertTrue(true);

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }

    }


    /**
     * Just take GVRCustomPostEffectShader object from lib
     */
    public void testRegisterDrawFrameListener() {

        try {

            TestDefaultGVRViewManager.mGVRContext.registerDrawFrameListener(new GVRDrawFrameListener() {
                @Override
                public void onDrawFrame(float frameTime) {
                    Log.d(GVRViewManagerTest.class.getSimpleName(), "onDrawFrame time :" + frameTime);
                    assertTrue(true);
                }
            });

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }

    }


    /**
     * Just test setMainScene
     */
    public void testSetMainScene() {

        try {

            TestDefaultGVRViewManager.mGVRContext.setMainScene(new GVRScene(TestDefaultGVRViewManager.mGVRContext));
            assertTrue(true);

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }

    }

    /**
     * Just test setMainScene
     */
    public void testGetMainScene() {

        try {

            assertNotNull(TestDefaultGVRViewManager.mGVRContext.getMainScene());

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }

    }


    /**
     * Just test isKeyDown
     */
    public void testIsKeyDownNull() {

        try {

            Integer i = null;
            TestDefaultGVRViewManager.mGVRContext.isKeyDown(i);
            assertTrue(false);

        } catch (Throwable throwable) {

            assertTrue(true);
            throwable.printStackTrace();

        }

    }

    /**
     * Just test isKeyDown
     */
    public void testIsKeyDown() {
        try {

            TestDefaultGVRViewManager.mGVRContext.isKeyDown(1);
            assertTrue(true);

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }

    }

    /**
     * Just test bound isKeyDown
     * TODO-Bound
     */

    public void testisKeyDownBound() {
        try {


            int size = BoundsValues.getIntegerList().size();
            try {

                for (int i = 0; i < size; i++) {

                    TestDefaultGVRViewManager.mGVRContext.isKeyDown(BoundsValues.getIntegerList().get(i));

                }

                assertTrue(true);
            } catch (Exception e) {

                assertTrue(false);

            }

            assertTrue(true);

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }

    }

    /**
     * Just pass null parameters to take registerDrawFrameListener
     * TODO-Native Crashd
     */
    public void testSetMainSceneNullParameters() {

        // To get Exception from other thread
        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                System.out.println("Uncaught exception: " + ex);
            }
        };

        Thread thread = new Thread() {

            @Override
            public void run() {
                super.run();

                try {

                    TestDefaultGVRViewManager.mGVRContext.setMainScene(null);
                    assertTrue(false);
                } catch (NullPointerException e) {
                    assertTrue(true);
                    throw e;

                } catch (Exception e) {

                }
            }
        };

        thread.setUncaughtExceptionHandler(h);
        thread.start();

    }


    /**
     * Just test getFrameTime
     */
    public void testGetFrameTime() {

        try {

            TestDefaultGVRViewManager.mGVRContext.getFrameTime();

            assertTrue(true);

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }

    }


    /**
     * Just test runOnGlThread
     */
    public void testRunOnGlThread() {

        try {

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    assertTrue(true);
                }
            };

            TestDefaultGVRViewManager.mGVRContext.runOnGlThread(run);

        } catch (Throwable throwable) {
            assertTrue(false);
            throwable.printStackTrace();
        }
    }

//    /**
//     * Just test tryFrontBufferActivation
//     */
//    public void testTryFrontBufferActivation() {
//
//        try {
//
//            TestDefaultGVRViewManager.mGVRContext.tryFrontBufferActivation();
//
//        } catch (Throwable throwable) {
//            assertTrue(false);
//            throwable.printStackTrace();
//        }
//    }
}