/*
/* Copyright 2017 Samsung Electronics Co., LTD
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

package org.gearvrf.io;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRContext;
import org.gearvrf.GVREventReceiver;
import org.gearvrf.GVRImportSettings;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTransform;
import org.gearvrf.IActivityEvents;
import org.gearvrf.scene_objects.GVRLineSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * This class represents the Gear Controller.
 *
 * The input manager notifies the application when the controller has mConnected successfully.
 * Add a {@link org.gearvrf.io.GVRInputManager.ICursorControllerListener)} to the {@link GVREventReceiver}
 * of this controller to get notified when the controller is available to use.
 * To query the device specific information from the
 * Gear Controller make sure to type cast the returned {@link GVRCursorController} to
 * {@link GVRGearCursorController} like below:
 *
 * <code>
 * GearController controller = (GearController) gvrCursorController;
 * </code>
 *
 * You can add a listener for {@link IControllerEvent} to receive
 * notification whenever the controller information is updated.
 */
public final class GVRGearCursorController extends GVRCursorController
{
    public interface ControllerReader
    {
        boolean isConnected();

        boolean isTouched();

        void updateRotation(Quaternionf quat);

        void updatePosition(Vector3f vec);

        int getKey();

        float getHandedness();

        void updateTouchpad(PointF pt);
    }

    public enum CONTROLLER_KEYS
    {
        BUTTON_A (0x00000001),
        BUTTON_ENTER (0x00100000),
        BUTTON_BACK (0x00200000),
        BUTTON_UP (0x00010000),
        BUTTON_DOWN(0x00020000),
        BUTTON_LEFT (0x00040000),
        BUTTON_RIGHT (0x00080000),
        BUTTON_VOLUME_UP(0x00400000),
        BUTTON_VOLUME_DOWN(0x00800000),
        BUTTON_HOME(0x01000000);
        private int numVal;

        CONTROLLER_KEYS(int numVal)
        {
            this.numVal = numVal;
        }

        public int getNumVal()
        {
            return numVal;
        }
    }

    /**
     * Defines the handedness of the gear controller.
     */
    public enum Handedness
    {
        LEFT, RIGHT
    }
    private GVRSceneObject mControllerModel;
    private GVRSceneObject mRayModel;
    private GVRSceneObject mPivotRoot;
    private GVRSceneObject mControllerGroup;
    private ControllerReader mControllerReader;
    private boolean mShowControllerModel = false;

    private final Vector3f FORWARD = new Vector3f(0, 0, -1);
    private EventHandlerThread thread;
    private boolean initialized;
    private final MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
    private final MotionEvent.PointerProperties[] pointerPropertiesArray;
    private final MotionEvent.PointerCoords[] pointerCoordsArray;
    private long prevEnterTime;
    private long prevATime;
    private boolean actionDown = false;
    private float touchDownX = 0.0f;
    private static final float DEPTH_SENSITIVITY = 0.01f;

    public GVRGearCursorController(GVRContext context)
    {
        super(context, GVRControllerType.CONTROLLER);
        mPivotRoot = new GVRSceneObject(context);
        mPivotRoot.setName("GearCursorController_Pivot");
        mControllerGroup = new GVRSceneObject(context);
        mControllerGroup.setName("GearCursorController_ControllerGroup");
        mPivotRoot.addChildObject(mControllerGroup);
        mControllerGroup.addChildObject(mDragRoot);
        mControllerGroup.attachComponent(mPicker);
        thread = new EventHandlerThread();
        position.set(0.0f, 0.0f, -1.0f);
        MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_FINGER;
        pointerPropertiesArray = new MotionEvent.PointerProperties[]{properties};
        pointerCoordsArray = new MotionEvent.PointerCoords[]{pointerCoords};
    }

    public void attachReader(ControllerReader reader)
    {
        mControllerReader = reader;
    }

    @SuppressWarnings("unused")
    public GVRSceneObject getControllerModel() { return mControllerModel; }


