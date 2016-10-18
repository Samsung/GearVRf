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


import org.gearvrf.GVRBaseSensor;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ISensorEvents;
import org.gearvrf.SensorEvent;
import org.gearvrf.SensorEvent.EventGroup;
import org.gearvrf.io.cursor3d.CursorInputManager.IoDeviceListener;
import org.gearvrf.io.cursor3d.settings.SettingsView;
import org.gearvrf.io.cursor3d.settings.SettingsView.SettingsChangeListener;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;
import org.joml.FrustumCuller;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Create an instance of the {@link CursorManager} to obtain {@link Cursor} objects to be used by
 * the application.
 * <p/>
 * To get a list of all the active {@link Cursor} objects, use the {@link
 * CursorManager#getActiveCursors()} call. Additionally register a {@link
 * CursorActivationListener} to know about {@link Cursor} objects activated or deactivated
 * by the {@link CursorManager} during runtime.
 * <p/>
 * The {@link CursorManager} will add/remove {@link Cursor} objects based on the changes
 * requested by the application.
 *
 * Once a {@link Cursor} is obtained all {@link CursorEvent}s generated can be obtained using the
 * {@link Cursor#addCursorEventListener(CursorEventListener)} call.
 * <p/>
 * The {@link CursorManager} is also responsible for setting properties of the {@link
 * Cursor} objects - look at the various methods defined by this class.
 */
public class CursorManager {
    // Result of XML parsing in a package of all settings that need displaying.
    private static final String TAG = CursorManager.class.getSimpleName();
    private static final float DEFAULT_CURSOR_SCALE = 15.0f;
    static String SETTINGS_SOURCE = "settings.xml";

    private GVRContext context;
    private GVRScene scene;
    private CursorInputManager inputManager;

    private boolean attachedToHead;
    private boolean developerOptions;
    // would drag along the edge of the screen with a visual cue,
    private boolean resetOnBothButton;
    private CursorSensor cursorSensor;
    private List<CursorActivationListener> activationListeners;
    // List of all the cursors available to the user.
    private List<Cursor> cursors;
    private List<Cursor> unusedCursors;
    private List<IoDevice> usedIoDevices;
    private List<IoDevice> unusedIoDevices;

    private float cursorScale;
    private FrustumChecker frustumChecker;
    private Map<String, CursorTheme> themes;
    private final GlobalSettings globalSettings;
    //Create a laser cursor to use on the settings menu
    private LaserCursor settingsCursor;
    private float settingsIoDeviceFarDepth, settingsIoDeviceNearDepth;
    private CursorActivationListener activationListener;
    private List<SelectableBehavior> selectableBehaviors;

    /**
     * Create a {@link CursorManager}.
     * <p/>
     * Use the {@link CursorManager#CursorManager(GVRContext, GVRScene)} constructor to
     * create a
     * manager and also add any {@link Cursor} objects created to be automatically added to the
     * set scene.
     * <p/>
     * If a GVRScene is not set then it is up to the application to add/remove the
     * {@link Cursor} to/from the scene.
     * <p/>
     * Use the {@link #setScene(GVRScene)} call to set the {@link GVRScene}
     * explicitly.
     *
     * @param context The GVRf Context
     */
    public CursorManager(GVRContext context) {
        this(context, null, null);
    }

    /**
     * Create a {@link CursorManager}.
     * <p/>
     * Any {@link Cursor} object created would be added to the provided
     * {@link GVRScene} by default.
     *
     * @param context The GVRf Context
     * @param scene   the default {@link GVRScene} to use while adding/removing Cursor objects.
     */
    public CursorManager(GVRContext context, GVRScene scene) {
        this(context, scene, null);
    }

    /**
     * Create a {@link CursorManager}
     * <p/>
     * All {@link IoDevice}s passed in the constructor will be associated with a {@link
     * Cursor} if a match is found at initialization. Any {@link IoDevice} added after
     * initialization with {@link CursorManager#addIoDevice(IoDevice)} will be
     * only be assigned to a {@link Cursor} if that particular {@link Cursor} has not been attached
     * to any {@link IoDevice} yet.
     *
     * @param context   The GVRf Context
     * @param scene     the default {@link GVRScene} to use while adding/removing Cursor
     *                  objects.
     * @param ioDevices A list of {@link IoDevice}s to add to the {@link
     *                  CursorManager} at initialization.
     */
    public CursorManager(GVRContext context, GVRScene scene, List<IoDevice>
            ioDevices) {
        if (context == null) {
            throw new IllegalArgumentException("GVRContext cannot be null");
        }
        this.scene = scene;
        this.context = context;
        this.inputManager = CursorInputManager.getInstance(context);
        activationListeners = new ArrayList<CursorActivationListener>();
        globalSettings = GlobalSettings.getInstance();
        themes = new HashMap<String, CursorTheme>();
        cursors = new ArrayList<Cursor>();
        unusedCursors = new LinkedList<Cursor>();
        usedIoDevices = new ArrayList<IoDevice>();
        unusedIoDevices = new ArrayList<IoDevice>();
        selectableBehaviors = new ArrayList<SelectableBehavior>();
        cursorSensor = new CursorSensor(context);
        cursorScale = DEFAULT_CURSOR_SCALE;

        try {
            SettingsParser.parseSettings(context, this);
        } catch (XmlPullParserException e) {
            String errorMsg = "Error in parsing " + SETTINGS_SOURCE + " file:" + e.getMessage();
            Log.e(TAG, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        } catch (IOException e) {
            Log.d(TAG, "", e);
            throw new RuntimeException("Error in Opening settings.xml file");
        }

        for (Cursor cursor : unusedCursors) {
            CursorTheme theme = themes.get(cursor.clearSavedThemeId());
            if (theme == null) {
                throw new IllegalArgumentException("Illegal themeId used for cursor");
            }
            cursor.setCursorTheme(theme);
        }

        if (ioDevices != null) {
            for (IoDevice ioDevice : ioDevices) {
                inputManager.addIoDevice(ioDevice);
            }
        }
        inputManager.addIoDeviceListener(cursorIoDeviceListener);
        unusedIoDevices.addAll(inputManager.getAvailableIoDevices());
        assignIoDevicesToCursors();

        // disable all unused devices
        for(IoDevice device:unusedIoDevices){
            device.setEnable(false);
        }

        settingsCursor = new LaserCursor(context, this);
        settingsCursor.setScene(scene);

        /**
         * TODO: what if there isn't a laser theme. Might need to load manually
         */
        for (CursorTheme theme : getCursorThemes()) {
            if (theme.getType() == CursorType.LASER) {
                settingsCursor.setCursorTheme(theme);
                break;
            }
        }
    }

    /**
     * Gives a list of all the {@link CursorTheme}s listed in the settings.xml file. Use this
     * call to get a list of available {@link CursorTheme}s and {@link Cursor#setCursorTheme(CursorTheme)}
     * to change the {@link CursorTheme} associated with a {@link Cursor}
     * @return A list of {@link CursorTheme}s as defined in the settings.xml
     */
    public List<CursorTheme> getCursorThemes() {
        return new ArrayList<CursorTheme>(themes.values());
    }

    /**
     * This method resets the position of the active cursor to the
     * center of the camera view.
     *
     * This call does not work for Mouse or GearVr.
     */
    public void resetCursorPosition() {
        if (scene == null) {
            return;
        }
        for (Cursor cursor : cursors) {
            if (cursor.isActive()) {
                IoDevice ioDevice = cursor.getIoDevice();
                if (IoDeviceLoader.isGearVrDevice(ioDevice) || IoDeviceLoader.isMouseIoDevice
                        (ioDevice)) {
                    continue;
                }
                // place the cursor at a fixed depth
                Vector3f position = new Vector3f(0.0f, 0.0f, -cursorScale);
                // now get the position with respect to the camera.
                position.mulPoint(scene.getMainCameraRig().getHeadTransform().getModelMatrix4f());
                cursor.setPosition(position.x, position.y, position.z);
            }
        }
    }

    /**
     * Use this method to ensure that the cursor remains within the bounds of the users frustum
     *
     * @param enable <code>true</code> to enable, <code>false</code> to disable.
     */
    public void setCursorOnScreen(boolean enable) {
        if (enable && frustumChecker == null) {
            frustumChecker = new FrustumChecker(context, scene);
        } else if ((enable == false) && frustumChecker != null) {
            frustumChecker.close();
            frustumChecker = null;
        }
    }

    /**
     * Adds an {@link IoDevice} to the {@link CursorManager}. <p>Each {@link Cursor} has a
     * prioritized list of {@link IoDevice}s it is compatible with in the settings.xml file. The
     * added {@link IoDevice} will be attached to a cursor if: <br>1. The {@link Cursor} is
     * currently not attached to any other {@link IoDevice}. <br>2. The {@link IoDevice} is in
     * the list of the {@link Cursor}'s compatible {@link IoDevice}s as specified in the settings
     * .xml file. <br>3. There is no other {@link IoDevice} available that has a higher priority
     * than the added {@link IoDevice} for that particular {@link Cursor}.</p>
     *
     * @param ioDevice The {@link IoDevice} to be added.
     */
    public void addIoDevice(IoDevice ioDevice) {
        if (ioDevice != null) {
            inputManager.addIoDevice(ioDevice);
        }
    }

    Map<String, CursorTheme> getThemeMap() {
        return themes;
    }

    List<Cursor> getUnusedCursors() {
        return unusedCursors;
    }

    /**
     * This returns a list of {@link IoDevice}s that are connected and currently controlling a
     * {@link Cursor}. This list will not include any {@link IoDevice} that is not connected or
     * connected but not controlling a {@link Cursor}.
     * @return The list of used {@link IoDevice}s
     */
    public List<IoDevice> getUsedIoDevices() {
        return new ArrayList<IoDevice>(usedIoDevices);
    }

    private class FrustumChecker implements GVRDrawFrameListener {
        private final FrustumCuller culler;
        private final Matrix4f viewMatrix;
        private final Matrix4f projectionMatrix;
        private final Matrix4f vpMatrix;
        private final Vector3f position;
        private GVRPerspectiveCamera centerCamera;
        private GVRScene scene;
        private Vector3f savedPosition;
        private float savedDepth;
        private GVRSceneObject temp;
        private Quaternionf rotation;
        private final Vector3f result;

        FrustumChecker(GVRContext context, GVRScene scene) {
            culler = new FrustumCuller();
            viewMatrix = new Matrix4f();
            projectionMatrix = new Matrix4f();
            vpMatrix = new Matrix4f();
            position = new Vector3f();
            result = new Vector3f();
            this.scene = scene;
            temp = new GVRSceneObject(context);
            context.registerDrawFrameListener(this);
        }

        @Override
        public void onDrawFrame(float v) {
            if (scene == null) {
                return;
            }

            centerCamera = scene.getMainCameraRig().getCenterCamera();
            viewMatrix.set(scene.getMainCameraRig().getHeadTransform().getModelMatrix4f());
            viewMatrix.invert();
            projectionMatrix.identity();
            projectionMatrix.perspective(centerCamera.getFovY(), centerCamera.getAspectRatio(),
                    centerCamera
                            .getNearClippingDistance(), centerCamera.getFarClippingDistance());
            projectionMatrix.mul(viewMatrix, vpMatrix);
            culler.set(vpMatrix);

            for (Cursor cursor : cursors) {
                if (cursor.isActive() == false) {
                    position.set(cursor.getPositionX(), cursor.getPositionY(), cursor
                            .getPositionZ());
                    position.mulPoint(cursor.getMainSceneObject().getTransform().getModelMatrix4f
                            ());
                    boolean inFrustum = culler.isPointInsideFrustum(position);

                    if (inFrustum) {
                        savedPosition = null;
                        savedDepth = 0;
                    } else {
                        if (savedPosition == null) {
                            position.set(cursor.getPositionX(), cursor.getPositionY(), cursor
                                    .getPositionZ());
                            savedDepth = getDistance(position.x, position.y, position.z);
                            savedPosition = new Vector3f(0.0f, 0.0f, -savedDepth);
                            savedPosition.mulPoint(scene.getMainCameraRig().getHeadTransform()
                                    .getModelMatrix4f(), result);
                            rotation = getRotation(result, position);
                        } else {
                            savedPosition.mulPoint(scene.getMainCameraRig().getHeadTransform()
                                    .getModelMatrix4f(), result);
                            temp.getTransform().setPosition(result.x, result.y, result.z);
                            temp.getTransform().rotateWithPivot(rotation.w, rotation.x, rotation
                                    .y, rotation.z, 0.0f, 0.0f, 0.0f);
                            cursor.setPosition(temp.getTransform().getPositionX(),
                                    temp.getTransform().getPositionY(),
                                    temp.getTransform().getPositionZ());
                        }
                    }
                }
            }
        }

        /**
         * formulae for quaternion rotation taken from
         * http://lolengine.net/blog/2014/02/24/quaternion-from-two-vectors-final
         **/
        private Quaternionf getRotation(Vector3f start, Vector3f end) {
            float norm_u_norm_v = (float) Math.sqrt(start.dot(start) * end.dot(end));
            float real_part = norm_u_norm_v + start.dot(end);
            Vector3f w = new Vector3f();

            if (real_part < 1.e-6f * norm_u_norm_v) {
                /** If u and v are exactly opposite, rotate 180 degrees
                 * around an arbitrary orthogonal axis. Axis normalisation
                 * can happen later, when we normalise the quaternion.*/
                real_part = 0.0f;
                if (Math.abs(start.x) > Math.abs(start.z)) {
                    w = new Vector3f(-start.y, start.x, 0.f);
                } else {
                    w = new Vector3f(0.f, -start.z, start.y);
                }
            } else {
                /** Otherwise, build quaternion the standard way. */
                start.cross(end, w);
            }
            return new Quaternionf(w.x, w.y, w.z, real_part).normalize();
        }

        void close() {
            context.unregisterDrawFrameListener(this);
        }
    }

    IoDevice getAvailableIoDevice(IoDevice targetIoDevice) {
        int i = unusedIoDevices.indexOf(targetIoDevice);
        if (i == -1) {
            return null;
        } else {
            return unusedIoDevices.get(i);
        }
    }

    void markCursorUnused(Cursor cursor) {
        Log.d(TAG, "Marking cursor:" + cursor.getName() + " unused");
        scene.removeSceneObject(cursor.getMainSceneObject());
        synchronized (cursors) {
            cursors.remove(cursor);
        }
        unusedCursors.add(cursor);
        for (CursorActivationListener listener : activationListeners) {
            listener.onDeactivated(cursor);
        }
        markIoDeviceUnused(cursor.getIoDevice());
        cursor.close();
    }

    void markIoDeviceUnused(IoDevice ioDevice) {
        Log.d(TAG, "Marking ioDevice:" + ioDevice.getName() + " unused");
        usedIoDevices.remove(ioDevice);
        unusedIoDevices.add(ioDevice);
        assignIoDevicesToCursors();
    }

    void markIoDeviceUsed(IoDevice ioDevice) {
        unusedIoDevices.remove(ioDevice);
        usedIoDevices.add(ioDevice);
    }

    /**
     * Use this method to set a {@link GVRScene}. This call replaces the currently set {@link
     * GVRScene} (if one was set either using the constructor or a previous {@link
     * CursorManager#setScene(GVRScene)} call).
     * <br/>
     * <br/>
     *
     * If the provided {@link GVRScene} matches the currently set {@link GVRScene} then this call
     * has no effect. Calling this method with a <code>null</code> resets the scene currently set
     * with the {@link CursorManager}.
     *
     * @param scene the {@link GVRScene} to be set, or <code>null</code>.
     */
    public void setScene(GVRScene scene) {
        if (this.scene == scene) {
            //do nothing return
            return;
        }

        //cleanup on the currently set scene if there is one
        if (this.scene != null) {
            // false to remove
            updateCursorsInScene(this.scene, false);
        }

        this.scene = scene;

        if (scene == null) {
            return;
        }
        // process the new scene, use true to add
        updateCursorsInScene(scene, true);
    }

    /**
     * This method modifies the {@link Cursor} passed in the argument to a settings cursor. A
     * settings cursor is a {@link Cursor} of type {@link CursorType#LASER} used to interact with a
     * {@link GVRViewSceneObject}. Since it is easier to use a {@link Cursor} of type
     * {@link CursorType#LASER} to interract with {@link GVRViewSceneObject} this convinience
     * method is provided, so that the applications which do not use a {@link Cursor} of type
     * {@link CursorType#LASER} do not have to instantiate and manage two cursors while
     * interracting with a {@link GVRViewSceneObject}.
     *
     * @param cursor The {@link Cursor} whose {@link IoDevice} will be used for the settings
     *               cursor.
     */
    public void enableSettingsCursor(Cursor cursor) {
        menuCursor = cursor;
        IoDevice device = cursor.getIoDevice();
        settingsIoDeviceFarDepth = device.getFarDepth();
        settingsIoDeviceNearDepth = device.getNearDepth();
        scene.removeSceneObject(cursor.getMainSceneObject());
        settingsCursor.transferIoDevice(cursor);
        scene.addSceneObject(settingsCursor.getMainSceneObject());
    }

    /**
     * This method disables the settings cursor enabled by the
     * {@link CursorManager#enableSettingsCursor(Cursor)} method and restores the {@link Cursor}
     * that was passed as an argument to the {@link CursorManager#enableSettingsCursor(Cursor)}
     * method. This method is used once interraction with a {@link GVRViewSceneObject} is not
     * longer needed.
     */
    public void disableSettingsCursor() {
        if(menuCursor != null) {
            scene.removeSceneObject(settingsCursor.getMainSceneObject());
            menuCursor.transferIoDevice(settingsCursor);
            settingsCursor.ioDevice = null; // clear IoDevice of the settings cursor.
            scene.addSceneObject(menuCursor.getMainSceneObject());
            IoDevice device = menuCursor.ioDevice;
            device.setFarDepth(settingsIoDeviceFarDepth);
            device.setNearDepth(settingsIoDeviceNearDepth);
            scene.addSceneObject(menuCursor.getMainSceneObject());
            menuCursor = null;
        }
    }

    private Cursor menuCursor;
    /**
     * Presents the Cursor Settings to the User. Only works if scene is set.
     */
    private void showSettingsMenu(final Cursor cursor) {
        Log.d(TAG, "showSettingsMenu");
        enableSettingsCursor(cursor);
        context.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new SettingsView(context, scene, CursorManager.this,
                        settingsCursor.getIoDevice().getCursorControllerId(), cursor, new
                        SettingsChangeListener() {
                            @Override
                            public void onBack(boolean cascading) {
                                disableSettingsCursor();
                            }

                            @Override
                            public int onDeviceChanged(IoDevice device) {
                                // we are changing the io device on the settings cursor
                                IoDevice clickedDevice = getAvailableIoDevice(device);
                                IoDevice oldIoDevice = settingsCursor.getIoDevice();
                                settingsCursor.setIoDevice(clickedDevice);
                                markIoDeviceUsed(clickedDevice);
                                markIoDeviceUnused(oldIoDevice);
                                return device.getCursorControllerId();
                            }
                        });
            }
        });
    }

    /**
     * Use this method to show/hide the settings menu.
     *
     * @param cursor  The {@link Cursor} object that triggered this action
     * @param visible use <code>true</code> to show the settings menu, <code>false</code> to hide
     *                it.
     */
    public void setSettingsMenuVisibility(Cursor cursor, boolean visible) {
        if (visible) {
            showSettingsMenu(cursor);
        } else {
            disableSettingsCursor();
        }
    }

    /**
     * Request the manager for the list of currently active {@link Cursor}s.
     * <p/>
     * Make sure the {@link CursorActivationListener} is set after making this call to
     * get updates about Cursor objects activated or deactivated from the {@link CursorManager}.
     */
    public List<Cursor> getActiveCursors() {
        List<Cursor> returnList = new ArrayList<Cursor>();
        for (Cursor cursor : cursors) {
            if (cursor.isActive()) {
                returnList.add(cursor);
            }
        }
        return returnList;
    }

    /**
     * Request the {@link CursorManager} for a list of {@link Cursor}s that are not currently
     * active. </p>
     * These {@link Cursor}s are declared in the settings.xml but do not have an {@link
     * IoDevice} attached to them.
     *
     * @return the list of inactive {@link Cursor}s
     */
    public List<Cursor> getInactiveCursors() {
        List<Cursor> cursors = new ArrayList<Cursor>();
        cursors.addAll(unusedCursors);
        return cursors;
    }

    /**
     * Register a {@link CursorActivationListener} with the manager.
     * <p/>
     *
     * The listener notifies the application whenever a Cursor is added/removed from the
     * manager.
     *
     * Note that this call {@link CursorActivationListener#onActivated(Cursor)} would only return
     * the future {@link Cursor} objects activated. To know the list of {@link Cursor} objects
     * activated at the time of this call, make use of the {@link #getActiveCursors()}.
     *
     *
     * @param listener the {@link CursorActivationListener} to be added.<code>null</code>
     *                 objects are ignored.
     */
    public void addCursorActivationListener(
            CursorActivationListener listener) {
        if (null == listener) {
            // ignore null input
            return;
        }
        activationListeners.add(listener);
    }

    /**
     * Remove a registered {@link CursorActivationListener}
     *
     * @param listener the {@link CursorActivationListener} to be removed.<code>null</code>
     *                 objects are ignored.
     */
    public void removeCursorActivationListener(
            CursorActivationListener listener) {
        if (listener == null) {
            // ignore null input
            return;
        }
        activationListeners.remove(listener);
    }

    /**
     * Use this call to make all the {@link GVRSceneObject}s in the provided GVRScene to be
     * selectable.
     * <p/>
     * In order to have more control over objects that can be made selectable make use of the
     * {@link #addSelectableObject(GVRSceneObject)} method.
     * <p/>
     * Note that this call will set the current scene as the provided scene. If the provided
     * scene is same the currently set scene then this method will have no effect. Passing null
     * will remove any objects that are selectable and set the scene to null
     *
     * @param scene the {@link GVRScene} to be made selectable or <code>null</code>.
     */
    public void makeSceneSelectable(GVRScene scene) {
        if (this.scene == scene) {
            //do nothing return
            return;
        }

        //cleanup on the currently set scene if there is one
        if (this.scene != null) {
            //false to remove
            updateCursorsInScene(this.scene, false);
            for (GVRSceneObject object : this.scene.getSceneObjects()) {
                if (object.getSensor() == cursorSensor) {
                    object.setSensor(null);
                    object.getEventReceiver().removeListener(cursorSensor);
                }
            }
        }

        this.scene = scene;

        if (scene == null) {
            return;
        }
        // process the new scene

        cursorScale = 0;
        // TODO check if the objects are okay
        for (GVRSceneObject object : scene.getSceneObjects()) {
            addSelectableObject(object);
        }
        //true to add
        updateCursorsInScene(scene, true);
    }

    /**
     * Add or remove the active cursors from the provided scene.
     *
     * @param scene The GVRScene.
     * @param add   <code>true</code> for add, <code>false</code> to remove
     */
    private void updateCursorsInScene(GVRScene scene, boolean add) {
        for (Cursor cursor : cursors) {
            if (cursor.isActive()) {
                GVRSceneObject object = cursor.getMainSceneObject();
                if (add) {
                    scene.addSceneObject(object);
                } else {
                    scene.removeSceneObject(object);
                }
            }
        }
    }

    /**
     * Remove all the {@link GVRSceneObject}s that have been made selectable.
     */
    public void removeAllSelectableObjects() {
        // Not yet implemented.
        /**
         * Would need to keep track of selectable objects and remove the sensors.
         */
    }

    /**
     * This call makes sure that the {@link GVRSceneObject} passed is a
     * {@link Cursor} selectable object. The {@link Cursor} would deliver events
     * using the {@link CursorEventListener} every time an interaction happens
     * with the {@link GVRSceneObject}.
     * <p/>
     * The Cursor would also provide a visual cue when over an object that this
     * selectable to notify that the user can interact with the object.
     * <p/>
     * Passing an object makes it and its descendant tree selectable.
     * If the entire scene has been made selectable and the
     * {@link GVRSceneObject} is a part of the Scene, then this call will have
     * no effect.
     *
     * @param object the {@link GVRSceneObject} that is to be made selectable.
     * @return <code>true</code> on success or <code>false</code> if the object
     * does not have a {@link GVRMesh} or in case the object was already
     * set as selectable.
     */
    public boolean addSelectableObject(GVRSceneObject object) {
        if (null == object) {
            throw new IllegalArgumentException("GVRSceneObject cannot be null");
        }
        if(object.getSensor() == cursorSensor) {
            return true;
        }

        addSelectableBehavior(object);

        float scale = getDistance(object);
        if (scale > cursorScale) {
            for (Cursor cursor : cursors) {
                cursor.setScale(scale);
            }
            settingsCursor.setScale(scale);
            cursorScale = scale;
        }
        object.setSensor(cursorSensor);
        object.getEventReceiver().addListener(cursorSensor);
        return true;
    }

    /**
     * This call is for objects for which {@link CursorManager#addSelectableObject(GVRSceneObject)}
     * was called. After calling this on a {@link GVRSceneObject} there will be no
     * {@link CursorEvent}s generated when a {@link Cursor} interacts with this
     * {@link GVRSceneObject}. The {@link GVRSceneObject} that was passed in
     * {@link CursorManager#addSelectableObject(GVRSceneObject)} should be passed in here.
     * @param object The {@link GVRSceneObject} that is to be made un-selectable.
     * @return <code>true</code> on success or <code>false</code> if {@link GVRSceneObject} was not
     * set as selectable using the {@link CursorManager#addSelectableObject(GVRSceneObject)}
     */
    public boolean removeSelectableObject(GVRSceneObject object) {
        if (null == object) {
            throw new IllegalArgumentException("GVRSceneObject cannot be null");
        }
        removeSelectableBehavior(object);
        if(object.getSensor() != null) {
            object.setSensor(null);
            object.getEventReceiver().removeListener(cursorSensor);
            return true;
        } else {
            return false;
        }
    }

    private void addSelectableBehavior(GVRSceneObject object) {
        SelectableBehavior selectableBehavior = (SelectableBehavior) object.getComponent(
                SelectableBehavior.getComponentType());
        if (selectableBehavior == null) {
            selectableBehavior = (SelectableBehavior) object.getComponent(MovableBehavior.
                    getComponentType());
        }
        if (selectableBehavior != null) {
            Log.d(TAG, "Adding a Selectable Object");
            selectableBehaviors.add(selectableBehavior);
            if (activationListener == null) {
                createLocalActivationListener();
            }
            for (Cursor cursor : cursors) {
                selectableBehavior.onCursorActivated(cursor);
            }
        } else {
            Log.d(TAG, "Scene Object is not selectable");
        }
    }

    private void removeSelectableBehavior(GVRSceneObject object) {
        SelectableBehavior selectableBehavior = (SelectableBehavior) object.getComponent(
                SelectableBehavior.getComponentType());
        if (selectableBehavior == null) {
            selectableBehavior = (SelectableBehavior) object.getComponent(MovableBehavior.
                    getComponentType());
        }
        if (selectableBehavior != null) {
            selectableBehaviors.remove(selectableBehavior);
        }
    }

    private float getDistance(GVRSceneObject object) {
        // distance is simple since the origin is 0,0,0
        float x = object.getTransform().getPositionX();
        float y = object.getTransform().getPositionY();
        float z = object.getTransform().getPositionZ();
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private float getDistance(float x, float y, float z) {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * By default the all sounds defined by the {@link Cursor} are played during interaction. Use
     * this call to disable all sounds played.
     * <p/>
     * <code>true</code> to enable all sounds, <code>false</code> to disable.
     *
     * @param enable use <code>false</code> to disable all sounds and <code>true</code> to
     *               re-enable.
     */
    public void setSoundEnabled(boolean enable) {
        globalSettings.setSoundEnabled(enable);
    }

    /**
     * Gets the status of the Sounds for the {@link CursorManager}
     * @return <code>true</code> if sound is enabled, <code>false</code> if it is disabled
     */
    public boolean isSoundEnabled() {
        return globalSettings.isSoundEnabled();
    }

    /**
     * Called to perform post clean up of the {@link CursorManager}. All devices registered with
     * the {@link Cursor} objects are cleared and the all device listeners are unregistered.
     *
     * Usually called on destroy.
     */
    public void close() {
        // Use this to perform post cleanup.
        inputManager.removeIoDeviceListener(cursorIoDeviceListener);
        for (Cursor cursor : cursors) {
            cursor.close();
        }
    }

    void assignIoDevicesToCursors() {
        int priority = 0;
        boolean checkSavedIoDevice = true;
        boolean continueLoop = true;
        Log.d(TAG, "Assigning cursors to availableIoDevices");
        while (continueLoop) {
            continueLoop = false;
            for (Iterator<Cursor> cursorIterator = unusedCursors.iterator(); cursorIterator
                    .hasNext(); ) {
                IoDevice compatibleIoDevice = null;
                Cursor unUsedCursor = cursorIterator.next();
                if (!unUsedCursor.isEnabled()) {
                    continue;
                }

                if (checkSavedIoDevice) {
                    if (unUsedCursor.getSavedIoDevice() != null) {
                        compatibleIoDevice = unUsedCursor.getSavedIoDevice();
                        unUsedCursor.clearSavedIoDevice();
                    }
                } else {
                    compatibleIoDevice = unUsedCursor.getIoDeviceForPriority(priority);
                }

                if (compatibleIoDevice != null) {
                    continueLoop = true;

                    for (Iterator<IoDevice> ioDeviceIterator = unusedIoDevices
                            .iterator(); ioDeviceIterator.hasNext(); ) {
                        IoDevice availableIoDevice = ioDeviceIterator.next();

                        Log.d(TAG, "Trying to attach available ioDevice:" +
                                IoDeviceFactory.getXmlString(availableIoDevice) + " to cursor:" +
                                unUsedCursor.getId() + " cursor priority:" + priority + " Io:" +
                                IoDeviceFactory.getXmlString(compatibleIoDevice));
                        if (availableIoDevice.equals(compatibleIoDevice)) {
                            Log.d(TAG, "Found match attaching cursor to Io device");
                            ioDeviceIterator.remove();
                            cursorIterator.remove();
                            addNewCursor(unUsedCursor, availableIoDevice);
                            break;
                        }
                    }
                }
            }
            if (checkSavedIoDevice) {
                continueLoop = true;
                checkSavedIoDevice = false;
            } else {
                priority++;
            }
        }
    }

    private void addNewCursor(Cursor cursor, IoDevice ioDevice) {
        cursor.setIoDevice(ioDevice);
        if (scene != null) {
            scene.addSceneObject(cursor.getMainSceneObject());
            cursor.setScene(scene);
        }

        for (CursorActivationListener listener : activationListeners) {
            listener.onActivated(cursor);
        }
        cursor.setScale(cursorScale);
        usedIoDevices.add(ioDevice);
        cursors.add(cursor);
    }

    private IoDeviceListener cursorIoDeviceListener = new IoDeviceListener() {
        @Override
        public void onIoDeviceAdded(IoDevice addedIoDevice) {
            Log.d(TAG, "IoDevice added:" + addedIoDevice.getDeviceId());
            unusedIoDevices.add(addedIoDevice);
            assignIoDevicesToCursors();

            if(unusedIoDevices.contains(addedIoDevice)){
                addedIoDevice.setEnable(false);
            }
        }

        @Override
        public void onIoDeviceRemoved(IoDevice removedIoDevice) {

            Log.d(TAG, "IoDevice removed:" + removedIoDevice.getDeviceId());
            if (unusedIoDevices.remove(removedIoDevice)) {
                return;
            }

            if (usedIoDevices.remove(removedIoDevice)) {

                for (Iterator<Cursor> cursorIterator = cursors.iterator(); cursorIterator
                        .hasNext(); ) {
                    Cursor cursor = cursorIterator.next();

                    if (cursor.getIoDevice().equals(removedIoDevice)) {
                        cursor.resetIoDevice(removedIoDevice);
                        cursorIterator.remove();
                        unusedCursors.add(cursor);
                        if (scene != null) {
                            scene.removeSceneObject(cursor.getMainSceneObject());
                        }
                        for (CursorActivationListener listener : activationListeners) {
                            listener.onDeactivated(cursor);
                        }
                        assignIoDevicesToCursors();
                        break;
                    }
                }
            } else {
                Log.d(TAG, "Did not find ioDevice in cursormanager list");
            }
        }
    };

    public boolean isDepthOrderEnabled() {
        return cursorSensor.isDepthOrderEnabled();
    }

    /**
     * {@link CursorEvent}s can be grouped with other {@link CursorEvent}s according to the
     * depth of the {@link GVRSceneObject} that the event occurred on. This feature can be enabled
     * or disabled using {@link CursorManager#setDepthOrderEnabled(boolean)}. For eg. When a
     * {@link Cursor} changes position or state and if that change generated {@link CursorEvent}s
     * on multiple {@link GVRSceneObject}s, the generated {@link CursorEvent}s can be sent in
     * order of the distance of the {@link GVRSceneObject} from origin, where the {@link CursorEvent}
     * associated with the {@link GVRSceneObject} closest to the origin is delivered first and
     * has an {@link EventGroup#MULTI_START} as the {@link EventGroup}. All subsequent
     * {@link CursorEvent}s in the same group have {@link EventGroup#MULTI} and are delivered in
     * depth order as described above. The last {@link CursorEvent} in that group has
     * {@link EventGroup#MULTI_STOP} as the {@link EventGroup} value. {@link CursorEvent}s that
     * occurred on only a single {@link GVRSceneObject} have {@link EventGroup#SINGLE} set as
     * their {@link EventGroup}. However when depth order is disabled all {@link CursorEvent}s
     * have the {@link EventGroup#GROUP_DISABLED} as their {@link EventGroup} value.
     *
     * Enabling this feature will incur extra cost every time there is a change in the
     * {@link Cursor} position or state. The {@link CursorEvent}s need to be grouped
     * and sorted according to the distance of the associated {@link GVRSceneObject}s from the
     * origin. This feature should only be turned on if needed.
     *
     * The {@link EventGroup} given to {@link CursorEvent}s can be used in apps where there are
     * multiple overlapping {@link GVRSceneObject}s and the application has to decide which of
     * the {@link GVRSceneObject}s will handle the {@link CursorEvent}.
     *
     * @see CursorEvent#getEventGroup()
     * @param depthOrderEnabled
     */
    public void setDepthOrderEnabled(boolean depthOrderEnabled) {
        cursorSensor.setDepthOrderEnabled(depthOrderEnabled);
    }

    private class CursorSensor extends GVRBaseSensor implements ISensorEvents {

        public CursorSensor(GVRContext context) {
            super(context);
        }

        @Override
        public void onSensorEvent(SensorEvent event) {
            int id = event.getCursorController().getId();
            Cursor cursor;
            for (int i = 0; i < cursors.size(); i++) {
                synchronized (cursors) {
                    cursor = cursors.get(i);
                }
                if (cursor == null || cursor.getIoDevice() == null) {
                    continue;
                }
                int cursorControllerId = cursor.getIoDevice().getCursorControllerId();
                if (id == cursorControllerId) {
                    cursor.dispatchSensorEvent(event);
                }
            }
        }
    }

    /**
     * Save the configuration of the {@link CursorManager} that is currently in use.
     * </p>The configuration includes the following things:
     * <br> 1. The themes being used by each {@link Cursor}
     * <br> 2. The state of the {@link Cursor}s i.e. enabled/disabled
     * <br> 3. The {@link IoDevice} being used by each active {@link Cursor}.
     * The configuration will be persisted across app launches.
     */
    public void saveSettings() {
        try {
            SettingsParser.saveSettings(context.getContext(), this);
        } catch (IOException e) {
            Log.d(TAG, "Could not save settings to file", e);
        }
    }

    private void createLocalActivationListener() {
        activationListener = new CursorActivationListener() {

            @Override
            public void onDeactivated(Cursor cursor) {
                Log.d(TAG, "Cursor DeActivated:" + cursor.getName());
                cursor.removeCursorEventListener(cursorEventListener);
                for (SelectableBehavior selectableBehavior : selectableBehaviors) {
                    selectableBehavior.onCursorDeactivated(cursor);
                }
            }

            @Override
            public void onActivated(Cursor cursor) {
                Log.d(TAG, "On CursorActivated");
                for (SelectableBehavior selectableBehavior : selectableBehaviors) {
                    selectableBehavior.onCursorActivated(cursor);
                }
                cursor.addCursorEventListener(cursorEventListener);
            }
        };

        addCursorActivationListener(activationListener);
        // Collect all active cursors and register for all future active cursors.
        for (Cursor cursor : cursors) {
            if (cursor.isActive()) {
                activationListener.onActivated(cursor);
            }
        }
    }

    private CursorEventListener cursorEventListener = new CursorEventListener() {

        @Override
        public void onEvent(CursorEvent event) {
            GVRSceneObject sceneObject = event.getObject();
            while (sceneObject != null && !callEventHandler(sceneObject, event) && sceneObject
                    .getParent() != null) {
                sceneObject = sceneObject.getParent();
            }
        }
    };

    private boolean callEventHandler(GVRSceneObject sceneObject, CursorEvent event) {
        SelectableBehavior selectableBehavior = (SelectableBehavior) sceneObject.getComponent
                (SelectableBehavior.getComponentType());
        if (selectableBehavior == null) {
            selectableBehavior = (SelectableBehavior) sceneObject.getComponent(MovableBehavior
                    .getComponentType());
        }
        if (selectableBehavior != null) {
            selectableBehavior.handleCursorEvent(event);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the {@link GVRContext} instance associated with the {@link CursorManager}. This is
     * the {@link GVRContext} object passed in the constructor.
     * @return the {@link GVRContext} instance.
     */
    public GVRContext getGVRContext() {
        return context;
    }
}
