/*
 * Copyright (c) 2016. Samsung Electronics Co., LTD
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf.io.cursor3d;

import org.gearvrf.GVRCursorController;

class IoDeviceLoader {
    private static final String TAG = IoDeviceLoader.class.getSimpleName();

    public static final int MOUSE_VENDOR_ID = 0;
    public static final int MOUSE_PRODUCT_ID = 0;
    public static final String MOUSE_DEVICE_ID = "mouse";
    public static final String MOUSE_VENDOR_NAME = null;
    public static final String MOUSE_NAME = "PC Mouse";

    public static final int GAMEPAD_VENDOR_ID = 1;
    public static final int GAMEPAD_PRODUCT_ID = 1;
    public static final String GAMEPAD_DEVICE_ID = "android_gamepad";
    public static final String GAMEPAD_VENDOR_NAME = null;
    public static final String GAMEPAD_NAME = "Gamepad";

    public static final int SAMSUNG_VENDOR_ID = 1256;
    public static final int GEARVR_PRODUCT_ID = 42240;
    public static final String GEARVR_DEVICE_ID = "gearvr";
    public static final String SAMSUNG_VENDOR_NAME = "Samsung";
    public static final String GEARVR_NAME = "Gear VR";

    static boolean isMouseIoDevice(IoDevice ioDevice) {
        return (ioDevice.getVendorId() == MOUSE_VENDOR_ID && ioDevice.getProductId() ==
                MOUSE_PRODUCT_ID && ioDevice.getDeviceId().equals(MOUSE_DEVICE_ID));
    }

    static boolean isGearVrDevice(IoDevice ioDevice) {
        return (ioDevice.getVendorId() == SAMSUNG_VENDOR_ID && ioDevice.getProductId() ==
                GEARVR_PRODUCT_ID && ioDevice.getDeviceId().equals(GEARVR_DEVICE_ID));
    }

    static IoDevice getIoDevice(GVRCursorController gvrController) {
        switch (gvrController.getControllerType()) {
            case MOUSE:
                return new IoDevice(MOUSE_DEVICE_ID, MOUSE_VENDOR_ID, MOUSE_PRODUCT_ID,
                        MOUSE_NAME, MOUSE_VENDOR_NAME, gvrController);
            case CONTROLLER:
                return new IoDevice(GAMEPAD_DEVICE_ID, GAMEPAD_VENDOR_ID, GAMEPAD_PRODUCT_ID,
                        GAMEPAD_NAME, GAMEPAD_VENDOR_NAME, gvrController);
            case GAZE:
                return new IoDevice(GEARVR_DEVICE_ID, SAMSUNG_VENDOR_ID, GEARVR_PRODUCT_ID,
                        GEARVR_NAME, SAMSUNG_VENDOR_NAME, gvrController);
            default:
                throw new IllegalArgumentException("Cannot get path for " + gvrController
                        .getControllerType());
        }
    }

    static IoDevice getGearVrIoDevice() {
        return new IoDevice(GEARVR_DEVICE_ID, SAMSUNG_VENDOR_ID, GEARVR_PRODUCT_ID,
                GEARVR_NAME, SAMSUNG_VENDOR_NAME, null);
    }
}
