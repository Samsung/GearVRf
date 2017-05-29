/*
 * Copyright 2016 Samsung Electronics Co., LTD
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


package org.gearvrf.io.cursor3d;

/**
 * An output event that is created in the Android application and sent to an {@link IoDevice}.
 * (e.g. ACTION_VIBRATE in onStateChanged)
 * This call for this OutputEvent is exemplified as follows :
 *
 *  OutputEvent outputEvent = new OutputEvent(IoDevice.Actions.ACTION_VIBRATE.ordinal());
 *  cursor.handleOutputEvent(outputEvent);
 */
public class OutputEvent{

    public int action;
    public Object value;

    /**
     * @param action   The action which we want the {@link IoDevice} to do
     * @param value   A parameter for this action (i.e. the length of the action)
     */
    public OutputEvent(int action, Object value) {
        this.action = action;
        this.value = value;
    }

    public OutputEvent(int action)
    {
        this.action = action;
        value = null;
    }

}
