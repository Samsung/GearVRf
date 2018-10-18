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


import android.view.MotionEvent;

import org.gearvrf.GVRBoundsPicker;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventReceiver;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRSensor;
import org.gearvrf.GVRSwitch;
import org.gearvrf.IEventReceiver;
import org.gearvrf.IEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRGearCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.io.cursor3d.settings.SettingsView;
import org.gearvrf.io.cursor3d.settings.SettingsView.SettingsChangeListener;
import org.gearvrf.scene_objects.GVRViewSceneObject;
import org.gearvrf.utility.Log;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Create an instance of the {@link CursorManager} to obtain {@link Cursor} objects to be used by
 * the application.
 * <p/>
 * To get a list of all the active {@link Cursor} objects, use the {@link
 * CursorManager#getActiveCursors()} call. Additionally register a {@link
 * ICursorActivationListener} to know about {@link Cursor} objects activated or deactivated
 * by the {@link CursorManager} during runtime.
 * <p/>
 * The {@link CursorManager} will add/remove {@link Cursor} objects based on the changes
 * requested by the application. The {@link CursorManager} is also responsible for setting
 * properties of the {@link Cursor} objects - look at the various methods defined by this class.
 */
public final class CursorManager implements IEventReceiver
{
    // Result of XML parsing in a package of all settings that need displaying.
    private static final String TAG = CursorManager.class.getSimpleName();
    private static final float DEFAULT_CURSOR_SCALE = 10.0f;
    static String SETTINGS_SOURCE = "settings.xml";

