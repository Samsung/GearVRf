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
import java.util.concurrent.Future;

import org.gearvrf.utility.Threads;

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
 *
 * @deprecated use GVRMeshCollider or GVRSphereCollider
 */
public class GVREyePointeeHolder extends GVRCollider {
    private final List<GVRCollider> pointees = new ArrayList<GVRCollider>();

    static GVRCollider lookup(GVRContext gvrContext, long nativePointer) {
        return GVRCollider.lookup(nativePointer);
    }

    /**
     * Constructor
     * 
     * @param gvrContext
     *            Current {@link GVRContext}
     */
    public GVREyePointeeHolder(GVRContext gvrContext) {
        this(gvrContext, NativeColliderGroup.ctor());
    }

    public GVREyePointeeHolder(GVRContext gvrContext, GVRSceneObject owner) {
        this(gvrContext, NativeColliderGroup.ctor());
        setOwnerObject(owner);
    }
    
    private GVREyePointeeHolder(GVRContext gvrContext, long nativePointer) {
        super(gvrContext, nativePointer);
    }        
        
    
    /**
     * Is this holder enabled?
     * 
     * If this holder is disabled, then picking will <b>not</b> occur against
     * its {@link GVREyePointee}s.
     * @return true if enabled, false otherwise.
     * @deprecated use GVRComponent.isEnabled()
     */
    public boolean getEnable() {
        return super.isEnabled();
    }


    /**
     * Add a {@link GVREyePointee} to this holder
     * 
     * @param eyePointee
     *            The {@link GVREyePointee} to add
     * @deprecated use addCollider
     */
    public void addPointee(GVREyePointee eyePointee) {
        pointees.add(eyePointee);
        NativeColliderGroup.addCollider(getNative(), eyePointee.getNative());
    }

    /**
     * Add a {@link GVREyePointee} to this holder
     * 
     * @param eyePointee
     *            The {@link GVREyePointee} to add
     * 
     */
    public void addCollider(GVRCollider eyePointee) {
        pointees.add(eyePointee);
        NativeColliderGroup.addCollider(getNative(), eyePointee.getNative());
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
        NativeColliderGroup.removeCollider(getNative(),
                eyePointee.getNative());
    }

    /**
     * Get the x, y, z of the point of where the hit occurred in model space
     * 
     * @return Three floats representing the x, y, z hit point.
     * @see getHitDistance
     */
    public float[] getHit()
    {
        return NativeColliderGroup.getHit(getNative());
    }
    
    /**
     * Remove a {@link GVRCollider} from this holder.
     * 
     * No exception is thrown if the collider is not held by this holder.
     * 
     * @param eyePointee
     *            The {@link GVRCollider} to remove
     * 
     */
    public void removeCollider(GVRCollider eyePointee) {
        pointees.remove(eyePointee);
        NativeColliderGroup.removeCollider(getNative(),
                eyePointee.getNative());
    }
}

class NativeColliderGroup {
    static native long ctor();

    static native float[] getHit(long eyePointeeHolder);
    
    static native void addCollider(long eyePointeeHolder, long eyePointee);

    static native void removeCollider(long eyePointeeHolder, long eyePointee);
}