    /**
     * Show or hide the controller model and picking ray.
     *
     * The scene objects remain in the scene but they are not rendered.
     * @param flag true to show the model and ray, false to hide it.
     */
    public void showControllerModel(boolean flag)
    {
        boolean show = flag && isEnabled();
        mShowControllerModel = flag;
        if (mControllerModel != null)
        {
            mControllerModel.setEnable(show);
            mControllerGroup.setEnable(show);
        }
        else if (show)
        {
            createControllerModel();
        }
    }

    @Override
    public void setCursorDepth(float depth)
    {
        super.setCursorDepth(depth);
        if (mRayModel != null)
        {
            mRayModel.getTransform().setScaleZ(mCursorDepth);
        }
    }


    protected void updateCursor(GVRPicker.GVRPickedObject collision)
    {
        super.updateCursor(collision);
        if (mRayModel != null)
        {
            if ((mCursorControl == CursorControl.PROJECT_CURSOR_ON_SURFACE) ||
                (mCursorControl == CursorControl.ORIENT_CURSOR_WITH_SURFACE_NORMAL))
            {
                mRayModel.getTransform().setScaleZ(collision.hitDistance);
            }
            else
            {
                mRayModel.getTransform().setScaleZ(mCursorDepth);
            }
        }
    }

    protected void moveCursor()
    {
        super.moveCursor();
        if (mRayModel != null)
        {
            mRayModel.getTransform().setScaleZ(mCursorDepth);
        }
    }

    @Override
    public void setPosition(float x, float y, float z)
    {
        position.set(x, y, z);
        pickDir.set(x, y, z);
        pickDir.normalize();
        invalidate();
    }

    @Override
    public void setEnable(boolean flag)
    {
        mControllerGroup.setEnable(flag);
        if (!enable && flag)
        {
            //set the enabled flag on the handler thread
            enable = true;
            if (initialized)
            {
                thread.setEnabled(true);
            }
        }
        else if (enable && !flag)
        {
            enable = false;
            if (initialized)
            {
                //set the disabled flag on the handler thread
                thread.setEnabled(false);
            }
        }
    }

