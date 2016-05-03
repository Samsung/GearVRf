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

package org.gearvrf.animation;

/** The three supported repeat modes. */
public abstract class GVRRepeatMode {
    /**
     * Run once.
     * 
     * This is the default: {@link GVRAnimation#setRepeatMode(int)
     * setRepeatMode()} lets you specify either of the alternatives. Run-once
     * will leave the animated property in the 'new' state, which is normally
     * what you want: you use a run-once animation to smooth a transition.
     */
    public static final int ONCE = 0;
    /**
     * Run repeatedly: start to finish; start to finish; and so on.
     * 
     * This will leave the animated property in the 'start' state. In some
     * cases, this will be visually jarring; in others (like when you are doing
     * a full 360 degree rotation) this will be exactly what you want.
     */
    public static final int REPEATED = 1;
    /**
     * Run repeatedly: start to finish, finish to start; start to finish, finish
     * to start; and so on.
     * 
     * This is perfect for 'wiggle' or grow-shrink animations. Note that an odd
     * {@linkplain GVRAnimation#setRepeatCount(int) repeat count} will leave the
     * animated property in the 'new' state, just like {@linkplain #ONCE
     * run-once} does; an even repeat count will leave the animated property in
     * the 'start' state, just like {@linkplain #REPEATED repeated} does. The
     * default repeat count is 2, so you don't have to explicitly set a repeat
     * count to 'pulse' an object with a ping pong animation.
     */
    public static final int PINGPONG = 2;

    static boolean invalidRepeatMode(int typeCode) {
        switch (typeCode) {
        case ONCE:
        case REPEATED:
        case PINGPONG:
            return false;
        default:
            return true;
        }
    }
}