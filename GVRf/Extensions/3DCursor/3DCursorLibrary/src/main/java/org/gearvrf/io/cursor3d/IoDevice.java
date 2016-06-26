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

import android.view.KeyEvent;

import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.io.GVRControllerType;

//TODO have this class extend GVRCursorController

/**
 * An {@link IoDevice} controls the position of the {@link Cursor}.
 * </p>
 * To add support for a new hardware device to control a {@link Cursor} extend this class and
 * implement a custom {@link IoDevice}. Call {@link IoDevice#setPosition(float, float, float)}
 * and {@link IoDevice#setKeyEvent (KeyEvent)} from the new class for changing the position of
 * the {@link Cursor} and sending {@link KeyEvent}s to the {@link Cursor} respectively. Each
 * {@link IoDevice} is identified by a unique set of <code>vendorId</code>,
 * <code>productId</code> and <code>deviceId</code>.
 */
public class IoDevice {
    private static final String TAG = IoDevice.class.getSimpleName();
    private final GVRCursorController gvrCursorController;
    private boolean isConnected = true;
    private IoDeviceConnectionListener connectionListener;
    private int vendorId;
    private int productId;
    private String deviceId;
    private String name;
    private String vendorName;

    /**
     * Create a new {@link IoDevice}. Call this from a subclass constructor.
     * <p/>
     * Each {@link IoDevice} is identified by a unique set of <code>vendorId</code>,
     * <code>productId</code> and <code>deviceId</code>, so make sure that all {@link IoDevice}s
     * you create are unique. The <code>vendorId</code>, <code>productId</code> and
     * <code>deviceId</code> you pass in here should match the values specified in the io.xml for
     * the particular {@link IoDevice}. This constructor assumes that the hardware device
     * associated with the {@link IoDevice} is already connected. An {@link IoDevice} created
     * using this constructor will be ready to be attached to a {@link Cursor}. To create an
     * {@link IoDevice} whose hardware device is not connected at initialization use {@link
     * IoDevice#IoDevice(String, int, int, String, String, boolean)}.
     *
     * @param deviceId   deviceId of the {@link IoDevice}. This differentiates
     *                   multiple devices from the same vendor with same productId and
     *                   vendorId.
     * @param vendorId   vendorId of the {@link IoDevice}.
     * @param productId  productId of the {@link IoDevice}.
     * @param name       name of the {@link IoDevice}. This is used to display the name of the
     *                   IoDevice to the User.
     * @param vendorName name of the vendor for the {@link IoDevice}
     */
    protected IoDevice(String deviceId, int vendorId, int productId, String name, String
            vendorName) {
        this(deviceId, vendorId, productId, name, vendorName, true);
    }

    /**
     * Create a new {@link IoDevice}. Call this from a subclass constructor.
     * <p/>
     * Each {@link IoDevice} is identified by a unique set of <code>vendorId</code>,
     * <code>productId</code> and <code>deviceId</code>, so make sure that all {@link IoDevice}s
     * you create are unique. The <code>vendorId</code>, <code>productId</code> and
     * <code>deviceId</code> you pass in here should match the values specified in the io.xml for
     * the particular {@link IoDevice}.
     *
     * @param deviceId    deviceId of the {@link IoDevice}. This differentiates
     *                    multiple devices from the same manufacturer with same productId and
     *                    vendorId.
     * @param vendorId    vendorId of the {@link IoDevice}.
     * @param productId   productId of the {@link IoDevice}.
     * @param name        name of the {@link IoDevice}. This is used to display the name of the
     *                    IoDevice to the User.
     * @param vendorName  name of the vendor for the {@link IoDevice}
     * @param isConnected <code>true</code> is the corresponding hardware device is connected at
     *                    initialization. <code>false</code> if the hardware device is not
     *                    connected at initialization. The {@link IoDevice#setConnected(boolean)}
     *                    call can be used to indicate the hardware device connection later.
     */
    protected IoDevice(String deviceId, int vendorId, int productId, String name, String
            vendorName, boolean isConnected) {
        this(deviceId, vendorId, productId, name, vendorName, isConnected, new
                GVRExternalCursorController());
    }

    IoDevice(String deviceId, int vendorId, int productId, String name, String
            vendorName, GVRCursorController gvrCursorController) {
        this(deviceId, vendorId, productId, name, vendorName, true, gvrCursorController);
    }

    private IoDevice(String deviceId, int vendorId, int productId, String name, String
            vendorName, boolean isConnected, GVRCursorController gvrCursorController) {
        this.vendorId = vendorId;
        this.productId = productId;
        this.deviceId = deviceId;
        this.name = name;
        this.vendorName = vendorName;
        this.isConnected = isConnected;
        this.gvrCursorController = gvrCursorController;
    }

