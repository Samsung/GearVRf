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

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.io.CursorControllerListener;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRInputManager;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Define a class of type {@link GVRCursorController} to register a new cursor
 * controller with the {@link GVRInputManager}.
 *
 * You can request for all the {@link GVRCursorController}s in the system by
 * querying the {@link GVRInputManager#getCursorControllers()} call.
 *
 * Alternatively all notifications for {@link GVRCursorController}s attached or
 * detached from the framework can be obtained by registering a
 * {@link CursorControllerListener} with the {@link GVRInputManager}.
 *
 * Make use of the {@link GVRCursorController#setSceneObject(GVRSceneObject)} to
 * add a Cursor for the controller in 3D space. The {@link GVRInputManager} will
 * manipulate the {@link GVRSceneObject} based on the input coming in to the
 * {@link GVRCursorController}.
 *
 * Use the {@link GVRInputManager#addCursorController(GVRCursorController)} call
 * to add an external {@link GVRCursorController} to the framework.
 */
public abstract class GVRCursorController {
    private static final String TAG = "GVRCursorController";
    private static int uniqueControllerId = 0;
    private final int controllerId;
    private final GVRControllerType controllerType;
    private boolean previousActive;
    private ActiveState activeState = ActiveState.NONE;
    private boolean active;
    private float nearDepth, farDepth = -Float.MAX_VALUE;
    protected final Vector3f position, origin;
    private boolean enable = true;
    private List<KeyEvent> keyEvent;
    private List<KeyEvent> processedKeyEvent;
    private List<MotionEvent> motionEvent;
    private List<MotionEvent> processedMotionEvent;
    private GVRSceneObject sceneObject;
    private Object sceneObjectLock = new Object();
    private Object eventLock = new Object();
    private List<ControllerEventListener> controllerEventListeners;

    private String name;
    private int vendorId, productId;
    private GVRScene scene;
    protected GVRPicker mPicker = null;
    protected GVRContext context;
    protected volatile boolean connected = false;

    /**
     * Create an instance of {@link GVRCursorController} only using the
     * {@link GVRControllerType}.
     *
     * Note that this constructor creates a {@link GVRCursorController} with no
     * name (<code>null</code>) and vendor and product id set to 0.
     *
     * @param controllerType the type of this {@link GVRCursorController}.
     */
    public GVRCursorController(GVRContext context, GVRControllerType controllerType) {
        this(context, controllerType, null);
    }

    /**
     * Create an instance of {@link GVRCursorController} using a
     * {@link GVRControllerType} and a name.
     *
     * Note that this constructor creates a {@link GVRCursorController} with  vendor and product
     * id set to 0.
     *
     * @param controllerType the type of this {@link GVRCursorController}.
     * @param name           the name for this {@link GVRCursorController}
     */
    public GVRCursorController(GVRContext context, GVRControllerType controllerType, String name) {
        this(context, controllerType, name, 0, 0);
    }

    /**
     * Create an instance of {@link GVRCursorController}.
     *
     * @param controllerType the type of this {@link GVRCursorController}.
     * @param name           the name for this {@link GVRCursorController}
     * @param vendorId       the vendor id for this {@link GVRCursorController}
     * @param productId      the product id for this {@link GVRCursorController}
     */
    public GVRCursorController(GVRContext context, GVRControllerType controllerType, String name,
                               int vendorId, int productId) {
        this.context = context;
        this.controllerId = uniqueControllerId;
        this.controllerType = controllerType;
        this.name = name;
        this.vendorId = vendorId;
        this.productId = productId;
        uniqueControllerId++;
        position = new Vector3f(0, 0, -1);
        origin = new Vector3f(0, 0, 0);
        keyEvent = new ArrayList<KeyEvent>();
        processedKeyEvent = new ArrayList<KeyEvent>();
        motionEvent = new ArrayList<MotionEvent>();
        processedMotionEvent = new ArrayList<MotionEvent>();
        controllerEventListeners = new CopyOnWriteArrayList<ControllerEventListener>();
        if (mPicker == null)
        {
            mPicker = new GVRPicker(this, false);
        }
    }

    synchronized public boolean dispatchKeyEvent(KeyEvent event)
    {
        synchronized (eventLock) {
            this.keyEvent.add(event);
        }
        if (event != null) {
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN && active == false) {
                setActive(true);
            } else if (action == KeyEvent.ACTION_UP && active == true) {
                setActive(false);
            }
        }
        return true;
    }

    synchronized public boolean dispatchMotionEvent(MotionEvent event)
    {
        synchronized (eventLock) {
            this.motionEvent.add(event);
        }
        return true;
    }

    /**
     * Return an id that represent this {@link GVRCursorController}
     *
     * @return an integer id that identifies this controller.
     */
    public int getId() {
        return controllerId;
    }

    enum ActiveState {
        ACTIVE_PRESSED, ACTIVE_RELEASED, NONE
    }

    ActiveState getActiveState() {
        return activeState;
    }

    public GVRContext getGVRContext() { return context; }

    /**
     * Use this method to set the active state of the{@link GVRCursorController}.
     * It indicates whether or not the "active" button is pressed.
     * <p>
     * It is up to the developer to map the designated button to the active flag.
     * Eg. A Gamepad could attach {@link KeyEvent#KEYCODE_BUTTON_A} to active.
     * @param active    Setting active to true causes the {@link SensorEvent} generated
     *                  from collisions with the cursor to have {@link SensorEvent#isActive()} as
     *                  <code>true</code>. Clearing it will emit events with <code>false</code>.
     *                  The active flag is also propagated to the picker, setting the value of
     *                  {@link GVRPicker.GVRPickedObject#touched}.
     */
    protected void setActive(boolean active) {
        // check if the controller is enabled
        if (isEnabled() == false) {
            return;
        }

        this.active = active;
        update();
    }

    /**
     * Set a {@link GVRSceneObject} to be controlled by the
     * {@link GVRCursorController}.
     *
     * @param object the {@link GVRSceneObject}
     */
    public void setSceneObject(GVRSceneObject object) {
        synchronized (sceneObjectLock) {
            if (object != null) {
                Vector3f objectPosition = position;
                if (sceneObject != null) {
                    // if there is already an attached scene object transfer the
                    // position to the new one
                    objectPosition = new Vector3f(sceneObject.getTransform()
                            .getPositionX(), sceneObject.getTransform()
                            .getPositionY(), sceneObject.getTransform()
                            .getPositionZ());
                }
                object.getTransform().setPosition(objectPosition.x,
                        objectPosition.y, objectPosition.z);
            }
            sceneObject = object;
        }
    }

    /**
     * This method clears the currently set {@link GVRSceneObject} if there is
     * one.
     */
    public void resetSceneObject() {
        setSceneObject(null);
    }

    /**
     * Return the currently set {@link GVRSceneObject}.
     *
     * @return the currently set {@link GVRSceneObject} if there is one, else
     * return <code>null</code>
     */
    public GVRSceneObject getSceneObject() {
        synchronized (sceneObjectLock) {
            return sceneObject;
        }
    }

    /**
     * The method will force a process cycle that may result in an
     * {@link ISensorEvents} being generated if there is a significant event
     * that affects a {@link GVRBaseSensor} or {@link IPickEvents} if the
     * cursor pick ray intersects a collider.
     * <p>
     * In most cases when a new position
     * or key event is received, the {@link GVRCursorController} internally
     * invalidates its own data. However there may be situations where the
     * controller data remains the same while the scene graph is changed. This
     * {@link #invalidate()} call can help force the {@link GVRCursorController}
     * to run a new process loop on its existing information against the changed
     * scene graph to generate possible {@link ISensorEvents} for
     * {@link GVRBaseSensor}s.
     */
    public void invalidate() {
        // check if the controller is enabled
        if (!isEnabled()) {
            return;
        }
        update();
    }

    /**
     * Return the {@link GVRControllerType} associated with the
     * {@link GVRCursorController}.
     *
     * In most cases, this method should return
     * {@link GVRControllerType#EXTERNAL}. {@link GVRControllerType#EXTERNAL}
     * allows the input device to define its own input behavior. If the device
     * wishes to implement {@link GVRControllerType#MOUSE} or
     * {@link GVRControllerType#GAMEPAD} make sure that the behavior is
     * consistent with that defined in GVRMouseDeviceManager and
     * GVRGamepadDeviceManager.
     *
     * @return the {@link GVRControllerType} for the {@link GVRCursorController}
     * .
     */
    public GVRControllerType getControllerType() {
        return controllerType;
    }

    /**
     * Get the picker associated with this controller
     * @return GVRPicker used to pick for this controller
     */
    public GVRPicker getPicker() { return mPicker; }

    /**
     * Set a key event. Note that this call can be used in lieu of
     * {@link GVRCursorController#setActive(boolean)}.
     *
     * The {@link GVRCursorController} processes a ACTION_DOWN
     * as active <code>true</code> and ACTION_UP as active
     * <code>false</code>.
     *
     * In addition the key event passed is used as a reference for applications
     * that wish to use the contents from the class.
     *
     * {@link #setActive(boolean)} can still be used for applications that do
     * not want to expose key events.
     *
     * @param keyEvent
     */
    protected void setKeyEvent(KeyEvent keyEvent) {
        synchronized (eventLock) {
            this.keyEvent.add(keyEvent);
        }
        if (keyEvent != null) {
            int action = keyEvent.getAction();
            if (action == KeyEvent.ACTION_DOWN && active == false) {
                setActive(true);
            } else if (action == KeyEvent.ACTION_UP && active == true) {
                setActive(false);
            }
        }
    }

    /**
     * Get the all the key events processed by the {@link GVRCursorController}
     * if there are any. This call returns all the motion events reports since
     * the last callback was made.
     *
     * Note that not all {@link GVRCursorController} report {@link KeyEvent}s),
     * this function could also return an empty list for
     * {@link GVRCursorController}s that do not generate {@link KeyEvent}s.
     *
     * To get every {@link KeyEvent} reported by the {@link GVRCursorController}
     * use the {@link ControllerEventListener} or the {@link ISensorEvents}
     * listener to query for the {@link KeyEvent} whenever a a callback is made.
     *
     * The {@link KeyEvent} would be valid for the lifetime of that callback and
     * would be reset to null on completion.
     *
     * @return the list of {@link KeyEvent}s processed by the
     * {@link GVRCursorController}.
     */
    public List<KeyEvent> getKeyEvents() {
        synchronized (eventLock) {
            return processedKeyEvent;
        }
    }

    /**
     * Get the latest key event processed by the {@link GVRCursorController} if
     * there is one (not all {@link GVRCursorController} report {@link KeyEvent}
     * s). Note that this value will be null if the latest event processed by
     * the {@link GVRCursorController} did not contain a {@link KeyEvent}.
     *
     * Note that this function also returns a null. To get every
     * {@link KeyEvent} reported by the {@link GVRCursorController} use the
     * {@link ControllerEventListener} or the {@link ISensorEvents} listener to
     * query for the {@link KeyEvent} whenever a a callback is made.
     *
     * The {@link KeyEvent} would be valid for the lifetime of that callback and
     * would be reset to null on completion.
     *
     * @return the {@link KeyEvent} or null if there isn't one.
     */
    public KeyEvent getKeyEvent() {
        synchronized (eventLock) {
            if (processedKeyEvent.isEmpty()) {
                return null;
            } else {
                return processedKeyEvent.get(processedKeyEvent.size() - 1);
            }
        }
    }

    /**
     * Set the latest motion event processed by the {@link GVRCursorController}.
     *
     * Make sure not to recycle the passed {@link MotionEvent}. The
     * {@link GVRCursorController} will recycle the {@link MotionEvent} after
     * completion.
     *
     * @param motionEvent the {@link MotionEvent} processed by the
     *                    {@link GVRCursorController}.
     */
    protected void setMotionEvent(MotionEvent motionEvent) {
        synchronized (eventLock) {
            this.motionEvent.add(motionEvent);
        }
    }

    /**
     * Get the all the {@link MotionEvent} processed by the
     * {@link GVRCursorController} if there are any. This call returns all the
     * motion events reports since the last callback was made.
     *
     * Note that not all {@link GVRCursorController}s report {@link MotionEvent}
     * s), this function also returns an empty list. To get every
     * {@link MotionEvent} reported by the {@link GVRCursorController} use the
     * {@link ControllerEventListener} or the {@link ISensorEvents} listener to
     * query for the {@link MotionEvent}s whenever a a callback is made.
     *
     * The {@link MotionEvent}s reported would be valid for the lifetime of that
     * callback and would be recycled and reset on completion. Make use to the
     * {@link MotionEvent#obtain(MotionEvent)} to clone a copy of the
     * {@link MotionEvent}.
     *
     * @return a list of {@link MotionEvent}s processed by the
     * {@link GVRCursorController} .
     */
    public List<MotionEvent> getMotionEvents() {
        synchronized (eventLock) {
            return processedMotionEvent;
        }
    }

    /**
     * Get the latest {@link MotionEvent} processed by the
     * {@link GVRCursorController} if there is one (not all
     * {@link GVRCursorController}s report {@link MotionEvent}s)
     *
     * Note that this function also returns a null. To get every
     * {@link MotionEvent} reported by the {@link GVRCursorController} use the
     * {@link ControllerEventListener} or the {@link ISensorEvents} listener to
     * query for the {@link MotionEvent} whenever a a callback is made.
     *
     * The {@link MotionEvent} would be valid for the lifetime of that callback
     * and would be recycled and reset to null on completion. Make use to the
     * {@link MotionEvent#obtain(MotionEvent)} to clone a copy of the
     * {@link MotionEvent}.
     *
     * @return the latest {@link MotionEvent} processed by the
     * {@link GVRCursorController} or null.
     */
    public MotionEvent getMotionEvent() {
        synchronized (eventLock) {
            if (processedMotionEvent.isEmpty()) {
                return null;
            } else {
                return processedMotionEvent
                        .get(processedMotionEvent.size() - 1);
            }
        }
    }

    /**
     * This call sets the position of the {@link GVRCursorController}.
     *
     * Use this call to also set an initial position for the Cursor when a new
     * {@link GVRCursorController} is returned by the
     * {@link CursorControllerListener}.
     *
     * @param x the x value of the position.
     * @param y the y value of the position.
     * @param z the z value of the position.
     */
    public void setPosition(float x, float y, float z) {
        // check if the controller is enabled
        if (isEnabled() == false) {
            return;
        }

        position.set(x, y, z);
        if (sceneObject != null) {
            synchronized (sceneObjectLock) {
                // if there is an attached scene object then set its absolute position.
                sceneObject.getTransform().setPosition(x, y, z);
            }
        }
        update();
    }


    public Vector3f getPosition(Vector3f pos) {
        pos.set(position);
        return pos;
    }

    /**
     * Register a {@link ControllerEventListener} to receive a callback whenever
     * the {@link GVRCursorController} has been updated.
     *
     * Use the {@link GVRCursorController} methods to query for information
     * about the {@link GVRCursorController}.
     */
    public interface ControllerEventListener {
        void onEvent(GVRCursorController controller);
    }

    /**
     * Add a {@link ControllerEventListener} to receive updates from this
     * {@link GVRCursorController}.
     *
     * @param listener the {@link CursorControllerListener} to be added.
     */
    public void addControllerEventListener(ControllerEventListener listener) {
        controllerEventListeners.add(listener);
    }

    /**
     * Remove the previously added {@link ControllerEventListener}.
     *
     * @param listener {@link ControllerEventListener} that was previously added .
     */
    public void removeControllerEventListener(ControllerEventListener listener) {
        controllerEventListeners.remove(listener);
    }

    /**
     * Add a {@link IPickEvents} or {@link ITouchEvents} listener to receive updates from this
     * {@link GVRCursorController}. A pick event is emitted whenever
     * the pick ray from the controller intersects a {@link GVRCollider}.
     * A touch event is emitted when the active button is pressed while
     * the pick ray is inside the collider.
     *
     * @param listener the {@link IPickEvents} or {@link ITouchEvents} listener to be added.
     */
    public void addPickEventListener(IEvents listener)
    {
        if (IPickEvents.class.isAssignableFrom(listener.getClass()) ||
            ITouchEvents.class.isAssignableFrom(listener.getClass()))
        {
            mPicker.getEventReceiver().addListener(listener);
        }
        else
        {
            throw new IllegalArgumentException("Pick event listener must be derive from IPickEvents or ITouchEvents");
        }
    }

    /**
     * Remove the previously added pick or touch listener.
     *
     * @param listener {@link IPickEvents} or {@link ITouchEvents} listener that was previously added.
     */
    public void removePickEventListener(IEvents listener)
    {
        mPicker.getEventReceiver().removeListener(listener);
    }

    /**
     * Use this method to enable or disable the {@link GVRCursorController}.
     *
     * By default the {@link GVRCursorController} is enabled. If disabled, the
     * controller would not report new positions for the cursor and would not
     * generate {@link SensorEvent}s to {@link GVRBaseSensor}s.
     *
     * @param enable <code>true</code> to enable the {@link GVRCursorController},
     *               <code>false</code> to disable.
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
        if (enable)
        {
            addPickEventListener(GVRBaseSensor.getPickHandler());
            mPicker.setEnable(true);
        }
        else
        {
            mPicker.setEnable(false);
            removePickEventListener(GVRBaseSensor.getPickHandler());
            context.getInputManager().deactivateCursorController(this);
        }
        if (this.enable == enable) {
            // nothing to be done here, return
            return;
        }

        if (enable == false) {
            // reset
            position.zero();
            if (previousActive) {
                active = false;
            }

            synchronized (eventLock) {
                keyEvent.clear();
                motionEvent.clear();
            }
            update();
        }
    }

    /**
     * Check if the {@link GVRCursorController} is enabled or disabled.
     *
     * By default the {@link GVRCursorController} is enabled.
     *
     * @return <code>true</code> if the {@link GVRCursorController} is enabled,
     * <code>false</code> otherwise.
     */
    public boolean isEnabled() {
        return enable;
    }

    /**
     * Check if the {@link GVRCursorController} is connected and providing
     * input data.
     * @return true if controller is connected, else false
     */
    public boolean isConnected() { return connected; }

    /**
     * Set the near depth value for the controller. This is the closest the
     * {@link GVRCursorController} can get in relation to the {@link GVRCamera}.
     *
     * By default this value is set to zero.
     *
     * @param nearDepth
     */
    public void setNearDepth(float nearDepth) {
        this.nearDepth = nearDepth;
    }

    /**
     * Set the far depth value for the controller. This is the farthest the
     * {@link GVRCursorController} can get in relation to the {@link GVRCamera}.
     *
     * By default this value is set to negative {@link Float#MAX_VALUE}.
     *
     * @param farDepth
     */
    public void setFarDepth(float farDepth) {
        this.farDepth = farDepth;
    }

    /**
     * Get the near depth value for the controller.
     *
     * @return value representing the near depth. By default the value returned
     * is zero.
     */
    public float getNearDepth() {
        return nearDepth;
    }

    /**
     * Get the far depth value for the controller.
     *
     * @return value representing the far depth. By default the value returned
     * is negative {@link Float#MAX_VALUE}.
     */
    public float getFarDepth() {
        return farDepth;
    }

    /**
     * Get the product id associated with the {@link GVRCursorController}
     *
     * @return an integer representing the product id if there is one, else
     * return zero.
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Get the vendor id associated with the {@link GVRCursorController}
     *
     * @return an integer representing the vendor id if there is one, else
     * return zero.
     */
    public int getVendorId() {
        return vendorId;
    }

    /**
     * Get the name associated with the {@link GVRCursorController}.
     *
     * @return a string representing the {@link GVRCursorController} is there is
     * one, else return <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Change the scene associated with this controller (and
     * its associated picker). The picker is not enabled
     * and will not automatically pick. The controller
     * explicity calls GVRPicker.processPick each tima a
     * controller event is received.
     * @param scene The scene from which to pick colliders
     */
    protected void setScene(GVRScene scene)
    {
        this.scene = scene;
        mPicker.setScene(scene);
    }

    /**
     * Update the state of the picker. If it has an owner, the picker
     * will use that object to derive its position and orientations.
     * The "active" state of this controller is use to indicate touch.
     */
    protected void updatePicker(MotionEvent event)
    {
        float l = 1.0f / position.length();

        if ((mPicker.getOwnerObject() == null) && (l > 0.00001f))
        {
            mPicker.setPickRay(origin.x, origin.y, origin.z, position.x, position.y, position.z);
        }
        mPicker.processPick(active, event);
    }

    private boolean eventHandledBySensor = false;

    /**
     * Returns whether events generated as a result of the latest change in the
     * GVRCursorController state, i.e. change in Position, or change in Active/Enable state were
     * handled by a sensor. This can be used by an application to know whether
     * there were any {@link SensorEvent}s generated as a result of any change in the
     * {@link GVRCursorController}.
     * @return <code>true</code> if event was handled by a sensor,
     * <code>false</code> if otherwise.
     */
    public boolean isEventHandledBySensorManager() {
        return eventHandledBySensor;
    }

    void setEventHandledBySensor()
    {
        eventHandledBySensor = true;
    }

    /**
     * Process the input data.
     */
    private void update() {
        // set the newly received key and motion events.
        synchronized (eventLock) {
            processedKeyEvent.addAll(keyEvent);
            keyEvent.clear();
            processedMotionEvent.addAll(motionEvent);
            motionEvent.clear();
        }

        if (previousActive == false && active) {
            activeState = ActiveState.ACTIVE_PRESSED;
        } else if (previousActive == true && active == false) {
            activeState = ActiveState.ACTIVE_RELEASED;
        } else {
            activeState = ActiveState.NONE;
        }
        previousActive = active;
        eventHandledBySensor = false;
        if (scene != null)
        {
            updatePicker(getMotionEvent());
        }
        for (ControllerEventListener listener : controllerEventListeners) {
            listener.onEvent(this);
        }
        // reset the set key and motion events.
        synchronized (eventLock) {
            processedKeyEvent.clear();
            // done processing, recycle
            for (MotionEvent event : processedMotionEvent) {
                event.recycle();
            }
            processedMotionEvent.clear();
        }
    }

    /**
     * Sets the x, y, z location from where the pick begins
     * Should match the location of the camera or the hand controller.
     * @param x X position of the camera
     * @param y Y position of the camera
     * @param z Z position of the camera
     */
    public void setOrigin(float x, float y, float z){
        origin.set(x,y,z);
    }


    /**
     * Returns the origin of the pick ray for this controller.
     * @return X,Y,Z origin of picking ray
     */
    public Vector3f getOrigin()
    {
        return origin;
    }
}