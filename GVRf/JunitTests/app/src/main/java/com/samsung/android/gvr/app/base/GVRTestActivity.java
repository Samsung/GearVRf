package com.samsung.android.gvr.app.base;

import com.samsung.android.gvr.app.base.viewmanager.TestViewManager;

public class GVRTestActivity extends DefaultGVRTestActivity {

    @Override
    protected void initGVRTestActivity() {
        setScript(new TestViewManager(), "gvr_s5.xml");
    }

}