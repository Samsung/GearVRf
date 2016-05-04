/* Copyright 2016 Samsung Electronics Co., LTD
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

/**
 * An interface for event receivers. An event receiver
 * contains a list of event listeners, and an event can be
 * delivered to it.
 */
public interface IEventReceiver {
    /**
     * Returns the event receiver implementation so that events can be delivered.
     * @return
     */
    GVREventReceiver getEventReceiver();
}