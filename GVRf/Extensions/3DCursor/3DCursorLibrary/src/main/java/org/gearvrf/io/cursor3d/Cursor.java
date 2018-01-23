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

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRCursorController;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.cursor3d.CursorAsset.Action;
import org.gearvrf.utility.Log;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
 * ICursorActivationListener} to know about {@link Cursor} objects added or removed from the
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
public abstract class Cursor extends GVRBehavior
{
    private static final String TAG = "Cursor";
    static private long TYPE_CURSOR = newComponentType(Cursor.class);
    protected static final float MAX_CURSOR_SCALE = 1000;
    protected ITouchEvents mTouchListener = null;
    protected IoDevice mIODevice;
    protected float mCursorDepth;
    protected final CursorType mCursorType;

    private IoDevice mSavedIODevice;
    private Position mStartPosition;
    private String mSavedThemeID;
    private List<PriorityIoDeviceTuple> mCompatibleDevices;
    private static int sUniqueCursorID = 0;
    private final int mCursorID;
    private CursorTheme mCursorTheme;
    private boolean mBusyLoading = false;

    private CursorAsset mCursorAsset;
    // Used to save a copy of the current cursor when busy loading is set.
    private CursorAsset mSavedCursorAsset;
    private CursorAudioManager mAudioManager;
    private CursorManager mCursorManager;
    private Vector3f mTempPosition, mDirection;

    enum Position {
        CENTER,
        LEFT,
        RIGHT,
        OTHER
    }

    Cursor(GVRContext context, CursorType type, CursorManager cursorManager) {
        super(cursorManager.getGVRContext());
        mType = getComponentType();
        mCursorType = type;
        mCursorID = sUniqueCursorID++;
        GVRSceneObject owner = new GVRSceneObject(context);
        mAudioManager = CursorAudioManager.getInstance(context.getContext());
        mCompatibleDevices = new ArrayList<PriorityIoDeviceTuple>();
        mTempPosition = new Vector3f();
        mDirection = new Vector3f();
        mCursorManager = cursorManager;
        owner.attachComponent(this);
    }

    /**
     * Returns a unique long value associated with the {@link Cursor} class. Each
     * subclass of  {@link GVRBehavior} needs a unique component type value. Use this value to
     * get the instance of {@link Cursor} attached to any {@link GVRSceneObject}
     * using {@link GVRSceneObject#getComponent(long)}
     *
     * @return the component type value.
     */
    public static long getComponentType() {
        return TYPE_CURSOR;
    }

    void setIoDevice(IoDevice newIoDevice)
    {
        mIODevice = newIoDevice;
        setupIoDevice(newIoDevice);
    }

    void resetIoDevice(IoDevice ioDevice)
    {
        if (mIODevice == ioDevice)
        {
            mIODevice = null;
        }
        ioDevice.setEnable(false);
        ioDevice.getGvrCursorController().removePickEventListener(getTouchListener());
        ioDevice.resetSceneObject();
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

        if (theme == mCursorTheme || theme == null) {
            //nothing to do, return
            return;
        }
        if (theme.getCursorType() != mCursorType) {
            throw new IllegalArgumentException("Cursor Theme does not match the cursor type");
        }

        if (mCursorAsset != null) {
            mCursorAsset.reset(this);
        }

        if (mCursorTheme != null) {
            mCursorTheme.unload(this);
        }

        mCursorTheme = theme;
        mAudioManager.loadTheme(mCursorTheme);
        theme.load(this);
        if (mCursorAsset != null) {
            mCursorAsset = mCursorTheme.getAsset(mCursorAsset.getAction());
            if (mCursorAsset == null) {
                mCursorAsset = mCursorTheme.getAsset(Action.DEFAULT);
            }
            mCursorAsset.set(this);
        }
    }

    /**
     * Use this call to return the currently set {@link CursorTheme}.
     *
     * @return the currently set {@link CursorTheme}
     */
    public CursorTheme getCursorTheme() {
        return mCursorTheme;
    }


    // Means that the ioDevice is active
    boolean isActive() {
        return isEnabled() && (mIODevice != null) && mIODevice.isEnabled();
    }

    /**
     * Get the type of this {@link Cursor} object. Look at
     * {@link CursorType} to know more about the various types of cursor objects created by the
     * {@link CursorManager}.
     *
     * @return the {@link CursorType} of this {@link Cursor} object.
     */
    public CursorType getCursorType() {
        return mCursorType;
    }

