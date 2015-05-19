package com.samsung.android.gvr.test.unit;

import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;
import org.gearvrf.utility.Colors;

/**
 * Created by Douglas on 2/26/15.
 */
public class ColorsTest extends ActivityInstrumentationGVRf {

    public void testGetBytToGl() {
        float byteToGl = Colors.byteToGl(10);
        assertNotNull(byteToGl);
    }

    public void testGetGlToByte() {

        int glToByte = Colors.glToByte(10.0f);
        assertNotNull(glToByte);
    }

    public void testGetColor() {
        float[] rgb = {0, 0, 0};
        int color = Colors.toColor(rgb);
        assertNotNull(color);
    }

    public void testToColors() {
        float[] rgb = Colors.toColors(100);
        assertNotNull(rgb);
    }
}
