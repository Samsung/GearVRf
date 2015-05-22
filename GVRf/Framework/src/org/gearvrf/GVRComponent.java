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

package org.gearvrf;

import org.gearvrf.utility.Exceptions;

/**
 * Base class for classes that can be attached to a {@link GVRSceneObject scene
 * object}.
 */
class GVRComponent extends GVRHybridObject {
    // private static final String TAG = Log.tag(GVRComponent.class);

    GVRComponent(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    protected GVRSceneObject owner;

    /**
     * @return The {@link GVRSceneObject} this object is currently attached to.
     */
    public GVRSceneObject getOwnerObject() {
        if (owner != null) {
            return owner;
        }

        throw Exceptions.RuntimeAssertion("No Java owner: %s", getClass()
                .getSimpleName());
    }

    protected void setOwnerObject(GVRSceneObject owner) {
        this.owner = owner;
    }
}
