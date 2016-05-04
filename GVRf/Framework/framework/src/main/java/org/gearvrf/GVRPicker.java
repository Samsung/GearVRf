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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.gearvrf.utility.Log;

/**
 * Finds the scene objects you are pointing to.
 * 
 * For a {@linkplain GVRSceneObject scene object} to be pickable, it must have a
 * {@link GVREyePointeeHolder}
 * {@link GVRSceneObject#attachEyePointeeHolder(GVREyePointeeHolder) attached}
 * and {@linkplain GVREyePointeeHolder#setEnable(boolean) enabled.}
 * 
 * <p>
 * This picker "casts" a ray into the screen graph, and returns each enabled
 * {@link GVREyePointeeHolder} that the ray hits, sorted by distance from the
 * camera. Use {@link GVREyePointeeHolder#getOwnerObject()} to map the eye
 * pointee holder to a scene object.
 */
public class GVRPicker {
    private static final String TAG = Log.tag(GVRPicker.class);

    private GVRPicker() {
    }

    /**
     * Casts a ray into the scene graph, and returns the objects it intersects.
     * 
     * The ray is defined by its origin {@code [ox, oy, oz]} and its direction
     * {@code [dx, dy, dz]}.
     * 
     * <p>
     * The ray origin may be [0, 0, 0] and the direction components should be
     * normalized from -1 to 1: Note that the y direction runs from -1 at the
     * bottom to 1 at the top. To construct a picking ray originating at the
     * user's head and pointing into the scene along the camera lookat vector,
     * pass in 0, 0, 0 for the origin and 0, 0, -1 for the direction.
     * 
     * <p>
     * <em>Note:</em> The {@linkplain GVREyePointeeHolder#getHit() hit location}
     * is stored in the native eye pointee holder during the ray casting
     * operation: <em>It is only valid until the next ray cast operation.</em>
     * That is, either the {@linkplain #pickScene(GVRScene) short pickScene()},
     * or the
     * {@linkplain #pickScene(GVRScene, float, float, float, float, float, float)
     * long pickScene()}, or
     * {@linkplain #findObjects(GVRScene, float, float, float, float, float, float)
     * findObjects()} calls will invalidate previous hit data. There are two
     * ways to avoid getting invalid hit data:
     * <ul>
     * <li>Use the high-level
     * {@linkplain #findObjects(GVRScene, float, float, float, float, float, float)
     * findObjects()} method, which returns a list of {@link GVRPickedObject}:
     * each picked object pairs a {@link GVRSceneObject} with the hit data.
     * <li>Write your code so that you never call ray casting operation until
     * you have retrieved the previous operations hit data. (This is easy, if
     * you only ever do picking from the GL thread. It's significantly harder if
     * you are using multiple threads.)
     * </ul>
     * 
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * 
     * @param ox
     *            The x coordinate of the ray origin.
     * 
     * @param oy
     *            The y coordinate of the ray origin.
     * 
     * @param oz
     *            The z coordinate of the ray origin.
     * 
     * @param dx
     *            The x vector of the ray direction.
     * 
     * @param dy
     *            The y vector of the ray direction.
     * 
     * @param dz
     *            The z vector of the ray direction.
     * 
     * @return The {@linkplain GVREyePointeeHolder eye pointee holders}
     *         penetrated by the ray, sorted by distance from the camera rig.
     *         Use {@link GVREyePointeeHolder#getOwnerObject()} to get the
     *         corresponding scene objects.
     * 
     */
    public static final GVREyePointeeHolder[] pickScene(GVRScene scene, float ox, float oy, float oz, float dx,
            float dy, float dz) {
        sFindObjectsLock.lock();
        try {
            final long[] ptrs = NativePicker.pickScene(scene.getNative(), ox, oy, oz,
                    dx, dy, dz);
            final ArrayList<GVREyePointeeHolder> eyePointeeHolders = new ArrayList<GVREyePointeeHolder>(ptrs.length);
            final GVRContext gvrContext = scene.getGVRContext();

            for (int i = 0, length = ptrs.length; i < length; ++i) {
                Log.d(TAG, "pickScene(): ptrs[%d] = %x", i, ptrs[i]);
                final GVREyePointeeHolder holder = GVREyePointeeHolder.lookup(gvrContext, ptrs[i]);
                Log.d(TAG, "pickScene(): eyePointeeHolders[%d] = %s", i, holder);
                if (null != holder) {
                    eyePointeeHolders.add(holder);
                }
            }

            GVREyePointeeHolder[] result = new GVREyePointeeHolder[eyePointeeHolders.size()];
            return eyePointeeHolders.toArray(result);
        } finally {
            sFindObjectsLock.unlock();
        }
    }

