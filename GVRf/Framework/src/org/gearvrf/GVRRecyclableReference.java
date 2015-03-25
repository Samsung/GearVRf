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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * Phantom reference for recycling a recyclable object & deleting the native
 * object it wraps. This can be a superior approach compared to overriding
 * {@link Object#finalize() finalize()} as deletion does not occur until after
 * the wrapper object can no longer be revived.
 */
class GVRRecyclableReference extends PhantomReference<GVRRecyclableObject>
        implements GVRReference {
    private final long mPtr;
    private boolean mDeleted = false;

    /**
     * Constructs a new reference, and registers it with the given
     * {@link ReferenceQueue reference queue}.
     * 
     * @param ptr
     *            Pointer to the wrapped native object.
     * @param recyclableObject
     * @param queue
     */
    GVRRecyclableReference(long ptr, GVRRecyclableObject recyclableObject,
            ReferenceQueue<? super GVRRecyclableObject> queue) {
        super(recyclableObject, queue);
        mPtr = ptr;
    }

    @Override
    public long getPtr() {
        return mPtr;
    }

    @Override
    public long getNative() {
        return NativeHybridObject.getNativePointer(mPtr);
    }

    @Override
    public boolean isDeleted() {
        return mDeleted;
    }

    @Override
    public void delete() {
        if (!mDeleted) {
            NativeHybridObject.delete(mPtr);
            mDeleted = true;
        }
    }

    /**
     * @return The number of references to the wrapped native object.
     */
    int getUseCount() {
        return NativeHybridObject.getUseCount(mPtr);
    }

    /**
     * "Recycles" the native object. The exact semantics are dependent on the
     * native object implementation, but for current objects this means deleting
     * the GL shader program.
     */
    void recycle() {
        NativeRecyclableObject.recycle(mPtr);
    }
}
