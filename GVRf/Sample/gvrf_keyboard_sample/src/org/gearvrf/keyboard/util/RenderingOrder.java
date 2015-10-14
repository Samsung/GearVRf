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

package org.gearvrf.keyboard.util;

public class RenderingOrder {

    public static final int ORDER_RENDERING_EXCEPTION_ICON = 153;
    public static final int ORDER_RENDERING_EXCEPTION_RING = 152;
    public static final int ORDER_RENDERING_EXCEPTION_BLUR = 151;
    public static final int KEYBOARD_SOUND_WAVE = 141;
    public static final int KEYBOARD = 140;
    public static final int SPINNER = KEYBOARD + 10;

    // TODO set RenderRingOrder spinner skeleton

    public static final int SPINNER_BOX = KEYBOARD + 9;
    public static final int SPINNER_SHADOW = KEYBOARD + 8;

    // public static final int SPINNER_BOX = KEYBOARD - 4;
    // public static final int SPINNER_SHADOW = KEYBOARD - 5;

    public static final int ORDER_RENDERING_ICON = 131;
    public static final int ORDER_RENDERING_HOVER = 130;
    public static final int ORDER_RENDERING_RUN = 129; // reserved from 129 to
                                                       // 124
    public static final int ORDER_RENDERING_PROGRESS = 123;

}
