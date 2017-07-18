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

import android.view.MotionEvent;

/**
 * Interface definition for a callback to be invoked when a touch event is dispatched
 * to a {@link GVRSceneObject}.
 */
public interface ITouchEvents extends IEvents {

    /**
     * Called when a touch event is dispatched to a {@link GVRSceneObject}.
     *
     * @param sceneObject The {@link GVRSceneObject} the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     * @param hitLocation Hit location.
     */
    void onTouch(GVRSceneObject sceneObject, MotionEvent event, float[] hitLocation);
}
