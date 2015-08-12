package org.gearvrf.vuforiasample;

import java.util.ArrayList;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.DataSet;
import com.qualcomm.vuforia.HINT;
import com.qualcomm.vuforia.ImageTracker;
import com.qualcomm.vuforia.STORAGE_TYPE;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.Tracker;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.misc.VuforiaApplicationControl;
import com.qualcomm.vuforia.misc.VuforiaApplicationException;
import com.qualcomm.vuforia.misc.VuforiaApplicationSession;

import org.gearvrf.GVRActivity;

public class VuforiaSampleActivity extends GVRActivity implements
        VuforiaApplicationControl {

    private static final String TAG = "gvr-vuforia";
    private VuforiaSampleScript script;

    private VuforiaApplicationSession vuforiaAppSession;

    private static int VUFORIA_INACTIVE = 0;
    private static int VUFORIA_ACTIVE_INITIALIZED = 1;
    private static int VUFORIA_ACTIVE_PAUSED = 2;
    private static int VUFORIA_ACTIVE_RESUMED = 3;
    private static int VUFORIA_INITIALIZING = 4;

    private static int vuforiaState = VUFORIA_INACTIVE;

    private final int MAX_SIMULTANEOUS_IMAGE_TARGETS = 2;

    private DataSet currentDataset;
    private int currentDatasetSelectionIndex = 0;
    private ArrayList<String> datasetStrings = new ArrayList<String>();

    private boolean extendedTracking = false;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.d(TAG, "onCreate");
        
        script = new VuforiaSampleScript();
        setScript(script, "gvr_note4.xml");

        datasetStrings.add("StonesAndChips.xml");

        vuforiaState = VUFORIA_INITIALIZING;
        vuforiaAppSession = new VuforiaApplicationSession(this);
        vuforiaAppSession.initAR(this);
    }

    public static boolean isVuforiaActive() {
        if (vuforiaState == VUFORIA_ACTIVE_INITIALIZED
                || vuforiaState == VUFORIA_ACTIVE_RESUMED) {
            return true;
        } else {
            return false;
        }
    }

    protected void onResume() {
        super.onResume();

        if (vuforiaState == VUFORIA_ACTIVE_PAUSED) {
            Log.d(TAG, "onResume");

            try {
                vuforiaAppSession.resumeAR();
                vuforiaState = VUFORIA_ACTIVE_RESUMED;
            } catch (VuforiaApplicationException e) {
                Log.e(TAG, e.getString());
            }
        }
    }

    protected void onPause() {
        super.onPause();
        if (vuforiaState == VUFORIA_ACTIVE_INITIALIZED
                || vuforiaState == VUFORIA_ACTIVE_RESUMED) {
            Log.d(TAG, "onPause");

            try {
                vuforiaAppSession.pauseAR();
                vuforiaState = VUFORIA_ACTIVE_PAUSED;
            } catch (VuforiaApplicationException e) {
                Log.e(TAG, e.getString());
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();

        script = null;

        if (vuforiaState != VUFORIA_INACTIVE) {
            Log.d(TAG, "onDestroy");

            try {
                vuforiaAppSession.stopAR();
                vuforiaState = VUFORIA_INACTIVE;
            } catch (VuforiaApplicationException e) {
                Log.e(TAG, e.getString());
            }
        }
    }

    public void onConfigurationChanged(Configuration config) {
        Log.d(TAG, "onConfigurationChanged");
        vuforiaAppSession.onConfigurationChanged();
    }

    // Initializes AR application components.
    private void initApplicationAR() {
        Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS,
                MAX_SIMULTANEOUS_IMAGE_TARGETS);
        vuforiaState = VUFORIA_ACTIVE_INITIALIZED;
    }

    public void onInitARDone(VuforiaApplicationException exception) {

        if (exception == null) {
            initApplicationAR();

            try {
                vuforiaAppSession.startAR(CameraDevice.CAMERA.CAMERA_DEFAULT);
            } catch (VuforiaApplicationException e) {
                Log.e(TAG, e.getString());
            }

            boolean result = CameraDevice.getInstance().setFocusMode(
                    CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);

            if (result == false) {
                Log.e(TAG, "Unable to enable continuous autofocus");
            }
        } else {
            Log.e(TAG, exception.getString());
        }
    }

    // To be called to initialize the trackers
    public boolean doInitTrackers() {
        // Indicate if the trackers were initialized correctly
        boolean result = true;

        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;

        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ImageTracker.getClassType());
        if (tracker == null) {
            Log.e(TAG,
                    "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else {
            Log.i(TAG, "Tracker successfully initialized ");
        }
        return result;
    }

    // To be called to load the trackers' data
    public boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
                .getTracker(ImageTracker.getClassType());

        if (imageTracker == null) {
            return false;
        }

        if (currentDataset == null) {
            currentDataset = imageTracker.createDataSet();
        }

        if (currentDataset == null) {
            return false;
        }

        if (!currentDataset.load(
                datasetStrings.get(currentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE)) {
            return false;
        }

        if (!imageTracker.activateDataSet(currentDataset)) {
            return false;
        }

        int numTrackables = currentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = currentDataset.getTrackable(count);

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(TAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }

    // To be called to start tracking with the initialized trackers and their
    // loaded data
    public boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ImageTracker.getClassType());
        if (imageTracker != null) {
            imageTracker.start();
        }

        return result;
    }

    // To be called to stop the trackers
    public boolean doStopTrackers() {
        // Indicate if the trackers were stopped correctly
        boolean result = true;

        if (isVuforiaActive()) {
            Tracker imageTracker = TrackerManager.getInstance().getTracker(
                    ImageTracker.getClassType());
            if (imageTracker != null)
                imageTracker.stop();
        }
        
        return result;
    }

    // To be called to destroy the trackers' data
    public boolean doUnloadTrackersData() {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;

        if (isVuforiaActive()) {
            TrackerManager tManager = TrackerManager.getInstance();
            ImageTracker imageTracker = (ImageTracker) tManager
                    .getTracker(ImageTracker.getClassType());
            if (imageTracker == null)
                return false;

            if (currentDataset != null && currentDataset.isActive()) {
                if (imageTracker.getActiveDataSet().equals(currentDataset)
                        && !imageTracker.deactivateDataSet(currentDataset)) {
                    result = false;
                } else if (!imageTracker.destroyDataSet(currentDataset)) {
                    result = false;
                }

                currentDataset = null;
            }
        }
        return result;
    }

    // To be called to deinitialize the trackers
    public boolean doDeinitTrackers() {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;

        if (isVuforiaActive()) {
            TrackerManager tManager = TrackerManager.getInstance();
            tManager.deinitTracker(ImageTracker.getClassType());
        }

        return result;
    }

    boolean isExtendedTrackingActive() {
        return extendedTracking;
    }

    @Override
    public void onQCARUpdate(State s) {
        if (script.isInit()) {
            script.updateObjectPose(s);
        }
    }
}
