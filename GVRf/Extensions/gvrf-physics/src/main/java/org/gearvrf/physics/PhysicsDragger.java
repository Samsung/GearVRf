package org.gearvrf.physics;

import org.gearvrf.GVRBoxCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.utility.Log;

/**
 * This is a helper class to drag a rigid body.
 * It start/stop the drag of the internal virtual pivot object that is used by the physics world
 * as the pivotB of the point joint that is connected to the rigid bodyA that is the target
 * of the dragging action.
 *
 * [Target object with RigidBody]-----(point joint connect to) --->[Pivot object
 *                                                                  that is been dragged by cursor]
 */
class PhysicsDragger {
    private static final String TAG = PhysicsDragger.class.getSimpleName();

    private static final float COLLIDER_HALF_EXT_X = 1f;
    private static final float COLLIDER_HALF_EXT_Y = 1f;
    private static final float COLLIDER_HALF_EXT_Z = 1f;

    private final Object mLock = new Object();

    private final GVRContext mContext;

    private GVRSceneObject mPivotObject = null;
    private GVRSceneObject mDragMe = null;
    private GVRCursorController mCursorController = null;

    PhysicsDragger(GVRContext gvrContext) {
        mContext = gvrContext;

        gvrContext.getInputManager().selectController(new GVRInputManager.ICursorControllerSelectListener() {
            @Override
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                synchronized (mLock) {
                    mCursorController = newController;
                }
            }
        });
    }

    private static GVRSceneObject onCreatePivotObject(GVRContext gvrContext) {
        GVRSceneObject virtualObj = new GVRSceneObject(gvrContext);
        GVRBoxCollider collider = new GVRBoxCollider(gvrContext);
        collider.setHalfExtents(COLLIDER_HALF_EXT_X, COLLIDER_HALF_EXT_Y, COLLIDER_HALF_EXT_Z);
        virtualObj.attachComponent(collider);

        return virtualObj;
    }

    /**
     * Start the drag of the pivot object.
     *
     * @param dragMe Scene object with a rigid body.
     * @param relX rel position in x-axis.
     * @param relY rel position in y-axis.
     * @param relZ rel position in z-axis.
     *
     * @return Pivot instance if success, otherwise returns null.
     */
    public GVRSceneObject startDrag(GVRSceneObject dragMe, float relX, float relY, float relZ) {
        synchronized (mLock) {
            if (mCursorController == null) {
                Log.w(TAG, "Physics drag failed: Cursor controller not found!");
                return null;
            }

            if (mDragMe != null) {
                Log.w(TAG, "Physics drag failed: Previous drag wasn't finished!");
                return null;
            }

            if (mPivotObject == null) {
                mPivotObject = onCreatePivotObject(mContext);
            }

            mDragMe = dragMe;

            GVRTransform t = dragMe.getTransform();

            /* It is not possible to drag a rigid body directly, we need a pivot object.
            We are using the pivot object's position as pivot of the dragging's physics constraint.
             */
            mPivotObject.getTransform().setPosition(t.getPositionX() + relX,
                    t.getPositionY() + relY, t.getPositionZ() + relZ);

            mCursorController.startDrag(mPivotObject);
        }

        return mPivotObject;
    }

    /**
     * Stops the drag of the pivot object.
     */
    public void stopDrag() {
        if (mDragMe == null)
            return;

        mDragMe = null;
        mCursorController.stopDrag();
    }
}