    IoDevice() {
        gvrCursorController = null;
    }

    void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    /**
     * Get the vendorId associated with the {@link IoDevice}
     *
     * @return
     */
    public int getVendorId() {
        return vendorId;
    }

    void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * Get the productId associated with the {@link IoDevice}
     *
     * @return
     */
    public int getProductId() {
        return productId;
    }

    void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Get the deviceId associated with the {@link IoDevice}
     *
     * @return
     */
    public String getDeviceId() {
        return deviceId;
    }

    void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    /**
     * Get the name of the vendor for the {@link IoDevice}
     *
     * @return
     */
    public String getVendorName() {
        return vendorName;
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name associated with the {@link IoDevice}. The name is used to display the {@link
     * IoDevice} to the user.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    int getCursorControllerId() {
        return gvrCursorController.getId();
    }

    /**
     * Sets the state of the {@link IoDevice} to value of <code>enable</code>. <p/>
     * This
     * method is called with <code>false</code> when the {@link IoDevice} is not being
     * used and <code>true</code> when the {@link IoDevice} is being used. By
     * default the
     * value is set to <code>true</code> until a {@link IoDevice#setEnable(boolean)}
     * with a <code>false</code> value is called.
     *
     * @param enable Enabled value <code>true</code> or <code>false</code>
     */

    protected void setEnable(boolean enable) {
        gvrCursorController.setEnable(enable);
    }

    /**
     * Returns the state of the {@link IoDevice} if enabled or not. By
     * default the
     * value
     * is set to <code>true</code>. Enabled means it is being used, and Disabled
     * means it
     * is not
     * being used.
     *
     * @return <code>true</code> is enabled, <code>false</code> if disabled.
     */

    protected boolean isEnabled() {
        return gvrCursorController.isEnabled();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof IoDevice)) return false;

        IoDevice ioDevice = (IoDevice) o;

