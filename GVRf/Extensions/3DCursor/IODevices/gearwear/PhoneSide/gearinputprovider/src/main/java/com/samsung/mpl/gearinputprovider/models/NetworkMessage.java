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
 * limitations under the License..
 */

package com.samsung.mpl.gearinputprovider.models;

public class NetworkMessage {
    private Type type;
    private Event event;

    public enum Type {
        CONNECTION_STATUS,
        ERROR,
        INPUT
    }

    public enum Event {
        TOUCH_START, TOUCH_MOVE, TOUCH_END, CLICK, ROTARY, SWIPE, BACK;
    }

    public NetworkMessage(Type type) {
        this.type = type;
    }

    public NetworkMessage(Type type, Event event) {
        this(type);
        this.event = event;
    }

    public Type getType() {
        return type;
    }

    public Event getEvent() {
        return event;
    }
}
