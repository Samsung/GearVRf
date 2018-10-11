package org.gearvrf.widgetlib.content_scene;

import android.view.MotionEvent;

import org.gearvrf.widgetlib.log.Log;
import org.joml.Vector3f;

import static org.gearvrf.utility.Log.tag;
import org.gearvrf.widgetlib.widget.FlingHandler;

/**
 * The basic {@link ContentSceneController.ContentScene} implementation supporting
 * left-right-up-down fling
 */
abstract public class ScrollableContentScene implements ContentSceneController.ContentScene {
    private String TAG = tag(ScrollableContentScene.class);
    private FlingHandler mFlingHandler = new FlingHandler() {
        private float startX;
        private float endX;
        private float startY;
        private float endY;

        @Override
        public boolean onStartFling(MotionEvent event, Vector3f cursorPosition) {
            endX = startX = event.getX();
            endY = startY = event.getY();
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "startDrag: start = [%f, %f] end = [%f, %f] - %s",
                    startX, startY, endX, endY, event);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event, Vector3f cursorPosition) {
            if (startX == 0) {
                startX = event.getX();
            }
            endX = event.getX();

            if (startY == 0) {
                startY = event.getY();
            }
            endY = event.getY();
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "drag: start = [%f, %f] end = [%f, %f] - %s",
                    startX, startY, endX, endY, event);

            return true;
        }

        @Override
        public void onEndFling(FlingAction fling) {
            Log.d(Log.SUBSYSTEM.INPUT, TAG, "endDrag: start = [%f, %f] end = [%f, %f] - %s",
                    startX, startY, endX, endY, fling);
            if (endX - startX > 0) {
                scrollLeft();
            } else {
                scrollRight();
            }
            endX = startX = 0;

            if (endY - startY > 0) {
                scrollUp();
            } else {
                scrollDown();
            }
            endY = startY = 0;
        }
    };

    @Override
    public FlingHandler getFlingHandler() {
        return mFlingHandler;
    }

    /**
     * Use one combination of Left-Right or Up-Down, but not both
     */
    protected void scrollLeft() {
    }

    /**
     * Use one combination of Left-Right or Up-Down, but not both
     */
    protected void scrollRight() {
    }

    /**
     * Use one combination of Left-Right or Up-Down, but not both
     */
    protected void scrollUp() {
    }

    /**
     * Use one combination of Left-Right or Up-Down, but not both
     */
    protected void scrollDown() {
    }

}
