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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.gearvrf.utility.Log;
import org.joml.Vector3f;

/**
 * Finds the scene objects that are hit by a ray.
 *
 * The picker can function in two modes. One way is to simply call its
 * static functions to make a single scan through the scene to determine
 * what is hit by the picking ray.
 * <p/>
 * The other way is to add the picker as a component to a scene object.
 * The picking ray is generated from the camera viewpoint
 * each frame. It's origin is the camera position and it's direction is
 * the camera forward look vector (what the user is looking at).
 * <p/>
 * For a {@linkplain GVRSceneObject scene object} to be pickable, it must have a
 * {@link GVRCollider} component attached to it that is enabled.
 * The picker "casts" a ray into the screen graph, and returns an array
 * containing all the collisions as instances of GVRPickedObject.
 * The picked object contains the collider instance hit, the distance from the
 * camera and the hit position.
 * <p/>
 * The picker maintains the list of currently picked objects
 * (which can be obtained with getPicked()) and continually updates it each frame.
 * <p/>
 * In this mode, when the ray from the scene object hits a pickable object,
 * the picker generates one or more pick events (IPickEvents interface)
 * which are sent the event receiver of the scene. These events can be
 * observed by listeners.
 * <ul>>
 * <li>onEnter(GVRSceneObject)  called when the pick ray enters a scene object.</li>
 * <li>onExit(GVRSceneObject)   called when the pick ray exits a scene object.</li>
 * <li>onInside(GVRSceneObject) called while the pick ray penetrates a scene object.</li>
 * <li>onPick(GVRPicker)        called when the set of picked objects changes.</li>
 * <li>onNoPick(GVRPicker)      called once when nothing is picked.</li>
 * </ul
 * @see IPickEvents
 * @see GVRSceneObject#attachCollider(GVRCollider)
 * @see GVRCollider
 * @see GVRCollider#setEnable(boolean)
 * @see GVRPickedObject
 */
public class GVRPicker extends GVRBehavior {
    private static final String TAG = Log.tag(GVRPicker.class);
    static private long TYPE_PICKMANAGER = newComponentType(GVRPicker.class);
    private Vector3f mRayOrigin = new Vector3f(0, 0, 0);
    private Vector3f mRayDirection = new Vector3f(0, 0, -1);
    private float[] mPickRay = new float[6];

    protected GVRScene mScene;
    protected GVRPickedObject[] mPicked = null;

    /**
     * Construct a picker which picks from a given scene.
     * Instantiating the picker will cause it to scan the scene
     * every frame and generate pick events based on the result.
     *
     * @param context context that owns the scene
     * @param scene scene containing the scene objects to pick from
     */
    public GVRPicker(GVRContext context, GVRScene scene)
    {
        super(context);
        mScene = scene;
        mType = getComponentType();
        startListening();
    }

    /**
     * Construct a picker which picks from a given scene
     * using a ray emanating from the specified scene object.
     * The picker will be attached to the scene object and
     * will scan the scene every frame and generate pick events.
     * <p>
     * This constructor is useful when you want to pick from the
     * viewpoint of a scene object. It will not generate any
     * pick events until after the picker has been attached
     * to the scene object.
     *
     * @param owner scene object to own the picker
     * @param scene scene containing the scene objects to pick from
     */
    public GVRPicker(GVRSceneObject owner, GVRScene scene)
    {
        super(owner.getGVRContext());
        mScene = scene;
        setPickRay(0, 0, 0, 0, 0, 1);
        owner.attachComponent(this);
    }

    static public long getComponentType() { return TYPE_PICKMANAGER; }

    /**
     * Get the current ray to use for picking.
     * <p/>
     * If the picker is attached to a scene object,
     * this ray is derived from the scene object's transform.
     * The origin of the ray is the translation component
     * of the total model matrix and the ray direction
     * is the forward look vector.
     * <p/>
     * If not attached to a scene object, the origin of the
     * ray is the position of the viewer and its direction
     * is where the viewer is looking.
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
     * <p/>
     * Each collision with an object is described as a
     * GVRPickedObject which contains the scene object
     * and collider hit, the distance from the camera
     * and the hit position in the coordinate system
     * of the collision geometry. The objects in the pick
     * list are sorted based on increasing distance
     * from the origin of the pick ray.
     * @return GVRPickedObject array with objects picked or null if nothing picked.
     * @see #doPick()
     * @see IPickEvents
     * @see #setPickRay(float, float, float, float, float, float)
     */
    public final GVRPickedObject[] getPicked()
    {
        return mPicked;
    }

