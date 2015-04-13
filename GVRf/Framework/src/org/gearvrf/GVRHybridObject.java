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

import java.util.ArrayList;
import java.util.List;

import android.util.LongSparseArray;

/** Base wrapper class for GVRF C++ classes */
public class GVRHybridObject {

    private static final int REGISTRATIONS_BETWEEN_DEREFERENCE_SCANS = 250;

    /**
     * The 'generation bits' of the {@link #mFlags flags} field.
     * 
     * Each {@link GVRHybridObject} has a set of flags. Currently, these flags
     * contain only two values: the {@link #getKeepWrapper() keep-wrapper flag}
     * and the generation count. This mask gets the generation count's bits. (As
     * a matter of efficiency, the generation count is always in the low bits of
     * the flag field, and this mask doubles as a maximum generation-count
     * value.)
     * 
     * <p>
     * The generation count is used to prevent a race condition in
     * {@linkplain DereferenceThread the defererence thread:} if an app does a
     * lot of simultaneous allocations in {@link GVRScript#onInit(GVRContext)
     * onInit()} or {@link GVRScript#onStep() onStep()}, it is not impossible
     * that the deference thread will wake up one or more times before the app
     * has had a chance to link a wrapped object into the scene graph. That is,
     * the newly created object might not have any native references yet, and
     * the deference thread would remove it from {@link #sWrappers}, which would
     * mean that eye picking (say) would return a new wrapper object for the
     * native object, not the original, app-created wrapper object (which may be
     * subclassed, and contain values important to the app). By not
     * dereferencing the object until {@linkplain #sGenerationCounter the
     * generation counter} no longer matches the generation count in the flags
     * field, we guarantee that the object is not dereferenced until the app
     * callback has returned, which means that the app has had every chance to
     * either link the object into the scene graph or to set the
     * {@link #setKeepWrapper(boolean)} flag.
     */
    private static final int GENERATION_MASK = Integer.MAX_VALUE;

    /** The keep-wrapper bit of the {@link #mFlags flags} field. */
    private static final int KEEP_WRAPPER_MASK = ~GENERATION_MASK;

    /** Enables a 1:1 mapping between native objects and Java wrappers */
    private static final LongSparseArray<GVRHybridObject> sWrappers = new LongSparseArray<GVRHybridObject>();

    static {
        GVRContext.addResetOnRestartHandler(new Runnable() {

            @Override
            public void run() {
                sWrappers.clear();
            }
        });
    }

    private static int sRegistrationCount = 0;

    private static int sGenerationCounter = 0;

    /** Update the generation counter */
    static void onStep() {
        sGenerationCounter = (sGenerationCounter == GENERATION_MASK) ? 1
                : sGenerationCounter + 1;
    }

    /** Returns an existing wrapper, or {@code null} */
    protected static GVRHybridObject wrapper(long ptr) {
        final long nativePointer = NativeHybridObject.getNativePointer(ptr);
        synchronized (sWrappers) {
            int index = sWrappers.indexOfKey(nativePointer);
            if (index >= 0) {
                // We have a wrapper for the nativePointer - we should delete
                // the `new std::shared_ptr`, so that we don't waste its memory
                // and so that (even more importantly!) we don't accumulate a
                // spurious reference count to the native object
                NativeHybridObject.delete(ptr);
            }
            return index < 0 ? null : sWrappers.valueAt(index);
        }
    }

    /*
     * Connects the object to the reference queues handling memory.
     */
    private final GVRContext mGVRContext;

    /*
     * The way to access the c++ instance.
     */
    private final GVRReference mReference;

    private int mFlags = sGenerationCounter;

    GVRHybridObject(GVRContext gvrContext, long ptr) {
        mGVRContext = gvrContext;

        GVRReferenceQueue referenceQueue = gvrContext.getReferenceQueue();
        if (this instanceof GVRRecyclableObject) {
            mReference = new GVRRecyclableReference(ptr,
                    (GVRRecyclableObject) this,
                    referenceQueue.getRecyclableQueue());
        } else {
            mReference = new GVRHybridReference(ptr, this,
                    referenceQueue.getHybridReferenceQueue());
        }

        /*
         * Needed to save the reference from being garbage collected before the
         * linked hybrid object gets collected.
         */
        referenceQueue.addReference(mReference);

        if (registerWrapper()) {
            // 'Register' fully initialized object, so that getX() calls can
            // return this (possibly sub-classed) object, not a new wrapper
            final long nativePointer = NativeHybridObject.getNativePointer(ptr);
            synchronized (sWrappers) {
                sWrappers.put(nativePointer, this);

                if ((++sRegistrationCount % REGISTRATIONS_BETWEEN_DEREFERENCE_SCANS) == 0) {
                    sRegistrationCount = 0;
                    synchronized (sDereferenceThread) {
                        sDereferenceThread.notify();
                    }
                }
            }
        }
    }

