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
 * This interface defines events for GVRSceneObject. Please note that the GVRSceneObject
 * class does not implement this interface. To handle events delivered to it, user can
 * add an object that implements this interface to a GVRSceneObject using
 * {@link GVREventReceiver#addListener(IEvents)}. The GVREventReceiver can be obtained
 * using {@link GVRSceneObject#getEventReceiver()}.
 */
public interface ISceneObjectEvents extends ILifeCycleEvents {
    /**
     * Called when a {@link GVRSceneObject} is constructed.
     * @param gvrContext
     *         The GVRContext.
     * @param sceneObject
     *         The GVRSceneObject itself.
     */
    void onInit(GVRContext gvrContext, GVRSceneObject sceneObject);

    /**
     * Called after the object has been loaded from a model.
     */
    void onLoaded();
}