        if (vendorId != ioDevice.vendorId) return false;
        if (productId != ioDevice.productId) return false;
        return deviceId.equals(ioDevice.deviceId);
    }

    @Override
    public int hashCode() {
        int result = vendorId;
        result = 31 * result + productId;
        result = 31 * result + deviceId.hashCode();
        return result;
    }

    /**
     * Use this method from the {@link IoDevice} subclass to set the position of the
     * {@link Cursor}.
     *
     * @param x x value of the position
     * @param y y value of the position
     * @param z z value of the position
     */
    protected void setPosition(float x, float y, float z) {
        gvrCursorController.setPosition(x, y, z);
    }

    /**
     * Use this method from the {@link IoDevice} subclass to set the rotation of the
     * {@link Cursor} in quaternion terms.
     *
     * @param w 'W' component of the quaternion.
     * @param x 'X' component of the quaternion.
     * @param y 'Y' component of the quaternion.
     * @param z 'Z' component of the quaternion.
     */
    protected void setRotation(float w, float x, float y, float z) {
        GVRSceneObject sceneObject = gvrCursorController.getSceneObject();
        if (sceneObject != null) {
            sceneObject.getTransform().setRotation(w, x, y, z);
        }
    }

    /**
     * Use this method to turn on and off the visibility of the {@link Cursor}
     *
     * @param visible <code>true</code> makes the {@link Cursor} visible and
     *              <code>false</code> turns off its visibility.
     */
    protected void setVisible(boolean visible) {
        GVRSceneObject sceneObject = gvrCursorController.getSceneObject();
        if (sceneObject != null && sceneObject.isEnabled() != visible) {
            sceneObject.setEnable(visible);
        }
    }

    void setSceneObject(GVRSceneObject cursor) {
        gvrCursorController.setSceneObject(cursor);
    }

    void resetSceneObject() {
        gvrCursorController.resetSceneObject();
    }

    void addControllerEventListener(GVRCursorController.ControllerEventListener
                                            controllerEventListener) {
        gvrCursorController.addControllerEventListener(controllerEventListener);
    }

    void removeControllerEventListener(GVRCursorController.ControllerEventListener
                                               controllerEventListener) {
        gvrCursorController.removeControllerEventListener(controllerEventListener);
    }

    // TODO this has to go only used by CursorInputManager
    boolean contains(GVRCursorController gvrCursorController) {
        return this.gvrCursorController == gvrCursorController;
    }

    void setNearDepth(float nearDepth) {
        gvrCursorController.setNearDepth(nearDepth);
    }

    /**
     * Gets the value of the near depth set by the {@link Cursor} using the {@link IoDevice}. The
     * {@link Cursor} sets the near depth to indicate the minimum distance to maintain between the
     * origin and the cursor.
     *
     * @return the value of the near depth. By default the value is set to 0.
     */
    protected float getNearDepth() {
        if (gvrCursorController instanceof GVRExternalCursorController) {
            return ((GVRExternalCursorController) gvrCursorController).getNearDepth();
        } else {
            throw new UnsupportedOperationException("getNearDepth not supported");
        }
    }

    void setFarDepth(float farDepth) {
        gvrCursorController.setFarDepth(farDepth);
    }

    /**
     * Gets the value of the far depth set by the {@link Cursor} using the {@link IoDevice}. The
     * {@link Cursor} sets the far depth to indicate the maximum distance a cursor can travel
     * from the origin.
     *
     * @return the value of the far depth. By default the value is set to negative {@link
     * Float#MAX_VALUE}.
     */
    protected float getFarDepth() {
        if (gvrCursorController instanceof GVRExternalCursorController) {
            return ((GVRExternalCursorController) gvrCursorController).getFarDepth();
        } else {
            throw new UnsupportedOperationException("getFarDepth not supported");
        }
    }

    /**
     * Call this from a subclass of the {@link IoDevice} to send a corresponding {@link
     * CursorEvent}. Setting a {@link KeyEvent} with {@link KeyEvent#ACTION_DOWN} should generate
     * a {@link CursorEvent} with {@link CursorEvent#isActive()} set to <code>true</code>. A
     * {@link KeyEvent} with {@link KeyEvent#ACTION_UP} should generate a {@link CursorEvent}
     * with {@link CursorEvent#isActive()} set to <code>false</code>. The exact {@link KeyEvent}
     * can also be retrieved from the {@link CursorEvent} by calling {@link
     * CursorEvent#getKeyEvent()}
     *
     * @param keyEvent the event to be set.
     */
    protected void setKeyEvent(KeyEvent keyEvent) {
        if (gvrCursorController instanceof GVRExternalCursorController) {
            GVRExternalCursorController controller = (GVRExternalCursorController)
                    gvrCursorController;
            controller.setKeyEvent(keyEvent);
        } else {
            throw new UnsupportedOperationException("setKeyEvent not supported");
        }
    }

    void invalidate() {
        gvrCursorController.invalidate();
    }

    interface IoDeviceConnectionListener {
        void onIoDeviceConnected(IoDevice controller);

        void onIoDeviceDisconnected(IoDevice controller);
    }

    void setConnectionListener(IoDeviceConnectionListener listener) {
        this.connectionListener = listener;
    }

    IoDeviceConnectionListener getConnectionListener() {
        return connectionListener;
    }

    /**
     * Use this from a subclass of {@link IoDevice} to indicate the connection status of
     * the hardware associated with the {@link IoDevice}. For cases like a connected
     * bluetooth or USB device this can be set <code>true</code> when Bluetooth or USB
     * connection
     * is made and <code>false</code> when disconnected. If the hardware device is always
     * available the value can be set to <code>true</code> in the constructor and no calls to
     * this method are required. A {@link IoDevice} is only attached to a {@link Cursor}
     * if the {@link IoDevice#isConnected} value is true. On passing a
     * <code>false</code>
     * value here the {@link IoDevice} is removed from the {@link Cursor}.
     *
     * @param value <code>true</code> for connected and <code>false</code> for disconnected
     */
    protected final void setConnected(boolean value) {
        if (value && !isConnected) {
            isConnected = value;
            if (connectionListener != null) {
                connectionListener.onIoDeviceConnected(this);
            }
        } else if (!value && isConnected) {
            isConnected = value;
            if (connectionListener != null) {
                connectionListener.onIoDeviceDisconnected(this);
            }
        }
    }

    /**
     * Get connection state of the {@link IoDevice}
     *
     * @return <code>true</code> is the {@link IoDevice} has a connection to the associated
     * hardware device, <code>false</code> if there is not connection.
     */
    protected boolean isConnected() {
        return isConnected;
    }

    //TODO this will go once GVRf is changed
    static class GVRExternalCursorController extends GVRCursorController {
        GVRExternalCursorController() {
            super(GVRControllerType.EXTERNAL);
        }

        public void setKeyEvent(KeyEvent keyEvent) {
            super.setKeyEvent(keyEvent);
        }

        public float getNearDepth() {
            return super.getNearDepth();
        }

        public float getFarDepth() {
            return super.getFarDepth();
        }
    }

    GVRCursorController getGvrCursorController() {
        return gvrCursorController;
    }
}
