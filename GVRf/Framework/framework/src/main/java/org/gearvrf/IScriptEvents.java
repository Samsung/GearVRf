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

import org.gearvrf.script.IScriptable;

/**
 * This interface defines life-cycle and general per-frame
 * events that are handled by an application.
 */
public interface IScriptEvents extends IEvents {
    void onEarlyInit(GVRContext gvrContext);

    /**
     * Called after GVRMain.onInit returns for scripts which
     * have been added during onInit.
     * @param gvrContext
     * @throws Throwable
     */
    void onInit(GVRContext gvrContext) throws Throwable;

    void onAfterInit();
    
    /**
     * Called when a script is attached to a target.
     * @param target object script is attached to
     */
    void onAttach(IScriptable target);

    /**
     * Called when a script is detached from a target.
     * @param target object script is detached from.
     */
    void onDetach(IScriptable target);

    /**
     * Called once every frame.
     */
    void onStep();
}
