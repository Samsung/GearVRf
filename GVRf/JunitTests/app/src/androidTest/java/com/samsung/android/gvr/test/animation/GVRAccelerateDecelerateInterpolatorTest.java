package com.samsung.android.gvr.test.animation;

import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.animation.GVRAccelerateDecelerateInterpolator;
import com.samsung.android.gvr.app.base.GVRTestActivity;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

import java.security.InvalidParameterException;

/**
 * Created by diego on 2/26/15.
 */
public class GVRAccelerateDecelerateInterpolatorTest extends ActivityInstrumentationGVRf {

    public GVRAccelerateDecelerateInterpolatorTest() {
        super(GVRTestActivity.class);
    }

    public void testGetInstance() {
        assertEquals(GVRPerspectiveCamera.getDefaultFovY(), 95.0f, 0.0f);
        GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
        assertNotNull(interpolator);
    }

    public void testMapRatio() {
        GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
        assertEquals(interpolator.mapRatio(0.5f), 0.5f);
    }

    public void testMapRatioNegative() {
        try {
            GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
            interpolator.mapRatio(-0.5f);
            fail();
        } catch (InvalidParameterException e) {
            assertNotNull(e.getMessage(), "ratio - the current time ratio, >= 0 and <= 1");
        }
    }

    public void testMapRatioEqualThanOne() {
        GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
        assertEquals(interpolator.mapRatio(1f), 1f);
    }

    public void testMapRatioGreaterThanOne() {
        try {
            GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
            interpolator.mapRatio(2f);
            fail();
        } catch (InvalidParameterException e) {
            assertNotNull(e.getMessage(), "ratio - the current time ratio, >= 0 and <= 1");
        }
    }

    public void testMapRatioNaN() {
        GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
        assertEquals(interpolator.mapRatio(Float.NaN), Float.NaN);
    }

    public void testMapRatioPositiveInfinity() {
        GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
        assertEquals(interpolator.mapRatio(Float.POSITIVE_INFINITY), Float.POSITIVE_INFINITY);
    }

    public void testMapRatioNegativeInfinity() {
        GVRAccelerateDecelerateInterpolator interpolator = GVRAccelerateDecelerateInterpolator.getInstance();
        assertEquals(interpolator.mapRatio(Float.NEGATIVE_INFINITY), Float.NEGATIVE_INFINITY);
    }
}
