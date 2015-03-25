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
 * Phantom reference for deleting a hybrid object; part of JNI native object
 * management code.
 */
 class GVRHybridReference extends PhantomReference<GVRHybridObject>
        implements GVRReference {
    private final long mPtr;
    private boolean mDeleted = false;

    GVRHybridReference(long ptr, GVRHybridObject hybridObject,
            ReferenceQueue<? super GVRHybridObject> queue) {
        super(hybridObject, queue);
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
}
