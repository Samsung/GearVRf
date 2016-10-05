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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.input.InputManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityApi.GetCapabilityResult;
import com.google.android.gms.wearable.MessageApi.SendMessageResult;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import org.gearvrf.wearconstants.TouchEvent;
import org.gearvrf.wearconstants.WearConstants;

import java.util.HashSet;
import java.util.Set;

public class WearInputService extends Service {
    private static final String TAG = WearInputService.class.getSimpleName();
    private static final String TRACKPAD_CAPABILITY_NAME = "trackpad_navigation";
    static final String GET_LOCAL_BINDER_ACTION = "localbinder";
    static final String MOTION_INTENT_FILTER = "motionEvents";
    static final String TOUCH_EVENT_EXTRA = "motionEvent";
    static final String CONNECTION_INTENT_FILTER = "connectionEvents";
    static final String CONNECTION_STATUS_EXTRA = "ConnectionStatus";
    static final int CONNECT_FROM_WEAR_APP = 0;
    static final String WATCH_NODE_ID_EXTRA = "watchNodeId";

    private GoogleApiClient apiClient;
    private Set<Node> nodes;
    private Messenger receiveMessenger;
    private Messenger sendMessenger;
    private LocalBinder localBinder;
    private LocalBroadcastManager broadcastManager;
    private boolean connectedToWatch;
    private String watchNodeId;
    private MotionEventGenerator motionEventGenerator;

