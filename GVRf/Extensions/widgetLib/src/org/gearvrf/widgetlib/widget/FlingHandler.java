package org.gearvrf.widgetlib.widget;

import android.view.MotionEvent;

import org.joml.Vector3f;

/**
 * (Declaring {@link FlingHandler} as an {@code interface} instead of an
 * {@code abstract class} makes for a <em>little</em> less boilerplate code:
 * each handler can be implemented as an anonymous class, instead of a class
 * declaration that's only used in one place.)
 */
public interface FlingHandler {
    interface FlingAction {
        MotionEvent getStartEvent();
        MotionEvent getEndEvent();
        float getVelocityX();
        float getVelocityY();
        void clear();
    }

    /**
     * Set up; change mode if necessary.
     *
     * @return {@code True} if the handler will accept drags, {@code false}
     *         otherwise.
     */
    boolean onStartFling(MotionEvent event, Vector3f cursorPosition);

    /**
     * @return Return true if you have consumed the event, false if you
     *         haven't.
     */
    boolean onFling(MotionEvent event, Vector3f cursorPosition);

    /** Tear down; reset mode, if necessary */
    void onEndFling(FlingAction fling);
}