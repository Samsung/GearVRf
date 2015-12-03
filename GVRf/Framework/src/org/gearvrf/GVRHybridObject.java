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

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gearvrf.utility.Log;

/**
 * Root of the GVRF object hierarchy.
 * 
 * Descendant classes all have native (JNI) implementations; this base class
 * manages the native lifecycles.
 */
public abstract class GVRHybridObject implements Closeable {

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

    /**
     * If true, the native object will be deleted by this class; otherwise,
     * it won't be deleted.
     */
    private boolean mOwnNative;

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
        this(gvrContext, nativePointer, true);
    }

    /**
     * Normal constructor
     * 
     * @param gvrContext
     *            The current GVRF context
     * @param nativePointer
     *            The native pointer, returned by the native constructor
     */
    protected GVRHybridObject(GVRContext gvrContext, long nativePointer, boolean ownNative) {
        this(gvrContext, nativePointer, ownNative, null);
    }
    
    /**
     * Special constructor, for descendants like {#link GVRMeshEyePointee} that
     * need to 'unregister' instances.
     * 
     * @param gvrContext
     *            The current GVRF context
     * @param nativePointer
     *            The native pointer, returned by the native constructor
     * @param ownNative
     *            If true, the native object is owned by this hybrid object.
     *            It will be deleted when this object is destroyed.
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
    protected GVRHybridObject(GVRContext gvrContext, long nativePointer, boolean ownNative,
            List<NativeCleanupHandler> cleanupHandlers) {
        mGVRContext = gvrContext;
        mNativePointer = nativePointer;
        mOwnNative = ownNative;

        sReferenceSet
                .add(new GVRReference(this, nativePointer, mOwnNative, cleanupHandlers));
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

    public static long[] getNativePtrArray(Collection<? extends GVRHybridObject> objects) {
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

    /**
     * Our {@linkplain GVRReference references} are placed on this queue, once
     * they've been finalized
     */
    private static final ReferenceQueue<GVRHybridObject> sReferenceQueue = new ReferenceQueue<GVRHybridObject>();
    /**
     * We need hard references to {@linkplain GVRReference our references} -
     * otherwise, the references get garbage collected (usually before their
     * objects) and never get enqueued.
     */
    private static final Set<GVRReference> sReferenceSet = new HashSet<GVRReference>();

    static {
        new GVRFinalizeThread();
    }

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

    private static class GVRReference extends PhantomReference<GVRHybridObject> {

        // private static final String TAG = Log.tag(GVRReference.class);

        private long mNativePointer;
        private boolean mOwnNative;
        private final List<NativeCleanupHandler> mCleanupHandlers;

        private GVRReference(GVRHybridObject object, long nativePointer, boolean ownNative,
                List<NativeCleanupHandler> cleanupHandlers) {
            super(object, sReferenceQueue);

            mNativePointer = nativePointer;
            mOwnNative = ownNative;
            mCleanupHandlers = cleanupHandlers;
        }

        private void close() {
            if (mNativePointer != 0) {
                if (mCleanupHandlers != null) {
                    for (NativeCleanupHandler handler : mCleanupHandlers) {
                        handler.nativeCleanup(mNativePointer);
                    }
                }
                if (mOwnNative) {
                    NativeHybridObject.delete(mNativePointer);
                }
            }

            sReferenceSet.remove(this);
        }
    }

    private static class GVRFinalizeThread extends Thread {

        // private static final String TAG = Log.tag(GVRFinalizeThread.class);

        private GVRFinalizeThread() {
            setName("GVRF Finalize Thread");
            setPriority(MAX_PRIORITY);
            start();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    GVRReference reference = (GVRReference) sReferenceQueue
                            .remove();
                    reference.close();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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
     * @since 2.0.0
     */
    @Override
    public final void close() throws IOException {
        if (mNativePointer != 0L) {
            GVRReference reference = findReference(mNativePointer);
            if (reference != null) {
                reference.close();
                mNativePointer = 0L;
            }
        }
    }

    /**
     * Explicitly close()ing an object is going to be relatively rare - most
     * native memory will be freed when the owner-objects are garbage collected.
     * Doing a lookup in these rare cases means that we can avoid giving every @link
     * {@link GVRHybridObject} a hard reference to its {@link GVRReference}.
     */
    private static GVRReference findReference(long nativePointer) {
        for (GVRReference reference : sReferenceSet) {
            if (reference.mNativePointer == nativePointer) {
                return reference;
            }
        }
        // else
        return null;
    }
}

class NativeHybridObject {
    static native void delete(long nativePointer);
}
