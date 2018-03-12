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

package org.gearvrf.io;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventReceiver;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IEventReceiver;
import org.gearvrf.IEvents;
import org.gearvrf.R;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * An instance of this class is obtained using the
 * {@link GVRContext#getInputManager()} call. Use this class to query for all
 * the {@link GVRCursorController} objects in the framework.
 * <p>
 * The gvr.xml file passed to {@link org.gearvrf.GVRActivity#setMain(GVRMain, String)}
 * designates which cursor controllers GearVRF will consider in the "useControllerTypes"
 * field. This string is a comma separated list of controller types in increasing priority
 * order (the last string is the most important controller which will be chosen first
 * if it is available). The supported types are:
 * <ul>
 *     <li>gaze - GearVR touchpad or touch screen</li>
 *     <li>mouse - Bluetooth mouse</li>
 *     <li>gamepad - Bluetooth gamepad</li>
 *     <li>weartouchpad - Android Wear touchpaad</li>
 *     <li>controller - GearVR or Daydream hand controller</li>
 * </ul>
 * </p>
 * If your application only needs a single controller, {@link GVRInputManager#selectController()}
 * will select the highest priority controller from those currently connected and call the
 * {@link ICursorControllerSelectListener#onCursorControllerSelected(GVRCursorController, GVRCursorController)}
 * function for any listeners attached to the {@link GVREventReceiver} of the {@link GVRInputManager}.
 * If a higher priority controller becomes available while your application is running,
 * this function is called again to allow your app to switch controllers if desired.
 * <p>
 * You can also use a {@link ICursorControllerListener}
 * to get notified whenever a cursor controller is added or removed from the system.
 * In this case, your application can select one or more controllers based on these notifications.
 * A controller is selected or removed by calling {@link GVRCursorController#setEnable(boolean)}.
 * The {@link GVRInputManager#scanControllers()} function scans the current devices and emits
 * these events for the connected controllers.
 * <p>
 * You can also call the {@link GVRInputManager#getCursorControllers()} method
 * to query for all devices currently in the framework.
 * <p>
 * External input devices can also be added using the
 * {@link GVRInputManager#addCursorController(GVRCursorController)} method.
 * @see GVRCursorController
 * @see GVRContext#getInputManager()
 * @see #getEventReceiver()
 */
public class GVRInputManager implements IEventReceiver
{
    private static final String TAG = GVRInputManager.class.getSimpleName();
    private static final String WEAR_TOUCH_PAD_SERVICE_PACKAGE_NAME = "org.gearvrf.weartouchpad";
    private final InputManager inputManager;
    private final GVRContext context;
    private GVRAndroidWearTouchpad androidWearTouchpad;
    private GVRGamepadDeviceManager gamepadDeviceManager;
    private GVRMouseDeviceManager mouseDeviceManager;
    private GVRGearCursorController gearCursorController;
    private GVREventReceiver mListeners;
    private ArrayList<GVRControllerType> mEnabledControllerTypes;

    // maintain one instance of the gazeCursorController
    private GVRGazeCursorController gazeCursorController;

    private static final int GAZE_CACHED_KEY = (GVRDeviceConstants
            .OCULUS_GEARVR_TOUCHPAD_VENDOR_ID * 31 + GVRDeviceConstants
            .OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID) * 31 + GVRControllerType.GAZE.hashCode();

    private static final int CONTROLLER_CACHED_KEY = (GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_VENDOR_ID * 31 +
                                                      GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID) * 31 +
                                                      GVRControllerType.CONTROLLER.hashCode();


    /*
     * This class encapsulates the {@link InputManager} to detect all relevant
     * Input devices attached to the framework.
     * 
     * Another important function of this class to report multiple deviceIds
     * reported by the {@link InputManager} as one device to the framework.
     * 
     * This class internally recognizes mouse and gamepad devices attached to
     * the Android device.
     */

    // maps a given device Id to a controller id
    private final SparseArray<GVRCursorController> controllerIds;

    // maintains the ids already distributed to a given device.
    // We make use of the vendor and product id to identify a device.
    private final SparseArray<GVRCursorController> cache;

    private CopyOnWriteArrayList<GVRCursorController> controllers;

    /**
     * Construct an input manager which manages the designated cursor controllers.
     * <p>
     * The list of controller types comes from the gvr.xml file passed to
     * {@link org.gearvrf.GVRActivity#setMain}. (It is the comma-separated list
     * of controller types specified in the "useControllerTypes" field).
     * </p>
     * @param context       GVRContext input manager is attached to
     * @param enabledTypes  list of controller types allowed for this applications
     */
    public GVRInputManager(GVRContext context, ArrayList<GVRControllerType> enabledTypes)
    {
        Context androidContext = context.getContext();
        inputManager = (InputManager) androidContext.getSystemService(Context.INPUT_SERVICE);
        mEnabledControllerTypes = enabledTypes;
        this.context = context;
        mListeners = new GVREventReceiver(this);
        inputManager.registerInputDeviceListener(inputDeviceListener, null);
        controllerIds = new SparseArray<GVRCursorController>();
        cache = new SparseArray<GVRCursorController>();
        mouseDeviceManager = new GVRMouseDeviceManager(context);
        gamepadDeviceManager = new GVRGamepadDeviceManager();
        gearCursorController = new GVRGearCursorController(context);
        if ((enabledTypes != null) &&
            enabledTypes.contains(GVRControllerType.WEARTOUCHPAD) &&
            checkIfWearTouchPadServiceInstalled(context))
        {
            androidWearTouchpad = new GVRAndroidWearTouchpad(context);
        }
        controllers = new CopyOnWriteArrayList<GVRCursorController>();
    }

    /**
     * Get the event receiver which dispatches {@link ICursorControllerSelectListener}
     * and {@link ICursorControllerListener}.
     * @return GVREventReceiver to dispatch input manager events
     */
    public GVREventReceiver getEventReceiver() { return mListeners; }

    /**
     * Emit "onCursorControllerAdded" events to controller listeners
     * for all connected controllers.
     * <p>
     * To connect with controllers, the application must add a {@link ICursorControllerListener}
     * to listen for onCursorControllerAdded and onCursorControllerRemoved events
     * and then call this function to emit the events.
     * An event is sent immediately if the controller is actually ready
     * and connected. If the controller connects later, the event
     * will occur later and will not be sent from this function
     * (this can happen with Bluetooth devices).
     * @see ICursorControllerListener
     * @see #selectController()
     */
    public void scanControllers()
    {
        for (GVRCursorController controller : getCursorControllers())
        {
            if (!controllers.contains(controller))
            {
                controllers.add(controller);
            }
            if (controller.isConnected())
            {
                addCursorController(controller);
            }
        }
    }

    /**
     * Select an input controller based on the list of controller types in gvr.xml.
     * The list is in priority order with the highest priority controller last.
     * If you call this function and no controllers are specified in gvr.xml
     * it will default to "gaze,controller" (Gear controller first, then Gaze).
     * <p>
     * The "onCursorControllerSelected" event is emitted when
     * a cursor controller is chosen. The controller chosen is
     * the highest priority controller available when the call is made.
     * <p>
     * If a higher priority controller is connected afterwards,
     * the input manager switches to using the new controller
     * and "onCursorControllerSelected" is emitted again.
     * @param listener     listens for onCursorControllerSelected events.
     * @see ICursorControllerSelectListener
     * @see org.gearvrf.io.GVRInputManager.ICursorControllerSelectListener
     * @see #scanControllers()
     */
    public void selectController(ICursorControllerSelectListener listener)
    {
        if ((mEnabledControllerTypes == null) || (mEnabledControllerTypes.size() == 0))
        {
            mEnabledControllerTypes = new ArrayList<GVRControllerType> (Arrays.asList(GVRControllerType.GAZE, GVRControllerType.CONTROLLER));
            scanDevices();
        }
        GVRInputManager.SingleControllerSelector
                selector = new GVRInputManager.SingleControllerSelector(context, mEnabledControllerTypes);
        getEventReceiver().addListener(selector);
        getEventReceiver().addListener(listener);
        scanControllers();
    }

    /**
     * Select an input controller based on the list of controller types in gvr.xml.
     * The list is in priority order with the highest priority controller last.
     * If you call this function and no controllers are specified in gvr.xml
     * it will default to "gaze,controller" (Gear controller first, then Gaze).
     * <p>
     * The "onCursorControllerSelected" event is emitted when
     * a cursor controller is chosen. The controller chosen is
     * the highest priority controller available when the call is made.
     * <p>
     * If a higher priority controller is connected afterwards,
     * the input manager switches to using the new controller
     * and "onCursorControllerSelected" is emitted again.
     * <p>
     * This form of the function is useful when using JavaScript.
     * The event is routed to the event receiver of the {@link GVRInputManager}
     * and can be handled by attaching a script which contains a
     * function called "onCursorControllerSelected".
     * </p>
     * @see ICursorControllerSelectListener
     * @see org.gearvrf.io.GVRInputManager.ICursorControllerSelectListener
     */
    public void selectController()
    {
        if ((mEnabledControllerTypes == null) || (mEnabledControllerTypes.size() == 0))
        {
            mEnabledControllerTypes = new ArrayList<GVRControllerType> (Arrays.asList(GVRControllerType.GAZE, GVRControllerType.CONTROLLER));
            scanDevices();
        }
        GVRInputManager.SingleControllerSelector
                selector = new GVRInputManager.SingleControllerSelector(context, mEnabledControllerTypes);
        mListeners.addListener(selector);
        scanControllers();
    }

    /**
     * Define a {@link GVRCursorController} and add it to the
     * {@link GVRInputManager} for external input device handling by the
     * framework.
     *
     * @param controller the external {@link GVRCursorController} to be added to the
     *                   framework.
     */
    public void addCursorController(GVRCursorController controller) {
        controller.getGVRContext().getEventManager().sendEvent(this, ICursorControllerListener.class,
                "onCursorControllerAdded", controller);
    }


    /**
     * Remove the previously added {@link GVRCursorController} added to the
     * framework.
     *
     * @param controller the external {@link GVRCursorController} to be removed from
     *                   the framework.
     */
    public void removeCursorController(GVRCursorController controller)
    {
        controller.setEnable(false);
        controllers.remove(controller);
        controller.getGVRContext().getEventManager().sendEvent(this, ICursorControllerListener.class,
                "onCursorControllerRemoved", controller);
    }


    /**
     * This method sets a new scene for the {@link GVRInputManager}
     *
     * @param scene
     *
     */
    public void setScene(GVRScene scene)
    {
        for (GVRCursorController controller : controllers)
        {
            controller.setScene(scene);
            controller.invalidate();
        }
    }

    /**
     * Scan all the attached Android IO devices and gather
     * information about them.
     * <p>
     * This function is called once at initialization to enumerate
     * the Android devices. Calling it again is not harmful but
     * will not gather additional information.
     */
    public void scanDevices()
    {
        for (int deviceId : inputManager.getInputDeviceIds()) {
            addDevice(deviceId);
        }
        cache.put(CONTROLLER_CACHED_KEY, gearCursorController);
    }

    private boolean checkIfWearTouchPadServiceInstalled(GVRContext context) {
        PackageManager pm = context.getActivity().getPackageManager();
        try {
            pm.getPackageInfo(WEAR_TOUCH_PAD_SERVICE_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }

    /**
     * Get a list of the {@link GVRCursorController}s currently in the system.
     * <p>
     * Ideally this call needs to be done inside
     * {@link org.gearvrf.GVRMain#onInit(GVRContext)} so that all the cursor objects are
     * set up before the rendering starts.
     *
     * Remember to add a {@link ICursorControllerListener} or {@link ICursorControllerSelectListener}
     * to receivenotifications on {@link GVRCursorController} objects added or removed
     * during runtime.
     *
     * @return a list of all the {@link GVRCursorController} objects in the
     * system.
     */
    public List<GVRCursorController> getCursorControllers() {
        List<GVRCursorController> result = new ArrayList<GVRCursorController>();
        for (int index = 0, size = cache.size(); index < size; index++) {
            int key = cache.keyAt(index);
            GVRCursorController controller = cache.get(key);
            result.add(controller);
        }
        return result;
    }

    /**
     * Get the first controller of a specified type
     * @param type controller type to search for
     * @return controller found or null if no controllers of the given type
     */
    public GVRCursorController findCursorController(GVRControllerType type) {
        for (int index = 0, size = cache.size(); index < size; index++)
        {
            int key = cache.keyAt(index);
            GVRCursorController controller = cache.get(key);
            if (controller.getControllerType().equals(type)) {
                return controller;
            }
        }
        return null;
    }

    public GVRGearCursorController getGearController() {
        return gearCursorController;
    }

    /**
     * Queries the status of the connection to the Android wear watch.
     * @see IWearTouchpadEvents
     * @return true if android wear touchpad is connected, else false.
     */
    public boolean isConnectedToAndroidWearTouchpad() {
        if(androidWearTouchpad != null) {
            return androidWearTouchpad.isConnectedToWatch();
        }
        return false;
    }

    /**
     * Remove all controllers but leave input manager running.
     * @return number of controllers removed
     */
    public int clear()
    {
        int n = 0;
        for (GVRCursorController c : controllers)
        {
            c.stopDrag();
            removeCursorController(c);
            ++n;
        }
        return n;
    }

    /**
     * Shut down the input manager.
     *
     * After this call, GearVRf will not be able to access IO devices.
     */
    public void close()
    {
        inputManager.unregisterInputDeviceListener(inputDeviceListener);
        mouseDeviceManager.forceStopThread();
        gamepadDeviceManager.forceStopThread();
        controllerIds.clear();
        cache.clear();
        controllers.clear();
    }

    // returns null if no device is found.
    private GVRCursorController getUniqueControllerId(int deviceId) {
        GVRCursorController controller = controllerIds.get(deviceId);
        if (controller != null) {
            return controller;
        }
        return null;
    }

    private GVRControllerType getGVRInputDeviceType(InputDevice device) {
        if (device == null) {
            return GVRControllerType.UNKNOWN;
        }
        int sources = device.getSources();

        if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            return GVRControllerType.GAMEPAD;
        }

        int vendorId = device.getVendorId();
        int productId = device.getProductId();
        boolean isKeyBoard = ((sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD);
        boolean isTouchPad =   ((sources & InputDevice.SOURCE_TOUCHPAD) == InputDevice.SOURCE_TOUCHPAD);
        boolean isMouse =   ((sources & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE);

        if (isKeyBoard || isTouchPad) {
            // Allow gpio keyboard to be a gaze controller if enabled, also allow
            // any keyboard/touchpad device without a product/vendor id (assumed to be
            // system devices) to control the gaze controller.
            if (vendorId == GVRDeviceConstants.GPIO_KEYBOARD_VENDOR_ID
                    && productId == GVRDeviceConstants.GPIO_KEYBOARD_PRODUCT_ID
                    || (vendorId == 0 && productId == 0)) {
                return GVRControllerType.GAZE;
            }
        }

        if (isMouse) {
            // We do not want to add the Oculus touchpad as a mouse device.
            if (vendorId == GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_VENDOR_ID
                    && productId == GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID
                    || (vendorId == 0 && productId == 0)) {
                return GVRControllerType.GAZE;
            }
            return GVRControllerType.MOUSE;
        }
        return GVRControllerType.UNKNOWN;
    }

    // Return the key if there is one else return -1
    private int getCacheKey(InputDevice device, GVRControllerType controllerType) {
        if (controllerType != GVRControllerType.UNKNOWN &&
            controllerType != GVRControllerType.EXTERNAL) {
            // Sometimes a device shows up using two device ids
            // here we try to show both devices as one using the
            // product and vendor id

            int key = device.getVendorId();
            key = 31 * key + device.getProductId();
            key = 31 * key + controllerType.hashCode();

            return key;
        }
        return -1; // invalid key
    }

    // returns controller if a new device is found
    private GVRCursorController addDevice(int deviceId) {
        InputDevice device = inputManager.getInputDevice(deviceId);
        GVRControllerType controllerType = getGVRInputDeviceType(device);

        if (mEnabledControllerTypes == null)
        {
            return null;
        }
        if (controllerType == GVRControllerType.GAZE && !mEnabledControllerTypes.contains(GVRControllerType.GAZE))
        {
            return null;
        }

        int key;
        if (controllerType == GVRControllerType.GAZE) {
            // create the controller if there isn't one. 
            if (gazeCursorController == null) {
                gazeCursorController = new GVRGazeCursorController(context, GVRControllerType.GAZE,
                        GVRDeviceConstants.OCULUS_GEARVR_DEVICE_NAME,
                        GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_VENDOR_ID,
                        GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID);
            }
            // use the cached gaze key
            key = GAZE_CACHED_KEY;
        } else {
            key = getCacheKey(device, controllerType);
        }

        if (key != -1)
        {
            GVRCursorController controller = cache.get(key);
            if (controller == null)
            {
                if ((mEnabledControllerTypes == null) || !mEnabledControllerTypes.contains(controllerType))
                {
                    return null;
                }
                if (controllerType == GVRControllerType.MOUSE)
                {
                    controller = mouseDeviceManager.getCursorController(context, device.getName(), device.getVendorId(), device.getProductId());
                }
                else if (controllerType == GVRControllerType.GAMEPAD)
                {
                    controller = gamepadDeviceManager.getCursorController(context, device.getName(), device.getVendorId(), device.getProductId());
                }
                else if (controllerType == GVRControllerType.GAZE)
                {
                    controller = gazeCursorController;
                }
                cache.put(key, controller);
                controllerIds.put(device.getId(), controller);
                return controller;
            }
            else
            {
                controllerIds.put(device.getId(), controller);
            }
        }
        return null;
    }

    private GVRCursorController removeDevice(int deviceId)
    {
        /*
         * We can't use the inputManager here since the device has already been
         * detached and the inputManager would return a null. Instead use the
         * list of controllers to find the device and then do a reverse lookup
         * on the cached controllers to remove the cached entry.
         */
        GVRCursorController controller = controllerIds.get(deviceId);

        if (controller == null)
        {
            return null;
        }
        // Do a reverse lookup and remove the controller
        for (int index = 0; index < cache.size(); index++)
        {
            int key = cache.keyAt(index);
            GVRCursorController cachedController = cache.get(key);
            if (cachedController == controller)
            {
                controllerIds.remove(deviceId);
                if (controller.getControllerType() == GVRControllerType.MOUSE)
                {
                    mouseDeviceManager.removeCursorController(controller);
                }
                else if (controller.getControllerType() == GVRControllerType.GAMEPAD)
                {
                    gamepadDeviceManager.removeCursorController(controller);
                }
                cache.remove(key);
                return controller;
            }
        }
        controllerIds.remove(deviceId);
        return controller;
    }

    /**
     * Dispatch a {@link KeyEvent} to the {@link GVRInputManager}.
     *
     * @param event The {@link KeyEvent} to be processed.
     * @return <code>true</code> if the {@link KeyEvent} is handled by the
     * {@link GVRInputManager}, <code>false</code> otherwise.
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        GVRCursorController controller = getUniqueControllerId(event.getDeviceId());
        if (controller != null) {
            return controller.dispatchKeyEvent(event);
        }
        return false;
    }

    /**
     * Dispatch a {@link MotionEvent} to the {@link GVRInputManager}.
     *
     * @param event The {@link MotionEvent} to be processed.
     * @return <code>true</code> if the {@link MotionEvent} is handled by the
     * {@link GVRInputManager}, <code>false</code> otherwise.
     */
    public boolean dispatchMotionEvent(MotionEvent event) {
        GVRCursorController controller = getUniqueControllerId(event.getDeviceId());
        if ((controller != null) && controller.isEnabled()) {
            return controller.dispatchMotionEvent(event);
        }
        return false;
    }

    private InputDeviceListener inputDeviceListener = new InputDeviceListener() {

        @Override
        public void onInputDeviceRemoved(int deviceId) {
            GVRCursorController controller = removeDevice(deviceId);
            if (controller != null) {
                controller.setEnable(false);
            }
        }

        @Override
        public void onInputDeviceChanged(int deviceId) {
            // TODO: Not Used, see if needed.
        }

        @Override
        public void onInputDeviceAdded(int deviceId) {
            GVRCursorController controller = addDevice(deviceId);
            if (controller != null) {
                controller.setScene(context.getMainScene());
                controller.setEnable(true);
                addCursorController(controller);
            }
        }
    };

    /**
     * Add a {@link ICursorControllerListener} to the
     * {@link GVREventReceiver} of the {@link GVRInputManager} to
     * receive notifications whenever a {@link GVRCursorController} is added or
     * removed from the system at runtime.
     * @see #addCursorController(GVRCursorController)
     * @see #getEventReceiver()
     * @see GVREventReceiver#addListener
     */
    public interface ICursorControllerListener extends IEvents
    {
        /**
         * Called when a {@link GVRCursorController} is added to the system.
         *
         * Use {@link GVRCursorController#getId()} to uniquely identify the
         * {@link GVRCursorController} and use
         * {@link GVRCursorController#getControllerType()} to know its
         * {@link GVRControllerType}.
         *
         * @param controller
         *            the {@link GVRCursorController} added.
         */
        public void onCursorControllerAdded(GVRCursorController controller);

        /**
         * Called when the {@link GVRCursorController} previously added has been
         * removed.
         *
         * Use {@link GVRCursorController#getId()} to uniquely identify the
         * {@link GVRCursorController} and
         * {@link GVRCursorController#getControllerType()} to know its
         * {@link GVRControllerType}.
         *
         * @param controller
         *            the {@link GVRCursorController} removed.
         */
        public void onCursorControllerRemoved(GVRCursorController controller);
    };

    /**
     * Interface to listen for cursor controller selection events.
     * @see GVRInputManager#selectController()
     */
    public interface ICursorControllerSelectListener extends IEvents
    {
        public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController);
    }

    /**
     * Responds to cursor added or removed events and selects the highest
     * priority controller for the list of desired types.
     */
    protected static class SingleControllerSelector implements ICursorControllerListener
    {
        private ArrayList<GVRControllerType> mControllerTypes;
        private int mCurrentControllerPriority = -1;
        private GVRCursorController mCursorController = null;
        private GVRSceneObject mCursor = null;

        public SingleControllerSelector(GVRContext ctx, ArrayList<GVRControllerType> desiredTypes)
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
                if (gvrCursorController instanceof GVRGearCursorController)
                {
                    ((GVRGearCursorController) gvrCursorController).showControllerModel(true);
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
            GVRSceneObject cursor = new GVRSceneObject(ctx, 1, 1,
                                                       ctx.getAssetLoader().loadTexture(
                                                               new GVRAndroidResource(ctx, R.drawable.cursor)));
            GVRRenderData rdata = cursor.getRenderData();
            rdata.setDepthTest(false);
            rdata.disableLight();
            rdata.setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY + 10);
            cursor.getTransform().setScale(0.2f, 0.2f, 1.0f);
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
            controller.setEnable(true);
            ctx.getEventManager().sendEvent(ctx.getInputManager(),
                                            ICursorControllerSelectListener.class,
                                            "onCursorControllerSelected",
                                            controller,
                                            mCursorController);
            mCursorController = controller;
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
            if ((mCursorController == gvrCursorController) &&
                (gvrCursorController.getControllerType() != GVRControllerType.GAZE))
            {
                GVRContext ctx = gvrCursorController.getGVRContext();
                deselectController();
                GVRCursorController gaze = ctx.getInputManager().findCursorController(GVRControllerType.GAZE);
                if (null != gaze && gaze != gvrCursorController)
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
