/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License..
 */

package com.samsung.mpl.gearinputprovider.backend;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;
import com.samsung.mpl.gearinputprovider.R;
import com.samsung.mpl.gearinputprovider.models.NetworkMessage;
import com.samsung.mpl.gearinputprovider.models.NetworkMessage.Event;
import com.samsung.mpl.gearinputprovider.models.NetworkMessage.Type;
import com.samsung.mpl.gearinputprovider.utils.Utility;
import com.samsung.mpl.gearwearlibrary.EventManager;
import com.samsung.mpl.gearwearlibrary.models.events.Back;
import com.samsung.mpl.gearwearlibrary.models.events.Click;
import com.samsung.mpl.gearwearlibrary.models.events.Connected;
import com.samsung.mpl.gearwearlibrary.models.events.Disconnected;
import com.samsung.mpl.gearwearlibrary.models.events.Rotary;
import com.samsung.mpl.gearwearlibrary.models.events.Swipe;
import com.samsung.mpl.gearwearlibrary.models.events.TouchEnd;
import com.samsung.mpl.gearwearlibrary.models.events.TouchMove;
import com.samsung.mpl.gearwearlibrary.models.events.TouchStart;

import java.util.HashMap;

public class InputProviderService extends SAAgent {
    private static final String TAG = InputProviderService.class.getSimpleName();

    private static final int CHANNEL_ID = 104;

    private InputProviderConnection myConnection = null;
    private HashMap<Integer, InputProviderConnection> mConnectionsMap = null;
    private boolean connected;
    private ConnectionStatusBroadcastReceiver receiver = new ConnectionStatusBroadcastReceiver();

    public InputProviderService() {
        super(TAG, InputProviderConnection.class);
        Log.d(TAG, "InputProviderService constructor");
        Utility.setVLogsEnabled(false);
    }

    public class InputProviderConnection extends SASocket {
        private int mConnectionId;

        public InputProviderConnection() {
            super(InputProviderConnection.class.getName());
        }

        @Override
        public void onError(int channelId, String errorString, int error) {
            Utility.logE(TAG, "Connection is not alive. error: %s (%d)", errorString, error);
        }

        @Override
        public void onReceive(int channelId, byte[] data) {
            String string = new String(data);
            Utility.logV(TAG, "onReceive: channelId: %d, data: %s", channelId, string);

            if (channelId == CHANNEL_ID) {
                parseMessage(string);
            }
        }

