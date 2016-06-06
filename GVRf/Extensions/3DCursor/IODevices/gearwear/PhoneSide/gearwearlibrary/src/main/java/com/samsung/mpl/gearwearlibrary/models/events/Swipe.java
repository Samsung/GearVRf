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
 * Swipe event occurs when user presses down on the screen and moves to a different position,
 * above a threshold speed (determined by the device).
 */
public class Swipe implements Parcelable {
    public final Direction direction;

    /**
     * Swipe directions
     */
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public Swipe(Direction direction) {
        this.direction = direction;
    }

    protected Swipe(Parcel in) {
        direction = (Direction) in.readValue(Direction.class.getClassLoader());
    }

    public static final Creator<Swipe> CREATOR = new Creator<Swipe>() {
        @Override
        public Swipe createFromParcel(Parcel in) {
            return new Swipe(in);
        }

        @Override
        public Swipe[] newArray(int size) {
            return new Swipe[size];
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
        return "Swipe{" +
                "direction=" + direction +
                '}';
    }
}
