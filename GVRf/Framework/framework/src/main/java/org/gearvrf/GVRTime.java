/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

/** JNI methods about time. */
public class GVRTime {
    public static long NANO_TO_MILLIS = 1000000;

    private GVRTime() {
    }

    /**
     * The current time, using the CPU clock.
     * 
     * This is "wall clock time" - not {@link System#nanoTime()}. This method
     * lets GVRF Java methods use the same time base as GVRF native methods.
     * 
     * @return "Wall clock time" time, in nano seconds.
     */
    static long getCurrentTime() {
        return NativeTime.getCurrentTime();
    }

    /**
     * The current time, using the CPU clock. 
     * 
     * This is the native version of {@link System#nanoTime()}
     */
    public static long getNanoTime() {
        return NativeTime.getNanoTime();
    }

    /**
     * The current time in milliseocnds, using the CPU clock.
     * @return Time in millis.
     */
    public static long getMilliTime() {
        return GVRTime.getNanoTime() / GVRTime.NANO_TO_MILLIS;
    }
}

class NativeTime {
    static native long getCurrentTime();

    static native long getNanoTime();
}
