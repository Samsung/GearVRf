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
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Finds the scene objects that are hit by a ray.
 *
 * The picker can function in two modes. One way is to simply call its
 * static functions to make a single scan through the scene to determine
 * what is hit by the picking ray.
 *
 * The other way is to add the picker as a component to a scene object.
 * The picking ray is generated from the camera viewpoint
 * each frame. It's origin is the camera position and it's direction is
 * the camera forward look vector (what the user is looking at).
 *  
 * For a {@linkplain GVRSceneObject scene object} to be pickable, it must have a
 * {@link GVRCollider} component attached to it that is enabled. 
 * The picker "casts" a ray into the screen graph, and returns an array
 * containing all the collisions as instances of GVRPickedObject.
 * The picked object contains the collider instance hit, the distance from the
 * camera and the hit position.
 * 
 * If it is attached to a scene object, the picker maintains the list of currently
 * picked objects which can be obtains with getPicked() and continually
 * updates it each frame. (If it is not attached to a scene object,
 * you must manually call doPick() to cause pick events to be generated.)
 *
 * In this mode, when the ray from the scene object hits a pickable object,
 * the picker generates one or more pick events (IPickEvents interface)
 * which are sent the event receiver of the scene. These events can be
 * observed by listeners.
 *  - onEnter(GVRSceneObject)  called when the pick ray enters a scene object.
 *  - onExit(GVRSceneObject)   called when the pick ray exits a scene object.
 *  - onInside(GVRSceneObject) called while the pick ray penetrates a scene object.
 *  - onPick(GVRPicker)        called when the set of picked objects changes.
 *  - onNoPick(GVRPicker)      called once when nothing is picked.
 *
 * @see IPickEvents
 * @see GVRSceneObject.attachCollider
 * @see GVRCollider
 * @see GVRCollider.setEnable
 * @see GVRPickedObject
 */
public class GVRPicker extends GVRBehavior {
    private static final String TAG = Log.tag(GVRPicker.class);
    static private long TYPE_PICKMANAGER = newComponentType(GVRPicker.class);
    private Vector3f mRayOrigin = new Vector3f(0, 0, 0);
    private Vector3f mRayDirection = new Vector3f(0, 0, -1);
    private float[] mPickRay = new float[6];
    private boolean mHasChanged;
    private GVRScene mScene;
    private GVRPickedObject[] mPicked = null;

    /**
     * Construct a picker which picks from a given scene.
     * @param context context that owns the scene
     * @param scene scene containing the scene objects to pick from
     */
    public GVRPicker(GVRContext context, GVRScene scene)
    {
        super(context);
        mScene = scene;
        mHasChanged = true;
        mType = getComponentType();
    }

    static public long getComponentType() { return TYPE_PICKMANAGER; }

    /**
     * Get the current ray to use for picking.
     * 
     * If the picker is attached to a scene object,
     * this ray is derived from the scene object's transform.
     * The origin of the ray is the translation component
     * of the total model matrix and the ray direction
     * is the forward look vector.
     * 
     * @return pick ray
     */
    public float[] getPickRay()
    {
        mPickRay[0] = mRayOrigin.x;
        mPickRay[1] = mRayOrigin.y;
        mPickRay[2] = mRayOrigin.z;
        mPickRay[3] = mRayDirection.x;
        mPickRay[4] = mRayDirection.y;
        mPickRay[5] = mRayDirection.z;
        return mPickRay;
    }
    
    /**
     * Gets the current pick list.
     * 
     * Each collision with an object is described as a
     * GVRPickedObject which contains the scene object
     * and collider hit, the distance from the camera
     * and the hit position in the coordinate system
     * of the collision geometry. The objects in the pick
     * list are sorted based on increasing distance
     * from the origin of the pick ray.
     * @return GVRPickedObject array with objects picked or null if nothing picked.
     * @see doPick
     * @see IPickEvents
     * @see setPickRay
     */
    public final GVRPickedObject[] getPicked()
    {
        return mPicked;
    }
    /*
     * Sets the origin and direction of the pick ray.
     * 
     * If the picker is not attached to a scene object but you
     * still want to get pick events, you must set the pick
     * ray manually with this function and call {@link doPick}.
     * 
     * @param ox    X coordinate of origin in world coordinates.
     * @param oy    Y coordinate of origin in world coordinates.
     * @param oz    Z coordinate of origin in world coordinates.
     * @param dx    X coordinate of ray direction in world coordinates.
     * @param dy    Y coordinate of ray direction in world coordinates.
     * @param dz    Z coordinate of ray direction in world coordinates.
     * @see doPick
     */
    public void setPickRay(float ox, float oy, float oz, float dx, float dy, float dz)
    {
        mRayOrigin.x = ox;
        mRayOrigin.y = oy;
        mRayOrigin.z = oz;
        mRayDirection.x = dx;
        mRayDirection.y = dy;
        mRayDirection.z = dz;
        mHasChanged = true;
    }
    
