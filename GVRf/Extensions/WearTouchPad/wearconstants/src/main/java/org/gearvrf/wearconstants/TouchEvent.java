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

package org.gearvrf.wearconstants;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.MotionEvent;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class TouchEvent implements Serializable {
    public static final int BYTE_BUFFER_SIZE = 28;
    int action;
    long downTime;
    long eventTime;
    float x;
    float y;

    public TouchEvent(int action, long downTime, long eventTime, float x, float y) {
        this.action = action;
        this.downTime = downTime;
        this.eventTime = eventTime;
        this.x = x;
        this.y = y;
    }

    public TouchEvent(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        action = buffer.getInt();
        downTime = buffer.getLong();
        eventTime = buffer.getLong();
        x = buffer.getFloat();
        y = buffer.getFloat();
    }

    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
        buffer.putInt(action);
        buffer.putLong(downTime);
        buffer.putLong(eventTime);
        buffer.putFloat(x);
        buffer.putFloat(y);
        return buffer.array();
    }

    public long getDownTime() {
        return downTime;
    }

    public long getEventTime() {
        return eventTime;
    }

    public int getAction() {
        return action;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(30);
        if (VERSION.SDK_INT >= VERSION_CODES.KITKAT) {
            builder.append(MotionEvent.actionToString(action));
        } else {
            builder.append(action);
        }
        builder.append(", X=").append(String.format("%.2f", x)).append(", Y=").append(String
                .format("%.2f", y));
        return builder.toString();
    }
}
