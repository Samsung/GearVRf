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

import java.lang.ref.ReferenceQueue;
import java.util.HashSet;
import java.util.Set;

/*
 * Native heap memory allocated through gvrf is handled by reference counting.
 * There exists a std::shared_ptr in the native heap for every GVRHybridObject
 * and also the native heap instances hold handles almost everything using the
 * std::shared_ptr (except some std::weak_ptrs to avoid cyclic referencing.)
 * 
 * So the native heap memory gets deallocated if and only if there are no more
 * Java/C++ instances which are able to use the instance in the memory. Also
 * GL resources are handled in a similar way.
 */

/**
 * This is a class that handles memory allocated in the native heap.
 * GVRReferenceQueue will be declared in {@link GVRViewManager} which holds the
 * entire scene details. GVRReferenceQueue helps GVR scene to organize the
 * memory in native heap.
 */
class GVRReferenceQueue {
    /**
     * References pointing instances in the unreachable native heap memory.
     */
    private final ReferenceQueue<GVRHybridObject> mHybridReferenceQueue = new ReferenceQueue<GVRHybridObject>();
    /**
     * References pointing instances in the unreachable native heap memory and
     * holding GPU memory.
     */
    private final ReferenceQueue<GVRRecyclableObject> mRecyclableReferenceQueue = new ReferenceQueue<GVRRecyclableObject>();
    /**
     * Holds references to prevent them from disappearing before they go to the
     * reference queue.
     */
    private final Set<GVRReference> mReferences = new HashSet<GVRReference>();

    /**
     * Constructs an empty GVRReferenceQueue object
     */
    GVRReferenceQueue() {
        super();
    }

    /**
     * Returns the current {@link GVRHybridObject hybrid-reference} queue
     * 
     * @return current hybrid reference queue in ReferenceQueue<GVRHybridObject>
     *         type
     */
    ReferenceQueue<GVRHybridObject> getHybridReferenceQueue() {
        return mHybridReferenceQueue;
    }

    /**
     * Returns the current {@link GVRRecyclableObject recyclable} queue
     * 
     * @return current recyclable queue in ReferenceQueue<GVRRecyclableObject>
     *         type
     */
    ReferenceQueue<GVRRecyclableObject> getRecyclableQueue() {
        return mRecyclableReferenceQueue;
    }

    /**
     * Adds a {@link GVRReference reference} in the class
     * 
     * @param reference
     *            the reference to be added
     */
    void addReference(GVRReference reference) {
        mReferences.add(reference);
    }

    /**
     * Removes all the {@link GVRReference reference} and recycles them (note:
     * Properly deallocates native heap memory and Properly deallocates native
     * heap memory and GPU memory)
     */
    void clean() {
        deleteReferences();
        recycleReferences();
    }

    /**
     * Properly deallocates native heap memory.
     */
    void deleteReferences() {
        GVRHybridReference reference = (GVRHybridReference) mHybridReferenceQueue
                .poll();
        while (reference != null) {
            reference.delete();
            mReferences.remove(reference);
            reference = (GVRHybridReference) mHybridReferenceQueue.poll();
        }
    }

    /**
     * Properly deallocates native heap memory and GPU memory.
     */
    void recycleReferences() {
        GVRRecyclableReference reference = (GVRRecyclableReference) mRecyclableReferenceQueue
                .poll();
        while (reference != null) {
            if (reference.getUseCount() == 1) {
                reference.recycle();
            }
            reference.delete();
            mReferences.remove(reference);
            reference = (GVRRecyclableReference) mRecyclableReferenceQueue
                    .poll();
        }
    }

    void onDestroy() {
        /*
         * Since a new activity can be created while the process is not yet
         * killed, this finalization is necessary.
         */
        for (GVRReference reference : mReferences) {
            reference.delete();
        }
    }
}
