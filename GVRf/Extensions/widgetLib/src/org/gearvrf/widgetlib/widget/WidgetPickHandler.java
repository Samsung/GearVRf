package org.gearvrf.widgetlib.widget;

import android.view.KeyEvent;
import android.view.MotionEvent;


import org.gearvrf.GVRContext;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;

import org.gearvrf.widgetlib.log.Log;
import org.gearvrf.widgetlib.main.WidgetLib;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public void onDestroy(GVRContext context) {
        context.getInputManager().clear();
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

        public void onMotionOutside(GVRPicker picker, MotionEvent event) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "onMotionOutside()");
        }

        private final List<Widget> mTouched = new ArrayList<>();

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        }
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
}
