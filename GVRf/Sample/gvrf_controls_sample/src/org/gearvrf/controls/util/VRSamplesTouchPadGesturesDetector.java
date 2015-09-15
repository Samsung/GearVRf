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

package org.gearvrf.controls.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class VRSamplesTouchPadGesturesDetector extends Object implements
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final int MIN_MOVE_TIME = 250;
    private GestureDetectorCompat mDetector;
    private long downCurrentTimeMillis = 0;
    private SwipeDirection lastDirection = SwipeDirection.Ignore;

    private static final int SWIPE_MIN_DISTANCE = 80;
    private static final int ONTAP_MIN_DISTANCE = 0;

    public enum SwipeDirection {
        Backward, Forward, Down, Up, Ignore
    }

    VRSamplesTouchPadGesturesDetector.OnTouchPadGestureListener gestureListener;
    VRSamplesTouchPadGesturesDetector.OnTouchPadDoubleTapListener doubleTapListener;

    /**
     * The listener that is used to notify when gestures occur.
     */
    public interface OnTouchPadGestureListener {
        /**
         * Notified when a tap occurs with the up {@link MotionEvent} that
         * triggered it.
         * 
         * @param e The up motion event that completed the first tap
         * @return true if the event is consumed, else false
         */
        boolean onSingleTap(MotionEvent e);

        /**
         * Notified when a long press occurs with the initial on down
         * {@link MotionEvent} that triggered it.
         * 
         * @param e The initial on down motion event that started the longpress.
         */
        void onLongPress(MotionEvent e);

        /**
         * Notified when a swipe occurs.
         * 
         * @param e1 The first down motion event that started the fling.
         * @param swipeDirection The direction of swipe.
         * @param velocityX The velocity of this fling measured in pixels per
         *            second along the x axis.
         * @param velocityY The velocity of this fling measured in pixels per
         *            second along the y axis.
         * @return true if the event is consumed, else false
         */
        boolean onSwipe(MotionEvent e, SwipeDirection swipeDirection,
                float velocityX, float velocityY);

        void onSwiping(MotionEvent e, MotionEvent e2,
                float velocityX, float velocityY, SwipeDirection swipeDirection);

        void onSwipeOppositeLastDirection();
    }

    /**
     * The listener that is used to notify when a double-tap or a confirmed
     * single-tap occur.
     */
    public interface OnTouchPadDoubleTapListener {
        /**
         * Notified when a single-tap occurs.
         * 
         * @param e The down motion event of the single-tap.
         * @return true if the event is consumed, else false
         */
        boolean onSingleTapConfirmed(MotionEvent e);

        /**
         * Notified when a double-tap occurs.
         * 
         * @param e The down motion event of the first tap of the double-tap.
         * @return true if the event is consumed, else false
         */
        boolean onDoubleTap(MotionEvent e);
    }

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    int countSwipe = 0;

    public VRSamplesTouchPadGesturesDetector(Context context,
            VRSamplesTouchPadGesturesDetector.OnTouchPadGestureListener listener) {

        this.mDetector = new GestureDetectorCompat(context, this);
        this.mDetector.setIsLongpressEnabled(true);
        this.mDetector.setOnDoubleTapListener(this);
        this.gestureListener = listener;

        sharedPref = context.getSharedPreferences("VR", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

    }

    public void setOnDoubleTapListener(
            VRSamplesTouchPadGesturesDetector.OnTouchPadDoubleTapListener onDoubleTapListener) {
        doubleTapListener = onDoubleTapListener;
    }

    public boolean onTouchEvent(MotionEvent event) {

        if (mDetector == null) {
            return false;
        }
        return this.mDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        downCurrentTimeMillis = System.currentTimeMillis();

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        long moveTime = System.currentTimeMillis();

        if ((moveTime - downCurrentTimeMillis) > MIN_MOVE_TIME) {

            SwipeDirection direction = getSwipeDirection(e1.getX(), e1.getY(), e2.getX(), e2.getY());

            if (direction != lastDirection) {
                gestureListener.onSwipeOppositeLastDirection();
            }

            lastDirection = direction;
        }

        gestureListener.onSwiping(e1, e2, distanceX, distanceY, lastDirection);

        return true;
    }

    private void saveSwipeDistanceValue(float value) {
        editor.putFloat("SWIPE_DISTANCE" + countSwipe, value);
        editor.commit();
        countSwipe++;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

        if (mDetector == null) {
            return false;
        }

        double distance = Math.sqrt(Math.pow(e2.getX() - e1.getX(), 2)
                + Math.pow(e2.getY() - e1.getY(), 2));

        saveSwipeDistanceValue((float) distance);

        if (distance > SWIPE_MIN_DISTANCE) {
            try {

                double deltaY = e2.getY() - e1.getY();
                double deltaX = e2.getX() - e1.getX();

                double angle = Math.toDegrees(Math.atan2(deltaY, deltaX)) + 180 + 45;

                if (angle > 360)
                    angle -= 360;

                if (angle < 90) {
                    return gestureListener
                            .onSwipe(e1, SwipeDirection.Forward, velocityX, velocityY);
                } else if (angle < 180) {
                    return gestureListener.onSwipe(e1, SwipeDirection.Up, velocityX, velocityY);
                } else if (angle < 270) {
                    return gestureListener.onSwipe(e1, SwipeDirection.Backward, velocityX,
                            velocityY);
                } else {
                    return gestureListener.onSwipe(e1, SwipeDirection.Down, velocityX, velocityY);
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
    public void onLongPress(MotionEvent event) {

        if (gestureListener != null) {
            gestureListener.onLongPress(event);
        }

    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {

        if (gestureListener != null) {
            return gestureListener.onSingleTap(event);
        }

        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        if (doubleTapListener != null) {
            return doubleTapListener.onDoubleTap(event);
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        if (doubleTapListener != null) {
            return doubleTapListener.onSingleTapConfirmed(event);
        }
        return false;
    }

    private SwipeDirection getSwipeDirection(float x1, float y1, float x2, float y2) {

        Double angle = Math.toDegrees(Math.atan2(y1 - y2, x2 - x1));

        if (angle > 45 && angle <= 135) {
            return SwipeDirection.Up;
        } else if (angle >= 135 && angle < 180 || angle < -135 && angle > -180) {
            return SwipeDirection.Ignore; // left to right
        } else if (angle < -45 && angle >= -135) {
            return SwipeDirection.Down;
        } else if (angle > -45 && angle <= 45) {
            return SwipeDirection.Ignore; // right to left
        }

        return SwipeDirection.Ignore;
    }
}
