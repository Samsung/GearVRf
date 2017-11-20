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

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class is obtained using the
 * {@link GVRContext#getInputManager()} call. Use this class to query for all
 * the {@link GVRCursorController} objects in the framework.
 *
 * Use the
 * {@link GVRInputManager#addCursorControllerListener(CursorControllerListener)}
 * call to notify the application whenever a {@link GVRCursorController} is
 * added or removed from the framework.
 *
 * Alternatively use the {@link GVRInputManager#getCursorControllers()} method
 * to know all the devices currently in the framework.
 *
 * The class also allows external input devices to be added using the
 * {@link GVRInputManager#addCursorController(GVRCursorController)} method.
 */
public abstract class GVRInputManager {
    private static final String TAG = GVRInputManager.class.getSimpleName();
    private static final String WEAR_TOUCH_PAD_SERVICE_PACKAGE_NAME = "org.gearvrf.weartouchpad";
    private final InputManager inputManager;
    private final GVRContext context;
    private GVRAndroidWearTouchpad androidWearTouchpad;
    private boolean useGazeCursorController;
    private GVRGamepadDeviceManager gamepadDeviceManager;
    private GVRMouseDeviceManager mouseDeviceManager;
    private GearCursorController gearCursorController;

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

    protected GVRInputManager(GVRContext context, boolean useGazeCursorController,
                              boolean useAndroidWearTouchpad) {
        Context androidContext = context.getContext();
        inputManager = (InputManager) androidContext
                .getSystemService(Context.INPUT_SERVICE);
        this.context = context;
        this.useGazeCursorController = useGazeCursorController;
        inputManager.registerInputDeviceListener(inputDeviceListener, null);
        controllerIds = new SparseArray<GVRCursorController>();
        cache = new SparseArray<GVRCursorController>();
        mouseDeviceManager = new GVRMouseDeviceManager(context);
        gamepadDeviceManager = new GVRGamepadDeviceManager();
        if(useAndroidWearTouchpad && checkIfWearTouchPadServiceInstalled(context)) {
            androidWearTouchpad = new GVRAndroidWearTouchpad(context);
        }
        scanDevices();
    }

    abstract public void scanControllers();

    private void scanDevices()
    {
        for (int deviceId : inputManager.getInputDeviceIds()) {
            addDevice(deviceId);
        }
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
     *
     * Ideally this call needs to be done inside
     * {@link org.gearvrf.GVRMain#onInit(GVRContext)} so that all the cursor objects are
     * set up before the rendering starts.
     *
     * Remember to add a {@link CursorControllerListener} to receive
     * notifications on {@link GVRCursorController} objects added or removed
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

    public GearCursorController getGearController() {
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

    protected void close() {
        inputManager.unregisterInputDeviceListener(inputDeviceListener);
        controllerIds.clear();
        cache.clear();
        mouseDeviceManager.forceStopThread();
        gamepadDeviceManager.forceStopThread();
        if (gazeCursorController != null) {
            gazeCursorController.close();
        }
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

        if ((sources & InputDevice.SOURCE_TOUCHSCREEN) == InputDevice.SOURCE_TOUCHSCREEN
                && (sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD) {
            return GVRControllerType.CONTROLLER;
        }

        if ((sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD ||
            (sources & InputDevice.SOURCE_TOUCHPAD) == InputDevice.SOURCE_TOUCHPAD) {
            // Allow gpio keyboard to be a gaze controller if enabled, also allow
            // any keyboard/touchpad device without a product/vendor id (assumed to be
            // system devices) to control the gaze controller.
            if (vendorId == GVRDeviceConstants.GPIO_KEYBOARD_VENDOR_ID
                    && productId == GVRDeviceConstants.GPIO_KEYBOARD_PRODUCT_ID
                    || (vendorId == 0 && productId == 0)) {
                return GVRControllerType.GAZE;
            }
        }

        if ((sources & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
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

        if (controllerType == GVRControllerType.GAZE && (false == useGazeCursorController)) {
            return null;
        }

        int key;
        if (controllerType == GVRControllerType.CONTROLLER) {
            key =  CONTROLLER_CACHED_KEY;
        }
        else if (controllerType == GVRControllerType.GAZE) {
            // create the controller if there isn't one. 
            if (gazeCursorController == null) {
                gazeCursorController = new GVRGazeCursorController(context, GVRControllerType.GAZE,
                        GVRDeviceConstants.OCULUS_GEARVR_DEVICE_NAME,
                        GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_VENDOR_ID,
                        GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID);
            }
            gazeCursorController.incrementReferenceCount();
            // use the cached gaze key
            key = GAZE_CACHED_KEY;
        } else {
            key = getCacheKey(device, controllerType);
        }

        if (key != -1) {
            GVRCursorController controller = cache.get(key);
            if (controller == null) {
                if (controllerType == GVRControllerType.CONTROLLER) {
                    controller = gearCursorController = new GearCursorController(context);
                }
                if (controllerType == GVRControllerType.MOUSE) {
                    controller = mouseDeviceManager
                            .getCursorController(context, device.getName(), device.getVendorId(),
                                    device.getProductId());
                } else if (controllerType == GVRControllerType.GAMEPAD) {
                    controller = gamepadDeviceManager
                            .getCursorController(context, device.getName(), device.getVendorId(),
                                    device.getProductId());
                } else if (controllerType == GVRControllerType.GAZE) {
                    controller = gazeCursorController;
                }
                cache.put(key, controller);
                controllerIds.put(device.getId(), controller);
                return controller;
            } else {
                controllerIds.put(device.getId(), controller);
            }
        }
        return null;
    }

    private GVRCursorController removeDevice(int deviceId) {
        /*
         * We can't use the inputManager here since the device has already been
         * detached and the inputManager would return a null. Instead use the
         * list of controllers to find the device and then do a reverse lookup
         * on the cached controllers to remove the cached entry.
         */
        GVRCursorController controller = controllerIds.get(deviceId);

        if (controller != null) {
            // Do a reverse lookup and remove the controller
            for (int index = 0; index < cache.size(); index++) {
                int key = cache.keyAt(index);
                GVRCursorController cachedController = cache.get(key);
                if (cachedController == controller) {
                    controllerIds.remove(deviceId);
                    if (controller.getControllerType() == GVRControllerType.MOUSE) {
                        mouseDeviceManager.removeCursorController(controller);
                    } else if (controller
                            .getControllerType() == GVRControllerType.GAMEPAD) {
                        gamepadDeviceManager.removeCursorController(controller);
                    } else if (controller.getControllerType() == GVRControllerType.GAZE) {
                        if(!((GVRGazeCursorController) controller).decrementReferenceCount()){
                            // do not remove the controller yet.
                            return null;
                        }
                    }
                    cache.remove(key);
                    return controller;
                }
            }
            controllerIds.remove(deviceId);
        }
        return null;
    }

    private InputDeviceListener inputDeviceListener = new InputDeviceListener() {

        @Override
        public void onInputDeviceRemoved(int deviceId) {
            GVRCursorController controller = removeDevice(deviceId);
            if (controller != null) {
                removeCursorController(controller);
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
                addCursorController(controller);
            }
        }
    };

    /**
     * Dispatch a {@link KeyEvent} to the {@link GVRInputManager}.
     *
     * @param event The {@link KeyEvent} to be processed.
     * @return <code>true</code> if the {@link KeyEvent} is handled by the
     * {@link GVRInputManager}, <code>false</code> otherwise.
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        GVRCursorController controller = getUniqueControllerId(
                event.getDeviceId());
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
        GVRCursorController controller = getUniqueControllerId(
                event.getDeviceId());
        if (controller != null) {
            return controller.dispatchMotionEvent(event);
        }
        return false;
    }

    /**
     * Add a {@link CursorControllerListener} to receive an event whenever a
     * {@link GVRCursorController} is added or removed from the framework.
     *
     * @param listener the {@link CursorControllerListener} to be added.
     */
    public abstract void addCursorControllerListener(
            CursorControllerListener listener);

    /**
     * Remove the previously added {@link CursorControllerListener}.
     *
     * @param listener the {@link CursorControllerListener} to be removed.
     */
    public abstract void removeCursorControllerListener(
            CursorControllerListener listener);

    /**
     * Define a {@link GVRCursorController} and add it to the
     * {@link GVRInputManager} for external input device handling by the
     * framework.
     *
     * @param controller the external {@link GVRCursorController} to be added to the
     *                   framework.
     */
    public abstract void addCursorController(GVRCursorController controller);

    /**
     * Remove the previously added {@link GVRCursorController} added to the
     * framework.
     *
     * @param controller the external {@link GVRCursorController} to be removed from
     *                   the framework.
     */
    public abstract void removeCursorController(GVRCursorController controller);

    /**
     * Signal the previously added {@link GVRCursorController} is now
     * active and generating events.
     * @param controller the external {@link GVRCursorController} that is active
     */
    public abstract void activateCursorController(GVRCursorController controller);

    /**
     * Signal the previously added {@link GVRCursorController} is no longer
     * active and is not generating events.
     * @param controller the external {@link GVRCursorController} that is inactive
     */
    public abstract void deactivateCursorController(GVRCursorController controller);
}
