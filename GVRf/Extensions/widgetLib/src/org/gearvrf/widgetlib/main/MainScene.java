package org.gearvrf.widgetlib.main;

import org.gearvrf.widgetlib.log.Log;

import static org.gearvrf.widgetlib.main.Utility.equal;

import org.gearvrf.widgetlib.widget.GroupWidget;
import org.gearvrf.widgetlib.widget.Widget;
import org.gearvrf.widgetlib.widget.Widget.ChildInfo;
import org.gearvrf.widgetlib.widget.Widget.Visibility;

import org.gearvrf.widgetlib.widget.layout.basic.AbsoluteLayout;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import static org.gearvrf.utility.Log.tag;

import org.joml.Quaternionf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Encapsulates common operations on the main scene, the main camera rig, and
 * the left and right cameras. A {@link GVRSceneObject} instance is used as the
 * base of the scene graph; this allows the entire scene graph to be scaled
 * without requiring any support from other objects in the scene graph.
 */
public class MainScene {
    /**
     * Specifies the left camera of the camera rig
     */
    public static final int LEFT_CAMERA = 1;
    /**
     * Specifies the right camera of the camera rig
     */
    public static final int RIGHT_CAMERA = 2;
    /**
     * Specifies the both right and left cameras of the rig
     */
    public static final int BOTH_CAMERAS = LEFT_CAMERA | RIGHT_CAMERA;
    /**
     * Specifies the horizon distance behind all objects
     */
    public static final float HORIZON = 1500f;

    /**
     * Implement this interface and
     * {@linkplain MainScene#addOnScaledListener(OnScaledListener) register} to
     * be notified of {@linkplain MainScene#setScale(float) changes to the
     * scene's scale}. Generally speaking this is not necessary: scene objects
     * that have been added to either the
     * {@linkplain MainScene#addSceneObject(GVRSceneObject) scene} or the
     * {@linkplain MainScene#addChildObjectToCamera(GVRSceneObject) camera} will
     * already be scaled as needed.
     */
    public interface OnScaledListener {
        /**
         * Called when the {@link MainScene} scale has
         * {@linkplain MainScene#setScale(float) changed}.
         *
         * @param scale The MainScene's new scale.
         */
        public void onScaled(final float scale);
    }

    /**
     * Implement this interface and
     * {@linkplain MainScene#addOnFrontRotationChangedListener(OnFrontRotationChangedListener)
     * register} to be notified of
     * {@linkplain MainScene#updateFrontFacingRotation(float) changes to the scene's
     * "front"}.
     */
    public interface OnFrontRotationChangedListener {
        /**
         * Called when the {@link MainScene} "front" rotation has
         * {@linkplain MainScene#updateFrontFacingRotation(float) changed}.
         *
         * @param mainScene   The {@code MainScene} instance.
         * @param newRotation The new rotation value.
         * @param oldRotation Previous rotation value.
         */
        public void onFrontRotationChanged(MainScene mainScene, float newRotation, float oldRotation);
    }

    /**
     * Construct a MainScene instance.
     * <p>
     * The underlying GVRScene is obtained using
     * {@link GVRContext#getMainScene()} and frustum culling is
     * {@linkplain GVRScene#setFrustumCulling(boolean) enabled}.
     *
     * @param gvrContext A valid GVRContext instance.
     */
    public MainScene(final GVRContext gvrContext) {
        mContext = gvrContext;
        mSceneRootObject = new GVRSceneObject(gvrContext);
        mMainCameraRootObject = new GVRSceneObject(gvrContext);
        mLeftCameraRootObject = new GVRSceneObject(gvrContext);
        mRightCameraRootObject = new GVRSceneObject(gvrContext);

        mSceneRootWidget = new RootWidget(mSceneRootObject);
        mSceneRootWidget.setName(TAG);
        mMainCameraRootWidget = new GroupWidget(gvrContext,
                mMainCameraRootObject);
        mMainCameraRootWidget.applyLayout(new AbsoluteLayout());
        mLeftCameraRootWidget = new GroupWidget(gvrContext,
                mLeftCameraRootObject);
        mLeftCameraRootWidget.applyLayout(new AbsoluteLayout());
        mRightCameraRootWidget = new GroupWidget(gvrContext,
                mRightCameraRootObject);
        mRightCameraRootWidget.applyLayout(new AbsoluteLayout());

        mMainScene = mContext.getMainScene();
        mMainScene.setFrustumCulling(true);
        mMainScene.addSceneObject(mSceneRootObject);

        getMainCameraRig().addChildObject(mMainCameraRootObject);
        getLeftCamera().addChildObject(mLeftCameraRootObject);
        getRightCamera().addChildObject(mRightCameraRootObject);
        mSceneRootObject.setName(TAG);
        onFirstStep();
    }

