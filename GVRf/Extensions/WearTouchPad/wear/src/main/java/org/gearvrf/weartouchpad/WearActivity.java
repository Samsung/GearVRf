/*
 * Copyright 2016 Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.gearvrf.weartouchpad;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WatchViewStub.OnLayoutInflatedListener;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityApi.GetCapabilityResult;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.gearvrf.wearconstants.TouchEvent;
import org.gearvrf.wearconstants.WearConstants;

import java.util.Set;

public class WearActivity extends WearableActivity implements OnLayoutInflatedListener {

    private static final String TAG = WearActivity.class.getSimpleName();
    private static final String TOUCH_EVENT_HANDLER = "TouchEventHandler";
    private static final int SEND_TOUCH_EVENT = 0;
    private static final int CONNECT_TO_PHONE = 1;
    private static final String TOUCH_LISTENER_CAPABILITY_NAME = "touch_listener";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1234;

    private DismissOverlayView mDismissOverlay;
    private GestureDetector gestureDetector;
    private TextView tvConnectionStatus;
    private GoogleApiClient apiClient;
    private MessageHandler messageHandler;
    private String handsetNodeId;
    private HandlerThread handlerThread;
    private boolean gestureDetectorEnabled;
    private int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        handsetNodeId = getHandsetNodeId(getIntent());
        stub.setOnLayoutInflatedListener(this);
        setAmbientEnabled();
        handlerThread = new HandlerThread(TOUCH_EVENT_HANDLER);
        handlerThread.start();
        messageHandler = new MessageHandler(handlerThread.getLooper());
        gestureDetector = new GestureDetector(WearActivity.this,
                simpleOnGestureListener);
        if (!checkPlayServices()) {
            Log.d(TAG, "Google Play service is not available");
        }
        apiClient = new GoogleApiClient.Builder(WearActivity.this)
                .addApi(Wearable.API)
                .build();
        apiClient.connect();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                apiAvailability.makeGooglePlayServicesAvailable(this);
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLAY_SERVICES_RESOLUTION_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Google Play services are now available");
            } else {
                Log.e(TAG, "Google Play services could not be updated");
                finish();
            }
        }
    }

    public void onLayoutInflated(WatchViewStub stub) {
        tvConnectionStatus = (TextView) findViewById(R.id.tvConnectionStatus);
        if (handsetNodeId != null) {
            gestureDetectorEnabled = false;
            tvConnectionStatus.setText(getString(R.string.connected));
        } else {
            gestureDetectorEnabled = true;
            Message.obtain(messageHandler, CONNECT_TO_PHONE).sendToTarget();
        }
        mDismissOverlay = (DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.long_press_intro);
        mDismissOverlay.showIntroIfNecessary();
    }

    private String getHandsetNodeId(Intent intent) {
        if (MessageListenerService.START_ACTIVITY_ACTION.equals(intent.getAction())) {
            String handsetNodeId = intent.getStringExtra(MessageListenerService
                    .HANDSET_NODE_ID_EXTRA);
            return handsetNodeId;
        }
        return null;
    }

    private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener() {
        public void onLongPress(MotionEvent ev) {
            if (mDismissOverlay != null) {
                mDismissOverlay.show();
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (MessageListenerService.START_ACTIVITY_ACTION.equals(intent.getAction())) {
            gestureDetectorEnabled = false;
            handsetNodeId = getHandsetNodeId(intent);
            tvConnectionStatus.setText(getString(R.string.connected));
        } else if (MessageListenerService.STOP_ACTIVITY_ACTION.equals(intent.getAction())) {
            finish();
        } else {
            messageHandler.obtainMessage(CONNECT_TO_PHONE).sendToTarget();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (handsetNodeId != null) {
            // reversing the xPos to make swipe left/right direction feel like the touchpad on
            // gearvr
            float xPos = screenWidth - event.getRawX();
            TouchEvent touchEvent = new TouchEvent(event.getAction(), event.getDownTime(), event
                    .getEventTime(), xPos, event.getRawY());
            Message msg = messageHandler.obtainMessage(SEND_TOUCH_EVENT, touchEvent);
            msg.sendToTarget();
        } else {
            Log.d(TAG, "HandsetNodeIs is null");
        }
        boolean gestureDetected = false;
        if (gestureDetector != null && gestureDetectorEnabled) {
            gestureDetected = gestureDetector.onTouchEvent(event);
        }
        return gestureDetected || super.onTouchEvent(event);
    }

    private final class MessageHandler extends Handler {

        public MessageHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SEND_TOUCH_EVENT:
                    TouchEvent touchEvent = (TouchEvent) msg.obj;
                    SendMessageResult result = Wearable.MessageApi.sendMessage(
                            apiClient, handsetNodeId, WearConstants.TOUCH_EVENT_PATH, touchEvent
                                    .toBytes()).await();
                    if (!result.getStatus().isSuccess()) {
                        Log.e(TAG, "Could not send message");
                    }
                    break;
                case CONNECT_TO_PHONE:
                    boolean connectionResult = false;
                    Log.d(TAG, "Connecting to phone");
                    GetCapabilityResult capabilityResult = Wearable.CapabilityApi.getCapability
                            (apiClient, TOUCH_LISTENER_CAPABILITY_NAME, CapabilityApi
                                    .FILTER_REACHABLE).await();
                    Set<Node> nodes = capabilityResult.getCapability().getNodes();
                    for (Node node : nodes) {
                        if (node.isNearby()) {
                            result = Wearable.MessageApi.sendMessage(apiClient, node.getId(),
                                    WearConstants.START_TOUCH_LISTENER_PATH, null).await();
                            connectionResult = result.getStatus().isSuccess();
                            handsetNodeId = node.getId();
                            break;
                        }
                    }
                    if (!connectionResult) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvConnectionStatus.setText(getString(R.string.no_phone));
                            }
                        });
                    } else {
                        Log.d(TAG, "Connected to phone waiting for app to connect back");
                    }
                    break;
                default:
                    Log.e(TAG, "Unknown Message Type");
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        if (apiClient.isConnected() || apiClient.isConnecting()) {
            apiClient.disconnect();
        }
        super.onDestroy();
    }
}
