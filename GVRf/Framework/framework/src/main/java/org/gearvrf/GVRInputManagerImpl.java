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

package org.gearvrf;

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.io.GearCursorController;
import org.gearvrf.utility.Log;

/**
 * Manages input devices and cursor controllers.
 * The input received from the {@link GVRCursorController} is used
 * to generate {@IPickEvents}, {@ITouchEvents} and {@ISensorEvents}.
 * These events can be observed by the application to determine
 * which objects are interacting with the controller. For example,
 * the {@link GVRBaseSensor} component provides access to the
 * sensor events. Pick and touch events may be observed directly]
 * by adding listeners to the controller.
 * @see GVRCursorController#addPickEventListener(IEvents)
 */
class GVRInputManagerImpl extends GVRInputManager {
    private static final String TAG = GVRInputManagerImpl.class.getSimpleName();
    private List<CursorControllerListener> listeners;
    private List<GVRCursorController> controllers;

    GVRInputManagerImpl(GVRContext gvrContext, boolean useGazeCursorController,
                        boolean useAndroidWearTouchpad)
    {
        super(gvrContext, useGazeCursorController, useAndroidWearTouchpad);

        controllers = new ArrayList<GVRCursorController>();
        listeners = new ArrayList<CursorControllerListener>();
    }

    /**
     * Emit "onCursorControllerAdded" events to controller listeners
     * for all connected controllers.
     * <p>
     * To connect with controllers, the application must add a ControllerEventListener
     * to listen for onCursorControllerAdded and onCursorControllerRemoved events
     * and then call this function to emit the events.
     * An event is sent immediately if the controller is actually ready
     * and connected. If the controller connects later, the event
     * will occur later and will not be sent from this function
     * (this can happen with Bluetooth devices).
     * @see CursorControllerListener
     */
    public void scanControllers()
    {
        for (GVRCursorController controller : super.getCursorControllers())
        {
            controllers.add(controller);
            if (controller.isConnected())
            {
                addCursorController(controller);
            }
        }
    }

    /**
     * Select an input controller based on a prioritized list.
     * <p>
     * The "onCursorControllerSelected" event is emitted when
     * a cursor controller is chosen. The controller chosen is
     * the highest priority controller available when the call is made.
     * <p>
     * If a higher priority controller is connected afterwards,
     * the input manager switches to using the new controller
     * and "onCursorControllerSelected" is emitted again.
     * @param desiredTypes list of accepable controller types in prioritized
     *                     order, highest priority is last in the list.
     * @param listener     listens for onCursorControllerSelected events.
     * @see CursorControllerListener
     * @see ICursorControllerSelectListener
     * @see org.gearvrf.io.GVRInputManager.ICursorControllerSelectListener
      */
    public void selectController(GVRContext ctx, GVRControllerType[] desiredTypes, ICursorControllerSelectListener listener)
    {
        SingleControllerSelector selector = new SingleControllerSelector(ctx, desiredTypes);
        addCursorControllerListener(selector);
        getEventReceiver().addListener(listener);
        scanControllers();
    }

    @Override
    public void addCursorControllerListener(CursorControllerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeCursorControllerListener(
            CursorControllerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public void addCursorController(GVRCursorController controller) {
        synchronized (listeners) {
            for (CursorControllerListener listener : listeners) {
                listener.onCursorControllerAdded(controller);
            }
        }
    }


    @Override
    public void removeCursorController(GVRCursorController controller) {
        controllers.remove(controller);
        synchronized (listeners) {
            for (CursorControllerListener listener : listeners) {
                listener.onCursorControllerRemoved(controller);
            }
        }
    }


    /**
     * This method sets a new scene for the {@link GVRInputManagerImpl}
     * 
     * @param scene
     * 
     */
    void setScene(GVRScene scene) {
        for (GVRCursorController controller : controllers) {
            controller.setScene(scene);
            controller.invalidate();
        }
    }

    @Override
    protected void close() {
        if (controllers.size() > 0)
        {
            controllers.get(0).removePickEventListener(GVRBaseSensor.getPickHandler());
        }
        super.close();
        controllers.clear();
    }

    @Override
    public List<GVRCursorController> getCursorControllers() {
        return controllers;
    }


    protected static class SingleControllerSelector implements CursorControllerListener
    {
        private GVRControllerType[] mControllerTypes;
        private int mCurrentControllerPriority = -1;
        private GVRCursorController mCursorController = null;
        private GVRSceneObject mCursor = null;
        private GVRCursorController.CursorControl mCursorControl = GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH;

        public SingleControllerSelector(GVRContext ctx, GVRControllerType[] desiredTypes)
        {
            mControllerTypes = desiredTypes;
            mCursor = makeDefaultCursor(ctx);
        }

        @Override
        synchronized public void onCursorControllerAdded(GVRCursorController gvrCursorController)
        {
            if (mCursorController == gvrCursorController)
            {
                return;
            }

            int priority = getControllerPriority(gvrCursorController.getControllerType());
            if (priority > mCurrentControllerPriority)
            {
                deselectController();
                selectController(gvrCursorController);
                if (gvrCursorController instanceof GearCursorController)
                {
                    ((GearCursorController) gvrCursorController).showControllerModel(true);
                }
                mCurrentControllerPriority = priority;
            }
            else
            {
                gvrCursorController.setEnable(false);
            }
        }

        public GVRCursorController getController()
        {
            return mCursorController;
        }

        public GVRSceneObject getCursor() { return mCursor; }

        public void setCursor(GVRSceneObject cursor)
        {
            mCursor = cursor;
        }

        private GVRSceneObject makeDefaultCursor(GVRContext ctx)
        {
            GVRSceneObject cursor = new GVRSceneObject(ctx, 0.2f, 0.2f,
                                                       ctx.getAssetLoader().loadTexture(
                                                               new GVRAndroidResource(ctx, R.drawable.cursor)));
            cursor.getRenderData().setDepthTest(false);
            cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
            return cursor;
        }

        private void selectController(GVRCursorController controller)
        {
            GVRContext ctx = controller.getGVRContext();
            controller.setScene(controller.getGVRContext().getMainScene());
            if (mCursor != null)
            {
                mCursor.getTransform().setPosition(0, 0, 0);
                controller.setCursor(mCursor);
            }
            ctx.getEventManager().sendEvent(ctx.getInputManager(),
                                            ICursorControllerSelectListener.class,
                                            "onCursorControllerSelected",
                                            controller,
                                            mCursorController);
            mCursorController = controller;
            mCursorController.setEnable(true);
            Log.d(TAG, "selected " + controller.getClass().getSimpleName());
        }

        private void deselectController()
        {
            GVRCursorController c = mCursorController;
            if (c != null)
            {
                mCursorController = null;
                mCurrentControllerPriority = -1;
                c.setCursor(null);
                c.setEnable(false);
            }
        }

        public void onCursorControllerRemoved(GVRCursorController gvrCursorController)
        {
            /*
             * If we are removing the controller currently being used,
             * switch to the Gaze controller if possible
             */
            if (mCursorController == gvrCursorController)
            {
                GVRContext ctx = gvrCursorController.getGVRContext();
                deselectController();
                GVRCursorController gaze = ctx.getInputManager().findCursorController(GVRControllerType.GAZE);
                if (gaze != null)
                {
                    ctx.getInputManager().addCursorController(gaze);
                }
            }
        }

        private int getControllerPriority(GVRControllerType type)
        {
            int i = 0;
            for (GVRControllerType t : mControllerTypes)
            {
                if (t.equals(type))
                {
                    return i;
                }
                ++i;
            }
            return -1;
        }
    };

}