    /**
     * Tests the {@link GVRSceneObject}s contained within scene against the
     * camera rig's lookat vector.
     * 
     * <p>
     * <em>Note:</em> The {@linkplain GVREyePointeeHolder#getHit() hit location}
     * is stored in the native eye pointee holder during the ray casting
     * operation: <em>It is only valid until the next ray cast operation.</em>
     * That is, either the {@linkplain #pickScene(GVRScene) short pickScene()},
     * or the
     * {@linkplain #pickScene(GVRScene, float, float, float, float, float, float)
     * long pickScene()}, or
     * {@linkplain #findObjects(GVRScene, float, float, float, float, float, float)
     * findObjects()} calls will invalidate previous hit data. There are two
     * ways to avoid getting invalid hit data:
     * <ul>
     * <li>Use the high-level
     * {@linkplain #findObjects(GVRScene, float, float, float, float, float, float)
     * findObjects()} method, which returns a list of {@link GVRPickedObject}:
     * each picked object pairs a {@link GVRSceneObject} with the hit data.
     * <li>Write your code so that you never call ray casting operation until
     * you have retrieved the previous operations hit data. (This is easy, if
     * you only ever do picking from the GL thread. It's significantly harder if
     * you are using multiple threads.)
     * </ul>
     * 
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * 
     * @return the {@link GVREyePointeeHolder}s which are penetrated by the
     *         picking ray. The holders are sorted by distance from the camera
     *         rig.
     * 
     */
    public static final GVREyePointeeHolder[] pickScene(GVRScene scene) {
        return pickScene(scene, 0, 0, 0, 0, 0, -1.0f);
    }

    /**
     * Tests the {@link GVRSceneObject} against the camera rig's lookat vector.
     * 
     * @param sceneObject
     *            The {@link GVRSceneObject} to be tested.
     * 
     * @param cameraRig
     *            The {@link GVRCameraRig} to use for ray testing.
     * 
     * @return the distance from the camera rig. It returns positive infinity if
     *         the cameraRig is not pointing to the sceneObject.
     * 
     */
    public static final float pickSceneObject(GVRSceneObject sceneObject,
            GVRCameraRig cameraRig) {
        return NativePicker.pickSceneObject(sceneObject.getNative(),
                cameraRig.getNative());
    }

    /**
     * 
     * Tests the {@link GVRSceneObject} against the specified ray.
     * 
     * The ray is defined by its origin {@code [ox, oy, oz]} and its direction
     * {@code [dx, dy, dz]}.
     * 
     * <p>
     * The ray origin may be [0, 0, 0] and the direction components should be
     * normalized from -1 to 1: Note that the y direction runs from -1 at the
     * bottom to 1 at the top.
     * 
     * @param sceneObject
     *            The {@link GVRSceneObject} to be tested.
     * 
     * @param ox
     *            The x coordinate of the ray origin.
     * 
     * @param oy
     *            The y coordinate of the ray origin.
     * 
     * @param oz
     *            The z coordinate of the ray origin.
     * 
     * @param dx
     *            The x vector of the ray direction.
     * 
     * @param dy
     *            The y vector of the ray direction.
     * 
     * @param dz
     *            The z vector of the ray direction.
     * 
     * @return The coordinates of the hit point if successful, <code>null</code>
     *         otherwise.
     */
    static final float[] pickSceneObjectAgainstBoundingBox(
            GVRSceneObject sceneObject, float ox, float oy, float oz, float dx,
            float dy, float dz) {
        return NativePicker.pickSceneObjectAgainstBoundingBox(
                sceneObject.getNative(), ox, oy, oz, dx, dy, dz);
    }

