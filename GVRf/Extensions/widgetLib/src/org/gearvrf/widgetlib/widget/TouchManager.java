package org.gearvrf.widgetlib.widget;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.Selector;

import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRSceneObject;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

@Deprecated
public class TouchManager {

    /**
     * Widgets indicate their willingness to manage touch through the implementing {@link OnTouch}
     * interface.
     */

    public interface OnTouch {
        /**
         * To determine if a sceneObject is touchable.
         *
         * @param sceneObject
         * @param coords      GVRF raw coordinates
         * @return True, when event has been handled & no further processing
         * needed. False, when event was intercepted and may need future
         * processing
         */
        boolean touch(GVRSceneObject sceneObject, final float[] coords);

        /**
         * To determine if a sceneObject is processing back key.
         *
         * @param sceneObject
         * @param coords      GVRF raw coordinates
         * @return True, when back key event has been handled & no further processing
         * needed. False, when event was intercepted and may need future
         * processing
         */
        boolean onBackKey(GVRSceneObject sceneObject, final float[] coords);
    }

    /**
     * Creates TouchManager
     * @param gvrContext
     */
    public TouchManager(GVRContext gvrContext) {
    }

    /**
     * Makes the scene object touchable and associates the {@link OnTouch handler} with it.
     * The TouchManager will not hold strong references to sceneObject and handler.
     *
     * @param sceneObject
     * @param handler
     */
    public void makeTouchable(GVRSceneObject sceneObject, OnTouch handler) {
        if (handler != null) {
            if (sceneObject.getRenderData() != null) {
                if (!touchHandlers.containsKey(sceneObject)) {
                    makePickable(sceneObject);
                    touchHandlers.put(sceneObject, new WeakReference<>(handler));
                }
            } else if (sceneObject.getChildrenCount() > 0) {
                for (GVRSceneObject child : sceneObject.getChildren()) {
                    makeTouchable(child, handler);
                }
            }
        }
    }

    /**
     * Makes the object unpickable and removes the touch handler for it
     * @param sceneObject
     * @return true if the handler has been successfully removed
     */
    public boolean removeHandlerFor(final GVRSceneObject sceneObject) {
        sceneObject.detachComponent(GVRCollider.getComponentType());
        return null != touchHandlers.remove(sceneObject);
    }

    /**
     * Makes the scene object pickable by eyes. However, the object has to be touchable to process
     * the touch events.
     *
     * @param sceneObject
     */
    public void makePickable(GVRSceneObject sceneObject) {
        try {
            GVRMeshCollider collider = new GVRMeshCollider(sceneObject.getGVRContext(), false);
            sceneObject.attachComponent(collider);
        } catch (Exception e) {
            // Possible that some objects (X3D panel nodes) are without mesh
            Log.e(Log.SUBSYSTEM.INPUT, TAG, "makePickable(): possible that some objects (X3D panel nodes) are without mesh!");
        }
    }

    /**
     * Checks if the object is touchable and the touch handler is associated with this object to
     * process the touch events
     * @param sceneObject
     * @return true if the object is touchable, false otherwise
     */
    public boolean isTouchable(GVRSceneObject sceneObject) {
        return touchHandlers.containsKey(sceneObject);
    }

    /** Click code for left button */
    public static final int LEFT_CLICK_EVENT = 1;

    /** Click code for back button */
    public static final int BACK_KEY_EVENT = 2;

    /**
     * Touch Filter specifies the objects interested in processing the touch.
     * {@link Selector#select} defines the selection
     */
    public interface TouchManagerFilter extends Selector<GVRSceneObject> {
    }

    /**
     * Registers touch filter to limit the set of the objects processing the touch
     * More than one filter might be applied at the same time
     * @param filter
     */
    public void registerTouchFilter(final TouchManagerFilter filter) {
        mTouchFilters.add(filter);
    }

    /**
     * Unregisters touch filter
     * @param filter
     */
    public void unregisterTouchFilter(final TouchManagerFilter filter) {
        mTouchFilters.remove(filter);
    }

    /**
     * Registers touch filter to limit the set of the objects processing the back key event
     * More than one filter might be applied at the same time
     * @param filter
     */
    public void registerBackKeyFilter(final TouchManagerFilter filter) {
        mBackKeyFilters.add(filter);
    }

    /**
     * Unregisters touch filter
     * @param filter
     */
    public void unregisterBackKeyFilter(final TouchManagerFilter filter) {
        mBackKeyFilters.remove(filter);
    }

