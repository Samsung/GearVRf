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

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Finds the scene objects that are within a view frustum.
 *
 * The picker can function in two modes. One way is to simply call its
 * static functions to make a single scan through the scene to determine
 * what is within the view frustum.
 *
 * The other way is to add the picker as a component to a scene object
 * and specify the view frustum dimensions. The viewpoint of the frustum
 * is the center of the scene object. The view direction is the forward
 * direction of the scene object. The frustum will pick what a camera
 * attached to the scene object with that view frustum would see.
 *
 * For a {@linkplain GVRSceneObject scene object} to be pickable, it must have a
 * {@link GVRCollider} component attached to it that is enabled.
 * The picker returns an array containing all the collisions as instances of GVRPickedObject.
 * The picked object contains the collider instance, the distance from the
 * origin of the view frustum and the center of the object.
 *
 * The picker maintains the list of currently
 * picked objects which can be obtained with getPicked() and continually
 * updates it each frame. When a pickable object is inside the view frustum,
 * the picker generates one or more pick events (IPickEvents interface)
 * which are sent the event receiver of the scene. These events can be
 * observed by listeners.
 *  - onEnter(GVRSceneObject)  called when the scene object enters the frustum.
 *  - onExit(GVRSceneObject)   called when the scene object exits the frustum.
 *  - onInside(GVRSceneObject) called while the scene object is inside the frustum.
 *  - onPick(GVRPicker)        called when the set of picked objects changes.
 *  - onNoPick(GVRPicker)      called once when nothing is picked.
 *
 * @see IPickEvents
 * @see GVRSceneObject#attachComponent(GVRComponent)
 * @see GVRCollider
 * @see GVRComponent#setEnable(boolean)
 * @see org.gearvrf.GVRPicker.GVRPickedObject
 */
public class GVRFrustumPicker extends GVRPicker {
    protected FrustumIntersection mCuller;
    protected float[] mProjMatrix = null;
    protected Matrix4f mProjection = null;

    /**
     * Construct a picker which picks from a given scene.
     * @param context context that owns the scene
     * @param scene scene containing the scene objects to pick from
     */
    public GVRFrustumPicker(GVRContext context, GVRScene scene)
    {
        super(context, scene);
        setFrustum(90.0f, 1.0f, 0.1f, 1000.0f);
    }

    /**
     * Set the view frustum to pick against from the minimum and maximum corners.
     * The viewpoint of the frustum is the center of the scene object
     * the picker is attached to. The view direction is the forward
     * direction of that scene object. The frustum will pick what a camera
     * attached to the scene object with that view frustum would see.
     * If the frustum is not attached to a scene object, it defaults to
     * the view frustum of the main camera of the scene.
     *
     * @param frustum array of 6 floats as follows:
     *                frustum[0] = left corner of frustum
     *                frustum[1] = bottom corner of frustum
     *                frustum[2] = front corner of frustum (near plane)
     *                frustum[3] = right corner of frustum
     *                frustum[4] = top corner of frustum
     *                frustum[5 = back corner of frustum (far plane)
     */
    public void setFrustum(float[] frustum)
    {
        Matrix4f projMatrix = new Matrix4f();
        projMatrix.setFrustum(frustum[0], frustum[3], frustum[1], frustum[4], frustum[2], frustum[5]);
        setFrustum(projMatrix);
    }

    /**
     * Set the view frustum to pick against from the field of view, aspect
     * ratio and near, far clip planes. The viewpoint of the frustum
     * is the center of the scene object the picker is attached to.
     * The view direction is the forward direction of that scene object.
     * The frustum will pick what a camera attached to the scene object
     * with that view frustum would see. If the frustum is not attached
     * to a scene object, it defaults to the view frustum of the main camera of the scene.
     *
     * @param fovy  vertical field of view in degrees
     * @param aspect aspect ratio (width / height)

     */
    public void setFrustum(float fovy, float aspect, float znear, float zfar)
    {
        Matrix4f projMatrix = new Matrix4f();
        projMatrix.perspective((float) Math.toRadians(fovy), aspect, znear, zfar);
        setFrustum(projMatrix);
    }