    /**
     * Calling on first {@link org.gearvrf.GVRMain#onStep} to process first rendering
     * @return true if it is first rendering, otherwise - false
     */
    private boolean onFirstStep() {
        if (!mFirstStepDone) {
            Log.d(TAG, "onFirstStep()");
            mFirstStepDone = true;
            float yaw = getMainCameraRigYaw();
            Log.d(TAG, "Update frontFacingRotation at start: %f", yaw);
            updateFrontFacingRotation(yaw);

            // Now we will do our first layout
            mSceneRootWidget.onFirstStep();

            return true;
        }

        return false;
    }

    /**
     * Register a listener for {@link OnScaledListener#onScaled(float)
     * onScaled()} notifications.
     * <p>
     * Newly registered listeners are immediately called with the current
     * scaling.
     *
     * @param listener An implementation of {@link OnScaledListener}.
     * @return {@code True} if the listener is not already added, {@code false}
     * if it has already been registered.
     */
    public boolean addOnScaledListener(OnScaledListener listener) {
        final boolean added = mOnScaledListeners.add(listener);
        if (added) {
            listener.onScaled(mSceneRootObject.getTransform().getScaleX());
        }
        return added;
    }

    /**
     * Unregister a listener for {@link OnScaledListener#onScaled(float)
     * onScaled()} notifications.
     *
     * @param listener An implementation of {@link OnScaledListener}.
     * @return {@code True} if the listener is registered and was removed,
     * {@code false} if the listener was not previously registered.
     */
    public boolean removeOnScaledListener(OnScaledListener listener) {
        return mOnScaledListeners.remove(listener);
    }

    /**
     * Register a listener for
     * {@link OnFrontRotationChangedListener#onFrontRotationChanged(MainScene, float, float)
     * onFrontRotationChanged()} notifications.
     *
     * @param listener An implementation of {@link OnFrontRotationChangedListener}.
     * @return {@code True} if the listener is not already added, {@code false}
     * if it has already been registered.
     */
    public boolean addOnFrontRotationChangedListener(OnFrontRotationChangedListener listener) {
        return mOnFrontRotationChangedListeners.add(listener);
    }

    /**
     * Unregister a listener for
     * {@link OnFrontRotationChangedListener#onFrontRotationChanged(MainScene, float, float)
     * onFrontRotationChanged()} notifications.
     *
     * @param listener An implementation of {@link OnFrontRotationChangedListener}.
     * @return {@code True} if the listener is registered and was removed,
     * {@code false} if the listener was not previously registered.
     */
    public boolean removeOnFrontRotationChangedListener(OnFrontRotationChangedListener listener) {
        return mOnFrontRotationChangedListeners.remove(listener);
    }

    /**
     * Add a scene object to the scene.
     *
     * @param sceneObject The {@link GVRSceneObject} to add.
     */
    public void addSceneObject(final GVRSceneObject sceneObject) {
        mSceneRootObject.addChildObject(sceneObject);
    }

    /**
     * Remove a scene object from the scene.
     *
     * @param sceneObject The {@link GVRSceneObject} to remove.
     */
    public void removeSceneObject(final GVRSceneObject sceneObject) {
        mSceneRootObject.removeChildObject(sceneObject);
    }

