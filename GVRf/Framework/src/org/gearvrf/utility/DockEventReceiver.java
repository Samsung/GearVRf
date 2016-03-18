
package org.gearvrf.utility;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public final class DockEventReceiver {
    public DockEventReceiver(final Context context, final Runnable runOnDock,
            final Runnable runOnUndock) {
        mRunOnDock = runOnDock;
        mRunOnUndock = runOnUndock;
        mApplicationContext = context.getApplicationContext();
    }

    public void start() {
        if (!mIsStarted) {
            final IntentFilter dockEventFilter = new IntentFilter();
            dockEventFilter.addAction(Intent.ACTION_DOCK_EVENT);
            mApplicationContext.registerReceiver(mBroadcastReceiver, dockEventFilter);
            mIsStarted = true;
        }
    }

    public void stop() {
        if (mIsStarted) {
            mApplicationContext.unregisterReceiver(mBroadcastReceiver);
            mIsStarted = false;
        }
    }

    private final class BroadcastReceiverImpl extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            if (Intent.ACTION_DOCK_EVENT.equals(intent.getAction())) {
                final int dockState = intent.getIntExtra(Intent.EXTRA_DOCK_STATE, Intent.EXTRA_DOCK_STATE_UNDOCKED);
                if (Intent.EXTRA_DOCK_STATE_UNDOCKED == dockState && null != mRunOnUndock) {
                    mRunOnUndock.run();
                } else if (EXTRA_DOCK_STATE_HMT == dockState && null != mRunOnDock) {
                    mRunOnDock.run();
                }
            }
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiverImpl();
    private final Context mApplicationContext;
    private boolean mIsStarted;
    private final Runnable mRunOnDock;
    private final Runnable mRunOnUndock;

    private final static int EXTRA_DOCK_STATE_HMT = 11;   //Intent.EXTRA_DOCK_STATE_HMT
}