    /**
     * This method should be called externally from touch event dispatcher to run the logic for
     * widget lib
     * @param pickedObjectList list of picked objects
     * @param event touch event code
     * @return true if the input has been accepted and processed by some object, otherwise - false
     */
    public boolean handleClick(List<GVRPickedObject> pickedObjectList, int event) {
        Log.d(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): new click event");
        boolean isClickableItem = false;
        if (pickedObjectList == null) {
            Log.w(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): NULL pickedObjectList!");
            return event == LEFT_CLICK_EVENT ?
                    takeDefaultLeftClickAction() : takeDefaultRightClickAction();
        } else if (pickedObjectList.isEmpty()) {
            Log.w(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): EMPTY pickedObjectList!");
            return event == LEFT_CLICK_EVENT ?
                    takeDefaultLeftClickAction() : takeDefaultRightClickAction();
        }

        // Process result(s)
        for (GVRPickedObject pickedObject : pickedObjectList) {
            if (pickedObject == null) {
                Log.w(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): got a null reference in the pickedObject");
                continue;
            }
            GVRSceneObject sceneObject = pickedObject.getHitObject();
            if (sceneObject == null) {
                Log.w(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): got a null reference in the pickedObject.getHitObject()");
                continue;
            }
            Log.w(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): trying '%s' ...", Helpers.getFullName(sceneObject));

            final float[] hit = pickedObject.getHitLocation();

            synchronized (mOnTouchInterceptors) {
                for (OnTouch interceptor : mOnTouchInterceptors) {
                    isClickableItem = event == LEFT_CLICK_EVENT ?
                            interceptor.touch(sceneObject, hit) :
                            interceptor.onBackKey(sceneObject, hit);
                }
            }

            if (!isClickableItem) {
                Set<TouchManagerFilter> filters = event == LEFT_CLICK_EVENT ?
                        mTouchFilters : mBackKeyFilters;
                synchronized (mTouchFilters) {
                    boolean processTouch = true;
                    for (TouchManagerFilter filter: filters) {
                        if (!filter.select(sceneObject)) {
                            processTouch = false;
                            break;
                        }
                    }
                    if (!processTouch) {
                        continue;
                    }
                }

                final WeakReference<OnTouch> handler = touchHandlers.get(sceneObject);
                final OnTouch h = null != handler ? handler.get() : null;
                if (null != h) {
                    isClickableItem = event == LEFT_CLICK_EVENT ?
                            h.touch(sceneObject, hit) :
                            h.onBackKey(sceneObject, hit);

                    Log.d(Log.SUBSYSTEM.INPUT, TAG,
                            "handleClick(): handler for '%s' hit = %s handled event: %b",
                            sceneObject.getName(), hit, isClickableItem);

                } else {
                    Log.e(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): No handler or displayID for %s",
                            Helpers.getFullName(sceneObject));
                    touchHandlers.remove(sceneObject);
                }
            }

            if (isClickableItem) {
                Log.w(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): '%s' was clicked!",
                        Helpers.getFullName(sceneObject));
                break;
            }

            Log.w(Log.SUBSYSTEM.INPUT, TAG, "handleClick(): '%s' not clickable",
                    Helpers.getFullName(sceneObject));
        }

        if (!isClickableItem) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "No clickable items");
            isClickableItem = event == LEFT_CLICK_EVENT ?
                    takeDefaultLeftClickAction() : takeDefaultRightClickAction();
        }
        return isClickableItem;
    }

    /**
     * Sets default action on left click
     * @param runnable code running on left click
     */
    public void setDefaultLeftClickAction(Runnable runnable) {
        defaultLeftClickAction = runnable;
    }

    /**
     * Gets default action on left click
     * @return default runnable executing on left click
     */
    public Runnable getDefaultLeftClickAction() {
        return defaultLeftClickAction;
    }

    /**
     * Gets default action on right click
     * @return default runnable executing on right click
     */
    public Runnable getDefaultRightClickAction() {
        return defaultRightClickAction;
    }

    /**
     * Sets default action on right click
     * @param runnable code running on right click
     */
    public void setDefaultRightClickAction(Runnable runnable) {
        defaultRightClickAction = runnable;
    }

    /**
     * Sets the default action on both right and left click
     * @param runnable code running on click
     */
    public void setDefaultClickAction(Runnable runnable) {
        setDefaultLeftClickAction(runnable);
        setDefaultRightClickAction(runnable);
    }

    /**
     * Takes default action on left click
     * @return true if the default action is assigned on left click, otherwise - false
     */
    public boolean takeDefaultLeftClickAction() {
        if (defaultLeftClickAction != null) {
            defaultLeftClickAction.run();
            return true;
        }
        return false;
    }

    /**
     * Takes default action on right click
     * @return true if the default action is assigned on right click, otherwise - false
     */
    public boolean takeDefaultRightClickAction() {
        if (defaultRightClickAction != null) {
            defaultRightClickAction.run();
            return true;
        }
        return false;
    }

    /**
     * Add a interceptor for {@linkplain OnTouch#touch(GVRSceneObject, float[])}
     * and {@linkplain OnTouch#onBackKey(GVRSceneObject, float[])}} to handle the touch event
     * before it will be passed to other scene objects
     *
     * @param interceptor
     *            An implementation of {@link OnTouch}.
     * @return {@code true} if the interceptor was successfully registered,
     *         {@code false} if the interceptor is already registered.
     */
    public boolean addOnTouchInterceptor(final OnTouch interceptor) {
        return mOnTouchInterceptors.add(interceptor);
    }

    /**
     * Remove a previously {@linkplain #addOnTouchInterceptor(OnTouch)
     * registered} {@linkplain OnTouch interceptor}.
     *
     * @param interceptor
     *            An implementation of {@link OnTouch}.
     * @return {@code true} if the interceptor was successfully unregistered,
     *         {@code false} if the interceptor was not previously
     *         registered with this object.
     */
    public boolean removeOnTouchInterceptor(final OnTouch interceptor) {
        return mOnTouchInterceptors.remove(interceptor);
    }

    private final Map<GVRSceneObject, WeakReference<OnTouch>> touchHandlers = new WeakHashMap<GVRSceneObject, WeakReference<OnTouch>>();
    private Runnable defaultLeftClickAction = null;
    private Runnable defaultRightClickAction = null;
    private Set<OnTouch> mOnTouchInterceptors = new LinkedHashSet<>();

    private Set<TouchManagerFilter> mTouchFilters = new LinkedHashSet<>();
    private Set<TouchManagerFilter> mBackKeyFilters = new LinkedHashSet<>();

    private final static String TAG = org.gearvrf.utility.Log.tag(TouchManager.class);
}