    /**
     * Add a scene object to the scene.
     *
     * @param sceneObject The {@link GVRSceneObject} to add.
     */
    public void addSceneObject(final Widget sceneObject) {
        mSceneRootWidget.addChild(sceneObject);
    }

    /**
     * Remove a scene object from the scene.
     *
     * @param sceneObject The {@link GVRSceneObject} to remove.
     */
    public void removeSceneObject(final Widget sceneObject) {
        mSceneRootWidget.removeChild(sceneObject);
    }

    /**
     * @param includeHidden Pass {@code false} to only count children whose
     *                      {@link Widget#setVisibility(Visibility) visibility} is
     *                      {@link Visibility#VISIBLE}.
     * @return The {@link Widget#getChildCount(boolean) count} of {@link Widget}
     * children of the scene's root.
     */
    public int getChildWidgetCount(final boolean includeHidden) {
        return mSceneRootWidget.getChildCount(includeHidden);
    }

    /**
     * @return A recursive count of the {@link GVRSceneObject} children of the
     * scene's root.
     */
    public int getChildSceneObjectCount() {
        return getSceneObjectChildCount(mSceneRootObject);
    }

    private int getSceneObjectChildCount(final GVRSceneObject root) {
        int count = 0;
        for (GVRSceneObject child : root.getChildren()) {
            ++count;
            count += getSceneObjectChildCount(child);
        }
        return count;
    }

    /**
     * @param includeHidden Pass {@code false} to only count children whose
     *                      {@link Widget#setVisibility(Visibility) visibility} is
     *                      {@link Visibility#VISIBLE}.
     * @return A {@linkplain Widget#getChildInfo(boolean) list} of
     * {@linkplain ChildInfo} for the scene's root.
     */
    public List<Widget.ChildInfo> getChildInfo(final boolean includeHidden) {
        return mSceneRootWidget.getChildInfo(includeHidden);
    }

    /**
     * Adjusts the clipping distance for the left, right, and center cameras.
     */
    public void adjustClippingDistanceForAllCameras() {
        adjustClippingDistance(getRightCamera());
        adjustClippingDistance(getLeftCamera());
        adjustClippingDistance(getCenterCamera());
    }

    /**
     * Convenience method to add a scene object to both the left and right
     * cameras.
     *
     * @param child The {@link GVRSceneObject} to add.
     * @see #addChildObjectToCamera(GVRSceneObject, int)
     */
    public void addChildObjectToCamera(final GVRSceneObject child) {
        addChildObjectToCamera(child, BOTH_CAMERAS);
    }

    /**
     * Adds a scene object to one or both of the left and right cameras.
     *
     * @param child  The {@link GVRSceneObject} to add.
     * @param camera {@link #LEFT_CAMERA} or {@link #RIGHT_CAMERA}; these can be
     *               or'd together to add {@code child} to both cameras.
     */
    public void addChildObjectToCamera(final GVRSceneObject child, int camera) {
        switch (camera) {
            case LEFT_CAMERA:
                mLeftCameraRootObject.addChildObject(child);
                break;
            case RIGHT_CAMERA:
                mRightCameraRootObject.addChildObject(child);
                break;
            default:
                mMainCameraRootObject.addChildObject(child);
                break;
        }
    }

    /**
     * Convenience method to remove a scene object from both the left and right
     * cameras.
     *
     * @param child The {@link GVRSceneObject} to remove.
     * @see #removeChildObjectFromCamera(GVRSceneObject, int)
     */
    public void removeChildObjectFromCamera(final GVRSceneObject child) {
        removeChildObjectFromCamera(child, BOTH_CAMERAS);
    }

    /**
     * Removes a scene object from one or both of the left and right cameras.
     *
     * @param child  The {@link GVRSceneObject} to remove.
     * @param camera {@link #LEFT_CAMERA} or {@link #RIGHT_CAMERA}; these can be
     *               or'd together to remove {@code child} from both cameras.
     */
    public void removeChildObjectFromCamera(final GVRSceneObject child,
                                            int camera) {
        switch (camera) {
            case LEFT_CAMERA:
                mLeftCameraRootObject.removeChildObject(child);
                break;
            case RIGHT_CAMERA:
                mRightCameraRootObject.removeChildObject(child);
                break;
            default:
                mMainCameraRootObject.removeChildObject(child);
                break;
        }
    }

