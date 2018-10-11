package org.gearvrf.widgetlib.widget;

import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;


import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.WidgetLib;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

class WidgetPickHandler implements GVRInputManager.ICursorControllerSelectListener,
        GVRInputManager.ICursorControllerListener {
    private PickEventsListener mPickEventListener = new PickEventsListener();
    private TouchEventsListener mTouchEventsListener = new TouchEventsListener();
    private ControllerEvent mControllerEvent = new ControllerEvent();

    @Override
    public void onCursorControllerSelected(GVRCursorController newController,
                                           GVRCursorController oldController) {
        if (oldController != null) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG,
                    "onCursorControllerSelected(): removing from old controller (%s)",
                    oldController.getName());
            oldController.setEnable(false);
            oldController.removePickEventListener(mPickEventListener);
            oldController.removePickEventListener(mTouchEventsListener);
            oldController.removeControllerEventListener(mControllerEvent);
        }
        Log.d(Log.SUBSYSTEM.INPUT, TAG,
                "onCursorControllerSelected(): adding to new controller \"%s\" (%s)",
                newController.getName(), newController.getClass().getSimpleName());
        GVRPicker picker = newController.getPicker();
        picker.setPickClosest(false);
        newController.addPickEventListener(mPickEventListener);
        newController.addPickEventListener(mTouchEventsListener);
        newController.addControllerEventListener(mControllerEvent);
        newController.setEnable(true);
    }

    private void dispatchKeyEvent(KeyEvent keyEvent) {
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                WidgetLib.getTouchManager().handleClick(null, KeyEvent.KEYCODE_BACK);
                break;

        }
    }

    @Override
    public void onCursorControllerAdded(GVRCursorController gvrCursorController) {
        Log.d(Log.SUBSYSTEM.INPUT, TAG,"onCursorControllerAdded: %s",
                gvrCursorController.getClass().getSimpleName());

    }

    @Override
    public void onCursorControllerRemoved(GVRCursorController gvrCursorController) {
        Log.d(Log.SUBSYSTEM.INPUT, TAG,"onCursorControllerRemoved: %s",
                gvrCursorController.getClass().getSimpleName());

        gvrCursorController.removePickEventListener(mPickEventListener);
        gvrCursorController.removePickEventListener(mTouchEventsListener);
        gvrCursorController.removeControllerEventListener(mControllerEvent);
    }

    static private class PickEventsListener implements IPickEvents {

        public void onEnter(final GVRSceneObject sceneObj, final GVRPicker.GVRPickedObject collision) {
            WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Widget widget = WidgetBehavior.getTarget(sceneObj);

                    if (widget != null && widget.isFocusEnabled()) {
                        mSelected.add(widget);
                            Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onEnter(%s): select widget %s",
                                    sceneObj.getName(), widget.getName());
                    }
                }
            });
        }

        public void onExit(final GVRSceneObject sceneObj) {
            WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (sceneObj != null) {
                        Widget widget = WidgetBehavior.getTarget(sceneObj);
                        if (widget != null && mSelected.remove(widget) && !hasFocusGroupMatches(widget)) {
                            widget.dispatchOnFocus(false);
                            Log.e(Log.SUBSYSTEM.FOCUS, TAG, "onExit(%s) deselect widget = %s", sceneObj.getName(), widget.getName());
                        }
                    }
                }
            });
        }

        private boolean hasFocusGroupMatches(Widget widget) {
            for (Widget sel: mSelected) {
                Log.d(TAG, "hasFocusGroupMatches : widget [%s] sel [%s] = %b", widget.getName(), sel.getName(),
                        widget.isFocusHandlerMatchWith(sel));
                if (widget.isFocusHandlerMatchWith(sel)) {
                    return true;
                }
            }
            return false;
        }

        public void onPick(final GVRPicker picker) {
            if (picker.hasPickListChanged()) {
                final List<GVRPicker.GVRPickedObject> pickedList =  Arrays.asList(picker.getPicked());
                WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        for (GVRPicker.GVRPickedObject hit : pickedList) {
                            Widget widget = WidgetBehavior.getTarget(hit.hitObject);
                            if (widget != null && mSelected.contains(widget) &&
                                    (widget.isFocused() ||
                                            widget.dispatchOnFocus(true))) {
                                Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onPick(%s) widget focused %s",
                                        hit.hitObject.getName(), widget.getName());
                                break;
                            }
                        }
                    }
                });
            }
        }

        public void onNoPick(final GVRPicker picker) {
            if (picker.hasPickListChanged()) {
                WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(Log.SUBSYSTEM.FOCUS, TAG, "onNoPick(): selection cleared");
                        mSelected.clear();
                    }
                });
            }
        }

        private final Set<Widget> mSelected = new HashSet<>();

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }
    }

    static private class TouchEventsListener implements ITouchEvents {

        public void onTouchStart(final GVRSceneObject sceneObj, final GVRPicker.GVRPickedObject collision) {
            WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchStart(%s)", sceneObj.getName());
                    Widget widget = WidgetBehavior.getTarget(sceneObj);

                    if (widget != null && widget.isTouchable() && !mTouched.contains(widget)) {
                        Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchStart(%s) start touch widget %s",
                                sceneObj.getName(), widget.getName());
                        mTouched.add(widget);
                    }
                }
            });
        }

        public void onTouchEnd(final GVRSceneObject sceneObj, final GVRPicker.GVRPickedObject collision) {
            WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchEnd(%s)", sceneObj.getName());
                    Widget widget = WidgetBehavior.getTarget(sceneObj);

                    if (widget != null && widget.isTouchable() && mTouched.contains(widget)) {
                        if (widget.dispatchOnTouch(sceneObj, collision.hitLocation)) {
                            Log.d(Log.SUBSYSTEM.INPUT, TAG, "onTouchEnd(%s) end touch widget %s",
                                    sceneObj.getName(), widget.getName());
                            mTouched.clear();
                        } else {
                            mTouched.remove(widget);
                        }
                    }
                }
            });
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }

        public void onMotionOutside(final GVRPicker picker, final MotionEvent event) {
            WidgetLib.getMainThread().runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (mFlingHandler != null) {
                        GestureDetector gestureDetector = new GestureDetector(
                                picker.getGVRContext().getContext(), mGestureListener);

                        gestureDetector.onTouchEvent(event);
                        Vector3f pos = new Vector3f();
                        Log.d(Log.SUBSYSTEM.INPUT, TAG, "onMotionOutside() event = %s mFling = %s", event, mFling);

                        switch (event.getAction()) {
                            case ACTION_DOWN:
                                mFlingHandler.onStartFling(event, picker.getController().getPosition(pos));
                                break;
                            case ACTION_MOVE:
                                mFlingHandler.onFling(event, picker.getController().getPosition(pos));
                                break;
                            case ACTION_UP:
                                mFlingHandler.onEndFling(mFling);
                                break;
                        }
                    }
                }
            });

        }

        private final List<Widget> mTouched = new ArrayList<>();
        private FlingHandler mFlingHandler;
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }

        private FlingHandler.FlingAction mFling;

        private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.OnGestureListener() {
            class Fling implements FlingHandler.FlingAction {
                MotionEvent startEvent;
                MotionEvent endEvent;
                float velocityX;
                float velocityY;

                Fling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                    startEvent = MotionEvent.obtain(e1);
                    endEvent = MotionEvent.obtain(e2);
                    velocityX = vX;
                    velocityY = vY;
                }

                @Override
                public String toString() {
                    return "startEvent: " + startEvent + " endEvemnt = " + endEvent
                            + " velocityX = " + velocityX + " velosityY = " + velocityY;
                }

                @Override
                public void clear() {
                    startEvent.recycle();
                    endEvent.recycle();
                }

                @Override
                public MotionEvent getStartEvent() {
                    return startEvent;
                }

                @Override
                public MotionEvent getEndEvent() {
                    return endEvent;
                }

                @Override
                public float getVelocityX() {
                    return velocityX;
                }

                @Override
                public float getVelocityY() {
                    return velocityY;
                }

            }

            private void setFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                if (e1 != null && e2 != null) {
                    if (mFling != null) {
                        mFling.clear();
                    }
                    mFling = new Fling(e1, e2, vX, vY);
                }
            }

            public boolean onDown(MotionEvent e) {
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onDown e = %s", e);
                return true;
            }

            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onFling event1: " + e1 + " event2: " + e2
                        + " velocityX = " + velocityX + " velocityY = " + velocityY);
                setFling(e1, e2, velocityX, velocityY);
                return true;
            }

            public void onLongPress(MotionEvent e) {
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onLongPress e = %s", e);
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                    float distanceX, float distanceY) {
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onScroll e1 = %s, e2 = %s distanceX = %f, distanceY = %f",
                        e1, e2, distanceX, distanceY);
                return true;
            }

            public void onShowPress(MotionEvent e) {
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onShowPress e = %s", e);
            }

            public boolean onSingleTapUp(MotionEvent e) {
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onSingleTapUp e = %s", e);
                return true;
            }
        };
    }

    private class ControllerEvent implements GVRCursorController.IControllerEvent {
        @Override
        public void onEvent(GVRCursorController controller, boolean isActive) {
            if (controller != null) {
                final List<KeyEvent> keyEvents = controller.getKeyEvents();
                Log.d(Log.SUBSYSTEM.INPUT, TAG, "onEvent(): Key events:");
                for (KeyEvent keyEvent : keyEvents) {
                    Log.d(Log.SUBSYSTEM.INPUT, TAG, "onEvent():   keyCode: %d",
                            keyEvent.getKeyCode());
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        dispatchKeyEvent(keyEvent);
                    }
                }
            }
        }
    }

    private static final String TAG = WidgetPickHandler.class.getSimpleName();

    void setFlingHandler(FlingHandler flingHandler ) {
        mTouchEventsListener.mFlingHandler = flingHandler;
    }

    FlingHandler getFlingHandler() {
        return mTouchEventsListener.mFlingHandler;
    }
}