    private void createControllerModel()
    {
        if (mRayModel == null)
        {
            mRayModel = new GVRLineSceneObject(context, 1, new Vector4f(1, 0, 0, 1), new Vector4f(1, 0, 0, 0));
            final GVRRenderData renderData = mRayModel.getRenderData();
            final GVRMaterial rayMaterial = renderData.getMaterial();

            mRayModel.setName("GearCursorController_Ray");
            rayMaterial.setLineWidth(4.0f);
            renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY + 10);
            renderData.setDepthTest(false);
            renderData.setAlphaBlend(true);
            mControllerGroup.addChildObject(mRayModel);
            mRayModel.getTransform().setScaleZ(mCursorDepth);
        }
        try
        {
            EnumSet<GVRImportSettings> settings = GVRImportSettings.getRecommendedSettingsWith(EnumSet.of(GVRImportSettings.NO_LIGHTING));
            mControllerModel = context.getAssetLoader().loadModel("gear_vr_controller.obj", settings, true, null);
        }
        catch (IOException ex)
        {
            Log.e("GVRGearCursorController", "ERROR: cannot load controller model gear_vr_controller.obj");
            return;
        }
        mControllerGroup.addChildObject(mControllerModel);
        mControllerGroup.setEnable(true);
    }

    @Override
    public void setScene(GVRScene scene) {
        GVRSceneObject parent = mPivotRoot.getParent();

        mPicker.setScene(scene);
        this.scene = scene;
        if (parent != null)
        {
            parent.removeChildObject(mPivotRoot);
        }
        if (scene != null)
        {
            scene.addSceneObject(mPivotRoot);
        }
        showControllerModel(mShowControllerModel);
    }

    @Override
    public void invalidate() {
        if (!initialized) {
            //do nothing
            return;
        }
        thread.sendInvalidate();
    }

    public void onDrawFrame()
    {
        boolean wasConnected = mConnected;

        mConnected = (mControllerReader != null) && mControllerReader.isConnected();
        if (!wasConnected && mConnected)
        {
            context.getInputManager().addCursorController(GVRGearCursorController.this);
        }
        if (!initialized)
        {
            if (!thread.isAlive())
            {
                thread.start();
                thread.prepareHandler();
            }
            if (!wasConnected && mConnected)
            {
                thread.initialize();
            }
            initialized = true;
        }
        if (isEnabled())
        {
            ControllerEvent event = ControllerEvent.obtain();

            mControllerReader.updateRotation(event.rotation);
            mControllerReader.updatePosition(event.position);
            event.touched = mControllerReader.isTouched();
            event.key = mControllerReader.getKey();
            event.handedness = mControllerReader.getHandedness();
            mControllerReader.updateTouchpad(event.pointF);
            thread.sendEvent(event);
        }
    }

    public synchronized boolean dispatchKeyEvent(KeyEvent e)
    {
        return false;
    }

    public synchronized boolean dispatchMotionEvent(MotionEvent e)
    {
        return false;
    }

    /**
     * Return the current position of the Gear Controller.
     *
     * @return a {@link Vector3f} representing the position of the controller. This function
     * returns <code>null</code> if the controller is unavailable or the data is stale.
     */
    public Vector3f getPosition() {
        if (thread == null || thread.currentControllerEvent == null || thread
                .currentControllerEvent.isRecycled()) {
            return null;
        }
        return thread.currentControllerEvent.position;
    }

    /**
     * Return the current rotation of the Gear Controller.
     *
     * @return a {@link Quaternionf} representing the rotation of the controller. This function
     * returns <code>null</code> if the controller is unavailable or the data is stale.
     */
    public Quaternionf getRotation() {
        if (thread == null || thread.currentControllerEvent == null || thread
                .currentControllerEvent.isRecycled()) {
            return null;
        }
        return thread.currentControllerEvent.rotation;
    }

    /**
     * Return the current touch coordinates of the Gear Controller touchpad.
     *
     * @return a {@link PointF} representing the touch coordinates on the controller. If the
     * user is not using the touchpad (0.0f, 0.0f) is returned. This function
     * returns <code>null</code> if the controller is unavailable or the data is stale.
     */
    public PointF getTouch() {
        if (thread == null || thread.currentControllerEvent == null || thread
                .currentControllerEvent.isRecycled()) {
            return null;
        }
        return thread.currentControllerEvent.pointF;
    }

    /**
     * Return the current handedness of the Gear Controller.
     *
     * @return returns whether the user is using the controller left or right handed. This function
     * returns <code>null</code> if the controller is unavailable or the data is stale.
     */
    @SuppressWarnings("unused")
    public Handedness getHandedness() {
        if (thread == null || thread.currentControllerEvent == null || thread
                .currentControllerEvent.isRecycled()) {
            return null;
        }
        return thread.currentControllerEvent.handedness == 0.0f ? Handedness.LEFT : Handedness
                .RIGHT;
    }

    public void onDestroy() {
        if (initialized) {
            thread.uninitialize();
            thread.quitSafely();
            initialized = false;
        }
    }

    private final class EventHandlerThread extends HandlerThread {
        private static final String THREAD_NAME = "GVREventHandlerThread";
        private Handler handler;
        private Vector3f result = new Vector3f();
        private int prevButtonEnter = KeyEvent.ACTION_UP;
        private int prevButtonA = KeyEvent.ACTION_UP;
        private int prevButtonBack = KeyEvent.ACTION_UP;
        private int prevButtonVolumeUp = KeyEvent.ACTION_UP;
        private int prevButtonVolumeDown = KeyEvent.ACTION_UP;
        private int prevButtonHome = KeyEvent.ACTION_UP;

        private static final int MSG_INITIALIZE = 1;
        private static final int MSG_UNINITIALIZE = 2;
        private static final int MSG_EVENT = 3;
        public static final int MSG_SET_ENABLE = 4;
        public static final int MSG_SET_SCENE = 5;
        public static final int MSG_SEND_INVALIDATE = 6;

        public static final int ENABLE = 0;
        public static final int DISABLE = 1;

        private ControllerEvent currentControllerEvent;

        EventHandlerThread() {
            super(THREAD_NAME);
        }

        void prepareHandler() {
            handler = new Handler(getLooper(), new Handler.Callback() {
                @Override
                public boolean handleMessage(Message message) {
                    switch (message.what) {
                        case MSG_INITIALIZE:
                            context.getInputManager().addCursorController(GVRGearCursorController.this);
                            break;
                        case MSG_EVENT:
                            handleControllerEvent((ControllerEvent) message.obj);
                            break;
                        case MSG_UNINITIALIZE:
                            context.getInputManager().removeCursorController(GVRGearCursorController.this);
                            break;
                        case MSG_SET_ENABLE:
                            GVRGearCursorController.super.setEnable(message.arg1 == ENABLE);
                            break;
                        case MSG_SET_SCENE:
                            GVRGearCursorController.super.setScene((GVRScene) message.obj);
                            break;
                        case MSG_SEND_INVALIDATE:
                            GVRGearCursorController.super.invalidate();
                            break;
                        default:
                            break;
                    }
                    return true;
                }
            });
        }

        private final SendEvents mPropagateEvents = new SendEvents(getGVRContext().getActivity());

        void handleControllerEvent(final ControllerEvent event) {
            context.getEventManager().sendEvent(context.getActivity(), IActivityEvents.class, "onControllerEvent",
                                                event.position, event.rotation, event.pointF, event.touched);

            this.currentControllerEvent = event;
            Quaternionf q = event.rotation;
            Vector3f pos = event.position;
            int key = event.key;
            GVRTransform camTrans = context.getMainScene().getMainCameraRig().getTransform();
            float cameraX = camTrans.getPositionX();
            float cameraY = camTrans.getPositionY();
            float cameraZ = camTrans.getPositionZ();

            q.normalize();
            mPivotRoot.getTransform().setRotation(q.w, q.x, q.y, q.z);
            q.transform(FORWARD, result);
            setOrigin(cameraX + pos.x, cameraY + pos.y, cameraZ + pos.z);
            mPivotRoot.getTransform().setPosition(cameraX + pos.x, cameraY + pos.y, cameraZ + pos.z);

            int handleResult = handleEnterButton(key, event.pointF, event.touched);
            prevButtonEnter = handleResult == -1 ? prevButtonEnter : handleResult;

            handleResult = handleAButton(key);
            prevButtonA = handleResult == -1 ? prevButtonA : handleResult;

            handleResult = handleButton(key, CONTROLLER_KEYS.BUTTON_BACK,
                                        prevButtonBack, KeyEvent.KEYCODE_BACK);
            prevButtonBack = handleResult == -1 ? prevButtonBack : handleResult;

            handleResult = handleButton(key, CONTROLLER_KEYS.BUTTON_VOLUME_UP,
                                        prevButtonVolumeUp, KeyEvent.KEYCODE_VOLUME_UP);
            prevButtonVolumeUp = handleResult == -1 ? prevButtonVolumeUp : handleResult;

            handleResult = handleButton(key, CONTROLLER_KEYS.BUTTON_VOLUME_DOWN,
                                        prevButtonVolumeDown, KeyEvent.KEYCODE_VOLUME_DOWN);
            prevButtonVolumeDown = handleResult == -1 ? prevButtonVolumeDown : handleResult;

            handleResult = handleButton(key, CONTROLLER_KEYS.BUTTON_HOME,
                                        prevButtonHome, KeyEvent.KEYCODE_HOME);
            prevButtonHome = handleResult == -1 ? prevButtonHome : handleResult;
            event.recycle();
            if (mSendEventsToActivity && ((keyEvent.size() > 0) || (motionEvent.size() > 0)))
            {
                mPropagateEvents.init(keyEvent, motionEvent);
                getGVRContext().getActivity().runOnUiThread(mPropagateEvents);
            }
            GVRGearCursorController.super.invalidate();
        }

        void sendEvent(ControllerEvent event) {
            handler.sendMessage(Message.obtain(null, MSG_EVENT, event));
        }

        void initialize() {
            handler.sendMessage(Message.obtain(null, MSG_INITIALIZE));
        }

        void uninitialize() {
            handler.sendMessage(Message.obtain(null, MSG_UNINITIALIZE));
        }

        public void setEnabled(boolean enable) {
            handler.removeMessages(MSG_SET_ENABLE);
            Message msg = Message.obtain(handler, MSG_SET_ENABLE, enable ? ENABLE : DISABLE, 0);
            msg.sendToTarget();
        }

        void setScene(GVRScene scene) {
            handler.removeMessages(MSG_SET_SCENE);
            Message msg = Message.obtain(handler, MSG_SET_SCENE, scene);
            msg.sendToTarget();
        }

        void sendInvalidate() {
            handler.removeMessages(MSG_SEND_INVALIDATE);
            Message msg = Message.obtain(handler, MSG_SEND_INVALIDATE);
            msg.sendToTarget();
        }
    }

    private int handleEnterButton(int key, PointF pointF, boolean touched)
    {
        long time = SystemClock.uptimeMillis();
        int handled = handleButton(key, CONTROLLER_KEYS.BUTTON_ENTER, thread.prevButtonEnter, KeyEvent.KEYCODE_ENTER);
        if ((handled == KeyEvent.ACTION_UP) || (actionDown && !touched))
        {
            pointerCoords.x = pointF.x;
            pointerCoords.y = pointF.y;
            MotionEvent motionEvent = MotionEvent.obtain(prevEnterTime, time,
                    MotionEvent.ACTION_UP, 1, pointerPropertiesArray, pointerCoordsArray,
                    0, MotionEvent.BUTTON_PRIMARY, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHPAD, 0);
            setMotionEvent(motionEvent);
            setActive(false);
        }
        else if ((handled == KeyEvent.ACTION_DOWN) || (touched && !actionDown))
        {
            pointerCoords.x = pointF.x;
            pointerCoords.y = pointF.y;
            MotionEvent motionEvent = MotionEvent.obtain(time, time,
                                                         MotionEvent.ACTION_DOWN, 1,
                                                         pointerPropertiesArray,
                                                         pointerCoordsArray,
                                                         0, MotionEvent.BUTTON_PRIMARY, 1f, 1f,
                                                         0, 0, InputDevice.SOURCE_TOUCHPAD, 0);
            setMotionEvent(motionEvent);
            if ((mTouchButtons & MotionEvent.BUTTON_PRIMARY) != 0)
            {
                setActive(true);
            }
            prevEnterTime = time;
        }
        else if (actionDown && touched)
        {
            pointerCoords.x = pointF.x;
            pointerCoords.y = pointF.y;
            MotionEvent motionEvent = MotionEvent.obtain(prevEnterTime, time,
                    MotionEvent.ACTION_MOVE, 1, pointerPropertiesArray, pointerCoordsArray,
                    0, MotionEvent.BUTTON_PRIMARY, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHPAD, 0);
            setMotionEvent(motionEvent);
        }
        /*
         * If the controller is allowed to change the cursor depth,
         * update it from the X delta on the controller touchpad.
         * The near and far depth values are NEGATIVE,
         * the controller depth is POSITIVE, hence the strange math.
         */
        if (touched && (mCursorControl == CursorControl.CURSOR_DEPTH_FROM_CONTROLLER))
        {
            float cursorDepth = getCursorDepth();
            float dx = pointF.x;

            if (!actionDown)
            {
                touchDownX = dx;
            }
            else
            {
                dx -= touchDownX;
                cursorDepth += dx * DEPTH_SENSITIVITY;
                if ((cursorDepth >= getNearDepth()) && (cursorDepth <= getFarDepth()))
                {
                    setCursorDepth(cursorDepth);
                }
            }
        }
        actionDown = touched;
        return handled;
    }

    private int handleAButton(int key)
    {
        long time = SystemClock.uptimeMillis();
        int handled = handleButton(key, CONTROLLER_KEYS.BUTTON_A, thread.prevButtonA, KeyEvent.KEYCODE_A);
        if (handled == KeyEvent.ACTION_UP)
        {
            setActive(false);
            pointerCoords.x = 0;
            pointerCoords.y = 0;
            MotionEvent motionEvent = MotionEvent.obtain(prevATime, time,
                    MotionEvent.ACTION_UP, 1, pointerPropertiesArray, pointerCoordsArray,
                    0, MotionEvent.BUTTON_SECONDARY, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHPAD, 0);
            setMotionEvent(motionEvent);
            Log.d("EVENT:", "handleAButton action=%d button=%d x=%f y=%f",
                  motionEvent.getAction(), motionEvent.getButtonState(), motionEvent.getX(), motionEvent.getY());
        }
        else if (handled == KeyEvent.ACTION_DOWN)
        {
            pointerCoords.x = 0;
            pointerCoords.y = 0;
            MotionEvent motionEvent = MotionEvent.obtain(time, time,
                    MotionEvent.ACTION_DOWN, 1, pointerPropertiesArray, pointerCoordsArray,
                    0, MotionEvent.BUTTON_SECONDARY, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHPAD, 0);
            setMotionEvent(motionEvent);
            prevATime = time;
            if ((mTouchButtons & MotionEvent.BUTTON_SECONDARY) != 0)
            {
                setActive(true);
            }
            Log.d("EVENT:", "handleAButton action=%d button=%d x=%f y=%f",
                  motionEvent.getAction(), motionEvent.getButtonState(), motionEvent.getX(), motionEvent.getY());
        }
        return handled;
    }

    private int handleButton(int key, CONTROLLER_KEYS button, int prevButton, int keyCode) {
        if ((key & button.getNumVal()) != 0) {
            Log.d("EVENT:", "keyPress button=%d code=%d", button.getNumVal(), keyCode);
            if (prevButton != KeyEvent.ACTION_DOWN) {
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
                setKeyEvent(keyEvent);
                return KeyEvent.ACTION_DOWN;
            }
        } else {
            if (prevButton != KeyEvent.ACTION_UP) {
                KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
                setKeyEvent(keyEvent);
                return KeyEvent.ACTION_UP;
            }
        }
        return -1;
    }

    private static final class ControllerEvent {
        private static final int MAX_RECYCLED = 5;
        private static final Object recyclerLock = new Object();
        private static int recyclerUsed;
        private static ControllerEvent recyclerTop;
        private ControllerEvent next;
        private Quaternionf rotation = new Quaternionf();
        private Vector3f position = new Vector3f();
        private PointF pointF = new PointF();
        private int key;
        private float handedness;
        private boolean recycled = false;
        private boolean touched = false;

        static ControllerEvent obtain() {
            final ControllerEvent event;
            synchronized (recyclerLock) {
                event = recyclerTop;
                if (event == null) {
                    return new ControllerEvent();
                }
                event.recycled = false;
                recyclerTop = event.next;
                recyclerUsed -= 1;
            }
            event.next = null;
            return event;
        }

        final void recycle() {
            synchronized (recyclerLock) {
                if (recyclerUsed < MAX_RECYCLED) {
                    recyclerUsed++;
                    next = recyclerTop;
                    recyclerTop = this;
                    recycled = true;
                }
            }
        }

        boolean isRecycled() {
            return recycled;
        }
    }
}

final class SendEvents implements Runnable
{
    private final ConcurrentLinkedQueue<KeyEvent> mKeyEvents = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<MotionEvent> mMotionEvents = new ConcurrentLinkedQueue<>();
    private final Activity mActivity;

    SendEvents(final Activity activity) {
        mActivity = activity;
    }

    public void init(List<KeyEvent> keyEvents, List<MotionEvent> motionEvents)
    {
        mKeyEvents.addAll(keyEvents);
        mMotionEvents.addAll(motionEvents);
    }

    public void run()
    {
        for (final Iterator<KeyEvent> it = mKeyEvents.iterator(); it.hasNext(); ) {
            final KeyEvent e = it.next();
            mActivity.dispatchKeyEvent(e);
            it.remove();
        }

        for (Iterator<MotionEvent> it = mMotionEvents.iterator(); it.hasNext(); ) {
            final MotionEvent e = it.next();
            final MotionEvent dupe = MotionEvent.obtain(e);
            it.remove();

            mActivity.dispatchTouchEvent(dupe);
            dupe.recycle();
        }
    }
}
