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
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * The input received from the {@link GVRCursorController} is dispatched across
 * the attached scene graph to activate the related sensors in the graph.
 * 
 * The {@link GVRBaseSensor} nodes provide the app with the {@link SensorEvent}s
 * generated as a result of the processing done by the
 * {@link GVRInputManagerImpl}.
 * 
 */
class GVRInputManagerImpl implements GVRInputManager {
    private static final String TAG = "GVRInputManagerImpl";
    private static final String WEAR_TOUCH_PAD_SERVICE_PACKAGE_NAME = "org.gearvrf.weartouchpad";

    private final List<CursorControllerListener> listeners = new ArrayList<>();
    private final List<GVRCursorController> controllers = new ArrayList<>();
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
    private final SparseArray<GVRCursorController> controllerIds = new SparseArray<>();

    // maintains the ids already distributed to a given device.
    // We make use of the vendor and product id to identify a device.
    private final SparseArray<GVRCursorController> cache = new SparseArray<>();


    GVRInputManagerImpl(GVRContext context, boolean useGazeCursorController,
                        boolean useAndroidWearTouchpad) {

        Context androidContext = context.getContext();
        inputManager = (InputManager) androidContext
                .getSystemService(Context.INPUT_SERVICE);
        this.context = context;
        this.useGazeCursorController = useGazeCursorController;

        inputManager.registerInputDeviceListener(inputDeviceListener, null);

        mouseDeviceManager = new GVRMouseDeviceManager(context);
        gamepadDeviceManager = new GVRGamepadDeviceManager();
        if(useAndroidWearTouchpad && checkIfWearTouchPadServiceInstalled(context)) {
            androidWearTouchpad = new GVRAndroidWearTouchpad(context);
        }

        scanDevices();
    }

    public void scanControllers()
    {
        for (GVRCursorController controller : getCursorControllers()) {
            addCursorController(controller);
        }
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
        controllers.add(controller);
        synchronized (listeners) {
            for (CursorControllerListener listener : listeners) {
                listener.onCursorControllerAdded(controller);
            }
        }
    }

    @Override
    public void activateCursorController(GVRCursorController controller) {
        controller.setScene(context.getMainScene());
        synchronized (listeners) {
            for (CursorControllerListener listener : listeners) {
                listener.onCursorControllerActive(controller);
            }
        }
    }

    @Override
    public void deactivateCursorController(GVRCursorController controller) {
        synchronized (listeners) {
            for (CursorControllerListener listener : listeners) {
                listener.onCursorControllerInactive(controller);
            }
        }
    }

    @Override
    public void removeCursorController(GVRCursorController controller) {
        controllers.remove(controller);
        controller.setScene(null);
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
    public List<GVRCursorController> getCursorControllers() {
        List<GVRCursorController> result = new ArrayList<>();
        for (int index = 0, size = cache.size(); index < size; index++) {
            int key = cache.keyAt(index);
            GVRCursorController controller = cache.get(key);
            result.add(controller);
        }
        return result;
    }

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

    @Override
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

    @Override
    public GearCursorController getGearController() {
        return gearCursorController;
    }

    @Override
    public boolean isConnectedToAndroidWearTouchpad() {
        if(androidWearTouchpad != null) {
            return androidWearTouchpad.isConnectedToWatch();
        }
        return false;
    }

    void close() {
        if (controllers.size() > 0)
        {
            controllers.get(0).removePickEventListener(GVRBaseSensor.getPickHandler());
        }

        inputManager.unregisterInputDeviceListener(inputDeviceListener);
        controllerIds.clear();
        cache.clear();
        mouseDeviceManager.forceStopThread();
        gamepadDeviceManager.forceStopThread();
        if (gazeCursorController != null) {
            gazeCursorController.close();
        }

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
                gazeCursorController = new GVRGazeCursorController(context, this,
                        GVRControllerType.GAZE,
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

    private InputManager.InputDeviceListener inputDeviceListener = new InputManager.InputDeviceListener() {

        @Override
        public void onInputDeviceRemoved(int deviceId) {
            GVRCursorController controller = removeDevice(deviceId);
            if (controller != null) {
                removeCursorController(controller);
            }
        }

        @Override
        public void onInputDeviceChanged(int deviceId) {
        }

        @Override
        public void onInputDeviceAdded(int deviceId) {
            GVRCursorController controller = addDevice(deviceId);
            if (controller != null) {
                addCursorController(controller);
            }
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        GVRCursorController controller = getUniqueControllerId(
                event.getDeviceId());
        if (controller != null) {
            return controller.dispatchKeyEvent(event);
        }
        return false;
    }

    @Override
    public boolean dispatchMotionEvent(MotionEvent event) {
        GVRCursorController controller = getUniqueControllerId(
                event.getDeviceId());
        if (controller != null) {
            return controller.dispatchMotionEvent(event);
        }
        return false;
    }
}
