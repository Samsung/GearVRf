package com.samsung.android.gvr.test.periodic;

import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

import org.gearvrf.periodic.GVRPeriodicEngine;

/**
 * Created by nathan.a on 24/02/2015.
 */
public class GVRPeriodicEngineTest extends ActivityInstrumentationGVRf {

    private String DELAY_ERROR_MESSAGE = "delay must be >= 0";
    private String PERIOD_ERROR_MESSAGE = "period must be > 0";

    public void testCreateNewGVRPeriodicEngineTest() {
        assertNotNull(GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext));
    }

    public void testCreateNewGVRPeriodicEngineTestWithNullContext() {
        try {
            GVRPeriodicEngine.getInstance(null);
            fail("Cannot create GVRPeriodicEngine instance with null context");
        } catch (NullPointerException e) {
        }
    }

    public void testRunAfterNegativeDelay() {
        float delay = -200f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        try {
            periodicEngine.runAfter(new Runnable() {
                @Override
                public void run() {

                }
            }, delay);
            fail("Delay cannot be negative");
        } catch (IllegalArgumentException e) {
            assertEquals(DELAY_ERROR_MESSAGE, e.getMessage());
        }
    }

    public void testRunAfterZeroDelay() {
        float delay = 0;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        periodicEngine.runAfter(new Runnable() {
            @Override
            public void run() {

            }
        }, delay);
    }

    /**
     * valid values for decimal points are:
     * .0
     * .0625
     * .125
     * .1875
     * .25
     * .3125
     * .375
     * .4375
     * .5
     * .5625
     * .625
     * .6875
     * .75
     * .8125
     * .875
     * .9375
     */
    public void testRunAfterPositiveDelay() {
        float delay = 28.125f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(delay, periodicEngine.runAfter(new Runnable() {
            @Override
            public void run() {

            }
        }, delay).getCurrentWait());
    }

    public void testRunEveryNegativeDelay() {
        float delay = -200f;
        float period = 3.234f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        try {
            periodicEngine.runEvery(new Runnable() {
                @Override
                public void run() {

                }
            }, delay, period);
            fail("Delay cannot be negative");
        } catch (IllegalArgumentException e) {
            assertEquals(DELAY_ERROR_MESSAGE, e.getMessage());
        }
    }

    public void testRunEveryZeroDelay() {
        float delay = 0;
        float period = 3.234f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        periodicEngine.runEvery(new Runnable() {
            @Override
            public void run() {

            }
        }, delay, period);
    }

    public void testRunEveryPositiveDelay() {
        float delay = 28.125f;
        float period = 3.234f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        assertEquals(delay, periodicEngine.runEvery(new Runnable() {
            @Override
            public void run() {

            }
        }, delay, period).getCurrentWait());
    }

    public void testRunEveryNegativePeriod() {
        float delay = 0f;
        float period = -8.2564f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        try {
            periodicEngine.runEvery(new Runnable() {
                @Override
                public void run() {

                }
            }, delay, period);
            fail("Period cannot be negative");
        } catch (IllegalArgumentException e) {
            assertEquals(PERIOD_ERROR_MESSAGE, e.getMessage());
        }
    }

    public void testRunEveryZeroPeriod() {
        float delay = 0f;
        float period = 0f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        try {
            periodicEngine.runEvery(new Runnable() {
                @Override
                public void run() {

                }
            }, delay, period);
            fail("Delay cannot be zero");
        } catch (IllegalArgumentException e) {
            assertEquals(PERIOD_ERROR_MESSAGE, e.getMessage());
        }
    }

    //TODO: Remover assertEquals
    public void testRunEveryPositivePeriod() {
        float delay = 0f;
        float period = 12.25f;
        GVRPeriodicEngine periodicEngine = GVRPeriodicEngine.getInstance(TestDefaultGVRViewManager.mGVRContext);
        periodicEngine.runEvery(new Runnable() {
            @Override
            public void run() {

            }
        }, delay, period);
    }

}
