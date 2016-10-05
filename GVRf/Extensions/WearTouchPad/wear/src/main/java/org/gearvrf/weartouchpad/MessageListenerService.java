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

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.gearvrf.wearconstants.WearConstants;

import java.util.Arrays;

public class MessageListenerService extends WearableListenerService {

    private static final String TAG = MessageListenerService.class.getSimpleName();
    static final String START_ACTIVITY_ACTION = "startActivity";
    static final String STOP_ACTIVITY_ACTION = "stopActivity";
    static final String HANDSET_NODE_ID_EXTRA = "HANDSET_NODE_ID";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(WearConstants.START_ACTIVITY_PATH)) {
            if (Arrays.equals(WearConstants.CONNECT_TO_APP, messageEvent.getData())) {
                String handsetNodeId = messageEvent.getSourceNodeId();
                Intent intent = new Intent(this, WearActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(START_ACTIVITY_ACTION);
                intent.putExtra(HANDSET_NODE_ID_EXTRA, handsetNodeId);
                startActivity(intent);
            } else if (Arrays.equals(WearConstants.DISCONNECT_FROM_APP, messageEvent.getData())) {
                Intent intent = new Intent(this, WearActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(STOP_ACTIVITY_ACTION);
                startActivity(intent);
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
