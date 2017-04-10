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
 * Back event that can occur in different ways, depending on the device.
 * <p>
 * Examples:
 * <ul>
 * <li>Device: Samsung Gear S2
 * <ol>
 * <li>Swipe down from top edge</li>
 * <li>Back button press</li>
 * </ol>
 * </li>
 * <li>Device: Samsung Gear 2
 * <ol>
 * <li>Swipe down from top edge</li>
 * </ol>
 * </li>
 * </ul>
 */
public class Back implements Parcelable {

    public static final Creator<Back> CREATOR = new Creator<Back>() {
        @Override
        public Back createFromParcel(Parcel in) {
            return new Back();
        }

        @Override
        public Back[] newArray(int size) {
            return new Back[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public String toString() {
        return Back.class.getSimpleName();
    }
}