    /**
     * Casts a ray into the scene graph, and returns the objects it intersects.
     * 
     * The ray is defined by its origin {@code [ox, oy, oz]} and its direction
     * {@code [dx, dy, dz]}.
     * 
     * <p>
     * The ray origin may be [0, 0, 0] and the direction components should be
     * normalized from -1 to 1: Note that the y direction runs from -1 at the
     * bottom to 1 at the top. To construct a picking ray originating at the
     * user's head and pointing into the scene along the camera lookat vector,
     * pass in 0, 0, 0 for the origin and 0, 0, -1 for the direction.
     * 
     * <p>
     * This method is higher-level and easier to use than
     * {@link #pickScene(GVRScene, float, float, float, float, float, float)
     * pickScene():} Not only does it return the hit scene object (not its
     * holder) and the hit location directly, it is thread safe in a way that
     * the lower-level methods are not: The
     * {@linkplain GVREyePointeeHolder#getHit() hit location} is stored in the
     * native eye pointee holder during the ray casting operation and it is only
     * valid until the next ray cast operation. This method guarantees that only
     * one thread at a time is doing a ray cast into a particular scene graph,
     * and it extracts the hit data during within its synchronized block. You
     * can then examine the return list without worrying about another thread
     * corrupting your hit data.
     * 
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * 
     * @param ox
     *            The x coordinate of the ray origin.
     * 
     * @param oy
     *            The y coordinate of the ray origin.
     * 
     * @param oz
     *            The z coordinate of the ray origin.
     * 
     * @param dx
     *            The x vector of the ray direction.
     * 
     * @param dy
     *            The y vector of the ray direction.
     * 
     * @param dz
     *            The z vector of the ray direction.
     * @return A list of {@link GVRPickedObject}, sorted by distance from the
     *         camera rig. Each {@link GVRPickedObject} contains the object
     *         within the {@link GVREyePointeeHolder} along with the hit
     *         location. (Note that the hit location is actually the point where
     *         the cast ray intersected the holder's axis-aligned bounding box,
     *         which may not be exactly where the ray would intersect the scene
     *         object itself.)
     * 
     * @since 1.6.6
     */
    public static final List<GVRPickedObject> findObjects(GVRScene scene, float ox, float oy, float oz, float dx,
            float dy, float dz) {
        sFindObjectsLock.lock();
        try {
            final GVRContext gvrContext = scene.getGVRContext();

            final long[] pointers = NativePicker.pickScene(scene.getNative(), ox, oy, oz, dx, dy, dz);
            final List<GVRPickedObject> result = new ArrayList<GVRPickedObject>(pointers.length);
            for (final long pointer : pointers) {
                final GVREyePointeeHolder holder = GVREyePointeeHolder.lookup(gvrContext, pointer);
                if (null != holder) {
                    result.add(new GVRPickedObject(holder.getOwnerObject(), holder.getHit()));
                }
            }
            return result;
        } finally {
            sFindObjectsLock.unlock();
        }
    }

    /**
     * Tests the {@link GVRSceneObject}s contained within scene against the
     * camera rig's lookat vector.
     * 
     * <p>
     * This method uses higher-level function
     * {@linkplain #findObjects(GVRScene, float, float, float, float, float, float)
     * findObjects()} internally.
     * 
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * 
     * @return A list of {@link GVRPickedObject}, sorted by distance from the
     *         camera rig. Each {@link GVRPickedObject} contains the object
     *         within the {@link GVREyePointeeHolder} along with the hit
     *         location.
     * 
     */
    public static final List<GVRPickedObject> findObjects(GVRScene scene) {
        return findObjects(scene, 0, 0, 0, 0, 0, -1.0f);
    }

    /**
     * The result of a
     * {@link GVRPicker#findObjects(GVRScene, float, float, float, float, float, float)
     * findObjects()} call.
     * 
     * @since 1.6.6
     */
    public static class GVRPickedObject {
        private final GVRSceneObject mHitObject;
        private final float[] mHitLocation;

        /**
         * Creates a new instance of {@link GVRPickedObject}.
         *
         * @param hitObject
         *            The {@link GVRSceneObject} that the ray intersected.
         * @param hitLocation
         *            The hit location, as an [x, y, z] array.
         */
        public GVRPickedObject(GVRSceneObject hitObject, float[] hitLocation) {
            mHitObject = hitObject;
            mHitLocation = hitLocation;
        }

        /**
         * The {@link GVRSceneObject} within the eye pointee holder's bounding
         * box - the object that the ray intersected.
         * 
         * @return {@link GVREyePointeeHolder#getOwnerObject()}
         */
        public GVRSceneObject getHitObject() {
            return mHitObject;
        }

        /**
         * The hit location, as an [x, y, z] array.
         * 
         * @return A copy of the {@link GVREyePointeeHolder#getHit()} result:
         *         changing the result will not change the
         *         {@link GVRPickedObject picked object's} hit data.
         */
        public float[] getHitLocation() {
            return Arrays.copyOf(mHitLocation, mHitLocation.length);
        }

        /** The x coordinate of the hit location */
        public float getHitX() {
            return mHitLocation[0];
        }

        /** The y coordinate of the hit location */
        public float getHitY() {
            return mHitLocation[1];
        }

        /** The z coordinate of the hit location */
        public float getHitZ() {
            return mHitLocation[2];
        }
    }

    static final ReentrantLock sFindObjectsLock = new ReentrantLock();
}

final class NativePicker {
    static native long[] pickScene(long scene, float ox, float oy, float oz,
            float dx, float dy, float dz);

    static native float pickSceneObject(long sceneObject, long cameraRig);

    static native float[] pickSceneObjectAgainstBoundingBox(long sceneObject,
            float ox, float oy, float oz, float dx, float dy, float dz);
}
