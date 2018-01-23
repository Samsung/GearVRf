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
package org.gearvrf.io;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
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
 * <p>
 * This class listens for "fling" events and converts them to onSingleTapUp
 * or onSwipe calls.
 * </p>
 * To use this class:
 * <ul>
 * <li>Create an instance of the {@code GVRTouchPadGestureListener} and override
 * the methods you need.
 * <li>Create an instance of the {@code GestureListener} for your{@link View}
 * and use the GVRTouchPadGestureListener to listen for gestures.
 * <li>In the {@link View#onTouchEvent(MotionEvent)} method ensure you call
 * {@link #onTouchEvent(MotionEvent)}. The methods defined in your callback will
 * be executed when the events occur.
 * <li>
 * If you are using a {@link GVRViewSceneObject} you can call {@link GVRViewSceneObject#setGestureDetector}
 * to add an Android GestureDetector for that view
 * </li>
 * </ul>
 * @see org.gearvrf.scene_objects.GVRViewSceneObject
 */
public class GVRTouchPadGestureListener implements OnGestureListener
{
    private static final int SWIPE_MIN_DISTANCE = 80;
    private static final int ONTAP_MIN_DISTANCE = 0;
    private static final String TAG = "GVRTPadGestureListener";

    public enum Action
    {
        None, Tap, SwipeBackward, SwipeForward, SwipeDown, SwipeUp
    }

    /**
     * Notified when a swipe occurs.
     *
     * @param e
     *            The first down motion event that started the fling.
     * @param action
     *            The direction of swipe.
     * @param vx
     *            The velocity of this fling measured in pixels per second
     *            along the x axis.
     * @param vy
     *            The velocity of this fling measured in pixels per second
     *            along the y axis.
     * @return true if the event is consumed, else false
     */
    public  boolean onSwipe(MotionEvent e, Action action, float vx, float vy) { return false; }

    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) { return false; }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float vx, float vy) { return false; }

    @Override
    public void onShowPress(MotionEvent e) { }

    @Override
    public void onLongPress(MotionEvent e) { }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float vx, float vy)
    {
        if (null == e1 || null == e2)
        {
            Log.w(TAG, "can't process onFling; e1 is " + e1 + "; e2 is " + e2);
            return false;
        }
        Action action = getAction(e1, e2);

        if (action == Action.None)
        {
            return false;
        }
    /*
     * The gesture listener filters out dirty taps that look like swipes
     * altogether. This reduces usability as it's hard to get a clean
     * tap on the tracker
     */
        if (action == Action.Tap)
        {
            onSingleTapUp(e1);
            return true;
        }
        return onSwipe(e1, action, vx, vy);
    }

    protected Action getAction(MotionEvent e1, MotionEvent e2)
    {
        double distance = Math.sqrt(Math.pow(e2.getX() - e1.getX(), 2)
                + Math.pow(e2.getY() - e1.getY(), 2));

        if (distance < SWIPE_MIN_DISTANCE)
        {
            if (distance >= ONTAP_MIN_DISTANCE)
            {
                return Action.Tap;
            }
            return Action.None;
        }
        try
        {
            double deltaY = e2.getY() - e1.getY();
            double deltaX = e2.getX() - e1.getX();

            double angle = Math.toDegrees(Math.atan2(deltaY, deltaX)) + 180 + 45;

            if (angle > 360)
                angle -= 360;

            if (angle < 90)
            {
                return Action.SwipeForward;
            }
            else if (angle < 180)
            {
                return Action.SwipeUp;
            }
            else if (angle < 270)
            {
                return Action.SwipeBackward;
            }
            else
            {
                return Action.SwipeDown;
            }
        }
        catch (Exception e) { }
        return Action.None;
    }
}