    public void onDrawFrame(float frameTime)
    {
        if (getOwnerObject() != null)
        {
            GVRTransform trans = getOwnerObject().getTransform();
            Matrix4f worldmtx = trans.getModelMatrix4f();
            worldmtx.getTranslation(mRayOrigin);
            mRayDirection.x = 0;
            mRayDirection.y = 0;
            mRayDirection.z = -1;
            mRayDirection.mulDirection(worldmtx);
            mHasChanged = true;
        }
        if (mHasChanged)
        {
            if (isEnabled())
            {
                doPick();
            }
            mHasChanged = false;
        }        
    }
    
    /**
     * Scans the scene graph to collect picked items
     * and generates appropriate pick events.
     * This function is called automatically by
     * the picker if it is attached to a scene object.
     * You can instantiate the picker and not attach
     * it to a scene object. In this case you must
     * manually set the pick ray and call doPick()
     * to generate the pick events.
     * @see IPickEvents 
     * @see pickObjects
     */
    public void doPick()
    {
        boolean selectionChanged = false;
        GVRPickedObject[] picked = pickObjects(mScene,
                mRayOrigin.x, mRayOrigin.y, mRayOrigin.z,
                mRayDirection.x, mRayDirection.y, mRayDirection.z);
        /*
         * Send "onExit" events for colliders that were picked but
         * are not picked anymore.
         */
        if (mPicked != null)
        {
            for (GVRPickedObject collision : mPicked)
            {
                if (collision == null)
                {
                    continue;
                }
                GVRCollider collider = collision.hitCollider;
                if (!hasCollider(picked, collider))
                {
                    getGVRContext().getEventManager().sendEvent(mScene, IPickEvents.class, "onExit", collider.getOwnerObject());                   
                    selectionChanged = true;
                }
            }
        }
        /*
         * Send "onEnter" events for colliders that were picked for the first time.
         * Send "onInside" events for colliders that were already picked.
         */
        for (GVRPickedObject collision : picked)
        {
            if (collision == null)
            {
                continue;
            }
            GVRCollider collider = collision.hitCollider;
            if (!hasCollider(mPicked, collider))
            {
                getGVRContext().getEventManager().sendEvent(mScene, IPickEvents.class, "onEnter", collider.getOwnerObject(), collision);                   
                selectionChanged = true;
            }
            else
            {
                getGVRContext().getEventManager().sendEvent(mScene, IPickEvents.class, "onInside", collider.getOwnerObject(), collision);                   
            }
        }
        if (selectionChanged)
        {
            if ((picked != null) && (picked.length > 0))
            {
                mPicked = picked;
                getGVRContext().getEventManager().sendEvent(mScene, IPickEvents.class, "onPick", this);
            }
            else
            {
                mPicked = null;
                getGVRContext().getEventManager().sendEvent(mScene, IPickEvents.class, "onNoPick", this);                
            }
        }
    }

