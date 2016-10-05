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

public class WearConstants {
    public static final String START_ACTIVITY_PATH = "/start_activity";
    public static final byte[] CONNECT_TO_APP = new byte[]{0x01};
    public static final byte[] DISCONNECT_FROM_APP = new byte[]{0x02};
    public static final String TOUCH_EVENT_PATH = "/touch_event";
    public static final String START_TOUCH_LISTENER_PATH = "/start_touch_listener";
}
