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

package com.samsung.mpl.gearwearlibrary.models.events;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Occurs when user moves to a different touch area on the screen
 */
public class TouchMove implements Parcelable {
    /**
     * x-position
     */
    public final int x;

    /**
     * y-position
     */
    public final int y;

    /**
     * Create a touch move event
     *
     * @param x x-position
     * @param y y-position
     */
    public TouchMove(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected TouchMove(Parcel in) {
        x = in.readInt();
        y = in.readInt();
    }

    public static final Creator<TouchMove> CREATOR = new Creator<TouchMove>() {
        @Override
        public TouchMove createFromParcel(Parcel in) {
            return new TouchMove(in);
        }

        @Override
        public TouchMove[] newArray(int size) {
            return new TouchMove[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(y);
    }

    @Override
    public String toString() {
        return "TouchMove{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
