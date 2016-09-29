
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
            dockEventFilter.addAction(ACTION_HMT_CONNECT);
            dockEventFilter.addAction(ACTION_HMT_DISCONNECT);
            mApplicationContext.registerReceiver(mBroadcastReceiver, dockEventFilter);
            Log.v(TAG, "receiver registered");
            mIsStarted = true;
        }
    }

    public void stop() {
        if (mIsStarted) {
            mApplicationContext.unregisterReceiver(mBroadcastReceiver);
            Log.v(TAG, "receiver unregistered");
            mIsStarted = false;
        }
    }

    private final class BroadcastReceiverImpl extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, final Intent intent) {
            Log.v(TAG, "received " + intent);
            if (ACTION_HMT_DISCONNECT.equals(intent.getAction())) {
                mRunOnUndock.run();
            } else if (ACTION_HMT_CONNECT.equals(intent.getAction())) {
                mRunOnDock.run();
            }
        }
    }

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiverImpl();
    private final Context mApplicationContext;
    private boolean mIsStarted;
    private final Runnable mRunOnDock;
    private final Runnable mRunOnUndock;

    private final static String ACTION_HMT_DISCONNECT = "com.samsung.intent.action.HMT_DISCONNECTED";
    private final static String ACTION_HMT_CONNECT = "com.samsung.intent.action.HMT_CONNECTED";

    private static final String TAG = "DockEventReceiver";
}
