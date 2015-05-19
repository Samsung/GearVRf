package com.samsung.android.gvr.test.base;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.samsung.android.gvr.app.base.DefaultGVRTestActivity;

public class ActivityInstrumentationGVRf extends
        ActivityInstrumentationTestCase2<DefaultGVRTestActivity> {

    public static Activity mActivity = null;

    public ActivityInstrumentationGVRf() {
        super(DefaultGVRTestActivity.class);
    }

    public ActivityInstrumentationGVRf(Class activityClass) {
        super(activityClass);
    }

    @Override
    public synchronized void setUp() throws Exception {
        if (mActivity == null) {
            mActivity = getActivity();
        }

        if (!DefaultGVRTestActivity.sContextLoaded) {
            synchronized (DefaultGVRTestActivity.class) {
                try {
                    DefaultGVRTestActivity.class.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public synchronized void tearDown() throws Exception {
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        DefaultGVRTestActivity.getInstance().store(this);
        super.finalize();
    }
}
