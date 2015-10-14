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

import java.util.List;

import org.gearvrf.utility.Exceptions;

/**
 * Base class for classes that can be attached to a {@link GVRSceneObject scene
 * object}.
 */
class GVRComponent extends GVRHybridObject {
    // private static final String TAG = Log.tag(GVRComponent.class);

    /**
     * Normal constructor
     * 
     * @param gvrContext
     *            The current GVRF context
     * @param nativePointer
     *            The native pointer, returned by the native constructor
     */
    protected GVRComponent(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    /**
     * Special constructor, for descendants like {#link GVRMeshEyePointee} that
     * need to 'unregister' instances.
     * 
     * @param gvrContext
     *            The current GVRF context
     * @param nativePointer
     *            The native pointer, returned by the native constructor
     * @param cleanupHandlers
     *            Cleanup handler(s).
     * 
     *            <p>
     *            Normally, this will be a {@code private static} class
     *            constant, so that there is only one {@code List} per class.
     *            Descendants that supply a {@code List} and <em>also</em> have
     *            descendants that supply a {@code List} should use
     *            {@link CleanupHandlerListManager} to maintain a
     *            {@code Map<List<NativeCleanupHandler>, List<NativeCleanupHandler>>}
     *            whose keys are descendant lists and whose values are unique
     *            concatenated lists - see {@link GVREyePointeeHolder} for an
     *            example.
     */
    protected GVRComponent(GVRContext gvrContext, long nativePointer,
            List<NativeCleanupHandler> cleanupHandlers) {
        super(gvrContext, nativePointer, cleanupHandlers);
    }

    protected GVRSceneObject owner;

    /**
     * @return The {@link GVRSceneObject} this object is currently attached to.
     */
    protected GVRSceneObject getOwnerObject() {
        if (owner != null) {
            return owner;
        }

        throw Exceptions.RuntimeAssertion("No Java owner: %s", getClass()
                .getSimpleName());
    }

    protected void setOwnerObject(GVRSceneObject owner) {
        this.owner = owner;
    }

    /**
     * Checks if the {@link GVRComponent} is attached to a {@link GVRSceneObject}.
     *
     * @return true if a {@link GVRSceneObject} is attached.
     */
    public boolean hasOwnerObject() {
        return owner != null;
    }
}
