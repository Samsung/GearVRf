package org.gearvrf.vuforiasample;

import java.util.ArrayList;

import android.os.AsyncTask;
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
import com.qualcomm.vuforia.Vuforia.UpdateCallbackInterface;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationException;
import org.gearvrf.GVRActivity;

public class VuforiaSampleActivity extends GVRActivity implements
        UpdateCallbackInterface {

    private static final String TAG = "gvr-vuforia";
    
    public static boolean isVuforiaActive = false;

    VuforiaSampleScript script = new VuforiaSampleScript();
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setScript(script, "gvr_note4.xml");

        mDatasetStrings.add("StonesAndChips.xml");

        mVuforiaFlags = Vuforia.GL_20;
        SampleApplicationException vuforiaException = null;

        // Initialize Vuforia SDK asynchronously to avoid blocking the
        // main (UI) thread.
        //
        // NOTE: This task instance must be created and invoked on the
        // UI thread and it can be executed only once!
        if (mInitVuforiaTask != null) {
            String logMessage = "Cannot initialize SDK twice";
            vuforiaException = new SampleApplicationException(
                    SampleApplicationException.VUFORIA_ALREADY_INITIALIZATED,
                    logMessage);
            Log.e(TAG, logMessage);
        }

        if (vuforiaException == null) {
            try {
                mInitVuforiaTask = new InitVuforiaTask();
                mInitVuforiaTask.execute();
            } catch (Exception e) {
                String logMessage = "Initializing Vuforia SDK failed";
                vuforiaException = new SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage);
                Log.e(TAG, logMessage);
            }
        }

        if (vuforiaException != null)
            onInitARDone(vuforiaException);
    }

    // An async task to initialize Vuforia asynchronously.
    private class InitVuforiaTask extends AsyncTask<Void, Integer, Boolean> {
        // Initialize with invalid value:
        private int mProgressValue = -1;

        protected Boolean doInBackground(Void... params) {
            // Prevent the onDestroy() method to overlap with initialization:
            synchronized (mShutdownLock) {
                Vuforia.setInitParameters(VuforiaSampleActivity.this, mVuforiaFlags);

                do {
                    // Vuforia.init() blocks until an initialization step is
                    // complete, then it proceeds to the next step and reports
                    // progress in percents (0 ... 100%).
                    // If Vuforia.init() returns -1, it indicates an error.
                    // Initialization is done when progress has reached 100%.
                    mProgressValue = Vuforia.init();
                    Log.d(TAG, String.format("Progress: %d", mProgressValue));

                    // Publish the progress value:
                    publishProgress(mProgressValue);

                    // We check whether the task has been canceled in the
                    // meantime (by calling AsyncTask.cancel(true)).
                    // and bail out if it has, thus stopping this thread.
                    // This is necessary as the AsyncTask will run to completion
                    // regardless of the status of the component that
                    // started is.
                } while (!isCancelled() && mProgressValue >= 0
                        && mProgressValue < 100);

                return (mProgressValue > 0);
            }
        }

        protected void onProgressUpdate(Integer... values) {
            // Do something with the progress value "values[0]", e.g. update
            // splash screen, progress bar, etc.
        }

        protected void onPostExecute(Boolean result) {
            // Done initializing Vuforia, proceed to next application
            // initialization status:

            SampleApplicationException vuforiaException = null;

            if (result) {
                Log.d(TAG, "InitVuforiaTask.onPostExecute: Vuforia "
                        + "initialization successful");

                boolean initTrackersResult;
                initTrackersResult = doInitTrackers();

                if (initTrackersResult) {
                    try {
                        mLoadTrackerTask = new LoadTrackerTask();
                        mLoadTrackerTask.execute();
                    } catch (Exception e) {
                        String logMessage = "Loading tracking data set failed";
                        vuforiaException = new SampleApplicationException(
                                SampleApplicationException.LOADING_TRACKERS_FAILURE,
                                logMessage);
                        Log.e(TAG, logMessage);
                        onInitARDone(vuforiaException);
                    }

                } else {
                    vuforiaException = new SampleApplicationException(
                            SampleApplicationException.TRACKERS_INITIALIZATION_FAILURE,
                            "Failed to initialize trackers");
                    onInitARDone(vuforiaException);
                }
            } else {
                String logMessage;

                // NOTE: Check if initialization failed because the device is
                // not supported. At this point the user should be informed
                // with a message.
                if (mProgressValue == Vuforia.INIT_DEVICE_NOT_SUPPORTED) {
                    logMessage = "Failed to initialize Vuforia because this "
                            + "device is not supported.";
                } else {
                    logMessage = "Failed to initialize Vuforia.";
                }

                // Log error:
                Log.e(TAG, "InitVuforiaTask.onPostExecute: " + logMessage
                        + " Exiting.");

                // Send Vuforia Exception to the application and call initDone
                // to stop initialization process
                vuforiaException = new SampleApplicationException(
                        SampleApplicationException.INITIALIZATION_FAILURE,
                        logMessage);
                onInitARDone(vuforiaException);
            }
        }
    }

    // An async task to load the tracker data asynchronously.
    private class LoadTrackerTask extends AsyncTask<Void, Integer, Boolean> {
        protected Boolean doInBackground(Void... params) {
            // Prevent the onDestroy() method to overlap:
            synchronized (mShutdownLock) {
                // Load the tracker data set:
                return doLoadTrackersData();
            }
        }

        protected void onPostExecute(Boolean result) {

            SampleApplicationException vuforiaException = null;

            Log.d(TAG, "LoadTrackerTask.onPostExecute: execution "
                    + (result ? "successful" : "failed"));

            if (!result) {
                String logMessage = "Failed to load tracker data.";
                // Error loading dataset
                Log.e(TAG, logMessage);
                vuforiaException = new SampleApplicationException(
                        SampleApplicationException.LOADING_TRACKERS_FAILURE,
                        logMessage);
            } else {
                // Hint to the virtual machine that it would be a good time to
                // run the garbage collector:
                //
                // NOTE: This is only a hint. There is no guarantee that the
                // garbage collector will actually be run.
                System.gc();

                Vuforia.registerCallback(VuforiaSampleActivity.this);
            }

            // Done loading the tracker, update application status, send the
            // exception to check errors
            onInitARDone(vuforiaException);
        }
    }

    // To be called to initialize the trackers
    boolean doInitTrackers() {
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
            Log.i(TAG, "Tracker successfully initialized");
        }
        return result;
    }

    // To be called to load the trackers' data
    boolean doLoadTrackersData() {
        TrackerManager tManager = TrackerManager.getInstance();
        ImageTracker imageTracker = (ImageTracker) tManager
                .getTracker(ImageTracker.getClassType());
        if (imageTracker == null)
            return false;

        if (mCurrentDataset == null)
            mCurrentDataset = imageTracker.createDataSet();

        if (mCurrentDataset == null)
            return false;

        if (!mCurrentDataset.load(
                mDatasetStrings.get(mCurrentDatasetSelectionIndex),
                STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;

        if (!imageTracker.activateDataSet(mCurrentDataset))
            return false;

        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++) {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if (isExtendedTrackingActive()) {
                trackable.startExtendedTracking();
            }

            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(TAG, "UserData:Set the following user data "
                    + (String) trackable.getUserData());
        }

        return true;
    }

    // To be called to start tracking with the initialized trackers and their
    // loaded data
    boolean doStartTrackers() {
        // Indicate if the trackers were started correctly
        boolean result = true;

        Tracker imageTracker = TrackerManager.getInstance().getTracker(
                ImageTracker.getClassType());
        if (imageTracker != null)
            imageTracker.start();

        return result;
    }

    // To be called to stop the trackers
    boolean doStopTrackers() {
        return true;
    }

    // To be called to destroy the trackers' data
    boolean doUnloadTrackersData() {
        return true;
    }

    // To be called to deinitialize the trackers
    boolean doDeinitTrackers() {
        return true;
    }

    // This callback is called after the Vuforia initialization is complete,
    // the trackers are initialized, their data loaded and
    // tracking is ready to start
    void onInitARDone(SampleApplicationException exception) {
        if (exception == null) {
            isVuforiaActive = true;
            
            String error;
            int camera = CameraDevice.CAMERA.CAMERA_DEFAULT;
            if (!CameraDevice.getInstance().init(camera)) {
                error = "Unable to open camera device: "
                        + CameraDevice.CAMERA.CAMERA_DEFAULT;
                Log.e(TAG, error);
            } else {
                Log.d(TAG, "Vuforia camera initialization success.");
            }

            if (!CameraDevice.getInstance().selectVideoMode(
                    CameraDevice.MODE.MODE_DEFAULT)) {
                error = "Unable to set video mode";
                Log.e(TAG, error);
            } else {
                Log.d(TAG, "Vuforia camera setting video mode success.");
            }

            if (!CameraDevice.getInstance().start()) {
                error = "Unable to start camera device: " + camera;
                Log.e(TAG, error);
            } else {
                Log.d(TAG, "Vuforia camera starting success.");
            }

            // Hint to Vuforia for number of image targets
            Vuforia.setHint(HINT.HINT_MAX_SIMULTANEOUS_IMAGE_TARGETS,
                    MAX_SIMULTANEOUS_IMAGE_TARGETS);

            if (!doStartTrackers()) {
                error = "Unable to start tracker.";
                Log.e(TAG, error);
            } else {
                Log.d(TAG, "Vuforia start tracker success.");
            }
        }
    }

    // This callback is called every cycle
    void onQCARUpdate(State state) {

    }

    @Override
    public void QCAR_onUpdate(State s) {
        onQCARUpdate(s);
    }

    boolean isExtendedTrackingActive() {
        return mExtendedTracking;
    }

    // Vuforia initialization flags:
    private int mVuforiaFlags = 0;

    // The async tasks to initialize the Vuforia SDK:
    private InitVuforiaTask mInitVuforiaTask;
    private LoadTrackerTask mLoadTrackerTask;

    // An object used for synchronizing Vuforia initialization, dataset loading
    // and the Android onDestroy() life cycle event. If the application is
    // destroyed while a data set is still being loaded, then we wait for the
    // loading operation to finish before shutting down Vuforia:
    private Object mShutdownLock = new Object();

    private DataSet mCurrentDataset;
    private int mCurrentDatasetSelectionIndex = 0;
    private ArrayList<String> mDatasetStrings = new ArrayList<String>();

    private boolean mExtendedTracking = false;

    private final int MAX_SIMULTANEOUS_IMAGE_TARGETS = 2;
}