    /**
     * Convenience method to add a scene object to both the left and right
     * cameras.
     *
     * @param child The {@link Widget} to add.
     * @see #addChildObjectToCamera(Widget, int)
     */
    public void addChildObjectToCamera(final Widget child) {
        addChildObjectToCamera(child, BOTH_CAMERAS);
    }

    /**
     * Adds a scene object to one or both of the left and right cameras.
     *
     * @param child  The {@link Widget} to add.
     * @param camera {@link #LEFT_CAMERA} or {@link #RIGHT_CAMERA}; these can be
     *               or'd together to add {@code child} to both cameras.
     */
    public void addChildObjectToCamera(final Widget child, int camera) {
        switch (camera) {
            case LEFT_CAMERA:
                mLeftCameraRootWidget.addChild(child);
                break;
            case RIGHT_CAMERA:
                mRightCameraRootWidget.addChild(child);
                break;
            default:
                mMainCameraRootWidget.addChild(child);
                break;
        }
    }

    /**
     * Convenience method to remove a scene object from both the left and right
     * cameras.
     *
     * @param child The {@link Widget} to remove.
     * @see #removeChildObjectFromCamera(Widget, int)
     */
    public void removeChildObjectFromCamera(final Widget child) {
        removeChildObjectFromCamera(child, BOTH_CAMERAS);
    }

    /**
     * Removes a scene object from one or both of the left and right cameras.
     *
     * @param child  The {@link Widget} to remove.
     * @param camera {@link #LEFT_CAMERA} or {@link #RIGHT_CAMERA}; these can be
     *               or'd together to remove {@code child} from both cameras.
     */
    public void removeChildObjectFromCamera(final Widget child, int camera) {
        switch (camera) {
            case LEFT_CAMERA:
                mLeftCameraRootWidget.removeChild(child);
                break;
            case RIGHT_CAMERA:
                mRightCameraRootWidget.removeChild(child);
                break;
            default:
                mMainCameraRootWidget.removeChild(child);
                break;
        }
    }

    /**
     * @return The camera's absolute pitch in degrees.
     */
    public float getMainCameraRigPitch() {
        final float[] vector = this.getMainCameraRig().getLookAt();
        final float x = vector[0];
        final float y = vector[1];
        final float z = vector[2];
        final double distance = Math.sqrt(z * z + x * x);
        final float pitch = (float) Math.toDegrees(Math.asin(y / distance));
        return pitch;
    }

    /**
     * @return The camera's absolute yaw in degrees.
     */
    public float getMainCameraRigYaw() {
        GVRTransform transform = getMainCameraRig().getHeadTransform();

        float yaw = transform.getRotationYaw();
        float z = getMainCameraRig().getLookAt()[2];
        if (z == 0) {
            // Use the 'z' component of the rig's 'up' vector instead. Per
            // http://3dengine.org/Right-up-back_from_modelview, the 'up'
            // vector is the second row of the matrix. Since OpenGL matrices
            // are column-major, the second row is elements 1, 5, 9, 13; 1,
            // 5, and 9 are the up vector, 13 is the 'y' component of the
            // "encoded" camera center vector.
            z = transform.getModelMatrix()[9];
        }
        if (z < 0) {
            int flip = yaw < 0 ? -1 : 1;
            yaw = flip * (90 + (90 - Math.abs(yaw)));
        }
        return yaw;
    }

    /**
     * Apply the necessary rotation to the transform so that it is in front of
     * the camera.
     *
     * @param transform The transform to modify.
     */
    public void rotateToFaceCamera(final GVRTransform transform) {
        //see http://stackoverflow.com/questions/5782658/extracting-yaw-from-a-quaternion
        final GVRTransform t = getMainCameraRig().getHeadTransform();
        final Quaternionf q = new Quaternionf(0, t.getRotationY(), 0, t.getRotationW()).normalize();

        transform.rotateWithPivot(q.w, q.x, q.y, q.z, 0, 0, 0);
    }