    /**
     * Sets the origin and direction of the pick ray.
     *
     * @param ox    X coordinate of origin.
     * @param oy    Y coordinate of origin.
     * @param oz    Z coordinate of origin.
     * @param dx    X coordinate of ray direction.
     * @param dy    Y coordinate of ray direction.
     * @param dz    Z coordinate of ray direction.
     *
     * The coordinate system of the ray depends on the whether the
     * picker is attached to a scene object or not. When attached
     * to a scene object, the ray is in the coordinate system of
     * that object where (0, 0, 0) is the center of the scene object
     * and (0, 0, 1) is it's positive Z axis. If not attached to an
     * object, the ray is in the coordinate system of the scene's
     * main camera with (0, 0, 0) at the viewer and (0, 0, -1)
     * where the viewer is looking.
     *
     * @see #doPick()
     */
    public void setPickRay(float ox, float oy, float oz, float dx, float dy, float dz)
    {
        mRayOrigin.x = ox;
        mRayOrigin.y = oy;
        mRayOrigin.z = oz;
        mRayDirection.x = dx;
        mRayDirection.y = dy;
        mRayDirection.z = dz;
    }

    public void onDrawFrame(float frameTime)
    {
        if (isEnabled())
        {
            doPick();
        }
    }

    /**
     * Scans the scene graph to collect picked items
     * and generates appropriate pick events.
     * This function is called automatically by
     * the picker every frame.
     * @see IPickEvents
     * @see #pickObjects(GVRScene, float, float, float, float, float, float)
     */
    protected void doPick()
    {
        GVRSceneObject owner = getOwnerObject();
        GVRTransform trans = (owner != null) ? owner.getTransform() : null;
        GVRPickedObject[] picked = pickObjects(mScene, trans,
                mRayOrigin.x, mRayOrigin.y, mRayOrigin.z,
                mRayDirection.x, mRayDirection.y, mRayDirection.z);
        generatePickEvents(picked);
    }

