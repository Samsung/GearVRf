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

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.io.cursor3d.IoDevice.IoDeviceConnectionListener;
import org.gearvrf.utility.Log;

import java.util.ArrayList;
import java.util.List;

class CursorInputManager {
    private static final String TAG = CursorInputManager.class.getSimpleName();
    private static CursorInputManager inputManager;
    private GVRInputManager gvrInputManager;
    private List<IoDeviceListener> ioDeviceListeners;
    private List<IoDevice> availableIoDevices;
    private List<IoDevice> unavailableIoDevices;
    private static final Object lock = new Object();

    private CursorInputManager(GVRContext context) {
        gvrInputManager = context.getInputManager();
        availableIoDevices = new ArrayList<IoDevice>();
        unavailableIoDevices = new ArrayList<IoDevice>();
        ioDeviceListeners = new ArrayList<IoDeviceListener>();
        List<GVRCursorController> gvrCursorControllers = gvrInputManager.getCursorControllers();
        for (GVRCursorController gvrController : gvrCursorControllers) {
            IoDevice ioDevice = IoDeviceLoader.getIoDevice(gvrController);
            addIoDevice(ioDevice);
        }
    }

    //TODO fix the odd singleton pattern
    static CursorInputManager getInstance(GVRContext context) {
        if (inputManager == null) {
            inputManager = new CursorInputManager(context);
            return inputManager;
        }
        return inputManager;
    }

    List<IoDevice> getAvailableIoDevices() {
        List<IoDevice> returnList;
        synchronized (lock) {
            returnList = new ArrayList<IoDevice>(availableIoDevices);
        }
        return returnList;
    }

    void addIoDeviceListener(IoDeviceListener ioDeviceListener) {
        if (ioDeviceListeners.size() == 0) {
            gvrInputManager.addCursorControllerListener(gvrControllerListener);
        }
        ioDeviceListeners.add(ioDeviceListener);
    }

    void removeIoDeviceListener(IoDeviceListener cursorIoDeviceListener) {
        ioDeviceListeners.remove(cursorIoDeviceListener);
        if (ioDeviceListeners.size() == 0) {
            gvrInputManager.removeCursorControllerListener(gvrControllerListener);
        }
    }

    private CursorControllerListener gvrControllerListener = new CursorControllerListener() {
        @Override
        public void onCursorControllerAdded(GVRCursorController gvrCursorController) {
            if (gvrCursorController.getControllerType() == GVRControllerType.EXTERNAL) {
                return;
            }
            IoDevice addedIoDevice = IoDeviceLoader.getIoDevice(gvrCursorController);
            addIoDevice(addedIoDevice);
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController gvrCursorController) {
            // remove from list
            IoDevice removedIoDevice = null;
            if (gvrCursorController.getControllerType() == GVRControllerType.EXTERNAL) {
                return;
            }
            synchronized (lock) {
                for (int i = 0; i < availableIoDevices.size(); i++) {
                    if (availableIoDevices.get(i).contains(gvrCursorController)) {
                        removedIoDevice = availableIoDevices.get(i);
                        availableIoDevices.remove(i);
                        for (IoDeviceListener ioDeviceListener : ioDeviceListeners) {
                            ioDeviceListener.onIoDeviceRemoved(removedIoDevice);
                        }
                        break;
                    }
                }
            }
        }
    };

    interface IoDeviceListener {
        void onIoDeviceAdded(IoDevice ioDevice);

        void onIoDeviceRemoved(IoDevice ioDevice);
    }

    private IoDeviceConnectionListener ioDeviceConnectionListener = new
            IoDeviceConnectionListener() {
                @Override
                public void onIoDeviceConnected(IoDevice ioDevice) {
                    Log.d(TAG, "On IoDevice connected:" + ioDevice.getDeviceId());
                    synchronized (lock) {
                        if (unavailableIoDevices.remove(ioDevice)) {
                            gvrInputManager.addCursorController(ioDevice.getGvrCursorController());
                            availableIoDevices.add(ioDevice);
                            for (IoDeviceListener ioDeviceListener : ioDeviceListeners) {
                                ioDeviceListener.onIoDeviceAdded(ioDevice);
                            }
                        }
                    }
                }

                @Override
                public void onIoDeviceDisconnected(IoDevice ioDevice) {
                    Log.d(TAG, "On IoDevice disconnected:" + ioDevice.getDeviceId());
                    synchronized (lock) {
                        if (availableIoDevices.remove(ioDevice)) {
                            gvrInputManager.removeCursorController(ioDevice
                                    .getGvrCursorController());
                            unavailableIoDevices.add(ioDevice);
                            for (IoDeviceListener ioDeviceListener : ioDeviceListeners) {
                                ioDeviceListener.onIoDeviceRemoved(ioDevice);
                            }
                        }
                    }
                }
            };

    void addIoDevice(IoDevice addedIoDevice) {
        if (addedIoDevice.isConnected()) {
            Log.d(TAG, "Added a IoDevice which is already connected:" + addedIoDevice
                    .getDeviceId());
            GVRCursorController gvrCursorController = addedIoDevice.getGvrCursorController();
            synchronized (lock) {
                if (gvrCursorController.getControllerType() == GVRControllerType.EXTERNAL) {
                    gvrInputManager.addCursorController(gvrCursorController);
                }
                availableIoDevices.add(addedIoDevice);
                for (IoDeviceListener listener : ioDeviceListeners) {
                    listener.onIoDeviceAdded(addedIoDevice);
                }
            }
        } else {
            Log.d(TAG, "Added a ioDevice which is not connected:" + addedIoDevice
                    .getDeviceId());
            synchronized (lock) {
                unavailableIoDevices.add(addedIoDevice);
            }
        }
        addedIoDevice.setConnectionListener(ioDeviceConnectionListener);
    }
}
