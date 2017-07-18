package org.gearvrf;


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.view.MotionEvent;

import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Finds the first scene object that is hit by a ray to dispatch
 * {@linkplain IHoverEvents hover events} or {@linkplain ITouchEvents touch events} to it.
 *
 * User can register implementations of {@link IHoverEvents} or {@link ITouchEvents} to
 * {@code GVRSceneObject.getEventReceiver()} to handle these events.
 */
public class GVRPickerInput extends GVRPicker {
    private GVRSceneObject mHoveredSceneObject;
    private GVRSceneObject mPressedSceneObject;
    private GVRPickedObject mCurrentCollision;
    private ActivityEventsHandler mActivityEventsHandler;

    public GVRPickerInput(GVRContext context, GVRScene scene) {
        super(context, scene);
        initialize();
    }

    public GVRPickerInput(GVRSceneObject owner, GVRScene scene) {
        super(owner, scene);
        initialize();
    }

    private void initialize() {
        mHoveredSceneObject = null;
        mPressedSceneObject = null;
        mActivityEventsHandler = new ActivityEventsHandler();

        final GVREventReceiver touchEventReceiver = getGVRContext().getActivity().getEventReceiver();
        touchEventReceiver.addListener(mActivityEventsHandler);

        // NOTE: Improve this logic to start listening.
        mHasFrameCallback = true;
        startListening();
    }

    @Override
    protected void generatePickEvents(GVRPickedObject[] picked) {
        if (picked == null || picked.length == 0) {
            if (mHoveredSceneObject != null) {
                sendOnHoverExit(mHoveredSceneObject);
                mHoveredSceneObject = null;
            }
            mCurrentCollision = null;
            return;
        }

        mCurrentCollision = picked[0];
        GVRCollider collider = mCurrentCollision.hitCollider;

        if (mHoveredSceneObject != collider.getOwnerObject()) {
            if (mHoveredSceneObject != null) {
                sendOnHoverExit(mHoveredSceneObject);
            }

            mHoveredSceneObject = collider.getOwnerObject();
            sendOnHoverEnter(mHoveredSceneObject);
        }
    }

    private void onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        // TODO: send hit collision and synchronize with onDraw thread.

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mHoveredSceneObject != null) {
                    mPressedSceneObject = mHoveredSceneObject;
                    sendOnTouch(mPressedSceneObject, event, mCurrentCollision.getHitLocation());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mPressedSceneObject != null) {
                    if (mPressedSceneObject == mHoveredSceneObject) {
                        sendOnTouch(mPressedSceneObject, event, mCurrentCollision.getHitLocation());
                    } else {
                        sendOnTouch(mPressedSceneObject, event, null);
                    }
                    mPressedSceneObject = null;
                }
                break;
            default:
                if (mPressedSceneObject != null) {
                    sendOnTouch(mPressedSceneObject, event, null);
                }
                break;
        }
    }

    public GVRSceneObject getHovered() {
        return mHoveredSceneObject;
    }

    public GVRSceneObject getTouched() {
        return mPressedSceneObject;
    }

    private void sendOnTouch(GVRSceneObject target, MotionEvent event, float[] hitLocation) {
        getGVRContext().getEventManager().sendEvent(target, ITouchEvents.class, "onTouch",
                target, event, hitLocation);

    }

    private void sendOnHoverEnter(GVRSceneObject target) {
        getGVRContext().getEventManager().sendEvent(target, IHoverEvents.class, "onHoverEnter",
                target);
    }

    private void sendOnHoverExit(GVRSceneObject target) {
        getGVRContext().getEventManager().sendEvent(target, IHoverEvents.class, "onHoverExit",
                target);
    }

    private class ActivityEventsHandler implements  IActivityEvents {
        @Override
        public void onTouchEvent(MotionEvent event) {
            GVRPickerInput.this.onTouchEvent(event);
        }

        @Override
        public void onControllerEvent(Vector3f position, Quaternionf orientation, PointF touchpadPoint) {

        }

        @Override
        public void onPause() {}

        @Override
        public void onResume() {}

        @Override
        public void onDestroy() {}

        @Override
        public void onSetMain(GVRMain script) {}

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {}

        @Override
        public void onConfigurationChanged(Configuration config) {}

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {}

        @Override
        public void dispatchTouchEvent(MotionEvent event) {}
    }
}