    /**
     * Apply the necessary rotation to the transform so that it is in front of
     * the camera. The actual rotation is performed not using the yaw angle but
     * using equivalent quaternion values for better accuracy. But the yaw angle
     * is still returned for backward compatibility.
     *
     * @param widget The transform to modify.
     * @return The camera's yaw in degrees.
     */
    public float rotateToFaceCamera(final Widget widget) {
        final float yaw = getMainCameraRigYaw();
        GVRTransform t = getMainCameraRig().getHeadTransform();
        widget.rotateWithPivot(t.getRotationW(), 0, t.getRotationY(), 0, 0, 0, 0);
        return yaw;
    }

    private float frontFacingRotation;

    /**
     * get front facing rotation
     * @return rotation
     */
    public float getFrontFacingRotation() {
        return frontFacingRotation;
    }

    /**
     * Set new front facing rotation
     * @param rotation
     */
    public void updateFrontFacingRotation(float rotation) {
        if (!Float.isNaN(rotation) && !equal(rotation, frontFacingRotation)) {
            final float oldRotation = frontFacingRotation;
            frontFacingRotation = rotation % 360;
            for (OnFrontRotationChangedListener listener : mOnFrontRotationChangedListeners) {
                try {
                    listener.onFrontRotationChanged(this, frontFacingRotation, oldRotation);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e, "updateFrontFacingRotation()");
                }
            }
        }
    }

    /**
     * Rotate transform to make it facing to the front of the scene
     * @param transform
     */
    public void rotateToFront(final GVRTransform transform) {
        transform.rotateByAxisWithPivot(-frontFacingRotation + 180, 0, 1, 0, 0, 0, 0);
    }

    /**
     * Rotate widget to make it facing to the front of the scene
     * @param widget rotating widget
     */
    public void rotateToFront(final Widget widget) {
        widget.rotateByAxisWithPivot(-frontFacingRotation + 180, 0, 1, 0, 0, 0, 0);
    }

    /**
     * Rotate root widget to make it facing to the front of the scene
     */
    public void rotateToFront() {
        GVRTransform transform = mSceneRootObject.getTransform();
        transform.setRotation(1, 0, 0, 0);
        transform.rotateByAxisWithPivot(-frontFacingRotation + 180, 0, 1, 0, 0, 0, 0);
    }

    /**
     * Set the {@link org.gearvrf.GVRCameraRig.GVRCameraRigType type} of the camera rig.
     *
     * @param cameraRigType
     *            The rig {@link org.gearvrf.GVRCameraRig.GVRCameraRigType type}.
     */
    public void setCameraRigType(final int cameraRigType) {
        getMainCameraRig().setCameraRigType(cameraRigType);
    }

    /**
     * Get the {@link org.gearvrf.GVRCameraRig.GVRCameraRigType type} of the camera rig.
     *
     * @return The rig {@link org.gearvrf.GVRCameraRig.GVRCameraRigType type}.
     */
    public int getCameraRigType() {
        return getMainCameraRig().getCameraRigType();
    }

    /**
     * Scale all widgets in Main Scene hierarchy
     * @param scale
     */
    public void setScale(final float scale) {
        if (equal(mScale, scale) != true) {
            Log.d(TAG, "setScale(): old: %.2f, new: %.2f", mScale, scale);
            mScale = scale;
            setScale(mSceneRootObject, scale);
            setScale(mMainCameraRootObject, scale);
            setScale(mLeftCameraRootObject, scale);
            setScale(mRightCameraRootObject, scale);
            for (OnScaledListener listener : mOnScaledListeners) {
                try {
                    listener.onScaled(scale);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e, "setScale()");
                }
            }
        }
    }

    private GVRPerspectiveCamera getCenterCamera() {
        return getMainCameraRig().getCenterCamera();
    }

    private GVRPerspectiveCamera getLeftCamera() {
        return (GVRPerspectiveCamera) getMainCameraRig().getLeftCamera();
    }

    private GVRPerspectiveCamera getRightCamera() {
        return (GVRPerspectiveCamera) getMainCameraRig().getRightCamera();
    }

    private GVRCameraRig getMainCameraRig() {
        return mMainScene.getMainCameraRig();
    }

    private void setScale(final GVRSceneObject sceneObject, final float scale) {
        sceneObject.getTransform().setScale(scale, scale, scale);
    }

    @SuppressWarnings("unused")
    private static void adjustClippingDistance(GVRPerspectiveCamera camera) {
        float distance = camera.getFarClippingDistance();
        final float horizon = HORIZON;
        if (horizon > 500 && distance <= 1000) {
            camera.setFarClippingDistance(distance * 2);
        } else if (horizon <= 500 && distance > 1000) {
            camera.setFarClippingDistance(distance / 2);
        }
    }

    private class RootWidget extends GroupWidget {
        private String TAG = tag(RootWidget.class);

        public RootWidget(GVRSceneObject sceneObject) {
            super(sceneObject.getGVRContext(), sceneObject);
            applyLayout(new AbsoluteLayout());
        }

        public void onFirstStep() {
            mLayoutRunnable = mMainLayoutRunnable;
            requestLayout();
        }

        @Override
        public void requestLayout() {
            // Top of the food chain; time to actually lay stuff out
            super.requestLayout();
            Log.v(Log.SUBSYSTEM.LAYOUT, TAG, "requestLayout(%s): root layout requested; posting", getName());

            runOnGlThread(mLayoutRunnable);
        }

        @Override
        public boolean isInLayout() {
            return mInLayout;
        }

        @Override
        protected void requestInnerLayout(Widget widget) {
            if (widget != this) {
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "add requestInnerLayout %s", widget.getName());
                mInnerLayoutRequests.add(widget);
            }
        }

        private final Runnable mMainLayoutRunnable = new Runnable() {
            @Override
            public void run() {
                Log.v(Log.SUBSYSTEM.LAYOUT, TAG, "requestLayout(): running layout from MainScene");

                mInLayout = true;
                layout();
                mInLayout = false;

                Log.v(Log.SUBSYSTEM.LAYOUT, TAG, "requestLayout(): MainScene layout DONE");

                WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnGlThread(new Runnable() {
                            @Override
                            public void run() {
                                checkInnerLayoutRequest();
                            }
                        });
                    }
                });
            }
        };

        private Runnable mLayoutRunnable = new Runnable() {
            @Override
            public void run() {
                // This is the start-up placeholder Runnable
                Log.d(TAG, "requestLayout(): MainScene placeholder runnable");
            }
        };

        private void checkInnerLayoutRequest() {
            Set<Widget> copySet = new HashSet<>(mInnerLayoutRequests);
            mInnerLayoutRequests.clear();

            for (Widget widget: copySet) {
                Log.d(Log.SUBSYSTEM.WIDGET, TAG, "send requestInnerLayout %s", widget.getName());
                widget.requestLayout();
            }
        }

        private final Set<Widget> mInnerLayoutRequests = new HashSet<>();

        private volatile boolean mInLayout;
    }

    private final GVRContext mContext;
    private final GVRScene mMainScene;
    private final GVRSceneObject mSceneRootObject;
    private final GVRSceneObject mMainCameraRootObject;
    private final GVRSceneObject mLeftCameraRootObject;
    private final GVRSceneObject mRightCameraRootObject;
    private final RootWidget mSceneRootWidget;
    private final GroupWidget mMainCameraRootWidget;
    private final GroupWidget mLeftCameraRootWidget;
    private final GroupWidget mRightCameraRootWidget;
    private final Set<OnScaledListener> mOnScaledListeners = new HashSet<>();
    private final Set<OnFrontRotationChangedListener> mOnFrontRotationChangedListeners = new HashSet<>();
    private float mScale = 1;
    private boolean mFirstStepDone = false;

    private static final String TAG = MainScene.class.getSimpleName();

    public void bindShaders() {
    }
}