    /**
     * Set a new Cursor position if active and enabled.
     *
     * @param x x value of the position
     * @param y y value of the position
     * @param z z value of the position
     */
    public void setPosition(float x, float y, float z) {
        if (isActive()) {
            mIODevice.setPosition(x, y, z);
        }
    }

    /**
     * Get the current absolute x position of this {@link Cursor}.
     *
     * @return the current x position of the {@link Cursor}
     */
    public float getPositionX()
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            return owner.getTransform().getPositionX();
        }
        return 0;
    }

    /**
     * Get the current absolute y position of this {@link Cursor}.
     *
     * @return the current y position of the {@link Cursor}
     */
    public float getPositionY()
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            return owner.getTransform().getPositionY();
        }
        return 0;
    }

    /**
     * Get the current absolute z position of this {@link Cursor}.
     *
     * @return the current z position of the {@link Cursor}
     */
    public float getPositionZ()
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            return owner.getTransform().getPositionZ();
        }
        return 0;
    }

    /**
     * Get the current 'w' component of this {@link Cursor}'s rotation quaternion.
     *
     * @return the current 'w' component of this {@link Cursor}'s rotation quaternion.
     */
    public float getRotationW()
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            return owner.getTransform().getRotationW();
        }
        return 0;
    }

    /**
     * Get the current 'x' component of this {@link Cursor}'s rotation quaternion.
     *
     * @return the current 'x' component of this {@link Cursor}'s rotation quaternion.
     */
    public float getRotationX()
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            return owner.getTransform().getRotationX();
        }
        return 0;
    }

    /**
     * Get the current 'y' component of this {@link Cursor}'s rotation quaternion.
     *
     * @return the current 'y' component of this {@link Cursor}'s rotation quaternion.
     */
    public float getRotationY()
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            return owner.getTransform().getRotationY();
        }
        return 0;
    }

    /**
     * Get the current 'z' component of this {@link Cursor}'s rotation quaternion.
     *
     * @return the current 'z' component of this {@link Cursor}'s rotation quaternion.
     */
    public float getRotationZ()
    {
        GVRSceneObject owner = getOwnerObject();

        if (owner != null)
        {
            return owner.getTransform().getRotationZ();
        }
        return 0;
    }

    public void addChildObject(GVRSceneObject child) {
        getOwnerObject().addChildObject(child);
    }

    public void removeChildObject(GVRSceneObject child) {
        getOwnerObject().removeChildObject(child);
    }

    /**
     * The method will force a process cycle that may result in an
     * {@link ICursorEvents} being generated if there is a significant event
     * that affects a {@link Cursor}. In most cases when a new position
     * or key event is received, the {@link Cursor} internally
     * invalidates its own data. However there may be situations where the
     * cursor data remains the same while the scene graph is changed. This
     * {@link #invalidate()} call can help force the {@link Cursor}
     * to run a new process loop on its existing information against the changed
     * scene graph to generate possible {@link ICursorEvents}.
     */
    public void invalidate() {
        //generate a new event
        if (mIODevice != null) {
            mIODevice.invalidate();
        }
    }

    public float getCursorDepth() {
        return mCursorDepth;
    }

    void setCursorDepth(float depth) {
        mCursorDepth = depth;
        if (mIODevice != null) {
            mIODevice.getGvrCursorController().setCursorDepth(mCursorDepth);
            mIODevice.setPosition(0.0f, 0.0f, -mCursorDepth);
        }
    }

    /**
     * Perform all Cursor cleanup here.
     */
    void close() {
        mIODevice = null;
        GVRSceneObject owner = getOwnerObject();
        if (owner.getParent() != null) {
            owner.getParent().removeChildObject(owner);
        }
    }

    /* Set the asset only if it is not already set,
     * return true if the asset has been changed, else return false.
     */
    void checkAndSetAsset(Action action) {
        // check the theme if we have a asset
        CursorAsset asset = mCursorTheme.getAsset(action);

        if (asset == null) {
            return;
        }

        // do not set if the app is busy loading
        if (mCursorAsset == null || !mCursorAsset.getAction().equals(action)) {
            if (isBusyLoading()) {
                //save the new state for restore when busy loading is done
                mSavedCursorAsset = asset;
            } else {
                setAsset(asset);
            }
        }
    }

    private void setAsset(final CursorAsset asset) {
        if (asset == null) {
            return;
        }

        if (mCursorAsset != null) {
            mCursorAsset.reset(this);
        }
        // load new asset
        mCursorAsset = asset;
        mCursorAsset.set(this);
    }

    void setSavedIoDevice(IoDevice savedIoDevice) {
        mSavedIODevice = savedIoDevice;
    }

    IoDevice getSavedIoDevice() {
        return mSavedIODevice;
    }

    void clearSavedIoDevice() {
        mSavedIODevice = null;
    }

    void setName(String name)
    {
        getOwnerObject().setName(name);
    }

    /**
     * This method returns the name of this {@link Cursor} object.
     *
     * Note that the name is not necessarily unique.
     *
     * @return a String representing the Cursor
     */
    public String getName() {
        return getOwnerObject().getName();
    }

    void setStartPosition(Position position) {
        mStartPosition = position;
    }

    Position getStartPosition() {
        return mStartPosition;
    }

    void setSavedThemeId(String savedThemeId) {
        mSavedThemeID = savedThemeId;
    }

    String clearSavedThemeId() {
        String themeId = mSavedThemeID;
        mSavedThemeID = null;
        return themeId;
    }

    /**
     * Returns the {@link IoDevice} currently attached to the {@link Cursor}.
     *
     * @return the {@link IoDevice} attached. <code>null</code> is no device is attached.
     */
    public IoDevice getIoDevice() {
        return mIODevice;
    }

    /**
     * Returns the priority for the {@link IoDevice} currently attached to the {@link Cursor}.
     *
     * @return -1 if no device attached, otherwise returns device priority
     */
    int getCurrentDevicePriority() {
        if (mIODevice == null)
        {
            return -1;
        }
        for (PriorityIoDeviceTuple tuple : mCompatibleDevices)
        {
            if (tuple.getIoDevice().equals(mIODevice))
            {
                return tuple.getPriority();
            }
        }
        return -1;
    }

    /**
     * Returns the priority for the given {@link IoDevice} for this {@link Cursor}.
     *
     * @return -1 if device not compatible with this cursor, otherwise returns device priority
     */
    int getDevicePriority(IoDevice device)
    {
        for (PriorityIoDeviceTuple tuple : mCompatibleDevices)
        {
            if (tuple.getIoDevice().equals(device))
            {
                return tuple.getPriority();
            }
        }
        return -1;
    }

    /**
     * Returns a list of {@link IoDevice}s compatible with the {@link Cursor}.
     * The resulting list is sorted in priority order with highest priority
     * devices first.
     * @return list of compatible {@link IoDevice}s
     */
    public List<IoDevice> getCompatibleIoDevices() {
        List<IoDevice> ioDevices = new LinkedList<IoDevice>();
        for (PriorityIoDeviceTuple tuple : mCompatibleDevices) {
            ioDevices.add(tuple.getIoDevice());
        }
        return ioDevices;
    }

    List<PriorityIoDeviceTuple> getIoDevices() {
        return mCompatibleDevices;
    }

    /**
     * Determines whether the input {@link IoDevice} is compatible with the {@link Cursor}.
     * @return true if device is compatible, else false
     */
    public boolean isDeviceCompatible(final IoDevice device)
    {
        List<IoDevice> ioDevices = new LinkedList<IoDevice>();
        for (PriorityIoDeviceTuple tuple : mCompatibleDevices)
        {
            if (tuple.getIoDevice().equals(device))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * This method returns an integer value that can be used to
     * uniquely identify this {@link Cursor}.
     *
     * @return an integer representing the Cursor
     */
    public int getId() {
        return mCursorID;
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
        if (loading && (mBusyLoading == false)) {
            // save the state
            mSavedCursorAsset = mCursorAsset;
            setAsset(mCursorTheme.getAsset(Action.LOADING));
            mBusyLoading = true;
        } else if ((loading == false) && mBusyLoading && mSavedCursorAsset != null) {
            // restore saved state
            boolean soundEnabled = mSavedCursorAsset.isSoundEnabled();
            // we don't want a sound on restore
            mSavedCursorAsset.setSoundEnabled(false);
            setAsset(mSavedCursorAsset);
            mSavedCursorAsset.setSoundEnabled(soundEnabled);
            mSavedCursorAsset = null;
            mBusyLoading = false;
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
        return mBusyLoading;
    }


    public ITouchEvents getTouchListener() { return mTouchListener; }

    public void activate()
    {
        Log.d(TAG, Integer.toHexString(hashCode()) + " enabled");
        if (isEnabled() && (getIoDevice() == null))
        {
            mCursorManager.attachDevice(this);
        }
    }

    public void deactivate()
    {
        Log.d(TAG, Integer.toHexString(hashCode()) + " disabled");
        markIoDeviceUnused();
        mCursorManager.markCursorUnused(this);
        close();
    }

    void setupIoDevice(IoDevice ioDevice) {
        // should have a normal asset
        setAsset(mCursorTheme.getAsset(Action.DEFAULT));
        ioDevice.setEnable(true);
        ioDevice.setPosition(0.0f, 0.0f, -mCursorDepth);
        ioDevice.getGvrCursorController().addPickEventListener(getTouchListener());
    }

    void transferIoDevice(Cursor oldCursor) {
        IoDevice targetIoDevice = oldCursor.getIoDevice();
        mCursorManager.removeCursorFromScene(oldCursor);
        setIoDevice(targetIoDevice);
        mCursorManager.addCursorToScene(this);
    }


    void markIoDeviceUnused()
    {
        if (mIODevice != null)
        {
            Log.d(TAG, "Marking ioDevice:" + mIODevice.getName() + " unused");
            GVRCursorController controller = mIODevice.getGvrCursorController();

            controller.setCursor(null);
            controller.setEnable(false);
        }
    }

    IoDevice getIoDeviceForPriority(int priorityLevel) {
        if (priorityLevel < mCompatibleDevices.size()) {
            return mCompatibleDevices.get(priorityLevel).getIoDevice();
        } else {
            return null;
        }
    }

    /**
     * Get a list of currently available {@link IoDevice}s to use with the {@link Cursor}. The
     * {@link Cursor} defines a list of compatible {@link IoDevice}s in the settings.xml. This
     * method returns a subset from the compatible list of {@link IoDevice}s that are available to
     * the framework and not being used by any other cursor. This list is sorted in order
     * of priority with highest priority devices first.
     *
     * @return a list of available {@link IoDevice}.
     */
    public List<IoDevice> getAvailableIoDevices() {
        List<IoDevice> returnList = new ArrayList<IoDevice>();
        for (PriorityIoDeviceTuple compatibleIoDeviceTuple : mCompatibleDevices) {
            IoDevice compatibleIoDevice = compatibleIoDeviceTuple.getIoDevice();
            if (compatibleIoDevice.equals(mIODevice)) {
                returnList.add(mIODevice);
            } else {
                IoDevice ioDevice = mCursorManager.getIoDevice(compatibleIoDevice);
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
        if (!isEnabled()) {
            throw new IllegalStateException("Cursor not enabled");
        }
        IoDevice oldDevice = getIoDevice();
        if (oldDevice != null && oldDevice.equals(ioDevice)) {
            Log.d(TAG, "Current and desired Io device are same");
            return;
        }

        if (!isIoDeviceCompatible(ioDevice)) {
            throw new IllegalArgumentException("IO device not compatible");
        }
        IoDevice availableIoDevice = mCursorManager.getAvailableIoDevice(ioDevice);
        if (availableIoDevice == null) {
            throw new IOException("IO device cannot be attached");
        }

        Log.d(TAG, "Attaching ioDevice:" + availableIoDevice.getDeviceId() + " to cursor:"
                   + mCursorID);

        mCursorManager.removeCursorFromScene(this);
        setIoDevice(availableIoDevice);
        mCursorManager.addCursorToScene(this);
        if (oldDevice != null)
        {
            resetIoDevice(oldDevice);
        }
    }

    private boolean isIoDeviceCompatible(IoDevice ioDevice) {
        for (PriorityIoDeviceTuple compatibleIoDevice : mCompatibleDevices) {
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
    protected void lookAt() {mTempPosition.set(getPositionX(), getPositionY(), getPositionZ());
        mTempPosition.negate(mDirection);

        Vector3f up;
        mDirection.normalize();

        if (Math.abs(mDirection.x) < 0.00001
                && Math.abs(mDirection.z) < 0.00001) {
            if (mDirection.y > 0) {
                up = new Vector3f(0.0f, 0.0f, -1.0f); // if direction points in +y
            } else {
                up = new Vector3f(0.0f, 0.0f, 1.0f); // if direction points in -y
            }
        } else {
            up = new Vector3f(0.0f, 1.0f, 0.0f); // y-axis is the general up
        }

        up.normalize();
        Vector3f right = new Vector3f();
        up.cross(mDirection, right);
        right.normalize();
        mDirection.cross(right, up);
        up.normalize();

        float[] matrix = new float[]{right.x, right.y, right.z, 0.0f, up.x, up.y,
                up.z, 0.0f, mDirection.x, mDirection.y, mDirection.z, 0.0f,
                0.0f, 0.0f, 0.0f, 0.0f};
        getOwnerObject().getTransform().setModelMatrix(matrix);
    }

}
