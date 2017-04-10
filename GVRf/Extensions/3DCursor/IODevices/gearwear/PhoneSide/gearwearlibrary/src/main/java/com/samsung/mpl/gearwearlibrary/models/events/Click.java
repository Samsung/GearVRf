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
 * Click event that occurs when a user presses down at an initial position, then releases that
 * press at a location 'close' to the initial position.
 */
public class Click implements Parcelable {
    public final int x;
    public final int y;

    public Click(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected Click(Parcel in) {
        x = in.readInt();
        y = in.readInt();
    }

    public static final Creator<Click> CREATOR = new Creator<Click>() {
        @Override
        public Click createFromParcel(Parcel in) {
            return new Click(in);
        }

        @Override
        public Click[] newArray(int size) {
            return new Click[size];
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
        return "Click{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
