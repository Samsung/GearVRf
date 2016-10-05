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

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.gearvrf.wearconstants.TouchEvent;
import org.gearvrf.wearconstants.WearConstants;

public class MessageListenerService extends WearableListenerService {
    private static final String TAG = MessageListenerService.class.getSimpleName();
    private LocalBroadcastManager broadcastManager;


    @Override
    public void onCreate() {
        super.onCreate();
        broadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG,"Message Received");
        if (WearConstants.TOUCH_EVENT_PATH.equalsIgnoreCase(messageEvent.getPath())) {
            Intent intent = new Intent(WearInputService.MOTION_INTENT_FILTER);
            intent.putExtra(WearInputService.TOUCH_EVENT_EXTRA, messageEvent.getData());
            broadcastManager.sendBroadcast(intent);
        } else if (WearConstants.START_TOUCH_LISTENER_PATH.equalsIgnoreCase(messageEvent.getPath())) {
            Intent intent = new Intent(WearInputService.CONNECTION_INTENT_FILTER);
            intent.putExtra(WearInputService.CONNECTION_STATUS_EXTRA, WearInputService
                    .CONNECT_FROM_WEAR_APP);
            intent.putExtra(WearInputService.WATCH_NODE_ID_EXTRA,messageEvent.getSourceNodeId());
            broadcastManager.sendBroadcast(intent);
        } else {
            super.onMessageReceived(messageEvent);
        }
    }

    private static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }
}
