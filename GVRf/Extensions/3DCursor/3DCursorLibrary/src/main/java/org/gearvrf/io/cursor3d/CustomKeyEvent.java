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

package org.gearvrf.io.cursor3d;

import android.view.KeyEvent;

/**
 * Defines constants for custom actions from {@link KeyEvent}s.
 * <p>
 * These custom actions can be used to do special functions inside the the app. For eg. A
 * {@link KeyEvent} which returns KEYCODE_SWIPE_LEFT on calling {@link KeyEvent#getAction()} can be
 * used to close the Settings UI in an app. This is just a specialized class that defines
 * interaction with the Settings UI and does not affect the behavior of the Cursor. In the future
 * this will be replaced by gesture api.
 */
public abstract class CustomKeyEvent {
    public static final int ACTION_SWIPE = -188;
    public static final int KEYCODE_SWIPE_LEFT = 0;
    public static final int KEYCODE_SWIPE_RIGHT = 1;
    public static final int KEYCODE_SWIPE_UP = 2;
    public static final int KEYCODE_SWIPE_DOWN = 3;
}
