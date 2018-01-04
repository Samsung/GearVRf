package org.gearvrf;

import org.gearvrf.script.IScriptable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.view.MotionEvent;

/**
 * This class contains null implementations for the event interfaces
 * (subclasses of {@link IEvents}, such as {@link IScriptEvents} and {@link IActivityEvents}).
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
        
        @Override
        public void onAttach(IScriptable target) { }
        
        @Override
        public void onDetach(IScriptable target) { }
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
        public void onSetMain(GVRMain script) {
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
        public void onControllerEvent(Vector3f position, Quaternionf orientation, PointF touchpadPoint, boolean touched) {
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

    /**
     * Null implementation of {@link IPickEvents}
     */
    public static class PickEvents implements IPickEvents
    {
        @Override
        public void onPick(GVRPicker picker) { }
        @Override
        public void onNoPick(GVRPicker picker) { }
        @Override
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
        @Override
        public void onExit(GVRSceneObject sceneObj) { }
        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
}

    /**
     * Null implementation of {@link ITouchEvents}
     */
    public static class TouchEvents implements ITouchEvents
    {
        @Override
        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
        @Override
        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) { }
        @Override
        public void onMotionOutside(GVRPicker picker, MotionEvent event) { }
    }

    /**
     * Null implementation of {@link IAssetEvents}
     */
    public static class AssetEvents implements IAssetEvents
    {
        @Override
        public void onAssetLoaded(GVRContext context, GVRSceneObject model, String filePath, String errors) { }

        @Override
        public void onModelLoaded(GVRContext context, GVRSceneObject model, String filePath) { }

        @Override
        public void onTextureLoaded(GVRContext context, GVRTexture texture, String filePath) { }

        @Override
        public void onModelError(GVRContext context, String error, String filePath) { }

        @Override
        public void onTextureError(GVRContext context, String error, String filePath) { }
    }
}