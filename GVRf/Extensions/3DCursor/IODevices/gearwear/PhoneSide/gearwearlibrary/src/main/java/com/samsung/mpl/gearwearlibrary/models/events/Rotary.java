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
 * Rotary event occurs when the rotating bezel is moved, if the device supports it. The rotation
 * can either be clockwise or counter-clockwise.
 */
public class Rotary implements Parcelable {
    public final Direction direction;

    public enum Direction {
        /**
         * Clockwise
         */
        CW,
        /**
         * Counter-clockwise
         */
        CCW
    }

    public Rotary(Direction direction) {
        this.direction = direction;
    }

    protected Rotary(Parcel in) {
        direction = (Direction) in.readValue(Direction.class.getClassLoader());
    }

    public static final Creator<Rotary> CREATOR = new Creator<Rotary>() {
        @Override
        public Rotary createFromParcel(Parcel in) {
            return new Rotary(in);
        }

        @Override
        public Rotary[] newArray(int size) {
            return new Rotary[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(direction);
    }

    @Override
    public String toString() {
        return "Rotary{" +
                "direction=" + direction +
                '}';
    }
}
