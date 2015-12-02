/************************************************************************************

Filename    :   VRTouchPadGestureDetector.java
Content     :   
Created     :   
Authors     :   

Copyright   :   Copyright 2014 Oculus VR, Inc. All Rights reserved.

Licensed under the Oculus VR SDK License Version 3.0 (the "License"); 
you may not use the Oculus VR SDK except in compliance with the License, 
which is provided at the time of installation or download, or which 
otherwise accompanies this software in either electronic or hard copy form.

You may obtain a copy of the License at

http://www.oculusvr.com/licenses/LICENSE-3.0 

Unless required by applicable law or agreed to in writing, the Oculus VR SDK 
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 *************************************************************************************/
package org.gearvrf.util;

import android.content.Context;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects various gestures and events using the supplied {@link MotionEvent}s.
 * The {@link OnGestureListener} callback will notify users when a particular
 * motion event has occurred. This class should only be used with
 * {@link MotionEvent}s reported via touch (don't use for trackball events).
 * 
 * To use this class:
 * <ul>
 * <li>Create an instance of the {@code VRTouchPadGestureDetector} for your
 * {@link View}
 * <li>In the {@link View#onTouchEvent(MotionEvent)} method ensure you call
 * {@link #onTouchEvent(MotionEvent)}. The methods defined in your callback will
 * be executed when the events occur.
 * </ul>
 */
@SuppressWarnings("deprecation")
public class VRTouchPadGestureDetector extends Object implements
        OnGestureListener, OnDoubleTapListener {
    private static final int SWIPE_MIN_DISTANCE = 80;
    private static final int ONTAP_MIN_DISTANCE = 0;

    public enum SwipeDirection {
        Backward, Forward, Down, Up
    }

    GestureDetector gestureDetector;
    VRTouchPadGestureDetector.OnTouchPadGestureListener gestureListener;
    VRTouchPadGestureDetector.OnTouchPadDoubleTapListener doubleTapListener;

    /**
     * The listener that is used to notify when gestures occur.
     */
    public interface OnTouchPadGestureListener {
        /**
         * Notified when a tap occurs with the up {@link MotionEvent} that
         * triggered it.
         * 
         * @param e
         *            The up motion event that completed the first tap
         * @return true if the event is consumed, else false
         */
        boolean onSingleTap(MotionEvent e);

        /**
         * Notified when a long press occurs with the initial on down
         * {@link MotionEvent} that triggered it.
         * 
         * @param e
         *            The initial on down motion event that started the
         *            longpress.
         */
        void onLongPress(MotionEvent e);

        /**
         * Notified when a swipe occurs.
         * 
         * @param e1
         *            The first down motion event that started the fling.
         * @param swipeDirection
         *            The direction of swipe.
         * @param velocityX
         *            The velocity of this fling measured in pixels per second
         *            along the x axis.
         * @param velocityY
         *            The velocity of this fling measured in pixels per second
         *            along the y axis.
         * @return true if the event is consumed, else false
         */
        boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection,
                float velocityX, float velocityY);
    }

    /**
     * The listener that is used to notify when a double-tap or a confirmed
     * single-tap occur.
     */
    public interface OnTouchPadDoubleTapListener {
        /**
         * Notified when a single-tap occurs.
         * 
         * @param e
         *            The down motion event of the single-tap.
         * @return true if the event is consumed, else false
         */
        boolean onSingleTapConfirmed(MotionEvent e);

        /**
         * Notified when a double-tap occurs.
         * 
         * @param e
         *            The down motion event of the first tap of the double-tap.
         * @return true if the event is consumed, else false
         */
        boolean onDoubleTap(MotionEvent e);
    }

    @SuppressWarnings("deprecation")
    public VRTouchPadGestureDetector(
            VRTouchPadGestureDetector.OnTouchPadGestureListener listener) {
        gestureDetector = new GestureDetector(this);
        gestureListener = listener;

    }

    public VRTouchPadGestureDetector(Context context,
            VRTouchPadGestureDetector.OnTouchPadGestureListener listener) {
        gestureDetector = new GestureDetector(context, this);
        gestureListener = listener;
    }

    public VRTouchPadGestureDetector(Context context,
            VRTouchPadGestureDetector.OnTouchPadGestureListener listener,
            Handler handler) {
        gestureDetector = new GestureDetector(context, this, handler);
        gestureListener = listener;
    }

    public void setOnDoubleTapListener(
            VRTouchPadGestureDetector.OnTouchPadDoubleTapListener onDoubleTapListener) {
        doubleTapListener = onDoubleTapListener;
    }

    // OnGestureListener
    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
        if (gestureDetector == null)
            return false;

        double distance = Math.sqrt(Math.pow(e2.getX() - e1.getX(), 2)
                + Math.pow(e2.getY() - e1.getY(), 2));

        if (distance > SWIPE_MIN_DISTANCE) {
            try {

                double deltaY = e2.getY() - e1.getY();
                double deltaX = e2.getX() - e1.getX();

                double angle = Math.toDegrees(Math.atan2(deltaY, deltaX)) + 180 + 45;

                if (angle > 360)
                    angle -= 360;

                if (angle < 90) {
                    return gestureListener.onSwipe(e1, SwipeDirection.Forward,
                            velocityX, velocityY);
                } else if (angle < 180) {
                    return gestureListener.onSwipe(e1, SwipeDirection.Up,
                            velocityX, velocityY);
                } else if (angle < 270) {
                    return gestureListener.onSwipe(e1, SwipeDirection.Backward,
                            velocityX, velocityY);
                } else {
                    return gestureListener.onSwipe(e1, SwipeDirection.Down,
                            velocityX, velocityY);
                }

            } catch (Exception e) {
                // Ignore
            }
        } else if (distance >= ONTAP_MIN_DISTANCE) {
            /*
             * The gesture listener filters out dirty taps that look like swipes
             * altogether. This reduces usability as it's hard to get a clean
             * tap on the tracker
             */
            gestureListener.onSingleTap(e1);
            return true;
        }

        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        if (gestureListener != null) {
            gestureListener.onLongPress(arg0);
        }
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
            float arg3) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        if (gestureListener != null) {
            return gestureListener.onSingleTap(arg0);
        }

        return false;
    }

    /**
     * Analyzes the given motion event and if applicable triggers the
     * appropriate callbacks on the {@link OnGestureListener} supplied.
     * 
     * @param ev
     *            The current motion event.
     * @return true if the {@link OnGestureListener} consumed the event, else
     *         false.
     */
    public boolean onTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev);
    }

    @Override
    public boolean onDoubleTap(MotionEvent arg0) {
        if (doubleTapListener != null) {
            return doubleTapListener.onDoubleTap(arg0);
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent arg0) {
        if (doubleTapListener != null) {
            return doubleTapListener.onSingleTapConfirmed(arg0);
        }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

}