        @Override
        protected void onServiceConnectionLost(int result) {
            switch (result) {
                case SASocket.CONNECTION_LOST_DEVICE_DETACHED:
                    Utility.logE(TAG, "onServiceConnectionLost: CONNECTION_LOST_DEVICE_DETACHED");
                    break;
                case SASocket.CONNECTION_LOST_PEER_DISCONNECTED:
                    Utility.logE(TAG, "onServiceConnectionLost: CONNECTION_LOST_PEER_DISCONNECTED");
                    break;
                case SASocket.CONNECTION_LOST_RETRANSMISSION_FAILED:
                    Utility.logE(TAG, "onServiceConnectionLost: " +
                            "CONNECTION_LOST_RETRANSMISSION_FAILED");
                    break;
                case SASocket.CONNECTION_LOST_UNKNOWN_REASON:
                    Utility.logE(TAG, "onServiceConnectionLost: CONNECTION_LOST_UNKNOWN_REASON");
                    break;
                case SASocket.ERROR_FATAL:
                    Utility.logE(TAG, "onServiceConnectionLost: ERROR_FATAL");
                    break;
                default:
                    Utility.logE(TAG, "onServiceConnectionLost: unknown result (%d)", result);
                    break;
            }
            myConnection = null;
            Disconnected disconnected = new Disconnected();
            Log.d(TAG, "Disconnected, sending disconnected event");
            connected = false;
            sendEvent(InputProviderService.this, disconnected);

            if (mConnectionsMap != null) {
                mConnectionsMap.remove(mConnectionId);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Utility.logD(TAG, "onCreate");
        IntentFilter filter = new IntentFilter();
        filter.addAction(EventManager.ACTION_CONNECTION_STATUS_REQUEST);
        registerReceiver(receiver, filter);
        SA mAccessory = new SA();
        try {
            mAccessory.initialize(this);
        } catch (SsdkUnsupportedException e) {
            Utility.logE(TAG, "", e);
        } catch (Exception e1) {
            Utility.logE(TAG, "Cannot initialize SAccessory package.", e1);
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Utility.logD(TAG, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Utility.logD(TAG, "onDestroy");
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    @Override
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) {
        Utility.logD("onServiceConnectionRequested: peerAgent [appName:%s, getPeerId:%s]",
                peerAgent.getAppName(), peerAgent.getPeerId());
        acceptServiceConnectionRequest(peerAgent);
    }

    @Override
    protected void onFindPeerAgentResponse(SAPeerAgent arg0, int result) {
        Utility.logD(TAG, "onFindPeerAgentResponse: result (%d)", result);
    }

    @Override
    protected void onServiceConnectionResponse(SASocket thisConnection, int result) {
        if (result == CONNECTION_SUCCESS) {
            if (thisConnection != null) {
                Utility.logD(TAG, "SA Socket connection established");
                myConnection = (InputProviderConnection) thisConnection;

                if (mConnectionsMap == null) {
                    mConnectionsMap = new HashMap<Integer, InputProviderConnection>();
                }

                myConnection.mConnectionId = (int) (System.currentTimeMillis() & 255);
                Utility.logD(TAG, "onServiceConnectionResponse connectionID = (%d)",
                        myConnection.mConnectionId);
                mConnectionsMap.put(myConnection.mConnectionId, myConnection);
                Toast.makeText(getBaseContext(), R.string.connection_established_message,
                        Toast.LENGTH_LONG)
                        .show();

                Connected connected = new Connected();
                Log.d(TAG, "Connected, sending connected event");
                InputProviderService.this.connected = true;
                sendEvent(this, connected);
            } else {
                Utility.logE(TAG, "SASocket object is null");
            }
        } else if (result == CONNECTION_ALREADY_EXIST) {
            Utility.logD(TAG, "onServiceConnectionResponse, CONNECTION_ALREADY_EXIST");
        } else {
            switch (result) {
                case SAAgent.CONNECTION_FAILURE_NETWORK:
                    Utility.logE(TAG, "onServiceConnectionResponse: " +
                            "CONNECTION_FAILURE_NETWORK");
                    break;
                case SAAgent.CONNECTION_FAILURE_DEVICE_UNREACHABLE:
                    Utility.logE(TAG,
                            "onServiceConnectionResponse: CONNECTION_FAILURE_DEVICE_UNREACHABLE");
                    break;
                case SAAgent.CONNECTION_FAILURE_INVALID_PEERAGENT:
                    Utility.logE(TAG,
                            "onServiceConnectionResponse: CONNECTION_FAILURE_INVALID_PEERAGENT");
                    break;
                case SAAgent.CONNECTION_FAILURE_PEERAGENT_NO_RESPONSE:
                    Utility.logE(TAG,
                            "onServiceConnectionResponse: " +
                                    "CONNECTION_FAILURE_PEERAGENT_NO_RESPONSE");
                    break;
                case SAAgent.CONNECTION_FAILURE_PEERAGENT_REJECTED:
                    Utility.logE(TAG,
                            "onServiceConnectionResponse: CONNECTION_FAILURE_PEERAGENT_REJECTED");
                    break;
                default:
                    Utility.logE(TAG,
                            "onServiceConnectionResponse: unknown result (%d)", result);
                    break;
            }
        }
    }

    public void parseMessage(String message) {
        Gson gson = new Gson();
        //TODO: Find a way to parse only once
        NetworkMessage msg = gson.fromJson(message, NetworkMessage.class);

        Type type = msg.getType();
        if (type != Type.INPUT) {
            Utility.logD(TAG, "Unhandled Type=%s", type);
            return;
        }

        Event event = msg.getEvent();
        Utility.logV(TAG, "Event: Type=%s, Event=%s", Type.INPUT, event);
        switch (event) {
            case CLICK:
                Click click = gson.fromJson(message, Click.class);
                Utility.logV(TAG, "Click data: x=%d, y=%d", click.x, click.y);
                sendEvent(this, click);
                break;
            case TOUCH_START:
                TouchStart touchStart = gson.fromJson(message, TouchStart.class);
                Utility.logV(TAG, touchStart.toString());
                sendEvent(this, touchStart);
                break;
            case TOUCH_MOVE:
                TouchMove touchMove = gson.fromJson(message, TouchMove.class);
                Utility.logV(TAG, touchMove.toString());
                sendEvent(this, touchMove);
                break;
            case TOUCH_END:
                Utility.logV(TAG, TouchEnd.class.getSimpleName());
                sendEvent(this, new TouchEnd());
                break;
            case ROTARY:
                Rotary rotary = gson.fromJson(message, Rotary.class);
                Utility.logV(TAG, rotary.toString());
                sendEvent(this, rotary);
                break;
            case SWIPE:
                Swipe swipe = gson.fromJson(message, Swipe.class);
                Utility.logV(TAG, swipe.toString());
                sendEvent(this, swipe);
                break;
            case BACK:
                Utility.logV(TAG, "Back");
                sendEvent(this, new Back());
                break;
            default:
                Utility.logD(TAG, "Unknown event: " + event);
                break;
        }
    }

    /**
     * Send an event to other applications
     *
     * @param context context in which to send the broadcast
     * @param event   event to send
     */
    public static void sendEvent(Context context, Parcelable event) {
        Intent intent = new Intent(EventManager.ACTION_ACCESSORY_EVENT);
        intent.putExtra(EventManager.EXTRA_EVENT, event);
        context.sendBroadcast(intent);
    }

    private class ConnectionStatusBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == EventManager.ACTION_CONNECTION_STATUS_REQUEST) {
                Parcelable connectionStatus = connected ? new Connected() : new Disconnected();
                Utility.logD(TAG, "Received connection status request, sending %s",
                        connectionStatus);
                sendEvent(InputProviderService.this, connectionStatus);
            }
        }
    }
}
