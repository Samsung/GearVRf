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

import java.util.ArrayList;
import java.util.List;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRScript;
import org.gearvrf.utility.Log;

import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;

import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * 
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
 * 
 */
public abstract class GVRInputManager {
    private static final String TAG = GVRInputManager.class.getSimpleName();
    private final InputManager inputManager;
    private final GVRContext context;
    private boolean useGazeCursorController;
    private GVRGamepadDeviceManager gamepadDeviceManager;
    private GVRMouseDeviceManager mouseDeviceManager;
    
    // maintain one instance of the gazeCursorController
    private GVRGazeCursorController gazeCursorController;

    private static final long GAZE_CACHED_KEY = GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_VENDOR_ID << 32
            | GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID;

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
    private final SparseArray<GVRBaseController> controllerIds;

    // maintains the ids already distributed to a given device.
    // We make use of the vendor and product id to identify a device.
    private final LongSparseArray<GVRBaseController> cache;

    protected GVRInputManager(GVRContext context,
            boolean useGazeCursorController) {
        Context androidContext = context.getContext();
        inputManager = (InputManager) androidContext
                .getSystemService(Context.INPUT_SERVICE);
        this.context = context;
        this.useGazeCursorController = useGazeCursorController;
        inputManager.registerInputDeviceListener(inputDeviceListener, null);
        controllerIds = new SparseArray<GVRBaseController>();
        cache = new LongSparseArray<GVRBaseController>();
        mouseDeviceManager = new GVRMouseDeviceManager(androidContext);
        gamepadDeviceManager = new GVRGamepadDeviceManager(androidContext);
        for (int deviceId : inputManager.getInputDeviceIds()) {
            addDevice(deviceId);
        }
    }

    /**
     * Get a list of the {@link GVRCursorController}s currently in the system.
     * 
     * Ideally this call needs to be done inside
     * {@link GVRScript#onInit(GVRContext)} so that all the cursor objects are
     * set up before the rendering starts.
     * 
     * Remember to add a {@link CursorControllerListener} to receive
     * notifications on {@link GVRCursorController} objects added or removed
     * during runtime.
     * 
     * @return a list of all the {@link GVRCursorController} objects in the
     *         system.
     */
    public List<GVRCursorController> getCursorControllers() {
        List<GVRCursorController> result = new ArrayList<GVRCursorController>();
        for (int index = 0, size = cache.size(); index < size; index++) {
            long key = cache.keyAt(index);
            GVRBaseController controller = cache.get(key);
            result.add(controller);
        }
        return result;
    }

    protected void close() {
        inputManager.unregisterInputDeviceListener(inputDeviceListener);
        controllerIds.clear();
        cache.clear();
        mouseDeviceManager.stop();
        gamepadDeviceManager.stop();
        if(gazeCursorController != null){
            gazeCursorController.close();
        }
    }

    // returns null if no device is found.
    private GVRBaseController getUniqueControllerId(int deviceId) {
        GVRBaseController controller = controllerIds.get(deviceId);
        if (controller != null) {
            return controller;
        }
        return null;
    }

    private GVRCursorType getGVRInputDeviceType(InputDevice device) {
        if (device != null) {
            int sources = device.getSources();
            
            if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                return GVRCursorType.CONTROLLER;
            } 
            
            int vendorId = device.getVendorId();
            int productId = device.getProductId();
            
            if ((sources & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
                // We do not want to add the Oculus touchpad as a mouse device.
                if (vendorId == GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_VENDOR_ID
                        && productId == GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID) {
                    return  GVRCursorType.GAZE;
                            
                }
                return GVRCursorType.MOUSE;
            } 
            
            if ((sources & InputDevice.SOURCE_KEYBOARD) == InputDevice.SOURCE_KEYBOARD || 
                    (sources & InputDevice.SOURCE_TOUCHPAD) == InputDevice.SOURCE_TOUCHPAD) {
                // Allow gpio keyboard to be a gaze controller if enabled, also allow 
                // any keyboard/touchpad device without a product/vendor id (assumed to be 
                // system devices) to control the gaze controller. 
                if (vendorId == GVRDeviceConstants.GPIO_KEYBOARD_VENDOR_ID
                        && productId == GVRDeviceConstants.GPIO_KEYBOARD_PRODUCT_ID
                        || (vendorId == 0  && productId == 0)) {
                    return  GVRCursorType.GAZE;                 
                }
            }
        }
        return GVRCursorType.UNKNOWN;
    }

    // Return the key if there is one else return -1
    private long getCacheKey(InputDevice device, GVRCursorType cursorType) {
        if (cursorType != GVRCursorType.UNKNOWN
                && cursorType != GVRCursorType.EXTERNAL) {
            // Sometimes a device shows up using two device ids
            // here we try to show both devices as one using the
            // product and vendor id

            long key = device.getVendorId() << 32;
            key = key | device.getProductId();

            return key;
        }
        return -1; // invalid key
    }