    private GVRContext context;
    private GVRScene scene;
    // List of all the cursors available to the user.
    private final List<Cursor> mCursors = new ArrayList<>();;
    private final List<IoDevice> mIODevices = new ArrayList<IoDevice>();
    private float mCursorDepth;
    private Map<String, CursorTheme> themes;
    private final GlobalSettings globalSettings;
    //Create a laser cursor to use on the settings menu
    private LaserCursor settingsCursor;
    private float settingsIoDeviceFarDepth, settingsIoDeviceNearDepth;
    private ICursorActivationListener activationListener;
    private List<SelectableBehavior> selectableBehaviors;
    private GVRBoundsPicker objectCursorPicker;
    private GVREventReceiver listeners;

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
     * initialization will be
     * only be assigned to a {@link Cursor} if that particular {@link Cursor} has not been attached
     * to any {@link IoDevice} yet.
     *
     * @param context   The GVRf Context
     * @param scene     the default {@link GVRScene} to use while adding/removing Cursor
     *                  objects.
     * @param ioDevices A list of {@link IoDevice}s to add to the {@link
     *                  CursorManager} at initialization.
     */
    public CursorManager(GVRContext context, GVRScene scene, List<IoDevice> ioDevices) {
        if (context == null) {
            throw new IllegalArgumentException("GVRContext cannot be null");
        }
        this.scene = scene;
        this.context = context;
        listeners = new GVREventReceiver(this);
        globalSettings = GlobalSettings.getInstance();
        themes = new HashMap<String, CursorTheme>();
        selectableBehaviors = new ArrayList<SelectableBehavior>();
        mCursorDepth = DEFAULT_CURSOR_SCALE;

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

        synchronized (mCursors) {
            for (Cursor cursor : mCursors) {
                CursorTheme theme = themes.get(cursor.clearSavedThemeId());
                if (theme == null) {
                    throw new IllegalArgumentException("Illegal themeId used for cursor");
                }
                cursor.setCursorTheme(theme);
            }
        }

        GVRInputManager inputMgr = context.getInputManager();
        if (ioDevices != null)
        {
            for (IoDevice ioDevice : ioDevices)
            {
                inputMgr.addCursorController(ioDevice.getGvrCursorController());
            }
        }
        inputMgr.getEventReceiver().addListener(cursorIoDeviceListener);
        inputMgr.scanControllers();

        settingsCursor = new LaserCursor(context, this);

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
     * Get the {@link GVREventReceiver} which dispatches cursor events.
     * @return {@link GVREventReceiver} for cursor events.
     * @see GVREventReceiver#addListener(IEvents)
     */
    public GVREventReceiver getEventReceiver() { return listeners; }

    /**
     * Gives a list of all the {@link CursorTheme}s listed in the settings.xml file. Use this
     * call to get a list of available {@link CursorTheme}s and {@link Cursor#setCursorTheme(CursorTheme)}
     * to change the {@link CursorTheme} associated with a {@link Cursor}
     * @return A list of {@link CursorTheme}s as defined in the settings.xml
     */
    public List<CursorTheme> getCursorThemes() {
        return new ArrayList<CursorTheme>(themes.values());
    }

    private int getNumUnusedDevices()
    {
        synchronized (mIODevices)
        {
            int n = 0;
            for (IoDevice d : mIODevices)
            {
                if (!d.isEnabled())
                {
                    ++n;
                }
            }
            return n;
        }
    }

    Map<String, CursorTheme> getThemeMap() {
        return themes;
    }

    public boolean isDeviceActive(IoDevice device)
    {
        GVRCursorController controller = device.getGvrCursorController();
        if ((controller != null) && controller.isEnabled())
            return (controller.getCursor() != null);
        return false;
    }

    /**
     * This returns a list of {@link IoDevice}s that are connected and currently controlling a
     * {@link Cursor}. This list will not include any {@link IoDevice} that is not connected or
     * connected but not controlling a {@link Cursor}.
     * @return The list of used {@link IoDevice}s
     */
    public List<IoDevice> getUsedIoDevices()
    {
        final ArrayList<IoDevice> used = new ArrayList<>();
        synchronized (mIODevices)
        {
            for (IoDevice d : mIODevices)
            {
                if (isDeviceActive(d))
                {
                    used.add(d);
                }
            }
        }
        return used;
    }

    /**
     * This returns a list of {@link IoDevice}s that are available but not controlling a
     * {@link Cursor}.
     * @return The list of unused {@link IoDevice}s
     */
    private List<IoDevice> getAvailableIoDevices()
    {
        final ArrayList<IoDevice> used = new ArrayList<>();
        synchronized (mIODevices)
        {
            for (IoDevice d : mIODevices)
            {
                if (!isDeviceActive(d))
                {
                    used.add(d);
                }
            }
        }
        return used;
    }

    IoDevice getAvailableIoDevice(IoDevice targetIoDevice)
    {
        IoDevice d = getIoDevice(targetIoDevice);
        if (!isDeviceActive(d))
        {
            return d;
        }
        return null;
    }

    IoDevice getIoDevice(IoDevice targetIoDevice)
    {
        synchronized (mIODevices)
        {
            int i = mIODevices.indexOf(targetIoDevice);
            if (i == -1)
            {
                return null;
            }
            else
            {
                return mIODevices.get(i);
            }
        }
    }


    void addCursor(Cursor cursor) {
        synchronized (mCursors) {
            mCursors.add(cursor);
        }
    }

    int getCursorCount() {
        synchronized (mCursors) {
            return mCursors.size();
        }
    }

    void markCursorUnused(Cursor cursor) {
        Log.d(TAG, "Marking cursor:" + cursor.getName() + " unused");
        removeCursorFromScene(cursor);
        context.getEventManager().sendEvent(this, ICursorActivationListener.class, "onDeactivated", cursor);
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
        if (objectCursorPicker != null) {
            objectCursorPicker.setScene(scene);
        }
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
        if (device != null)
        {
            settingsIoDeviceFarDepth = device.getFarDepth();
            settingsIoDeviceNearDepth = device.getNearDepth();
        }
        settingsCursor.transferIoDevice(cursor);
    }

    /**
     * This method disables the settings cursor enabled by the
     * {@link CursorManager#enableSettingsCursor(Cursor)} method and restores the {@link Cursor}
     * that was passed as an argument to the {@link CursorManager#enableSettingsCursor(Cursor)}
     * method. This method is used once interraction with a {@link GVRViewSceneObject} is not
     * longer needed.
     */
    public void disableSettingsCursor() {
        if (menuCursor != null) {
            if (menuCursor.getIoDevice() == null)
            {
                menuCursor.transferIoDevice(settingsCursor);
            }
            IoDevice device = menuCursor.getIoDevice();
            device.setFarDepth(settingsIoDeviceFarDepth);
            device.setNearDepth(settingsIoDeviceNearDepth);
            menuCursor = null;
        }
    }

    public boolean replaceCursor(Cursor newCursor, Cursor oldCursor)
    {
        if (newCursor.isActive())
        {
            return true;
        }
        IoDevice d = oldCursor.getIoDevice();

        if ((d != null) && newCursor.isDeviceCompatible(d))
        {
            newCursor.setEnable(true);
            newCursor.transferIoDevice(oldCursor);
            return true;
        }
        for (Cursor c : getActiveCursors())
        {
            if ((c != newCursor) && c.isEnabled())
            {
                d = c.getIoDevice();
                if (newCursor.isDeviceCompatible(d))
                {
                    newCursor.transferIoDevice(c);
                    return true;
                }
            }
        }
        return false;
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
                                                 removeCursorFromScene(settingsCursor);
                                                 IoDevice clickedDevice = getAvailableIoDevice(device);
                                                 settingsCursor.setIoDevice(clickedDevice);
                                                 addCursorToScene(settingsCursor);
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
     * Make sure the {@link ICursorActivationListener} is set after making this call to
     * get updates about Cursor objects activated or deactivated from the {@link CursorManager}.
     */
    public List<Cursor> getActiveCursors() {
        List<Cursor> returnList = new ArrayList<Cursor>();
        synchronized (mCursors) {
            for (Cursor cursor : mCursors) {
                if (cursor.isActive()) {
                    returnList.add(cursor);
                }
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
    public List<Cursor> getInactiveCursors()
    {
        List<Cursor> returnList = new ArrayList<Cursor>();
        synchronized (mCursors)
        {
            for (Cursor cursor : mCursors)
            {
                if (!cursor.isActive())
                {
                    returnList.add(cursor);
                }
            }
        }
        return returnList;
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
        }

        this.scene = scene;

        if (scene == null) {
            return;
        }
        // process the new scene
        for (GVRSceneObject object : scene.getSceneObjects()) {
            addSelectableObject(object);
        }
        //true to add
        updateCursorsInScene(scene, true);
    }

    void addCursorToScene(Cursor cursor) {
        IoDevice ioDevice = cursor.getIoDevice();
        GVRCursorController controller = ioDevice.getGvrCursorController();

        controller.setEnable(true);
        controller.setCursor(cursor.getOwnerObject());
        if (controller instanceof GVRGearCursorController)
        {
            ((GVRGearCursorController) controller).showControllerModel(true);
        }
        cursor.setCursorDepth(mCursorDepth);
        if (cursor instanceof ObjectCursor)
        {
            ObjectCursor ocurs = (ObjectCursor) cursor;
            if (objectCursorPicker == null)
            {
                objectCursorPicker = new GVRBoundsPicker(scene, false);
                objectCursorPicker.getEventReceiver().addListener(GVRSensor.getPickHandler());
                objectCursorPicker.getEventReceiver().addListener(touchListener);
            }
            if (ocurs.getColliderID() < 0)
            {
                ocurs.setColliderID(objectCursorPicker.addCollidable(cursor.getOwnerObject()));
            }
            objectCursorPicker.setController(controller);
            controller.removePickEventListener(touchListener);
            controller.removePickEventListener(GVRSensor.getPickHandler());
            controller.setCursorControl(GVRCursorController.CursorControl.CURSOR_DEPTH_FROM_CONTROLLER);
        }
        else
        {
            controller.addPickEventListener(touchListener);
            controller.addPickEventListener(GVRSensor.getPickHandler());
            controller.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
        }
    }

    void removeCursorFromScene(Cursor cursor) {
        IoDevice ioDevice = cursor.getIoDevice();

        if (ioDevice != null)
        {
            cursor.close();
            ioDevice.getGvrCursorController().removePickEventListener(cursor.getTouchListener());
        }
        if (cursor instanceof ObjectCursor)
        {
            ObjectCursor ocurs = (ObjectCursor) cursor;
            if (ocurs.getColliderID() >= 0)
            {
                objectCursorPicker.removeCollidable(ocurs.getColliderID());
                ocurs.setColliderID(-1);
            }
        }
    }

    /**
     * Add or remove the active cursors from the provided scene.
     *
     * @param scene The GVRScene.
     * @param add   <code>true</code> for add, <code>false</code> to remove
     */
    private void updateCursorsInScene(GVRScene scene, boolean add) {
        synchronized (mCursors) {
            for (Cursor cursor : mCursors) {
                if (cursor.isActive()) {
                    if (add) {
                        addCursorToScene(cursor);
                    } else {
                        removeCursorFromScene(cursor);
                    }
                }
            }
        }
    }

    /**
     * This call makes sure that the {@link GVRSceneObject} passed is a
     * {@link Cursor} selectable object. The {@link Cursor} would deliver events
     * every time an interaction happens with the {@link GVRSceneObject}.
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
        addSelectableBehavior(object);
        return true;
    }

    /**
     * This call is for objects for which {@link CursorManager#addSelectableObject(GVRSceneObject)}
     * was called. After calling this on a {@link GVRSceneObject} there will be no
     * events generated when a {@link Cursor} interacts with this
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
        return true;
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
            synchronized (selectableBehaviors)
            {
                selectableBehaviors.add(selectableBehavior);
            }
            if (activationListener == null) {
                createLocalActivationListener();
            }
            synchronized (mCursors) {
                for (Cursor cursor : mCursors) {
                    selectableBehavior.onCursorActivated(cursor);
                }
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
            synchronized (selectableBehaviors)
            {
                selectableBehaviors.remove(selectableBehavior);
            }
        }
        object.detachComponent(GVRSwitch.getComponentType());
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
        synchronized (mCursors) {
            for (Cursor cursor : mCursors) {
                cursor.close();
            }
        }
    }

    void assignIoDevicesToCursors(boolean scanUsedCursors) {
        Log.d(TAG, "Assigning cursors to availableIoDevices");
        /*
         * First scan all the unused cursors and make sure each one has a device.
         */
        synchronized (mCursors)
        {
            for (Iterator<Cursor> cursorIterator = mCursors.iterator(); cursorIterator.hasNext(); )
            {
                final Cursor cursor = cursorIterator.next();
                if (!cursor.isActive() && cursor.isEnabled())
                {
                    assignDeviceToCursor(cursor);
                }
            }
        /*
         * If there are devices left over, scan the used cursors to see if
         * a device assignment needs to be changed.
         */
            if (scanUsedCursors && (getNumUnusedDevices() == 0))
            {
                for (Iterator<Cursor> cursorIterator = mCursors.iterator(); cursorIterator.hasNext(); )
                {
                    final Cursor cursor = cursorIterator.next();
                    if (cursor.isActive())
                    {
                        assignDeviceToCursor(cursor);
                    }
                }
            }
        }
    }

    /**
     * Assign an {@link IoDevice} to the given {@link Cursor}.
     * If the cursor is not already attached to an IO device,
     * the highest priority device that is compatible with the
     * cursor is selected. Otherwise a new device will be assigned
     * only if its priority is greater than the one already attached.
     * @param cursor
     * @return  1 = cursor attached to device and added to scene,
     *          0 = device replaced on cursor,
     *          -1 = nothing was done
     */
    int assignDeviceToCursor(final Cursor cursor)
    {
        int currentPriority = cursor.getCurrentDevicePriority();
        IoDevice savedDevice  = cursor.getSavedIoDevice();

        /*
         * Give preference to the saved device for this cursor
         */
        if ((savedDevice != null) && (currentPriority < 0))
        {
            int priority = cursor.getDevicePriority(savedDevice);
            cursor.clearSavedIoDevice();
            return attachCursorToDevice(cursor, savedDevice, 1, priority);
        }
        List<IoDevice> availableDevices = getAvailableIoDevices();
        for (IoDevice d : availableDevices)
        {
            IoDevice availableIoDevice = d;
            int priority = cursor.getDevicePriority(availableIoDevice);

            Log.d(TAG, "Trying to attach available ioDevice:" + IoDeviceFactory.getXmlString(availableIoDevice) + " to cursor:" + cursor.getId());
        /*
         * If the unused device is compatible with this cursor and
         * has a higher priority than the current device associated
         * with the cursor, use it instead of the current device.
         * If there is no device for this cursor, use the first available.
         */
            if (priority > 0)
            {
                if (currentPriority < 0)
                {
                    return attachCursorToDevice(cursor, availableIoDevice, 1, priority);
                }
                else if (priority < currentPriority)
                {
                    return attachCursorToDevice(cursor, availableIoDevice, 0, priority);
                }
            }
        }
        return -1;
    }

    int assignCursorToDevice(final IoDevice device)
    {
        synchronized (mCursors)
        {
            /*
             * First check all the enabled cursors which are not active.
             */
            for (Cursor cursor : mCursors)
            {
                if (cursor.isActive() || !cursor.isEnabled())
                {
                    continue;
                }
                int priority = cursor.getDevicePriority(device);
                int oldPriority = cursor.getCurrentDevicePriority();

                Log.d(TAG, "Trying to attach available ioDevice:" + IoDeviceFactory.getXmlString(device) + " to inactive cursor:" + cursor.getId());
                if (priority > 0)
                {
                    if (oldPriority < 0)
                    {
                        return attachCursorToDevice(cursor, device, 1, priority);
                    }
                    else if (priority < oldPriority)
                    {
                        return attachCursorToDevice(cursor, device, 0, priority);
                    }
                }
            }
            /*
             * Now check all the enabled cursors which are active to see if we should
             * replace their controller with this one
             */
            for (Cursor cursor : mCursors)
            {
                if (!cursor.isEnabled() || !cursor.isActive())
                {
                    continue;
                }
                int priority = cursor.getDevicePriority(device);
                int oldPriority = cursor.getCurrentDevicePriority();

                Log.d(TAG, "Trying to attach available ioDevice:" + IoDeviceFactory.getXmlString(device) + " to active cursor:" + cursor.getId());
                if (priority > 0)
                {
                    if (oldPriority < 0)
                    {
                        return attachCursorToDevice(cursor, device, 1, priority);
                    }
                    else if (priority < oldPriority)
                    {
                        return attachCursorToDevice(cursor, device, 0, priority);
                    }
                }
            }
        }
        return -1;
    }

    private int attachCursorToDevice(final Cursor cursor, final IoDevice device, int action, int priority)
    {
        getGVRContext().getEventManager().sendEvent(getGVRContext().getInputManager(),
                                                    GVRInputManager.ICursorControllerSelectListener.class,
                                                    "onCursorControllerSelected",
                                                    device.getGvrCursorController(),
                                                    null);
        if (action == 0)
        {
            Log.d(TAG, "Replacing ioDevice:" + IoDeviceFactory.getXmlString(cursor.getIoDevice()) + " for cursor:" + cursor.getId() + " with ioDevice:" + IoDeviceFactory.getXmlString(device) + " with priority " + priority);
            removeCursorFromScene(cursor);
            cursor.setIoDevice(device);
            addCursorToScene(cursor);
            return 0;
        }
        else
        {
            Log.d(TAG, "Attaching ioDevice:" + IoDeviceFactory.getXmlString(device) + " with priority " + priority + " to cursor:" + cursor.getId());
            addNewCursor(cursor, device);
            return 1;
        }
    }

    public void attachDevice(final Cursor cursor)
    {
        if (getNumUnusedDevices() == 0)
        {
            getGVRContext().getInputManager().scanControllers();
        }
        else
        {
            synchronized (mCursors)
            {
                assignDeviceToCursor(cursor);
            }
        }
    }

    public Cursor findCursorForController(GVRCursorController controller)
    {
        synchronized (mCursors)
        {
            for (int i = 0; i < mCursors.size(); i++)
            {
                Cursor cursor = mCursors.get(i);
                if ((cursor == null) || (cursor.getIoDevice() == null))
                {
                    continue;
                }
                if (controller == cursor.getIoDevice().getGvrCursorController())
                {
                    return cursor;
                }
            }
        }
        return null;
    }

    public Cursor findCursorByName(String cursorName)
    {
        synchronized (mCursors)
        {
            for (Cursor c : mCursors)
            {
                if (c.getName().equals(cursorName))
                {
                    return c;
                }
            }
        }
        return null;
    }

    public Cursor findCursorByDevice(IoDevice device)
    {
        synchronized (mCursors)
        {
            for (Cursor c : mCursors)
            {
                if (c.isDeviceCompatible(device))
                {
                    return c;
                }
            }
        }
        return null;
    }

    public Cursor findCursorByType(CursorType type)
    {
        synchronized (mCursors)
        {
            for (Cursor c : mCursors)
            {
                if (c.getCursorType().equals(type))
                {
                    return c;
                }
            }
        }
        return null;
    }

    private void addNewCursor(Cursor cursor, IoDevice ioDevice) {
        cursor.setIoDevice(ioDevice);
        if (scene != null) {
            addCursorToScene(cursor);
        }
        context.getEventManager().sendEvent(this, ICursorActivationListener.class, "onActivated", cursor);
    }

    private GVRInputManager.ICursorControllerListener cursorIoDeviceListener = new GVRInputManager.ICursorControllerListener() {
        @Override
        public void onCursorControllerAdded(GVRCursorController controller) {
            IoDevice ioDevice = IoDeviceLoader.getIoDevice(controller);
            Log.d(TAG, "IoDevice added:" + ioDevice.getDeviceId());
            synchronized (mIODevices)
            {
                if (!mIODevices.contains(ioDevice))
                {
                    mIODevices.add(ioDevice);
                }
            }
            controller.setScene(CursorManager.this.scene);

            synchronized (mCursors)
            {
                for (Cursor c : mCursors)
                {
                    if (c.getIoDevice() == ioDevice)
                    {
                        return;
                    }
                }
            }
            assignCursorToDevice(ioDevice);
        }

        @Override
        public void onCursorControllerRemoved(GVRCursorController controller)
        {
            IoDevice removedIoDevice = IoDeviceLoader.getIoDevice(controller);
            Log.d(TAG, "IoDevice removed:" + removedIoDevice.getDeviceId());
            synchronized (mIODevices)
            {
                if (!mIODevices.remove(removedIoDevice))
                {
                    Log.d(TAG, "Did not find ioDevice in cursormanager list");
                    return;
                }
            }
            synchronized (mCursors)
            {
                for (Iterator<Cursor> cursorIterator = mCursors.iterator(); cursorIterator.hasNext(); )
                {
                    Cursor cursor = cursorIterator.next();

                    if (removedIoDevice.equals(cursor.getIoDevice()))
                    {
                        cursor.resetIoDevice(removedIoDevice);
                        if (scene != null)
                        {
                            removeCursorFromScene(cursor);
                        }
                        context.getEventManager().sendEvent(this, ICursorActivationListener.class, "onDeactivated", cursor);
                        break;
                    }
                }
            }
            if (getNumUnusedDevices() == 0)
            {
                getGVRContext().getInputManager().scanControllers();
            }
            else
            {
                assignIoDevicesToCursors(false);
            }
        }
    };


    protected ITouchEvents touchListener = new ITouchEvents()
    {
        protected Cursor findCursor(GVRPicker.GVRPickedObject hit)
        {
            GVRCursorController controller = hit.getPicker().getController();
            if (hit.collidableIndex >= 0)
            {
                GVRBoundsPicker picker = (GVRBoundsPicker) hit.getPicker();
                GVRSceneObject cursorObj = picker.getCollidable(hit.collidableIndex);
                if (cursorObj != null)
                {
                    return (Cursor) cursorObj.getComponent(Cursor.getComponentType());
                }
                return null;
            }
            else if (controller != null)
            {
                return findCursorForController(controller);
            }
            return null;
        }


        protected SelectableBehavior findSelector(GVRSceneObject obj)
        {
            MovableBehavior b1 = (MovableBehavior) obj.getComponent(MovableBehavior.getComponentType());
            if (b1 != null)
            {
                return b1;
            }
            return (SelectableBehavior) obj.getComponent(SelectableBehavior.getComponentType());
        }

        public void onEnter(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
        {
            Cursor cursor = findCursor(hit);
            SelectableBehavior selector = findSelector(obj);
            if ((cursor == null) || (selector == null))
            {
                return;
            }
            float cursorDistance = getDistance(cursor.getPositionX(),
                                               cursor.getPositionY(),
                                               cursor.getPositionZ());
            float soDistance = getDistance(obj);

            if (cursorDistance > soDistance)
            {
                selector.setWireFrame(cursor, hit);
            }
            else
            {
                selector.setIntersect(cursor, hit);
            }
            getGVRContext().getEventManager().sendEvent(obj,
                                                        ICursorEvents.class,
                                                        "onEnter", cursor, hit);
        }

        public void onExit(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
        {
            Cursor cursor = findCursor(hit);
            if (cursor == null)
            {
                return;
            }

            SelectableBehavior selector = findSelector(obj);
            if (selector!= null)
            {
                selector.setDefault(cursor, hit);
            }
            getGVRContext().getEventManager().sendEvent(obj,
                                                        ICursorEvents.class,
                                                        "onExit", cursor, hit);
        }

        public void onTouchStart(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
        {
            Cursor cursor = findCursor(hit);
            if (cursor == null)
            {
                return;
            }

            SelectableBehavior selector = findSelector(obj);
            if (selector != null)
            {
                selector.setButtonPress(cursor, hit);
            }
            getGVRContext().getEventManager().sendEvent(obj,
                                                        ICursorEvents.class,
                                                        "onTouchStart", cursor, hit);
        }

        public void onTouchEnd(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
        {
            Cursor cursor = findCursor(hit);

            if (cursor == null)
            {
                return;
            }
            SelectableBehavior selector = findSelector(obj);
            if (selector != null)
            {
                selector.setDefault(cursor, hit);
            }
            getGVRContext().getEventManager().sendEvent(obj,
                                                        ICursorEvents.class,
                                                        "onTouchEnd", cursor, hit);
        }

        public void onInside(GVRSceneObject obj, GVRPicker.GVRPickedObject hit)
        {
            Cursor cursor = findCursor(hit);

            if (cursor == null)
            {
                return;
            }
            if (hit.isTouched())
            {
                float depth = cursor.getCursorDepth();
                if (depth != cursor.getIoDevice().getGvrCursorController().getCursorDepth())
                {
                    getGVRContext().getEventManager().sendEvent(cursor.getOwnerObject(),
                                                                ICursorEvents.class,
                                                                "onCursorScale", cursor);
                }
                getGVRContext().getEventManager().sendEvent(obj,
                                                            ICursorEvents.class,
                                                            "onDrag", findCursor(hit), hit);
            }
            onEnter(obj, hit);
        }

        public void onMotionOutside(GVRPicker picker, MotionEvent event)
        {
            GVRCursorController controller = picker.getController();
            if (controller != null)
            {
                Cursor cursor = findCursorForController(controller);
                if (cursor != null)
                {
                    float depth = cursor.getCursorDepth();
                    if (depth != cursor.getIoDevice().getGvrCursorController().getCursorDepth())
                    {
                        getGVRContext().getEventManager().sendEvent(cursor.getOwnerObject(),
                                                                    ICursorEvents.class,
                                                                    "onCursorScale", cursor);
                    }
                }
            }
        }
    };

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
        activationListener = new ICursorActivationListener() {

            @Override
            public void onDeactivated(Cursor cursor) {
                Log.d(TAG, "Cursor DeActivated:" + cursor.getName());
                synchronized (selectableBehaviors)
                {
                    for (SelectableBehavior selectableBehavior : selectableBehaviors)
                    {
                        selectableBehavior.onCursorDeactivated(cursor);
                    }
                }
            }

            @Override
            public void onActivated(Cursor cursor) {
                GVRCursorController controller = cursor.getIoDevice().getGvrCursorController();
                Log.d(TAG, "On CursorActivated");
                synchronized (selectableBehaviors)
                {

                    for (SelectableBehavior selectableBehavior : selectableBehaviors)
                    {
                        selectableBehavior.onCursorActivated(cursor);
                    }
                }
            }
        };
        listeners.addListener(activationListener);

        // Collect all active cursors and register for all future active cursors.
        final List<Cursor> activeCursorsCopy = getActiveCursors();
        for (Cursor cursor : activeCursorsCopy) {
            activationListener.onActivated(cursor);
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