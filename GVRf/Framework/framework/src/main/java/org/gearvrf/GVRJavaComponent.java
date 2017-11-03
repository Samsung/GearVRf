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

/**
 * Base class for defining Java components to extend the scene object.
 *
 * Java components are used to add Java behaviours to scene objects
 * that can be initiated by the rendering thread.
 * A GVRJavaComponent keeps a reference to the Java object in the
 * C++ layer so that functions on the object can be invoked
 * during rendering.

 */
class GVRJavaComponent extends GVRComponent {
    /**
     * Constructor for a component that is not attached to a scene object.
     *
     * @param gvrContext    The current GVRF context
     * @param nativePointer Pointer to the native object, returned by the native constructor
     */
    protected GVRJavaComponent(GVRContext gvrContext, long nativePointer) {
        super(gvrContext, nativePointer);
        setJava(nativePointer);
    }

    /**
     * Special constructor, for descendants like that
     * need to 'unregister' instances.
     *
     * @param gvrContext
     *            The current GVRF context
     * @param nativePointer
     *            The native pointer, returned by the native constructor
     * @param cleanupHandlers
     *            Cleanup handler(s).
     *
     * Normally, this will be a {@code private static} class
     * constant, so that there is only one {@code List} per class.
     * Descendants that supply a {@code List} and <em>also</em> have
     * descendants that supply a {@code List} should use
     * {@link CleanupHandlerListManager} to maintain a
     * {@code Map<List<NativeCleanupHandler>, List<NativeCleanupHandler>>}
     * whose keys are descendant lists and whose values are unique
     * concatenated lists - see {@link GVRCollider} for an example.
     */
    @SuppressWarnings("unused")
    protected GVRJavaComponent(GVRContext gvrContext, long nativePointer,
                               List<NativeCleanupHandler> cleanupHandlers) {
        super(gvrContext, nativePointer, cleanupHandlers);
        setJava(nativePointer);
    }

    native void setJava(long component);
}