    /**
     * Set or clear the keep-wrapper flag.
     * 
     * This is a very specialized operation: you will not use it often. The only
     * consequences of misuse are a small memory leak, but you should still know
     * what you're doing before calling this method.
     * 
     * <p>
     * You only need to set the keep-wrapper flag <em>in some cases</em> when
     * you're getting a GVRF object from another GVRF object, and it really
     * matters that the object that you get back is the object you put in (as
     * opposed to another object that refers to the same native object). The
     * most common scenario where this matters is one where you have declared
     * classes that {@code extend GVRSceneObject} and you want to get your
     * sub-classed object back from the {@link GVRPicker} because that instance
     * contains app-specific values that a 'generic' {@link GVRSceneObject} does
     * not.
     * 
     * <p>
     * Please do note that you do <em>not</em> need to set the keep-wrapper flag
     * for every instance of every object that descends from
     * {@link GVRSceneObject}! Most wrappers are kept alive by references in the
     * scene graph; when you remove them from the scene graph, they can
     * generally be dereferenced and (eventually) garbage collected without any
     * issues.
     * 
     * <p>
     * This method allows you to handle the special case where you are
     * continually adding and removing the same object to and from the scene
     * graph. In these cases, the dereference thread may see that there are no
     * native references to the object (when it it is not in the scene graph)
     * and remove the registered reference that lets GVRF return the original,
     * possibly sub-classed instance. The symptom of this is code like
     * {@link GVREyePointeeHolder#getOwnerObject()} returning a base class
     * (<i>eg.</i> {@link GVRSceneObject}) instead of the sub-classed object
     * that you expect. In these cases, setting the the keep-wrapper flag when
     * you create the object will preserve object-identity.
     */
    public void setKeepWrapper(boolean keep) {
        if (keep) {
            mFlags |= KEEP_WRAPPER_MASK; // set bit
        } else {
            mFlags &= ~KEEP_WRAPPER_MASK; // clear bit
        }
    }

    /**
     * Get the current state of the keep-wrapper flag.
     * 
     * See {@link #setKeepWrapper(boolean)} for details.
     */
    public boolean getKeepWrapper() {
        return (mFlags & KEEP_WRAPPER_MASK) != 0;
    }

    private int getGenerationCount() {
        return mFlags & GENERATION_MASK;
    }

    /**
     * We don't need to worry about object identity for every
     * {@link GVRHybridObject} descendant, just the ones that we can retrieve
     * through APIs like {@link GVRSceneObject#getCamera()} or
     * {@link GVRSceneObject#getChildByIndex(int)}. Classes that hide their
     * {@code  (GVRContext gvrContext, long ptr)} constructor behind a
     * {@code factory(GVRContext gvrContext, long ptr)} method should override
     * {@link #registerWrapper()} to return {@code true}.
     * 
     * @return Whether or not we should register wrapper classes, so that we can
     *         get the original instances back later
     */
    protected boolean registerWrapper() {
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
     */
    public long getPtr() {
        return mReference.getPtr();
    }

    /**
     * The actual address of the native object.
     * 
     * <p>
     * This is an internal method that may be useful in diagnostic code.
     */
    public long getNative() {
        return mReference.getNative();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof GVRHybridObject) {
            GVRHybridObject other = (GVRHybridObject) o;
            return NativeHybridObject.equals(getPtr(), other.getPtr());
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
     */
    public int getUseCount() {
        return NativeHybridObject.getUseCount(getPtr());
    }

    private static class DereferenceThread extends Thread {

        DereferenceThread() {
            super("Dereference thread");
            setPriority(MIN_PRIORITY);
            start();
        }

        @Override
        public void run() {
            try {
                while (true) {

                    synchronized (sDereferenceThread) {
                        // Wait for 'enough' registrations
                        sDereferenceThread.wait();
                    }

                    removeSingleReferences();
                }
            } catch (InterruptedException e) {
            }
        }

        private void removeSingleReferences() {
            final List<GVRRecyclableObject> recyclables = new ArrayList<GVRRecyclableObject>();
            synchronized (sWrappers) {
                for (int index = sWrappers.size() - 1; index >= 0; --index) {
                    GVRHybridObject wrapper = sWrappers.valueAt(index);
                    if (wrapper.getUseCount() == 1
                            && wrapper.getKeepWrapper() == false
                            && wrapper.getGenerationCount() != sGenerationCounter) {
                        sWrappers.removeAt(index);
                        if (wrapper instanceof GVRRecyclableObject) {
                            recyclables.add((GVRRecyclableObject) wrapper);
                        }
                    }
                }
            }

            if (recyclables.size() > 0) {
                // Pass list to GL thread, outside of synchronized block
                GVRContext gvrContext = recyclables.get(0).getGVRContext();
                gvrContext.runOnGlThread(new Runnable() {

                    @Override
                    public void run() {
                        for (GVRRecyclableObject recyclable : recyclables) {
                            NativeRecyclableObject.recycle(recyclable.getPtr());
                        }

                    }
                });
            }
        }
    }

    private static DereferenceThread sDereferenceThread = new DereferenceThread();
}

class NativeHybridObject {
    static native void delete(long hybridObject);

    static native boolean equals(long hybridObject, long other);

    static native int getUseCount(long hybridObject);

    static native long getNativePointer(long hybridObject);
}