    // returns controller if a new device is found
    private GVRBaseController addDevice(int deviceId) {
        InputDevice device = inputManager.getInputDevice(deviceId);
        GVRCursorType cursorType = getGVRInputDeviceType(device);
        
        if(cursorType == GVRCursorType.GAZE && (false == useGazeCursorController)){
            return null;
        }

        long key;
        if (cursorType == GVRCursorType.GAZE) {
            // create the controller if there isn't one. 
            if(gazeCursorController == null){
                gazeCursorController = new GVRGazeCursorController(context,
                    GVRCursorType.GAZE);
            }
            gazeCursorController.incrementReferenceCount();
            // use the cached gaze key
            key = GAZE_CACHED_KEY;
        } else {
            key = getCacheKey(device, cursorType);
        }
        
        if (key != -1) {
            GVRBaseController controller = cache.get(key);
            if (controller == null) {
                if (cursorType == GVRCursorType.MOUSE) {
                    controller = mouseDeviceManager
                            .getCursorController(context);
                } else if (cursorType == GVRCursorType.CONTROLLER) {
                    controller = gamepadDeviceManager
                            .getCursorController(context);
                } else if (cursorType == GVRCursorType.GAZE) {                   
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

    private GVRBaseController removeDevice(int deviceId) {
        /*
         * We can't use the inputManager here since the device has already been
         * detached and the inputManager would return a null. Instead use the
         * list of controllers to find the device and then do a reverse lookup
         * on the cached controllers to remove the cached entry.
         */
        GVRBaseController controller = controllerIds.get(deviceId);

        if (controller != null) {
            // Do a reverse lookup and remove the controller
            for (int index = 0; index < cache.size(); index++) {
                long key = cache.keyAt(index);
                GVRBaseController cachedController = cache.get(key);
                if (cachedController == controller) {
                    cache.remove(key);
                    controllerIds.remove(deviceId);
                    if (controller.getCursorType() == GVRCursorType.MOUSE) {
                        mouseDeviceManager.removeCursorController(controller);
                    } else if (controller
                            .getCursorType() == GVRCursorType.CONTROLLER) {
                        gamepadDeviceManager.removeCursorController(controller);
                    } else
                        if (controller.getCursorType() == GVRCursorType.GAZE) {
                        ((GVRGazeCursorController) controller)
                                .decrementReferenceCount();
                    }
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
            GVRBaseController controller = removeDevice(deviceId);
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
            GVRBaseController controller = addDevice(deviceId);
            if (controller != null) {
                addCursorController(controller);
            }
        }
    };

    /**
     * Dispatch a {@link KeyEvent} to the {@link GVRInputManager}.
     * 
     * @param event
     *            The {@link KeyEvent} to be processed.
     * @return <code>true</code> if the {@link KeyEvent} is handled by the
     *         {@link GVRInputManager}, <code>false</code> otherwise.
     * 
     */
    public boolean dispatchKeyEvent(KeyEvent event) {
        GVRBaseController controller = getUniqueControllerId(
                event.getDeviceId());
        if (controller != null) {
            return controller.dispatchKeyEvent(event);
        }
        return false;
    }

    /**
     * Dispatch a {@link MotionEvent} to the {@link GVRInputManager}.
     * 
     * @param event
     *            The {@link MotionEvent} to be processed.
     * @return <code>true</code> if the {@link MotionEvent} is handled by the
     *         {@link GVRInputManager}, <code>false</code> otherwise.
     * 
     */
    public boolean dispatchMotionEvent(MotionEvent event) {
        GVRBaseController controller = getUniqueControllerId(
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
     * @param listener
     *            the {@link CursorControllerListener} to be added.
     */
    public abstract void addCursorControllerListener(
            CursorControllerListener listener);

    /**
     * Remove the previously added {@link CursorControllerListener}.
     * 
     * @param listener
     *            the {@link CursorControllerListener} to be removed.
     */
    public abstract void removeCursorControllerListener(
            CursorControllerListener listener);

    /**
     * Define a {@link GVRCursorController} and add it to the
     * {@link GVRInputManagerImpl} for external input device handling by the
     * framework.
     * 
     * @param controller
     *            the external {@link GVRCursorController} to be added to the
     *            framework.
     */
    public abstract void addCursorController(GVRCursorController controller);

    /**
     * Remove the previously added {@link GVRCursorController} added to the
     * framework.
     * 
     * @param controller
     *            the external {@link GVRCursorController} to be removed from
     *            the framework.
     */
    public abstract void removeCursorController(GVRCursorController controller);
}