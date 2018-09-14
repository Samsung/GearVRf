package org.gearvrf.widgetlib.widget;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.gearvrf.widgetlib.thread.FPSCounter;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRPicker.GVRPickedObject;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.widgetlib.widget.properties.JSONHelpers;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A class for tracking line-of-sight focus for {@link Widget} instances. In
 * addition to notifying gain and loss of focus, also manages "long focus".
 * "Long focus" is similar to "long press" and occurs when an object has held
 * line-of-sight focus for {@link #LONG_FOCUS_TIMEOUT} milliseconds or longer.
 * The long focus timeout is reset each time an object gains focus and is
 * stopped entirely when no object has line-of-sight focus.
 */
@Deprecated
public class FocusManager {
    /**
     * Widgets indicate their willingness to take focus through the implementing {@link Focusable}
     * interface.
     */
    public interface Focusable {
        /**
         * Returns whether this View is currently able to take focus.
         * @return True if this view can take focus, or false otherwise.
         */
        boolean isFocusEnabled();

        /**
         * Called when the widget gains or loses focus.
         * @param focused True if the widget now has focus, false otherwise.
         * @return true if the widget accepts the focus. If the widget does not process focus event,
         * the focus event will be passed to the next widget in the stack.
         */
        boolean onFocus(boolean focused);
        /**
         * Called when the widget gains the long focus.
         * "Long focus" is similar to "long press" and occurs when an object has held
         * line-of-sight focus for {@link #LONG_FOCUS_TIMEOUT} milliseconds or longer.
         * The long focus timeout is reset each time an object gains focus and is
         * stopped entirely when no object has line-of-sight focus.
         */
        void onLongFocus();

        /**
         * Defines long focus timeout. {@link #LONG_FOCUS_TIMEOUT} milliseconds is default one.
         * @return
         */
        long getLongFocusTimeout();
    }

    /**
     * Similar to
     * {@link TouchManager#addOnTouchInterceptor(TouchManager.OnTouch)}
     * TouchManager.setTouchInterceptor(OnTouch)}, instances of this interface
     * can be used to filter the delivery of focus events to
     * {@linkplain GVRSceneObject scene objects}.
     */
    public interface FocusInterceptor {
        /**
         * If the interceptor has completely handled the event and no further
         * processing is necessary -- including the normal focus event mechanism
         * -- return {@code true}. To allow the normal focus mechanism to be
         * executed, return {@code false}.
         * <p>
         * Generally this is useful for restricting focus events to a subset of
         * visible {@linkplain GVRSceneObject scene objects}: return
         * {@code false} for the objects you want to get normal focus processing
         * for, and {@code true} for the ones you don't.
         *
         * @param sceneObject
         *            The {@code GVRSceneObject} to filter
         * @return {@code True} if the focus event has been handled,
         *         {@code false} otherwise.
         */
        boolean onFocus(GVRSceneObject sceneObject);
    }

    /**
     * Interface for listening the current focus.
     * It is called with {@code True} if no object on the screen had the focus before,
     * but now some {@link Focusable} has a focus.
     *
     * It is called with {@code False} if some object on the screen had the focus before,
     * but now no {@link Focusable} is in focus.
     *
     * @params focused
     *
     */
    public interface CurrentFocusListener {
        void onCurrentFocus(boolean focused);
    }

    private enum Properties {
        enabled
    }

    /**
     * Creates FocusManager
     */
    public FocusManager(GVRContext gvrContext) {
        final JSONObject properties = WidgetLib.getPropertyManager().getInstanceProperties(getClass(), TAG);
        Log.d(TAG, "FocusManager(): properties: %s", properties);
        mEnabled = JSONHelpers.optBoolean(properties, Properties.enabled, false);
        Log.d(TAG, "FocusManager(): mEnabled: %b", mEnabled);
        init(gvrContext);
    }

    /**
     * Adds current focus listener
     * @param listener
     * @return true if the listener has been successfully added
     */
    public boolean addCurrentFocusListener(CurrentFocusListener listener) {
        synchronized (mFocusListeners) {
            return mFocusListeners.add(listener);
        }
    }

    /**
     * Removes current focus listener
     * @param listener
     * @return true if the listener has been successfully removed
     */
    public boolean removeCurrentFocusListener(CurrentFocusListener listener) {
        synchronized (mFocusListeners) {
            return mFocusListeners.remove(listener);
        }
    }

    /**
     * Registers the {@link Focusable} to manage focus for the particular scene object.
     * The focus manager will not hold strong references to the sceneObject and the focusable.
     * @param sceneObject
     * @param focusable
     */
    public void register(final GVRSceneObject sceneObject, final Focusable focusable) {
        Log.d(Log.SUBSYSTEM.FOCUS, TAG, "register sceneObject %s , focusable = %s",
                sceneObject.getName(), focusable);
        mFocusableMap.put(sceneObject, new WeakReference<>(focusable));
    }

    /**
     * Unregisters the {@link Focusable} for the particular scene object. If the scene object
     * has a focus, the focus will be released.
     * @param sceneObject
     */
    public void unregister(final GVRSceneObject sceneObject) {
        unregister(sceneObject, false);
    }

    /**
     * Stops managing focus every frame
     */
    public void clear() {
        if (mContext != null && mEnabled) {
            mContext.unregisterDrawFrameListener(mDrawFrameListener);
        }
    }

    /**
     * Sets focus interceptor {@link FocusInterceptor}
     * @param interceptor if it is null the current interceptor is removed
     */
    public void setFocusInterceptor(FocusInterceptor interceptor) {
        focusInterceptor = interceptor;
    }

    void unregister(final GVRSceneObject sceneObject,
            final boolean softUnregister) {
        Log.d(Log.SUBSYSTEM.FOCUS, TAG, "unregister sceneObject %s", sceneObject.getName());
        final WeakReference<Focusable> focusableRef = mFocusableMap
                .remove(sceneObject);
        if (focusableRef != null) {
            final boolean allowRelease = !softUnregister
                    || !containsFocusable(focusableRef);
            WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (allowRelease && mCurrentFocus == focusableRef.get()) {
                        releaseCurrentFocus();
                    }
                }
            });
        }
    }

    private void init(GVRContext context) {
        if (mContext == null) {
            mContext = context;
            if (mEnabled) {
                mContext.registerDrawFrameListener(mDrawFrameListener);
            }
        }
    }

    private boolean containsFocusable(
            final WeakReference<Focusable> focusableRef) {
        final Focusable focusable = focusableRef.get();
        for (WeakReference<Focusable> ref : mFocusableMap.values()) {
            final Focusable f = ref.get();
            if (f != null && f == focusable) {
                return true;
            }
        }
        return false;
    }

    private void cancelLongFocusRunnable() {
        WidgetLib.getMainThread().removeCallbacks(mLongFocusRunnable);
    }

    private void postLongFocusRunnable(long timeout) {
        if (mCurrentFocus != null) {
            WidgetLib.getMainThread().runOnMainThreadDelayed(mLongFocusRunnable,
                                                            timeout);
        }
    }

    private GVRDrawFrameListener mDrawFrameListener = new GVRDrawFrameListener() {
        @Override
        public void onDrawFrame(float frameTime) {
            FPSCounter.timeCheck("onDrawFrame <START>: " + this + " frameTime = " + frameTime);

            final GVRScene mainScene = mContext.getMainScene();
            mPickedObjects = GVRPicker.pickObjects(mainScene, 0, 0, 0, 0, 0, -1.0f);

            WidgetLib.getMainThread().runOnMainThread(mFocusRunnable);
            FPSCounter.timeCheck("onDrawFrame <END>: " + this + " frameTime = " + frameTime);
        }
    };

    private final Runnable mFocusRunnable = new Runnable() {
        @Override
        public void run() {
            final GVRPickedObject[] pickedObjectList = mPickedObjects;
            boolean noCurrentFocus =  mCurrentFocus == null;

            // release old focus
            if (pickedObjectList == null || 0 == pickedObjectList.length) {
                if (mCurrentFocus != null) {
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): empty/null pick list; releasing current focus (%s)",
                        mCurrentFocusName);
                }
                releaseCurrentFocus();

                if (!noCurrentFocus) {
                    notifyFocusListeners(false);
                }
                return;
            }

            Focusable focusable = null;
            for (GVRPickedObject picked : pickedObjectList) {
                if (picked == null) {
                    Log.w(TAG, "onDrawFrame(): got a null reference in the pickedObjectList");
                    continue;
                }
                final GVRSceneObject quad = picked.getHitObject();
                if (quad != null) {
                    if (!compareCurrentFocusName(quad)) {
                        Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): checking '%s' for focus",
                            quad.getName());
                    }
                    WeakReference<Focusable> ref = mFocusableMap.get(quad);
                    if (null != ref) {
                        focusable = ref.get();
                    } else {
                        mFocusableMap.remove(quad);
                        focusable = null;
                    }
                }

                // already has a focus - do nothing
                if (mCurrentFocus != null && mCurrentFocus == focusable) {
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): already has focus (%s)",
                        quad != null ? quad.getName() : "<null>");
                    break;
                }

                if (null == focusable || !focusable.isFocusEnabled()) {
                    continue;
                }

                releaseCurrentFocus();

                if (takeNewFocus(quad, focusable)) {
                    mCurrentFocusName = quad.getName();
                    Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): '%s' took focus", mCurrentFocusName);
                    break;
                }
            }

            if (mCurrentFocus != null && focusable != mCurrentFocus) {
                Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onDrawFrame(): no eligible focusable found! (%s)", mCurrentFocusName);
                releaseCurrentFocus();
                notifyFocusListeners(false);
            }


            if (noCurrentFocus ^ (mCurrentFocus != null)) {
                notifyFocusListeners(!noCurrentFocus);
            }
        }

        private boolean compareCurrentFocusName(final GVRSceneObject quad) {
            final String quadName = quad.getName();
            return (mCurrentFocusName == null && quadName == null)
                    || (mCurrentFocusName != null && mCurrentFocusName
                            .equalsIgnoreCase(quadName));
        }
    };

    private boolean releaseCurrentFocus() {
        boolean ret = true;
        if (mCurrentFocus != null) {
            Log.d(Log.SUBSYSTEM.FOCUS, TAG, "releaseCurrentFocus(): releasing focus for '%s'",
                    mCurrentFocusName);
            cancelLongFocusRunnable();
            ret = mCurrentFocus.onFocus(false);
            mCurrentFocus = null;
            mCurrentFocusName = null;
        }
        return ret;
    }

    private boolean takeNewFocus(final GVRSceneObject quad, final Focusable newFocusable) {
        if (newFocusable != null &&
                newFocusable.isFocusEnabled()) {

            if (focusInterceptor != null && focusInterceptor.onFocus(quad)) {
                return false;
            }

            if (newFocusable.onFocus(true)) {
                mCurrentFocus = newFocusable;
                postLongFocusRunnable(newFocusable.getLongFocusTimeout());

                return true;
            }
        }
        return false;
    }

    private void notifyFocusListeners(boolean focused) {
        synchronized (mFocusListeners) {
            for (CurrentFocusListener listener : mFocusListeners) {
                    try {
                        listener.onCurrentFocus(focused);
                    } catch (Throwable t) {
                        Log.e(TAG, t, "");
                    }
                }
        }
    }

    private GVRContext mContext;
    private final boolean mEnabled;
    private Focusable mCurrentFocus = null;
    private String mCurrentFocusName = "";
    private Map<GVRSceneObject, WeakReference<Focusable>> mFocusableMap = new WeakHashMap<>();
    private Set<CurrentFocusListener> mFocusListeners = new LinkedHashSet<>();
    private FocusInterceptor focusInterceptor;
    private volatile GVRPickedObject[] mPickedObjects;

    private final Runnable mLongFocusRunnable = new Runnable() {


        @Override
        public void run() {
            if (mCurrentFocus != null) {
                mCurrentFocus.onLongFocus();
            }
        }
    };

    static final int LONG_FOCUS_TIMEOUT = 5000;

    @SuppressWarnings("unused")
    private static final String TAG =  org.gearvrf.utility.Log.tag(FocusManager.class);
}
