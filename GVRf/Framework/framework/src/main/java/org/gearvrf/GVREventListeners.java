package org.gearvrf;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.MotionEvent;

/**
 * This class contains null implementations for the event interfaces
 * (subclasses of {@link IEvent}, such as {@link IScriptEvents} and {@link IActivityEvents}).
 * They can be extended to override individual methods, which produces less code than
 * implementing the complete interfaces, when the latter is unnecessary.
 *
 * For example, code to implement complete IScriptEvents includes 4 methods or more.
 * If only one method needs to be overridden, {@code GVREventListener.ScriptEvents} can be
 * derived to produce shorter and clearer code:
 *
 * <pre>
 *     IScriptEvents myScriptEventsHandler = new GVREventListener.ScriptEvents {
 *         public onInit(GVRContext gvrContext) throws Throwable {
 *         }
 *     };
 * </pre>
 */
public class GVREventListeners {
    /**
     * Null implementation of {@link IScriptEvents}.
     */
    public static class ScriptEvents implements IScriptEvents {
        @Override
        public void onEarlyInit(GVRContext gvrContext) {
        }

        @Override
        public void onInit(GVRContext gvrContext) throws Throwable {
        }

        @Override
        public void onAfterInit() {
        }

        @Override
        public void onStep() {
        }
    }

    /**
     * Null implementation of {@link IActivityEvents}.
     */
    public static class ActivityEvents implements IActivityEvents {
        @Override
        public void onPause() {
        }

        @Override
        public void onResume() {
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public void onSetScript(GVRScript script) {
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
        }

        @Override
        public void onConfigurationChanged(Configuration config) {
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                Intent data) {
        }

        @Override
        public void onTouchEvent(MotionEvent event) {
        }

        @Override
        public void dispatchTouchEvent(MotionEvent event) {
        }
    }

    /**
     * Null implementation of {@link ISceneObjectEvents}.
     */
    public static class SceneObjectEvents implements ISceneObjectEvents {
        @Override
        public void onAfterInit() {
        }

        @Override
        public void onStep() {
        }

        @Override
        public void onInit(GVRContext gvrContext, GVRSceneObject sceneObject) {
        }

        @Override
        public void onLoaded() {
        }
    }
}