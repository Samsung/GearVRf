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
 * limitations under the License.
 */

package com.samsung.mpl.gearwearlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;

import com.samsung.mpl.gearwearlibrary.models.events.Connected;
import com.samsung.mpl.gearwearlibrary.models.events.Disconnected;

/**
 * Event manager is used to send Samsung Gear wearable input events to other applications, as
 * well as for other applications to receive these events.
 */
public class EventManager {
    public static final String ACTION_ACCESSORY_EVENT =
            "com.samsung.mpl.gearwear.ACTION_ACCESSORY_EVENT";
    public static final String ACTION_CONNECTION_STATUS_REQUEST =
            "com.samsung.mpl.gearwear.ACTION_CONNECTION_STATUS_REQUEST";
    public static final String EXTRA_EVENT = "event";

    /**
     * Register a receiver so it can receive input events.
     * <p>
     * You <b>must unregister the receiver</b> once you are done receiving events, otherwise a
     * memory leak will occur. If you wish to register the same receiver multiple times, be sure
     * to unregister it first.
     *
     * @param context  context in which to register for events
     * @param receiver receiver to register for events
     */
    public static void registerReceiver(Context context, BroadcastReceiver receiver) {
        context.registerReceiver(receiver, new IntentFilter(ACTION_ACCESSORY_EVENT));
    }

    /**
     * Unregister a receiver to stop receiving events on it.
     * <p>
     * This should <b>only</b> be called
     * if a receiver was previously registered with {@link #registerReceiver(Context,
     * BroadcastReceiver)}
     *
     * @param context  context in which to register for events
     * @param receiver receiver to unregister
     */
    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    /**
     * Get the event from the received intent. Should be used in your BroadcastReceiver
     *
     * @param intent intent from which to retrieve the event
     * @return event
     */
    public static Parcelable getEvent(Intent intent) {
        return intent.getParcelableExtra(EventManager.EXTRA_EVENT);
    }

    /**
     * Request connection status. You will receive a {@link Connected} or {@link Disconnected}
     * event in your broadcast receiver
     */
    public static void requestConnectionStatus(Context context) {
        Intent intent = new Intent(EventManager.ACTION_CONNECTION_STATUS_REQUEST);
        context.sendBroadcast(intent);
    }
}
