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
    /**
     * Constructor
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVREyePointeeHolder(GVRContext gvrContext) {
        super(gvrContext, NativeEyePointeeHolder.ctor());
    }

    private GVREyePointeeHolder(GVRContext gvrContext, long ptr) {
        super(gvrContext, ptr);
    }

    static GVREyePointeeHolder factory(GVRContext gvrContext, long ptr) {
        GVRHybridObject wrapper = wrapper(ptr);
        return wrapper == null ? new GVREyePointeeHolder(gvrContext, ptr)
                : (GVREyePointeeHolder) wrapper;
    }

    @Override
    protected final boolean registerWrapper() {
        return true;
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
        return NativeEyePointeeHolder.getEnable(getPtr());
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
        NativeEyePointeeHolder.setEnable(getPtr(), enable);
    }

    /**
     * Get the x, y, z of the point of where the hit occurred in model space
     * 
     * @return Three floats representing the x, y, z hit point.
     * 
     */
    public float[] getHit() {
        return NativeEyePointeeHolder.getHit(getPtr());
    }

    /**
     * Add a {@link GVREyePointee} to this holder
     * 
     * @param eyePointee
     *            The {@link GVREyePointee} to add
     * 
     */
    public void addPointee(GVREyePointee eyePointee) {
        NativeEyePointeeHolder.addPointee(getPtr(), eyePointee.getPtr());
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
        NativeEyePointeeHolder.removePointee(getPtr(), eyePointee.getPtr());
    }
}

class NativeEyePointeeHolder {
    public static native long ctor();

    public static native boolean getEnable(long eyePointeeHolder);

    public static native void setEnable(long eyePointeeHolder, boolean enable);

    public static native float[] getHit(long eyePointeeHolder);

    public static native void addPointee(long eyePointeeHolder, long eyePointee);

    public static native void removePointee(long eyePointeeHolder,
            long eyePointee);
}