    protected void generatePickEvents(GVRPickedObject[] picked)
    {
        boolean selectionChanged = false;

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
        // get the count of non null picked objects
        int pickedCount = 0;

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
            //increment the pick count
            pickedCount++;

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
            if (pickedCount > 0)
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
     * Tests the {@link GVRSceneObject} against the ray information passed to the function.
     *
     * @param sceneObject
     *            The {@link GVRSceneObject} to be tested.
     *
     * @param ox
     *            The x coordinate of the ray origin (in world coords).
     *
     * @param oy
     *            The y coordinate of the ray origin (in world coords).
     *
     * @param oz
     *            The z coordinate of the ray origin (in world coords).
     *
     * @param dx
     *            The x vector of the ray direction (in world coords).
     *
     * @param dy
     *            The y vector of the ray direction (in world coords).
     *
     * @param dz
     *            The z vector of the ray direction (in world coords).
     *
     * @return  a {@link GVRPicker.GVRPickedObject} containing the picking information
     *
     */
    public static final GVRPickedObject pickSceneObject(GVRSceneObject sceneObject, float ox, float oy, float oz, float dx,
                                                        float dy, float dz) {
        return NativePicker.pickSceneObject(sceneObject.getNative(), ox, oy, oz, dx, dy, dz);
    }

    /**
     * Tests the {@link GVRSceneObject} against the ray information passed to the function.
     *
     * @param sceneObject
     *            The {@link GVRSceneObject} to be tested.
     *
     * @return  a {@link GVRPicker.GVRPickedObject} containing the picking information
     *
     */
    public static final GVRPickedObject pickSceneObject(GVRSceneObject sceneObject) {
        GVRCameraRig cam = sceneObject.getGVRContext().getMainScene().getMainCameraRig();
        GVRTransform t = cam.getHeadTransform();
        float[] lookat = cam.getLookAt();
        return NativePicker.pickSceneObject(sceneObject.getNative(), t.getPositionX(), t.getPositionY(), t.getPositionZ(),
                lookat[0], lookat[1], lookat[2]);
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
     * @param readbackBuffer The readback buffer is a small optimization on this call. Instead of
     *                       creating a new float array every time this call is made, the
     *                       readback buffer allows the caller to forward a dedicated array that
     *                       can be populated by the native layer every time there is a
     *                       successful hit. Make use of the return value to know if the contents
     *                       of the buffer is valid or not. For multiple calls to this method a
     *                       {@link ByteBuffer} can be created once and used multiple times. Look
     *                       at the {@link SensorManager} class as an example of this methods use.
     *
     * @return <code>true</code> on a successful hit, <code>false</code> otherwise.
     */
    static final boolean pickSceneObjectAgainstBoundingBox(
            GVRSceneObject sceneObject, float ox, float oy, float oz, float dx,
            float dy, float dz, ByteBuffer readbackBuffer) {
        sFindObjectsLock.lock();
        try {
            return NativePicker.pickSceneObjectAgainstBoundingBox(
                    sceneObject.getNative(), ox, oy, oz, dx, dy, dz, readbackBuffer);
        } finally {
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
     * <p>
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
            final GVRPickedObject[] result = NativePicker.pickObjects(scene.getNative(), 0L, ox, oy, oz, dx, dy, dz);
            return result;
        } finally {
            sFindObjectsLock.unlock();
        }
    }

    /**
     * Casts a ray into the scene graph, and returns the objects it intersects.
     * <p/>
     * The ray is defined by its origin {@code [ox, oy, oz]} and its direction
     * {@code [dx, dy, dz]}. The ray is in the coordinate system of the
     * input transform, allowing it to be with respect to a scene object.
     *
     * <p>
     * The ray origin may be [0, 0, 0] and the direction components should be
     * normalized from -1 to 1: Note that the y direction runs from -1 at the
     * bottom to 1 at the top. To construct a picking ray originating at the
     * center of a scene object and pointing where that scene object looks,
     * attach the GVRPicker to the scene object and  pass (0, 0, 0) as
     * the ray origin and (0, 0, -1) for the direction.
     *
     * <p>
     * This method is thread safe because it guarantees that only
     * one thread at a time is doing a ray cast into a particular scene graph,
     * and it extracts the hit data during within its synchronized block. You
     * can then examine the return list without worrying about another thread
     * corrupting your hit data.
     * <p/>
     * Depending on the type of collider, that the hit location may not be exactly
     * where the ray would intersect the scene object itself. Rather, it is
     * where the ray intersects the collision geometry associated with the collider.
     *
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     * @param trans
     *            The {@link GVRTransform} establishing the coordinate system of the ray.
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
    public static final GVRPickedObject[] pickObjects(GVRScene scene, GVRTransform trans, float ox, float oy, float oz, float dx,
                                                      float dy, float dz) {
        sFindObjectsLock.lock();
        try {
            long nativeTrans = (trans != null) ? trans.getNative() : 0L;
            final GVRPickedObject[] result = NativePicker.pickObjects(scene.getNative(), nativeTrans, ox, oy, oz, dx, dy, dz);
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
            Log.d(TAG, "makeHit: cannot find collider for %x", colliderPointer);
            return null;
        }
        return new GVRPicker.GVRPickedObject(collider, new float[] { hitx, hity, hitz }, distance);
    }
    /**
     * Internal utility to help JNI add hit objects to the pick list. Specifically for MeshColliders with picking
     * for UV, Barycentric, and normal coordinates enabled
     */
    static GVRPickedObject makeHitMesh(long colliderPointer, float distance, float hitx, float hity, float hitz,
                                   int faceIndex, float barycentricx, float barycentricy, float barycentricz,
                                   float texu, float texv,  float normalx, float normaly, float normalz)
    {
        GVRCollider collider = GVRCollider.lookup(colliderPointer);
        if (collider == null)
        {
            Log.d(TAG, "makeHit: cannot find collider for %x", colliderPointer);
            return null;
        }
        return new GVRPicker.GVRPickedObject(collider, new float[] { hitx, hity, hitz }, distance, faceIndex,
                new float[] {barycentricx, barycentricy, barycentricz},
                new float[]{ texu, texv },
                new float[]{normalx, normaly, normalz});
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
     *         along with the hit location.
     *
     */
    public static final List<GVRPickedObject> findObjects(GVRScene scene) {
        return findObjects(scene, 0, 0, 0, 0, 0, -1.0f);
    }

    /**
     * The result of a pick request which hits an object.
     * <p/>
     * When a pick request is performed, each collision is
     * described as a GVRPickedObject.
     *
     * @since 1.6.6
     * @see GVRPicker#pickObjects(GVRScene, float, float, float, float, float, float)
     * @see GVRPicker#findObjects(GVRScene)
     */
    public static final class GVRPickedObject {
        public final GVRSceneObject hitObject;
        public final GVRCollider hitCollider;
        public final float[] hitLocation;
        public final float hitDistance;

        public final int faceIndex;
        public final float[] barycentricCoords;
        public final float[] textureCoords;
        public final float[] normalCoords;

        /**
         * Creates a new instance of {@link GVRPickedObject}.
         *
         * @param hitCollider
         *            The {@link GVRCollider} that the ray intersected.
         * @param hitDistance
         *            The distance from the origin if the ray.
         * @param hitLocation
         *            The hit location, as an [x, y, z] array.
         * @param faceIndex
         *            The index of the face intersected if a {@link GVRMeshCollider} was attached
         *            to the {@link GVRSceneObject}, -1 otherwise
         * @param barycentricCoords
         *            The barycentric coordinates of the hit location on the intersected face
         *            if a {@link GVRMeshCollider} was attached to the {@link GVRSceneObject},
         *            [ -1.0f, -1.0f, -1.0f ] otherwise.
         * @param textureCoords
         *            The texture coordinates of the hit location on the intersected face
         *            if a {@link GVRMeshCollider} was attached to the {@link GVRSceneObject},
         *            [ -1.0f, -1.0f ] otherwise.
         *
         * @see GVRPicker#pickObjects(GVRScene, float, float, float, float, float, float)
         * @see GVRCollider
         */
        public GVRPickedObject(GVRCollider hitCollider, float[] hitLocation, float hitDistance, int faceIndex,
                               float[] barycentricCoords, float[] textureCoords, float[] normalCoords) {
            hitObject = hitCollider.getOwnerObject();
            this.hitDistance = hitDistance;
            this.hitCollider = hitCollider;
            this.hitLocation = hitLocation;
            this.faceIndex = faceIndex;
            this.barycentricCoords = barycentricCoords;
            this.textureCoords = textureCoords;
            this.normalCoords = normalCoords;
        }

        public GVRPickedObject(GVRCollider hitCollider, float[] hitLocation, float hitDistance) {
            hitObject = hitCollider.getOwnerObject();
            this.hitDistance = hitDistance;
            this.hitCollider = hitCollider;
            this.hitLocation = hitLocation;
            this.faceIndex = -1;
            this.barycentricCoords = null;
            this.textureCoords = null;
            this.normalCoords = null;
        }

        public GVRPickedObject(GVRSceneObject hitObject, float[] hitLocation) {
            this.hitObject = hitObject;
            this.hitLocation = hitLocation;
            this.hitDistance = -1;
            this.hitCollider = null;
            this.faceIndex = -1;
            this.barycentricCoords = null;
            this.textureCoords = null;
            this.normalCoords = null;
        }

        /**
         * The {@link GVRSceneObject} that the ray intersected.
         *
         * This is the owner of the collider hit.
         *
         * @return scene object hit
         * @see GVRComponent#getOwnerObject()
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
         * @return A copy of the hit result
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


        /**
         * The barycentric coordinates of the hit location on the collided face
         * This will return -1 if the faceIndex isn't calculated
         */
        public int getFaceIndex() {
            return faceIndex;
        }

        /**
         * The barycentric coordinates of the hit location on the collided face
         * Returns null if the coordinates haven't been calculated.
         */
        public float[] getBarycentricCoords() {
            if(barycentricCoords != null)
                return Arrays.copyOf(barycentricCoords, barycentricCoords.length);
            else
                return null;
        }

        /**
         * The UV texture coordinates of the hit location on the mesh
         * Returns null if the coordinates haven't been calculated.
         */
        public float[] getTextureCoords() {
            if(textureCoords != null)
                return Arrays.copyOf(textureCoords, textureCoords.length);
            else
                return null;
        }

        /**
         * The normalized surface normal of the hit location on the mesh (in local coordinates).
         * Returns null if the coordinates haven't been calculated.
         */
        public float[] getNormalCoords() {
            if(normalCoords != null)
                return Arrays.copyOf(normalCoords, normalCoords.length);
            else
                return null;
        }
    }

    static final ReentrantLock sFindObjectsLock = new ReentrantLock();
}

final class NativePicker {
    static native long[] pickScene(long scene, float ox, float oy, float oz,
                                   float dx, float dy, float dz);

    static native GVRPicker.GVRPickedObject[] pickObjects(long scene, long transform, float ox, float oy, float oz,
                                                          float dx, float dy, float dz);

    static native GVRPicker.GVRPickedObject pickSceneObject(long sceneObject, float ox, float oy, float oz,
                                                            float dx, float dy, float dz);

    static native GVRPicker.GVRPickedObject[] pickVisible(long scene);

    static native boolean pickSceneObjectAgainstBoundingBox(long sceneObject,
                                                            float ox, float oy, float oz, float dx, float dy, float dz, ByteBuffer readbackBuffer);
}
