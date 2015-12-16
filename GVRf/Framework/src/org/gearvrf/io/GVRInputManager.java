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

import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;

import android.util.Log;
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
 * {@link GVRInputManager#addGVRCursorController(GVRCursorController)} method.
 * 
 * 
 */
public abstract class GVRInputManager {
    private static final String TAG = GVRInputManager.class.getSimpleName();
    private final InputManager inputManager;
    private final GVRContext context;
    private GVRGamepadDeviceManager gamepadDeviceManager;
    private GVRMouseDeviceManager mouseDeviceManager;

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

    protected GVRInputManager(GVRContext context) {
        Context androidContext = context.getContext();
        inputManager = (InputManager) androidContext
                .getSystemService(Context.INPUT_SERVICE);
        this.context = context;
        inputManager.registerInputDeviceListener(inputDeviceListener, null);
        controllerIds = new SparseArray<GVRBaseController>();
        cache = new LongSparseArray<GVRBaseController>();
        mouseDeviceManager = new GVRMouseDeviceManager(androidContext);
        gamepadDeviceManager = new GVRGamepadDeviceManager(androidContext,
                new int[] { KeyEvent.KEYCODE_BUTTON_A,
                        KeyEvent.KEYCODE_BUTTON_B, KeyEvent.KEYCODE_BUTTON_X,
                        KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_L1,
                        KeyEvent.KEYCODE_BUTTON_R1, KeyEvent.KEYCODE_BUTTON_L2,
                        KeyEvent.KEYCODE_BUTTON_R2, KeyEvent.KEYCODE_BUTTON_Y,
                        KeyEvent.KEYCODE_BUTTON_Z });
        for (int deviceId : inputManager.getInputDeviceIds()) {
            InputDevice device = inputManager.getInputDevice(deviceId);
            addDevice(device);
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
    protected List<GVRCursorController> getCursorControllers() {
        List<GVRCursorController> result = new ArrayList<GVRCursorController>();
        for (int index = 0, size = controllerIds
                .size(); index < size; index++) {
            int key = controllerIds.keyAt(index);
            GVRBaseController controller = controllerIds.get(key);
            result.add(controller);
        }
        return result;
    }

    protected void close() {
        inputManager.unregisterInputDeviceListener(inputDeviceListener);
        mouseDeviceManager.stop();
        gamepadDeviceManager.stop();
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
            if ((device.getSources()
                    & InputDevice.SOURCE_MOUSE) == InputDevice.SOURCE_MOUSE) {
                return GVRCursorType.MOUSE;
            } else if ((device.getSources()
                    & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                    || (device.getSources()
                            & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
                return GVRCursorType.CONTROLLER;
            }
        }
        return GVRCursorType.UNKNOWN;
    }

    // returns true if a new device is found
    private boolean addDevice(InputDevice device) {
        Log.d(TAG, "onInputDeviceAdded " + device.getName());
        GVRCursorType cursorType = getGVRInputDeviceType(device);
        if (cursorType != GVRCursorType.UNKNOWN
                && cursorType != GVRCursorType.EXTERNAL) {
            int vendorId = device.getVendorId();
            int productId = device.getProductId();

            // We do not want to add the Oculus touchpad as a mouse device.
            if (vendorId == GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_VENDOR_ID
                    && productId == GVRDeviceConstants.OCULUS_GEARVR_TOUCHPAD_PRODUCT_ID) {
                return false;
            }

            long key = device.getVendorId() << 32;
            key = key | device.getProductId();
            GVRBaseController controller = cache.get(key);

            if (controller == null) {
                if (cursorType == GVRCursorType.MOUSE) {
                    controller = mouseDeviceManager
                            .getGVRCursorController(context);
                } else if (cursorType == GVRCursorType.CONTROLLER) {
                    controller = gamepadDeviceManager
                            .getGVRCursorController(context);
                }
                cache.put(key, controller);
            }
            controllerIds.put(device.getId(), controller);
            return true;
        }
        return false;
    }

    private InputDeviceListener inputDeviceListener = new InputDeviceListener() {

        @Override
        public void onInputDeviceRemoved(int deviceId) {
            Log.d(TAG, "onInputDeviceRemoved " + deviceId);
            GVRBaseController controller = controllerIds.get(deviceId);
            if (controller != null) {
                removeGVRCursorController(controller);
                controllerIds.remove(deviceId);
            }
        }

        @Override
        public void onInputDeviceChanged(int deviceId) {
            // TODO: Not Used, see if needed.
        }

        @Override
        public void onInputDeviceAdded(int deviceId) {
            // Sometimes a device shows up using two device ids
            // here we try to show both devices as one using the
            // product and vendor id
            Log.d(TAG, "onInputDeviceAdded " + deviceId);

            InputDevice inputDevice = inputManager.getInputDevice(deviceId);

            if (addDevice(inputDevice)) {
                GVRBaseController controller = controllerIds.get(deviceId);
                addGVRCursorController(controller);
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
    public abstract void addGVRCursorController(GVRCursorController controller);

    /**
     * Remove the previously added {@link GVRCursorController} added to the
     * framework.
     * 
     * @param controller
     *            the external {@link GVRCursorController} to be removed from
     *            the framework.
     */
    public abstract void removeGVRCursorController(
            GVRCursorController controller);

}