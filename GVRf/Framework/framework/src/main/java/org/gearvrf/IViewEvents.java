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

import android.view.View;

import org.gearvrf.scene_objects.GVRViewSceneObject;

/**
 * This interface defines the callback interface of an Android {@code View}
 * that is inflated by a {@link GVRViewSceneObject}.
 */
public interface IViewEvents extends IEvents {
    void onInitView(GVRViewSceneObject sceneObject, View view);
    void onStartRendering(GVRViewSceneObject sceneObject, View view);
}
