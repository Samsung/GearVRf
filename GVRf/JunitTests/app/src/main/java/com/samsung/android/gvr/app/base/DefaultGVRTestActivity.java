package com.samsung.android.gvr.app.base;

import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;

import org.gearvrf.GVRActivity;
import com.samsung.android.gvr.app.base.viewmanager.TestDefaultGVRViewManager;

public class DefaultGVRTestActivity extends GVRActivity {

    protected static DefaultGVRTestActivity mMainActivity = null;
    private ActivityInstrumentationTestCase2 activityInstrumentationGVRf;

    public static boolean sContextLoaded = false;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mMainActivity = this;
        initGVRTestActivity();
      //  oneTimeInit();
    }

    protected void initGVRTestActivity() {
        setScript(new TestDefaultGVRViewManager(), "gvr_note4.xml");
    }

    public static DefaultGVRTestActivity getInstance() {
        return mMainActivity;
    }

    public void store(ActivityInstrumentationTestCase2 activityInstrumentationGVRf) {
        this.activityInstrumentationGVRf = activityInstrumentationGVRf;
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}