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

    /** Enables a 1:1 mapping between native objects and Java wrappers */
    protected static final LongSparseArray<GVRHybridObject> sWrappers = new LongSparseArray<GVRHybridObject>();

    private static int sRegistrationCount = 0;

    /** Returns an existing wrapper, or {@code null} */
    protected static GVRHybridObject wrapper(long ptr) {
        final long nativePointer = NativeHybridObject.getNativePointer(ptr);
        synchronized (sWrappers) {
            int index = sWrappers.indexOfKey(nativePointer);
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
                    if (wrapper.getUseCount() == 1) {
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