    /**
     * Set the view frustum to pick against from the given projection  matrix.
     *
     * If the projection matrix is null, the picker will revert to picking
     * objects that are visible from the viewpoint of the scene's current camera.
     * If a matrix is given, the picker will pick objects that are visible
     * from the viewpoint of it's owner the given projection matrix.
     *
     * @param projMatrix 4x4 projection matrix or null
     * @see GVRScene#setPickVisible(boolean)
     */
    public void setFrustum(Matrix4f projMatrix)
    {
        if (projMatrix != null)
        {
            if (mProjMatrix == null)
            {
                mProjMatrix = new float[16];
            }
            mProjMatrix = projMatrix.get(mProjMatrix, 0);
            mScene.setPickVisible(false);
            if (mCuller != null)
            {
                mCuller.set(projMatrix);
            }
            else
            {
                mCuller = new FrustumIntersection(projMatrix);
            }
        }
        mProjection = projMatrix;
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
     * the picker if it is attached to a scene object.
     * You can instantiate the picker and not attach
     * it to a scene object. In this case you must
     * manually set the pick ray and call doPick()
     * to generate the pick events.
     * @see IPickEvents
     * @see GVRFrustumPicker#pickVisible(GVRScene)
     */
    public void doPick()
    {
        GVRSceneObject owner = getOwnerObject();
        GVRPickedObject[] picked = pickVisible(mScene);

        if (mProjection != null)
        {
            Matrix4f view_matrix;
            if (owner != null)
            {
                view_matrix = owner.getTransform().getModelMatrix4f();
            }
            else
            {
                view_matrix = mScene.getMainCameraRig().getHeadTransform().getModelMatrix4f();
            }
            view_matrix.invert();

            for (int i = 0; i < picked.length; ++i)
            {
                GVRPickedObject hit = picked[i];

                if (hit != null)
                {
                    GVRSceneObject sceneObj = hit.hitObject;
                    GVRSceneObject.BoundingVolume bv = sceneObj.getBoundingVolume();
                    Vector4f center = new Vector4f(bv.center.x, bv.center.y, bv.center.z, 1);
                    Vector4f p = new Vector4f(bv.center.x, bv.center.y, bv.center.z + bv.radius, 1);
                    float radius;

                    center.mul(view_matrix);
                    p.mul(view_matrix);
                    p.sub(center, p);
                    p.w = 0;
                    radius = p.length();
                    boolean pointIn = mCuller.testPoint(center.x, center.y, center.z);
                    if (!mCuller.testSphere(center.x, center.y, center.z, radius))
                    {
                        picked[i] = null;
                    }
                }
            }
        }
        generatePickEvents(picked);
    }

    /**
     * Returns the list of colliders attached to scene objects that are
     * visible from the viewpoint of the camera.
     *
     * <p>
     * This method is thread safe because it guarantees that only
     * one thread at a time is picking against particular scene graph,
     * and it extracts the hit data during within its synchronized block. You
     * can then examine the return list without worrying about another thread
     * corrupting your hit data.
     *
     * The hit location returned is the world position of the scene object center.
     *
     * @param scene
     *            The {@link GVRScene} with all the objects to be tested.
     *
     * @return A list of {@link org.gearvrf.GVRPicker.GVRPickedObject}, sorted by distance from the
     *         camera rig. Each {@link org.gearvrf.GVRPicker.GVRPickedObject} contains the scene object
     *         which owns the {@link GVRCollider} along with the hit
     *         location and distance from the camera.
     *
     * @since 1.6.6
     */
    public static final GVRPickedObject[] pickVisible(GVRScene scene) {
        sFindObjectsLock.lock();
        try {
            final GVRPickedObject[] result = NativePicker.pickVisible(scene.getNative());
            return result;
        } finally {
            sFindObjectsLock.unlock();
        }
    }
}
