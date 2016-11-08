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

package org.gearvrf.io.cursor3d;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRCursorController.ControllerEventListener;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.SensorEvent;
import org.gearvrf.io.cursor3d.CursorAsset.Action;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a 3D cursor in GVRf, it encapsulates the intended behavior from a
 * device controlled 3D object.
 * <p>
 * While the application cannot create a {@link Cursor} object. There are two ways to obtain one:
 * <ul>
 * <li>Query the {@link CursorManager} for the currently available cursor objects using
 * {@link CursorManager#getActiveCursors()}. This call requests the {@link CursorManager} for
 * all the active {@link Cursor} objects with the {@link CursorManager}.</li>
 * <li> Use the {@link
 * CursorActivationListener} to know about {@link Cursor} objects added or removed from the
 * {@link CursorManager} during runtime.</li> </ul> </p>
 * <p>
 * As a general rule it an application would query the {@link CursorManager} for all available
 * {@link Cursor} objects on initialization using (1) and get updates about objects added or
 * removed during runtime using (2). Changes made to the {@link CursorManager} can lead to the
 * addition or removal of {@link Cursor}objects. For eg. switching {@link CursorType}s or setting
 * a new  {@link IoDevice} that supports multiple {@link Cursor} objects.
 * </p>
 * Each {@link Cursor} object has a {@link CursorType} that defines the type of events generated.
 */
public abstract class Cursor {
    private static final String TAG = Cursor.class.getSimpleName();
    private GVRContext context;
    protected GVRSceneObject cursor;
    private String name;
    private final CursorType type;
    private IoDevice savedIoDevice;
    private Position position;
    private String savedThemeId;
    private List<PriorityIoDeviceTuple> compatibleIoDevices;

    private static final float SQRT_2 = (float) Math.sqrt(2);

    private static int uniqueCursorId = 0;
    private final int cursorId;
    protected GVRScene scene;
    protected CursorSceneObject cursorSceneObject;

    private List<CursorEventListener> cursorEventListeners;
    private CursorTheme cursorTheme;
    private MouseEventListener mouseEventListener;
    private boolean busyLoading = false;
    IoDevice ioDevice;
    float scale;
    private boolean enabled = true;

    private CursorAsset currentCursorAsset;
    // Used to save a copy of the current cursor when busy loading is set.
    private CursorAsset savedCursorAsset;
    private CursorAudioManager audioManager;
    private CursorManager cursorManager;
    private Vector3f objectPosition, direction;

    enum Position {
        CENTER,
        LEFT,
        RIGHT,
        OTHER
    }

    Cursor(GVRContext context, CursorType type, CursorManager cursorManager) {
        this.context = context;
        this.type = type;
        this.cursorId = uniqueCursorId++;
        cursorSceneObject = new CursorSceneObject(context, cursorId);
        cursorEventListeners = new CopyOnWriteArrayList<CursorEventListener>();
        audioManager = CursorAudioManager.getInstance(context.getContext());
        compatibleIoDevices = new ArrayList<PriorityIoDeviceTuple>();
        objectPosition = new Vector3f();
        direction = new Vector3f();
        this.cursorManager = cursorManager;
    }

    void setIoDevice(IoDevice newIoDevice) {
        destroyIoDevice(this.ioDevice);
        this.ioDevice = newIoDevice;
        if (enabled) {
            setupIoDevice(newIoDevice);
        }
    }

    void resetIoDevice(IoDevice ioDevice) {
        if (this.ioDevice == ioDevice) {
            if (enabled) {
                destroyIoDevice(ioDevice);
            }
            this.ioDevice = null;
        }
    }

    /**
     * Set a {@link CursorTheme} for this cursor. Use the {@link CursorManager#getCursorThemes()}
     * call to know all the available cursor themes.
     *
     * Make sure that the theme cursor type matches this {@link Cursor} else this call will return
     * an {@link IllegalArgumentException}.
     *
     * @param theme the {@link CursorTheme} to be set.
     */
    public void setCursorTheme(final CursorTheme theme) {

        if (theme == cursorTheme || theme == null) {
            //nothing to do, return
            return;
        }
        if (theme.getCursorType() != type) {
            throw new IllegalArgumentException("Cursor Theme does not match the cursor type");
        }

        if (currentCursorAsset != null) {
            currentCursorAsset.reset(cursorSceneObject);
        }

        if (cursorTheme != null) {
            cursorTheme.unload(cursorSceneObject);
        }

        cursorTheme = theme;
        audioManager.loadTheme(cursorTheme);
        theme.load(cursorSceneObject);
        if (scene != null) {
            // new objects have been added to the cursorSceneObject
            // force bind shaders
            scene.bindShaders(cursorSceneObject.getMainSceneObject());
        }
        if (currentCursorAsset != null) {
            currentCursorAsset = cursorTheme.getAsset(currentCursorAsset.getAction());
            if (currentCursorAsset == null) {
                currentCursorAsset = cursorTheme.getAsset(Action.DEFAULT);
            }
            currentCursorAsset.set(cursorSceneObject);
        }
    }

    /**
     * Use this call to return the currently set {@link CursorTheme}.
     *
     * @return the currently set {@link CursorTheme}
     */
    public CursorTheme getCursorTheme() {
        return cursorTheme;
    }

    void setScene(GVRScene scene) {
        this.scene = scene;
    }

    // Means that the ioDevice is active
    boolean isActive() {
        return ioDevice != null && ioDevice.isEnabled();
    }

    /**
     * Get the type of this {@link Cursor} object. Look at
     * {@link CursorType} to know more about the various types of cursor objects created by the
     * {@link CursorManager}.
     *
     * @return the {@link CursorType} of this {@link Cursor} object.
     */
    public CursorType getCursorType() {
        return type;
    }

    /**
     * Set a new Cursor position if active and enabled.
     *
     * @param x x value of the position
     * @param y y value of the position
     * @param z z value of the position
     */
    public void setPosition(float x, float y, float z) {
        if (isActive() && enabled) {
            if (ioDevice != null) {
                ioDevice.setPosition(x, y, z);
            }
        }
    }

    /**
     * Get the current absolute x position of this {@link Cursor}.
     *
     * @return the current  x position of the {@link Cursor}
     */
    public float getPositionX() {
        return cursorSceneObject.getPositionX();
    }

    /**
     * Get the current absolute y position of this {@link Cursor}.
     *
     * @return the current y position of the {@link Cursor}
     */
    public float getPositionY() {
        return cursorSceneObject.getPositionY();
    }

    /**
     * Get the current absolute z position of this {@link Cursor}.
     *
     * @return the current  z position of the {@link Cursor}
     */
    public float getPositionZ() {
        return cursorSceneObject.getPositionZ();
    }

    /**
     * The method will force a process cycle that may result in an
     * {@link CursorEvent}s being generated if there is a significant event
     * that affects a {@link Cursor}. In most cases when a new position
     * or key event is received, the {@link Cursor} internally
     * invalidates its own data. However there may be situations where the
     * cursor data remains the same while the scene graph is changed. This
     * {@link #invalidate()} call can help force the {@link Cursor}
     * to run a new process loop on its existing information against the changed
     * scene graph to generate possible {@link CursorEvent}s.
     */
    public void invalidate() {
        //generate a new event
        if (ioDevice != null) {
            ioDevice.invalidate();
        }
    }

    void setScale(float scale) {
        this.scale = scale;
        if (ioDevice != null) {
            ioDevice.setPosition(0.0f, 0.0f, -scale);
        }
    }

    /**
     * Perform all Cursor cleanup here.
     */
    void close() {
        // remove listener only if ioDevice is not null
        if (ioDevice != null) {
            ioDevice.removeControllerEventListener(getControllerEventListener());
        }
        ioDevice = null;
        cursorEventListeners.clear();
    }

    /* Set the asset only if it is not already set,
     * return true if the asset has been changed, else return false.
     */
    void checkAndSetAsset(Action action) {
        // check the theme if we have a asset
        CursorAsset asset = cursorTheme.getAsset(action);

        if (asset == null) {
            return;
        }

        // do not set if the app is busy loading
        if (currentCursorAsset == null || currentCursorAsset.getAction().equals(action) == false) {
            if (isBusyLoading()) {
                //save the new state for restore when busy loading is done
                savedCursorAsset = asset;
            } else {
                setAsset(asset);
            }
        }
    }

    private void setAsset(final CursorAsset asset) {
        if (asset == null) {
            return;
        }

        if (currentCursorAsset != null) {
            currentCursorAsset.reset(cursorSceneObject);
        }
        // load new asset
        currentCursorAsset = asset;
        currentCursorAsset.set(cursorSceneObject);
    }

    void setSavedIoDevice(IoDevice savedIoDevice) {
        this.savedIoDevice = savedIoDevice;
    }

    IoDevice getSavedIoDevice() {
        return savedIoDevice;
    }

    void clearSavedIoDevice() {
        savedIoDevice = null;
    }

    void setName(String name) {
        this.name = name;
    }

    /**
     * This method returns the name of this {@link Cursor} object.
     *
     * Note that the name is not necessarily unique.
     *
     * @return a String representing the Cursor
     */
    public String getName() {
        return name;
    }

    void setStartPosition(Position position) {
        this.position = position;
    }

    Position getStartPosition() {
        return position;
    }

    void setSavedThemeId(String savedThemeId) {
        this.savedThemeId = savedThemeId;
    }

    String clearSavedThemeId() {
        String themeId = savedThemeId;
        savedThemeId = null;
        return themeId;
    }

    /**
     * Returns the {@link IoDevice} currently attached to the {@link Cursor}.
     *
     * @return the {@link IoDevice} attached. <code>null</code> is no device is attached.
     */
    public IoDevice getIoDevice() {
        return ioDevice;
    }

    /**
     * Returns a list of {@link IoDevice}s compatible with the {@link Cursor}.
     *
     * @return list of compatible {@link IoDevice}s
     */
    public List<IoDevice> getCompatibleIoDevices() {
        List<IoDevice> ioDevices = new LinkedList<IoDevice>();
        for (PriorityIoDeviceTuple tuple : compatibleIoDevices) {
            ioDevices.add(tuple.getIoDevice());
        }
        return ioDevices;
    }

    List<PriorityIoDeviceTuple> getIoDevices() {
        return compatibleIoDevices;
    }

    /**
     * Return the {@link GVRSceneObject} associated with the Cursor object.
     * <p/>
     * This call is useful for applications that need to change the properties of the
     * {@link GVRSceneObject} controlled by the {@link Cursor}.
     *
     * @return the {@link GVRSceneObject} representing the Cursor object.
     */
    public GVRSceneObject getSceneObject() {
        return cursorSceneObject.getExternalSceneObject();
    }

    GVRSceneObject getMainSceneObject() {
        return cursorSceneObject.getMainSceneObject();
    }

    /**
     * This method returns an integer value that can be used to
     * uniquely identify this {@link Cursor}.
     *
     * @return an integer representing the Cursor
     */
    public int getId() {
        return cursorId;
    }

    /**
     * Use this method to display a loading Cursor to the user. The intended usage for this
     * method is to let the user know that the app is currently working on a task and the cursor
     * would not respond until completion. This method should be called from the glthread.
     * <p/>
     * We leave it to the application to control the usage of this feature.
     * <p/>
     *
     * @param loading shows a loading animation on the cursor if <code>true</code>
     *                restores it to its original state when <code>false</code>.
     */
    public void setBusyLoading(boolean loading) {
        if (loading && (busyLoading == false)) {
            // save the state
            savedCursorAsset = currentCursorAsset;
            setAsset(cursorTheme.getAsset(Action.LOADING));
            busyLoading = true;
        } else if ((loading == false) && busyLoading && savedCursorAsset != null) {
            // restore saved state
            boolean soundEnabled = savedCursorAsset.isSoundEnabled();
            // we don't want a sound on restore
            savedCursorAsset.setSoundEnabled(false);
            setAsset(savedCursorAsset);
            savedCursorAsset.setSoundEnabled(soundEnabled);
            savedCursorAsset = null;
            busyLoading = false;
        }
    }

    /**
     * When the application is working on a task a feedback can be given using the {@link
     * Cursor#setBusyLoading(boolean)} call. This method lets the caller know if the
     * {@link Cursor} is busy loading or not.
     *
     * @return Return <code>true</code> if the {@link Cursor} is busy loading.
     * <code>false</code> otherwise.
     */
    public boolean isBusyLoading() {
        return busyLoading;
    }

    /**
     * Could be done as a part of the Sensor framework provided by GVRf.
     *
     * @param event
     */
    abstract void dispatchSensorEvent(SensorEvent event);

    abstract ControllerEventListener getControllerEventListener();

    /**
     * Use this method to check if the {@link Cursor} is enabled or disabled.
     *
     * By default every {@link Cursor} object is enabled.s
     *
     * @return <code>true</code> if the {@link Cursor} is enabled, <code>false</code> otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * When enabled the Cursor would report {@link CursorEvent}s. Disabling the
     * Cursor would stop the events and also remove the Cursor from the
     * scene (if one is provided). The {@link Cursor} can be enabled and disabled
     * from the settings menu
     * <p/>
     * <code>true</code> for enable, <code>false</code> for disable.
     * <p/>
     * Default <code>true</code> - enabled.
     *
     * @param value
     */
    public void setEnable(boolean value) {
        if (!enabled && value) {
            enabled = true;
            cursorManager.assignIoDevicesToCursors();
        } else if (enabled && !value) {
            if (ioDevice == null) {
                enabled = false;
            } else {
                enabled = false;
                Log.d(TAG, "Destroying Iodevice:" + ioDevice.getDeviceId());
                destroyIoDevice(ioDevice);
                cursorManager.markCursorUnused(this);
            }
        }
    }

    private void setupIoDevice(IoDevice ioDevice) {
        if (ioDevice != null) {
            // should have a normal asset
            setAsset(cursorTheme.getAsset(Action.DEFAULT));
            ioDevice.setSceneObject(cursorSceneObject.getMainSceneObject());
            ioDevice.setEnable(true);
            ioDevice.setPosition(0.0f, 0.0f, -scale);
            ioDevice.addControllerEventListener(getControllerEventListener());

            if (IoDeviceLoader.isMouseIoDevice(ioDevice)) {
                mouseEventListener = new MouseEventListener(ioDevice);
                ioDevice.addControllerEventListener(mouseEventListener);
            }
        }
    }

    void destroyIoDevice(IoDevice ioDevice) {
        if (ioDevice != null) {
            ioDevice.setEnable(false);
            ioDevice.removeControllerEventListener(getControllerEventListener());
            ioDevice.resetSceneObject();
            if (IoDeviceLoader.isMouseIoDevice(ioDevice)) {
                if (mouseEventListener != null) {
                    ioDevice.removeControllerEventListener(mouseEventListener);
                }
            }
        }
    }

    void transferIoDevice(Cursor targetCursor) {
        IoDevice targetIoDevice = targetCursor.getIoDevice();
        targetIoDevice.removeControllerEventListener(targetCursor.getControllerEventListener());
        targetIoDevice.resetSceneObject();
        if (IoDeviceLoader.isMouseIoDevice(targetIoDevice)) {
            if (targetCursor.mouseEventListener != null) {
                targetCursor.ioDevice.removeControllerEventListener(targetCursor
                        .mouseEventListener);
            }
        }
        ioDevice = targetIoDevice;
        setupIoDevice(targetIoDevice);
    }

    private enum State {
        FRONT, BACK, LEFT, RIGHT, FRONT_RIGHT, FRONT_LEFT, BACK_RIGHT, BACK_LEFT
    }

    private class MouseEventListener implements ControllerEventListener {
        private IoDevice mouseDevice;
        private State state;

        MouseEventListener(IoDevice device) {
            state = State.FRONT;
            mouseDevice = device;
        }

        @Override
        public void onEvent(GVRCursorController gvrCursorController) {
            if (scene == null) {
                return;
            }

            float[] lookAt = scene.getMainCameraRig().getLookAt();
            float lookAtX = lookAt[0];
            float lookAtZ = lookAt[2];
            float angle = (float) Math.toDegrees(Math.atan2(-lookAtX, -lookAtZ));
            if (angle > -12.5f && angle < 12.5f && state != State.FRONT) {
                state = State.FRONT;
                mouseDevice.setPosition(0.0f, 0.0f, -scale);
            } else if (angle > 77.5f && angle < 102.5 && state != State.LEFT) {
                state = State.LEFT;
                mouseDevice.setPosition(-scale, 0.0f, 0.0f);
            } else if (angle > -102.5f && angle < -77.5 && state != State.RIGHT) {
                state = State.RIGHT;
                mouseDevice.setPosition(scale, 0.0f, 0.0f);
            } else if (((angle < -167.5f && angle > -180f) || (angle > 167.5f && angle < 180.0f))
                    && state != State.BACK) {
                state = State.BACK;
                mouseDevice.setPosition(0.0f, 0.0f, scale);
            } else if (angle > -57.5f && angle < -32.5f && state != State.FRONT_RIGHT) {
                state = State.FRONT_RIGHT;
                mouseDevice.setPosition(scale / SQRT_2, 0.0f, -scale / SQRT_2);
            } else if (angle > 32.5f && angle < 57.5 && state != State.FRONT_LEFT) {
                state = State.FRONT_LEFT;
                mouseDevice.setPosition(-scale / SQRT_2, 0.0f, -scale / SQRT_2);
            } else if (angle > 122.5 && angle < 147.5 && state != State.BACK_LEFT) {
                state = State.BACK_LEFT;
                mouseDevice.setPosition(-scale / SQRT_2, 0.0f, scale / SQRT_2);
            } else if (angle > -147.5 && angle < -122.5 && state != State.BACK_RIGHT) {
                state = State.BACK_RIGHT;
                mouseDevice.setPosition(scale / SQRT_2, 0.0f, scale / SQRT_2);
            }
        }
    }

    /**
     * Register for events whenever the {@link Cursor} updates its position or
     * receives button clicks.
     *
     * @param listener the {@link CursorEventListener} to be added.<code>null</code>
     *                 objects are ignored.
     */
    public void addCursorEventListener(CursorEventListener listener) {
        if (listener == null) {
            // ignore null input
            return;
        }

        cursorEventListeners.add(listener);
        //trigger an event with the current position.
        if (ioDevice != null) {
            ioDevice.setPosition(0.0f, 0.0f, -scale);
        }
    }

    boolean isColliding(GVRSceneObject sceneObject) {
        return cursorSceneObject.isColliding(sceneObject);
    }

    /**
     * Remove the previously added {@link CursorEventListener}.
     *
     * @param listener the {@link CursorEventListener} to be removed.<code>null</code>
     *                 objects are ignored.
     */
    public void removeCursorEventListener(CursorEventListener listener) {
        if (listener == null) {
            // ignore null input
            return;
        }

        cursorEventListeners.remove(listener);
    }

    void dispatchCursorEvent(CursorEvent event) {
        if (enabled) {
            //TODO find better fix for concurrent modification
            for (CursorEventListener listener : cursorEventListeners) {
                listener.onEvent(event);
            }
        }
        event.recycle();
    }

    IoDevice getIoDeviceForPriority(int priorityLevel) {
        if (priorityLevel < compatibleIoDevices.size()) {
            return compatibleIoDevices.get(priorityLevel).getIoDevice();
        } else {
            return null;
        }
    }

    /**
     * Get a list of currently available {@link IoDevice}s to use with the {@link Cursor}. The
     * {@link Cursor} defines a list of compatible {@link IoDevice}s in the settings.xml. This
     * method returns a subset from the compatible list of {@link IoDevice}s that are available to
     * the framework and not being used by any other cursor.
     *
     * @return a list of available {@link IoDevice}.
     */
    public List<IoDevice> getAvailableIoDevices() {
        List<IoDevice> returnList = new ArrayList<IoDevice>();
        for (PriorityIoDeviceTuple compatibleIoDeviceTuple : compatibleIoDevices) {
            IoDevice compatibleIoDevice = compatibleIoDeviceTuple.getIoDevice();
            if (compatibleIoDevice.equals(getIoDevice())) {
                returnList.add(ioDevice);
            } else {
                IoDevice ioDevice = cursorManager.getAvailableIoDevice(compatibleIoDevice);
                if (ioDevice != null) {
                    returnList.add(ioDevice);
                }
            }
        }
        return returnList;
    }

    /**
     * Use this method to attach an available {@link IoDevice} to the {@link Cursor}. The
     * currently attached {@link IoDevice} is released and a new {@link IoDevice} is attached to
     * the {@link Cursor}.
     *
     * @param ioDevice the IoDevice to attach, this {@link IoDevice} should be one of the objects
     *                 returned by {@link Cursor#getAvailableIoDevices()}
     * @throws IOException The call return an {@link IOException} if this {@link IoDevice} cannot be
     *                     attached.
     */
    public void attachIoDevice(IoDevice ioDevice) throws IOException {
        if (!enabled) {
            throw new IllegalStateException("Cursor not enabled");
        }

        if (this.ioDevice != null && this.ioDevice.equals(ioDevice)) {
            Log.d(TAG, "Current and desired Io device are same");
            return;
        }

        if (!isIoDeviceCompatible(ioDevice)) {
            throw new IllegalArgumentException("IO device not compatible");
        }
        IoDevice availableIoDevice = cursorManager.getAvailableIoDevice(ioDevice);
        if (availableIoDevice == null) {
            throw new IOException("IO device cannot be attached");
        }

        Log.d(TAG, "Attaching ioDevice:" + availableIoDevice.getDeviceId() + " to cursor:"
                + cursorId);

        IoDevice oldIoDevice = this.ioDevice;
        setIoDevice(availableIoDevice);
        cursorManager.markIoDeviceUsed(availableIoDevice);
        cursorManager.markIoDeviceUnused(oldIoDevice);
    }

    private boolean isIoDeviceCompatible(IoDevice ioDevice) {
        for (PriorityIoDeviceTuple compatibleIoDevice : compatibleIoDevices) {
            if (compatibleIoDevice.getIoDevice().equals(ioDevice)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method makes sure that the {@link Cursor} is always facing the camera.
     *
     * Lookat implemented using:
     *
     * <p/>
     * http://mmmovania.blogspot.com/2014/03/making-opengl-object-look-at-another.html
     */
    protected void lookAt() {
        objectPosition.set(cursorSceneObject.getPositionX(), cursorSceneObject.getPositionY(),
                cursorSceneObject.getPositionZ());
        objectPosition.negate(direction);

        Vector3f up;
        direction.normalize();

        if (Math.abs(direction.x) < 0.00001
                && Math.abs(direction.z) < 0.00001) {
            if (direction.y > 0) {
                up = new Vector3f(0.0f, 0.0f, -1.0f); // if direction points in +y
            } else {
                up = new Vector3f(0.0f, 0.0f, 1.0f); // if direction points in -y
            }
        } else {
            up = new Vector3f(0.0f, 1.0f, 0.0f); // y-axis is the general up
        }

        up.normalize();
        Vector3f right = new Vector3f();
        up.cross(direction, right);
        right.normalize();
        direction.cross(right, up);
        up.normalize();

        float[] matrix = new float[]{right.x, right.y, right.z, 0.0f, up.x, up.y,
                up.z, 0.0f, direction.x, direction.y, direction.z, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f};
        cursorSceneObject.setModelMatrix(matrix);
    }
}
