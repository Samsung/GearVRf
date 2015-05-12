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

import org.gearvrf.utility.Log;

/** Base wrapper class for GVRF C++ classes */
public abstract class GVRHybridObject implements Closeable {

    private static final String TAG = Log.tag(GVRHybridObject.class);

    private final GVRContext mGVRContext;

    private long mNativePointer;

    GVRHybridObject(GVRContext gvrContext, long nativePointer) {
        mGVRContext = gvrContext;
        mNativePointer = nativePointer;
    }

    /**
     * The {@code finalize} method is {@code final} to protect the
     * {@link #close()} 'machinery' in {@link GVRHybridObject}.
     * 
     * If you need to run any code when a descendant is closed, override the
     * {@link #destructor()} method.
     * 
     * @since 2.0.0
     */
    @Override
    protected final void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Close this object, releasing any native resources.
     * 
     * This method is {@code final} to protect the {@code close()} /
     * {@code finalize()} 'machinery' - if you need to run any code when a
     * descendant is closed, override the {@link #destructor()} method.
     * 
     * @since 2.0.0
     */
    @Override
    public final void close() throws IOException {
        if (mNativePointer != 0) {
            destructor();
            mNativePointer = 0l;
        }
    }

    /**
     * Calls the native destructor, {@code virtual ~HybridObject()}.
     * 
     * This will be called only once, no matter how many times an app calls
     * {@link #close()}. Most classes will not need to override this; the
     * exception is classes that maintain any sort of 'registry' of 'live'
     * classes (like {@link GVREyePointeeHolder}). If you <em>do</em> override
     * this method, be sure to call {@code super.destructor()}.
     * 
     * @since 2.0.0
     */
    protected void destructor() {
        NativeHybridObject.delete(mNativePointer);
    }

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

}

class NativeHybridObject {
    static native void delete(long nativePointer);
}
