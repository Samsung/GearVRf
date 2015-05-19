package com.samsung.android.gvr.test.unit;

import org.gearvrf.GVRRenderTexture;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;
import com.samsung.android.gvr.test.base.ActivityInstrumentationGVRf;

/**
 * Created by santhyago on 2/27/15.
 */
public class GVRRenderTextureTest extends ActivityInstrumentationGVRf {


    public void testGVRRenderTextureCtx3Int() {
        GVRRenderTexture renderTex = new GVRRenderTexture(TestDefaultGVRViewManager.mGVRContext, 1, 2, 3);
        assertTrue(renderTex.getUseCount() > 0);
    }
}