    private boolean hasCollider(GVRPickedObject[] pickList, GVRCollider findme)
    {
        if (pickList == null)
        {
            return false;
        }
        for (GVRPickedObject hit : pickList)
        {
            if ((hit != null) && (hit.hitCollider == findme))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Casts a ray into the scene graph, and returns the GVREyePointeeHolders it intersects.
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
     * Note: this function only returns GVREyePointeeHolder colliders
     * and is deprecated in favor of pickObject which returns all colliders.
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
     * @return The {@linkplain GVRCollider colliders}
     *         penetrated by the ray, sorted by distance from the camera rig.
     *         Use {@link GVRCollider#getOwnerObject()} to get the
     *         corresponding scene objects.
     * @deprecated use pickObjects instead
     */
    public static final GVREyePointeeHolder[] pickScene(GVRScene scene, float ox, float oy, float oz, float dx,
            float dy, float dz) {
        sFindObjectsLock.lock();
        try {
            final long[] ptrs = NativePicker.pickScene(scene.getNative(), ox, oy, oz, dx, dy, dz);
            final ArrayList<GVREyePointeeHolder> colliders = new ArrayList<GVREyePointeeHolder>(ptrs.length);

            for (int i = 0, length = ptrs.length; i < length; ++i) {
                final GVRCollider collider = GVRCollider.lookup(ptrs[i]);
                if ((null != collider) && GVREyePointeeHolder.class.isAssignableFrom(collider.getClass()))
                {
                    colliders.add((GVREyePointeeHolder) collider);
                }
            }
            GVREyePointeeHolder[] holders = new GVREyePointeeHolder[colliders.size()];
            return colliders.toArray(holders);
        }
        finally {
            sFindObjectsLock.unlock();
        }
    }

    /**
     * Tests the {@link GVRSceneObject}s contained within scene against the
     * camera rig's lookat vector.
     * 
     * Note: this function only returns GVREyePointeeHolder colliders
     * and is deprecated in favor of pickObject which returns all colliders.
     * 
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * 
     * @return the {@link GVREyePointeeHolders which are penetrated by the
     *         picking ray. They are sorted by distance from the camera.
     * 
     * @deprecated use pickObjects instead
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
        sFindObjectsLock.lock();        
        try {
            return NativePicker.pickSceneObjectAgainstBoundingBox(
                    sceneObject.getNative(), ox, oy, oz, dx, dy, dz);
        }
        finally {
            sFindObjectsLock.unlock();            
        }
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
     * This method is thread safe because it guarantees that only
     * one thread at a time is doing a ray cast into a particular scene graph,
     * and it extracts the hit data during within its synchronized block. You
     * can then examine the return list without worrying about another thread
     * corrupting your hit data.
     * 
     * Depending on the type of collider, that the hit location may not be exactly
     * where the ray would intersect the scene object itself. Rather, it is
     * where the ray intersects the collision geometry associated with the collider.
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
     *         camera rig. Each {@link GVRPickedObject} contains the scene object
     *         which owns the {@link GVRCollider} along with the hit
     *         location and distance from the camera. 
     * 
     * @since 1.6.6
     */
    public static final GVRPickedObject[] pickObjects(GVRScene scene, float ox, float oy, float oz, float dx,
            float dy, float dz) {
        sFindObjectsLock.lock();        
        try {            
            final GVRPickedObject[] result = NativePicker.pickObjects(scene.getNative(), ox, oy, oz, dx, dy, dz);
            return result;
        } finally {
            sFindObjectsLock.unlock();
        }
    }

    /**
     * Casts a ray into the scene graph, and returns the objects it intersects.
     * 
     * @deprecated use GVRPickedObject[] pickObjects
     */
    public static final List<GVRPickedObject> findObjects(GVRScene scene, float ox, float oy, float oz, float dx,
            float dy, float dz) {
        return Arrays.asList(pickObjects(scene, ox, oy, oz, dx, dy, dz));
    }
    
    /**
     * Internal utility to help JNI add hit objects to the pick list.
     */
    static GVRPickedObject makeHit(long colliderPointer, float distance, float hitx, float hity, float hitz)
    {
       GVRCollider collider = GVRCollider.lookup(colliderPointer);
       if (collider == null)
       {
           Log.d("Picker", "makeHit: cannot find collider for %p", colliderPointer);
           return null;
       }
       return new GVRPicker.GVRPickedObject(collider, new float[] { hitx, hity, hitz }, distance);
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
     * The result of a pick request which hits an object.
     * 
     * When a pick request is performed, each collision is
     * described as a GVRPickedObject.
     * 
     * @since 1.6.6
     * @see GVRPicker.pickScene
     * @see GVRPicker.findObjects
     * @see GVRPicker.pickObjects
     */
    public static final class GVRPickedObject {
        public final GVRSceneObject hitObject;
        public final GVRCollider hitCollider;
        public final float[] hitLocation;
        public final float hitDistance;

        /**
         * Creates a new instance of {@link GVRPickedObject}.
         *
         * @param hitCollider
         *            The {@link GVRCollider} that the ray intersected.
         * @param hitDistance
         *            The distance from the origin if the ray.
         * @param hitLocation
         *            The hit location, as an [x, y, z] array.
         *
         * @see GVRPicker.pickScene
         * @see GVRPicker.pickObjects
         * @see GVRCollider
         */
        public GVRPickedObject(GVRCollider hitCollider, float[] hitLocation, float hitDistance) {
            hitObject = hitCollider.getOwnerObject();
            this.hitDistance = hitDistance;
            this.hitCollider = hitCollider;
            this.hitLocation = hitLocation;
        }

        /**
         * The {@link GVRSceneObject} that the ray intersected.
         *
         * This is the owner of the collider hit.
         *
         * @return scene object hit
         * @see GVRComponent.getOwnerObject
         */
        public GVRSceneObject getHitObject() {
            return hitObject;
        }

        /**
         * The {@link GVRCollider} that the ray intersected.
         * 
         * @return collider hit
         */
        public GVRCollider getHitCollider() {
            return hitCollider;
        }
        
        /**
         * The hit location, as an [x, y, z] array.
         * 
         * @return A copy of the {@link GVREyePointeeHolder#getHit()} result:
         *         changing the result will not change the
         *         {@link GVRPickedObject picked object's} hit data.
         */
        public float[] getHitLocation() {
            return Arrays.copyOf(hitLocation, hitLocation.length);
        }
        
        /**
         * The distance from the origin of the pick ray
         */
        public float getHitDistance() {
            return hitDistance;
        }

        /** The x coordinate of the hit location */
        public float getHitX() {
            return hitLocation[0];
        }

        /** The y coordinate of the hit location */
        public float getHitY() {
            return hitLocation[1];
        }

        /** The z coordinate of the hit location */
        public float getHitZ() {
            return hitLocation[2];
        }
    }

    static final ReentrantLock sFindObjectsLock = new ReentrantLock();
}

final class NativePicker {
    static native long[] pickScene(long scene, float ox, float oy, float oz,
            float dx, float dy, float dz);

    static native GVRPicker.GVRPickedObject[] pickObjects(long scene, float ox, float oy, float oz,
            float dx, float dy, float dz);

    static native float pickSceneObject(long sceneObject, long cameraRig);

    static native float[] pickSceneObjectAgainstBoundingBox(long sceneObject,
            float ox, float oy, float oz, float dx, float dy, float dz);
}

