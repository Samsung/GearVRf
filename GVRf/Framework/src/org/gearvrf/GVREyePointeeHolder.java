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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.gearvrf.utility.Threads;

import android.util.LongSparseArray;

/**
 * Holds any number of {@linkplain GVREyePointee 'eye pointees,'} which are
 * things the eye is pointing at.
 * 
 * Ray casting is computationally expensive. Rather than probing the entire
 * scene graph, GVRF requires you to mark parts of the scene as "pickable" by
 * adding their meshes (or, more cheaply if less precisely, their
 * {@linkplain GVRMesh#getBoundingBox() bounding box}) to a
 * {@link GVREyePointeeHolder};
 * {@linkplain GVRSceneObject#attachEyePointeeHolder(GVREyePointeeHolder)
 * attaching} that holder to a {@linkplain GVRSceneObject scene object}; and
 * setting the holder's {@linkplain #setEnable(boolean) enabled flag.}
 * 
 * <p>
 * When you call one of the {@linkplain GVRPicker#pickScene(GVRScene)
 * pickScene() overloads}, you get an array of {@linkplain GVREyePointeeHolder
 * eye pointee holders}. You can then call {@link #getOwnerObject()} to get the
 * scene object that a holder is attached to.
 */
public class GVREyePointeeHolder extends GVRComponent {

    // private static final String TAG = Log.tag(GVREyePointeeHolder.class);

    private static final LongSparseArray<WeakReference<GVREyePointeeHolder>> sEyePointeeHolders = new LongSparseArray<WeakReference<GVREyePointeeHolder>>();

    private final List<GVREyePointee> pointees = new ArrayList<GVREyePointee>();

    static GVREyePointeeHolder lookup(GVRContext gvrContext, long nativePointer) {
        WeakReference<GVREyePointeeHolder> weakReference = sEyePointeeHolders
                .get(nativePointer);
        return weakReference == null ? null : weakReference.get();
    }

    /**
     * Constructor
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVREyePointeeHolder(GVRContext gvrContext) {
        this(gvrContext, NativeEyePointeeHolder.ctor());
    }

    private GVREyePointeeHolder(GVRContext gvrContext, long nativePointer) {
        super(gvrContext, nativePointer);
        sEyePointeeHolders.put(nativePointer,
                new WeakReference<GVREyePointeeHolder>(this));
    }

    @Override
    protected void destructor() {
        sEyePointeeHolders.remove(getNative());
        super.destructor();
    }

    /**
     * Is this holder enabled?
     * 
     * If this holder is disabled, then picking will <b>not</b> occur against
     * its {@link GVREyePointee}s.
     * 
     * @return true if enabled, false otherwise.
     */
    public boolean getEnable() {
        return NativeEyePointeeHolder.getEnable(getNative());
    }

    /**
     * Enable or disable this holder.
     * 
     * If this holder is disabled, then picking will <b>not</b> occur against
     * its {@link GVREyePointee}s.
     * 
     * @param enable
     *            whether this holder should be enabled.
     */
    public void setEnable(boolean enable) {
        NativeEyePointeeHolder.setEnable(getNative(), enable);
    }

    /**
     * Get the x, y, z of the point of where the hit occurred in model space
     * 
     * @return Three floats representing the x, y, z hit point.
     * 
     */
    public float[] getHit() {
        return NativeEyePointeeHolder.getHit(getNative());
    }

    /**
     * Add a {@link GVREyePointee} to this holder
     * 
     * @param eyePointee
     *            The {@link GVREyePointee} to add
     * 
     */
    public void addPointee(GVREyePointee eyePointee) {
        pointees.add(eyePointee);
        NativeEyePointeeHolder.addPointee(getNative(), eyePointee.getNative());
    }

    /**
     * Add a Future {@link GVREyePointee} to this holder
     * 
     * @param eyePointee
     *            A Future {@link GVREyePointee}, probably from
     *            {@link GVRRenderData#getMeshEyePointee()}
     */
    public void addPointee(final Future<GVREyePointee> eyePointee) {
        // The Future<GVREyePointee> may well actually be a FutureWrapper, not a
        // 'real' Future
        if (eyePointee.isDone()) {
            addFutureEyePointee(eyePointee);
        } else {
            Threads.spawn(new Runnable() {

                @Override
                public void run() {
                    addFutureEyePointee(eyePointee);
                }
            });
        }
    }

    private void addFutureEyePointee(Future<GVREyePointee> eyePointee) {
        try {
            addPointee(eyePointee.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove a {@link GVREyePointee} from this holder.
     * 
     * No exception is thrown if the eye pointee is not held by this holder.
     * 
     * @param eyePointee
     *            The {@link GVREyePointee} to remove
     * 
     */
    public void removePointee(GVREyePointee eyePointee) {
        pointees.remove(eyePointee);
        NativeEyePointeeHolder.removePointee(getNative(),
                eyePointee.getNative());
    }
}

class NativeEyePointeeHolder {
    static native long ctor();

    static native boolean getEnable(long eyePointeeHolder);

    static native void setEnable(long eyePointeeHolder, boolean enable);

    static native float[] getHit(long eyePointeeHolder);

    static native void addPointee(long eyePointeeHolder, long eyePointee);

    static native void removePointee(long eyePointeeHolder, long eyePointee);
}