    @Override
    public void onCreate() {
        super.onCreate();
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        apiClient.connect();
        nodes = new HashSet<Node>();
        receiveMessenger = new Messenger(new IncomingMsgHandler());
        broadcastManager = LocalBroadcastManager.getInstance(this);
        localBinder = new LocalBinder();
        connectedToWatch = false;
        int touchScreenDeviceId = 0;
        InputManager im = (InputManager) getSystemService(Context.INPUT_SERVICE);
        for(int inputDevId : im.getInputDeviceIds()) {
            InputDevice inputDevice = im.getInputDevice(inputDevId);
            if((inputDevice.getSources() & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice
                    .SOURCE_TOUCHSCREEN) {
                touchScreenDeviceId = inputDevId;
                break;
            }
        }
        motionEventGenerator = new MotionEventGenerator(touchScreenDeviceId);
    }

    void connectToWearApp() {
        if (connectedToWatch) {
            sendConnectionStatus(WearMessageConstants.MSG_CONNECTION_SUCCESSFUL);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                GetCapabilityResult capabilityResult =
                        Wearable.CapabilityApi.getCapability(apiClient, TRACKPAD_CAPABILITY_NAME,
                                CapabilityApi.FILTER_REACHABLE).await();
                nodes.addAll(capabilityResult.getCapability().getNodes());

                for (Node node : nodes) {
                    if (node.isNearby()) {
                        watchNodeId = node.getId();
                        SendMessageResult result = Wearable.MessageApi.sendMessage(
                                apiClient, watchNodeId, WearConstants.START_ACTIVITY_PATH,
                                WearConstants.CONNECT_TO_APP).await();
                        connectedToWatch = result.getStatus().isSuccess();
                    }
                }

                if (connectedToWatch) {
                    sendConnectionStatus(WearMessageConstants.MSG_CONNECTION_SUCCESSFUL);
                } else {
                    sendConnectionStatus(WearMessageConstants.MSG_CONNECTION_UNSUCCESFUL);
                }
            }
        }).start();
    }

    private void sendConnectionStatus(int connectionStatus) {
        if (sendMessenger != null) {
            Message reply = Message.obtain(null, connectionStatus);
            try {
                sendMessenger.send(reply);
            } catch (RemoteException e) {
                Log.e(TAG, "Could not send reply ", e);
            }
        } else {
            Intent intent = new Intent(CONNECTION_INTENT_FILTER);
            intent.putExtra(CONNECTION_STATUS_EXTRA, connectionStatus);
            broadcastManager.sendBroadcast(intent);
        }
    }

    BroadcastReceiver connectionEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getIntExtra(CONNECTION_STATUS_EXTRA, -1) == WearInputService
                    .CONNECT_FROM_WEAR_APP) {
                watchNodeId = intent.getStringExtra(WATCH_NODE_ID_EXTRA);
                Wearable.MessageApi.sendMessage(apiClient, watchNodeId, WearConstants.START_ACTIVITY_PATH,
                        WearConstants.CONNECT_TO_APP);
            }
        }
    };

    private class IncomingMsgHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WearMessageConstants.MSG_CONNECT:
                    sendMessenger = msg.replyTo;
                    connectToWearApp();
                    break;
                default:
                    Log.d(TAG, "Unknown Message Type in handler");
                    super.handleMessage(msg);
            }
        }
    }

    private BroadcastReceiver motionEventReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] touchEventData = intent.getByteArrayExtra(TOUCH_EVENT_EXTRA);
            TouchEvent touchEvent = new TouchEvent(touchEventData);
            MotionEvent motionEvent = motionEventGenerator.processTouchEvent(touchEvent);
            Message msg = Message.obtain(null, WearMessageConstants.MSG_TOUCH_EVENT, motionEvent);
            try {
                sendMessenger.send(msg);
            } catch (RemoteException e) {
                Log.d(TAG, "", e);
            }
        }
    };

    private static class MotionEventGenerator {
        private static final float DEFAULT_PRESSURE = 1;
        private static final float DEFAULT_SIZE = 0.5f;
        private static final int DEFAULT_META_STATE = 0;
        private static final float DEFAULT_X_PRECISION = 1;
        private static final float DEFAULT_Y_PRECISION = 1;
        private static final int DEFAULT_EDGE_FLAGS = 0;
        private long motionEventDownTime;
        private int deviceId;
        MotionEventGenerator(int deviceId) {
            this.deviceId = deviceId;
        }

        private MotionEvent processTouchEvent( TouchEvent touchEvent ) {
            long motionEventTime;
            if(touchEvent.getAction() == MotionEvent.ACTION_DOWN) {
                motionEventDownTime = SystemClock.uptimeMillis();
                motionEventTime = motionEventDownTime;
            } else {
                motionEventTime = (touchEvent.getEventTime() - touchEvent.getDownTime()) +
                        motionEventDownTime;
            }
            MotionEvent motionEvent = MotionEvent.obtain(motionEventDownTime,motionEventTime,
                    touchEvent.getAction(), touchEvent.getX(), touchEvent.getY(),
                    DEFAULT_PRESSURE, DEFAULT_SIZE, DEFAULT_META_STATE, DEFAULT_X_PRECISION,
                    DEFAULT_Y_PRECISION, deviceId, DEFAULT_EDGE_FLAGS);
            return motionEvent;
        }
    }

    public class LocalBinder extends Binder {
        WearInputService getService() {
            return WearInputService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        broadcastManager.registerReceiver(connectionEventReceiver, new IntentFilter
                (WearInputService.CONNECTION_INTENT_FILTER));
        if (GET_LOCAL_BINDER_ACTION.equals(intent.getAction())) {
            return localBinder;
        } else {
            broadcastManager.registerReceiver(motionEventReceiver, new IntentFilter
                    (MOTION_INTENT_FILTER));
            return receiveMessenger.getBinder();
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        broadcastManager.unregisterReceiver(connectionEventReceiver);
        Wearable.MessageApi.sendMessage(apiClient, watchNodeId, WearConstants.START_ACTIVITY_PATH,
                WearConstants.DISCONNECT_FROM_APP);
        if (!GET_LOCAL_BINDER_ACTION.equals(intent.getAction())) {
            broadcastManager.unregisterReceiver(motionEventReceiver);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        apiClient.disconnect();
    }
}
