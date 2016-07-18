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

import org.gearvrf.utility.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Root of the GVRF object hierarchy.
 * 
 * Descendant classes all have native (JNI) implementations; this base class
 * manages the native lifecycles.
 */
public abstract class GVRHybridObject {

    private static final String TAG = Log.tag(GVRHybridObject.class);

    /*
     * Instance fields
     */

    private final GVRContext mGVRContext;
    /**
     * This is not {@code final}: the first call to {@link #close()} sets
     * {@link #mNativePointer} to 0, so that {@link #close()} can safely be
     * called multiple times.
     */
    private long mNativePointer;

    /*
     * Constructors
     */

    /**
     * Normal constructor
     *
     * @param gvrContext
     *            The current GVRF context
     * @param nativePointer
     *            The native pointer, returned by the native constructor
     */
    protected GVRHybridObject(GVRContext gvrContext, long nativePointer) {
        this(gvrContext, nativePointer, null);
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
    protected GVRHybridObject(GVRContext gvrContext, long nativePointer,
            List<NativeCleanupHandler> cleanupHandlers) {
        mGVRContext = gvrContext;
        mNativePointer = nativePointer;

        gvrContext.registerHybridObject(this,nativePointer, cleanupHandlers);
    }

    /*
     * Instance methods
     */

    /**
     * Set or clear the keep-wrapper flag.
     * 
     * @deprecated This is a no-op as of version 2.0, and will be removed
     *             sometime in (or after) Q4 2015.
     */
    public void setKeepWrapper(boolean keep) {
    }

    /**
     * Get the current state of the keep-wrapper flag.
     * 
     * @deprecated As of version 2.0 this always returns {@code false}, and will
     *             be removed sometime in (or after) Q4 2015.
     */
    public boolean getKeepWrapper() {
        return false;
    }

    /**
     * Get the {@link GVRContext} this object is attached to.
     * 
     * @return The object's {@link GVRContext}.
     */
    public GVRContext getGVRContext() {
        return mGVRContext;
    }

    /**
     * The address of the {@code std:shared_ptr} pointing to the native object.
     * 
     * <p>
     * This is an internal method that may be useful in diagnostic code.
     * 
     * @deprecated As of version 2.0, this is synonymous with
     *             {@link #getNative()}, and will be removed sometime in (or
     *             after) Q4 2015.
     */
    public long getPtr() {
        return getNative();
    }

    /**
     * The actual address of the native object.
     * 
     * <p>
     * This is an internal method that may be useful in diagnostic code.
     */
    public long getNative() {
        return mNativePointer;
    }

    /*package*/ static long[] getNativePtrArray(Collection<? extends GVRHybridObject> objects) {
        long[] ptrs = new long[objects.size()];

        int i = 0;
        for (GVRHybridObject obj : objects) {
            ptrs[i++] = obj.getNative();
        }

        return ptrs;
    }

    @Override
    public boolean equals(Object o) {
        // FIXME Since there is a 1:1 relationship between wrappers and native
        // objects, `return this == o` should be all we need ...
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof GVRHybridObject) {
            GVRHybridObject other = (GVRHybridObject) o;
            boolean nativeEquality = getNative() == other.getNative();
            nativeEquality &= (getNative() != 0);
            if (nativeEquality) {
                Log.d(TAG, "%s.equals(%s), but %s %c= %s", //
                        this, o, //
                        this, (this == o) ? '=' : '!', o);
            }
            return nativeEquality;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        Long nativePointer = getNative();
        return nativePointer.hashCode();
    }

    /**
     * How many references are there to the native object?
     * 
     * <p>
     * This is an internal method that may be useful in diagnostic code.
     * 
     * @deprecated This is meaningless, as of version 2.0, and will be removed
     *             sometime in (or after) Q4 2015.
     */
    public int getUseCount() {
        return 0;
    }

    /*
     * Native memory management
     */

    /** Optional after-finalization callback to 'deregister' native pointers. */
    protected interface NativeCleanupHandler {
        /**
         * Remove the native pointer from any maps or other data structures.
         * 
         * Do note that the Java 'owner object' has already been finalized.
         * 
         * @param nativePointer
         *            The native pointer associated with a Java object that has
         *            already been garbage collected.
         */
        void nativeCleanup(long nativePointer);
    }

    /**
     * Small class to help descendants keep the number of lists of native
     * cleanup handlers to a minimum.
     * 
     * Maintains a prefix list (the static list that the descendant class passes
     * to {@link GVRHybridObject#GVRHybridObject(GVRContext, long, List)}) and a
     * {@code Map} of suffixes: the {@code Map} lets there be one list per
     * descendant class that adds a list of cleanup handler(s), instead of
     * (potentially) one list per instance.
     * 
     * See the usage in {@link GVREyePointeeHolder}.
     */
    protected static class CleanupHandlerListManager {
        private final List<NativeCleanupHandler> mPrefixList;

        private final Map<List<NativeCleanupHandler>, List<NativeCleanupHandler>> //
        mUniqueCopies = new HashMap<List<NativeCleanupHandler>, List<NativeCleanupHandler>>();

        /**
         * Typically, descendants have a single (static) list of cleanup
         * handlers: pass that list to this constructor.
         * 
         * @param prefixList
         *            List of cleanup handler(s)
         */
        protected CleanupHandlerListManager(
                List<NativeCleanupHandler> prefixList) {
            mPrefixList = prefixList;
        }

        /**
         * Descendants that add a cleanup handler list use this method to create
         * unique concatenations of their list with any of <em>their</em>
         * descendants' list(s).
         * 
         * @param suffix
         *            Descendant's (static) list
         * @return A unique concatenation
         */
        protected List<NativeCleanupHandler> getUniqueConcatenation(
                List<NativeCleanupHandler> suffix) {
            if (suffix == null) {
                return mPrefixList;
            }

            List<NativeCleanupHandler> concatenation = mUniqueCopies
                    .get(suffix);
            if (concatenation == null) {
                concatenation = new ArrayList<NativeCleanupHandler>(
                        mPrefixList.size() + suffix.size());
                concatenation.addAll(mPrefixList);
                concatenation.addAll(suffix);
                mUniqueCopies.put(suffix, concatenation);
            }
            return concatenation;
        }
    }

    /**
     * Close this object, releasing any native resources.
     * 
     * Most objects will be automatically closed when Java's garbage collector
     * detects that they are no longer being used: Explicitly closing an object
     * that's still linked into the scene graph will almost certainly crash your
     * GVRF app. You should only {@code close()} transient objects (especially
     * those that use lots of memory, like large textures) that you
     * <em>know</em> are no longer being used.
     * 
     * @since 3.0.0
     */
    public final void releaseNative() {
        mGVRContext.releaseNative(this);
    }

    /**
     * Close this object, releasing any native resources.
     * 
     * Most objects will be automatically closed when Java's garbage collector
     * detects that they are no longer being used: Explicitly closing an object
     * that's still linked into the scene graph will almost certainly crash your
     * GVRF app. You should only {@code close()} transient objects (especially
     * those that use lots of memory, like large textures) that you
     * <em>know</em> are no longer being used.
     * 
     * @since 2.0.0
     */
    @Deprecated
    public final void close() throws IOException {
        mGVRContext.releaseNative(this);
    }
}

class NativeHybridObject {
    static native void delete(long nativePointer);
}
