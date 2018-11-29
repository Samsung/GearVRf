package org.gearvrf.animation;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;

import org.gearvrf.GVRComponent;
import org.gearvrf.GVRContext;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVREventReceiver;
import org.gearvrf.IEventReceiver;
import org.gearvrf.IEvents;
import org.gearvrf.scene_objects.GVRCameraSceneObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Handles gathering camera images for a body tracker.
 * <p>
 * A body tracker inspects a series of camera images
 * and updates the current pose of a target skeleton
 * based on the input images.
 * <p>
 * This class provides a framework for body tracking.
 * It does not actually implement tracking of any kind.
 * It exists as an interface to body tracker subclasses.
 * It is responsible for opening the camera and
 * gather camera images for body tracking.
 * <p>
 * A body tracker is also a {@link GVREventReceiver} and
 * can emit {@TrackerEvents} to its listeners indicating
 * when tracking starts, stops and when an image produces
 * tracking data.
 * <p>
 * Subclasses must provide implementations for several
 * functions:
 * <ul>
 * <li>trackFromImage - produces body tracking data from a camera image</li>
 * <li>updateSkeleton - updates the target skeleton from body tracking data</li>
 * <li>isRunning      - true if body tracker is capturing image data</li>
 * <li>isTracking     - true if body tracker is updating the skeleton</li>
 * </ul>
 */
public abstract class GVRBodyTracker extends GVRComponent implements IEventReceiver
{
    public interface TrackerEvents extends IEvents
    {
        public void onTrackStart(GVRBodyTracker tracker);
        public void onTrackEnd(GVRBodyTracker tracker);
        public void onTrackUpdate(GVRBodyTracker tracker);
    };

    protected GVRPose mDestPose;
    protected final int mWidth = 1280;
    protected final int mHeight = 960;
    protected GVRSkeleton mTargetSkeleton;
    protected Camera mCamera = null;
    protected GVRCameraSceneObject mCameraOwner;
    protected byte[] mImageData = null;
    private boolean mTryOpenCamera = true;
    protected GVREventReceiver mListeners;

    /**
     * Constructs a body tracker which updates a given skeleton.
     * This constructor is intended for subclasses only.
     * @param skel          target {@GVRSkeleton} to update
     * @param nativePtr     native pointer to C++ implementation for body tracking
     */
    protected GVRBodyTracker(GVRSkeleton skel, long nativePtr)
    {
        super(skel.getGVRContext(), nativePtr);
        mType = getComponentType();
        mTargetSkeleton = skel;
        mDestPose = new GVRPose(mTargetSkeleton);
        mListeners = new GVREventReceiver(this);
    }

    /**
     * Constructs a body tracker without a skeleton.
     * This constructor is intended for subclasses only.
     * @param context       {@GVRContext} which owns this tracker
     * @param nativePtr     native pointer to C++ implementation for body tracking
     */
    protected GVRBodyTracker(GVRContext context, long nativePtr)
    {
        super(context, nativePtr);
        mType = getComponentType();
        mTargetSkeleton = null;
        mDestPose = null;
        mListeners = new GVREventReceiver(this);
    }

    static public long getComponentType() { return NativeBodyTracker.getComponentType(); }

    /**
     * Get the {@GVREventReceiver} interface to add or remove
     * listeners for {@TrackerEvents}
     * @return {@GVREventReceiver} which generates tracking events.
     */
    public GVREventReceiver getEventReceiver() { return mListeners; }

    /**
     * Get the {@GVRSkeleton} being updated by this body tracker
     * @return skeleton or null if none
     */
    public GVRSkeleton getSkeleton()
    {
        return mTargetSkeleton;
    }

    /**
     * Get the {@GVRCameraSceneObject} that owns the camera
     * being used for tracking.
     * <p>
     * Adding this object to your scene will produce a display
     * of the images being tracked.
     * @return scene object to display camera
     */
    public GVRCameraSceneObject getCameraDisplay()
    {
        return mCameraOwner;
    }

    /**
     * Start body tracking from camera.
     * <p>
     * This function opens the camera and begins capturing camera previews.
     * It will throw exceptions if the camera cannot be accessed.
     * @throws CameraAccessException
     * @throws IOException
     * @throws IllegalAccessException
     */
    public void start() throws CameraAccessException, IOException, IllegalAccessException
    {
        if (mTryOpenCamera)
        {
            mCamera = openCamera();
            mCameraOwner = new GVRCameraSceneObject(getGVRContext(), mWidth / 2, mHeight / 2, mCamera);
            mCameraOwner.getTransform().setPositionZ(-200.0f);
            mTryOpenCamera = false;
        }
        if (isEnabled() && (mCamera != null))
        {
            mCamera.startPreview();
            if (!onStart())
            {
                throw new IllegalAccessException("Cannot access body tracker");
            }
        }
    }

    public void onEnable()
    {
        super.onEnable();
        if (!isRunning() && (mCamera != null))
        {
            mCamera.startPreview();
            onStart();
        }
    }

    public void onDisable()
    {
        super.onDisable();
        if (isRunning() && (mCamera != null))
        {
            mCamera.stopPreview();
            onStop();
        }
    }

    /**
     * Stop body tracking.
     * <p>
     * Stop capturing images and tracking from them.
     */
    public void stop()
    {
        if (mCamera != null)
        {
            mCamera.startPreview();
            mCamera.release();
            mCamera = null;
            mTryOpenCamera = false;
            onStop();
        }
    }

    /**
     * True if the body tracker is capturing images
     * @return true if camera preview images are being captured
     */
    public abstract boolean isRunning();

    /**
     * True if the body tracker is tracking from images
     * <p>
     * The body tracker may run a separate thread which automatically
     * captures images and tracks from them or it may rely on
     * the application to call {#track()} every time it wants
     * to track from an image.
     * @return true if tracking thread is running, false if not
     */
    public abstract boolean isTracking();

    /**
     * Returns the most recently capture camera image
     * @return camera image data, null if camera not previewing
     */
    protected synchronized byte[] getImageData()
    {
        return mImageData;
    }

    /**
     * Called when the camera gets another image.
     * It keeps a pointer to the captured image
     * for the tracking thread.
     * @param imageData new camera image data
     */
    protected synchronized void  setImageData(byte[] imageData)
    {
        mImageData = imageData;
    }

    /**
     * Compute new body pose from most recent camera image.
     * <p>
     * If the camera image has not changed since the last call,
     * a new body pose is not computed. The previously computed
     * body pose is used to update the skeleton.
     * @return true if successful (body data is valid) or false on failure
     */
    public boolean track()
    {
        byte[] imageData = getImageData();
        if (imageData == null)
        {
            return false;
        }
        if (trackFromImage(imageData))
        {
            getGVRContext().getEventManager().sendEvent(this, TrackerEvents.class, "onTrackUpdate", this);
            return updateSkeleton();
        }
        return false;
    }

    /**
     * Subclasses must implement this to produce body tracking
     * data from the capture image.
     * <p>
     * The format and location of this data is the responsibility
     * of the implementation and is not exposed by the framework.
     * @param imageData most recently capture image
     * @return true if tracking was successful, false on error
     */
    abstract protected boolean trackFromImage(byte[] imageData);

    /**
     * Subclasses must implement this to update the target
     * skeleton from the body tracking results.
     * <p>
     * This function will never be called if a target skeleton
     * is not provided.
     * @return true if skeleton was successfully updated, false on error
     */
    abstract protected boolean updateSkeleton();

    /**
     * Called when image capture starts.
     * <p>
     * This function emits the <b>onTrackStart</b> event.
     * Subclasses which override it must call the parent
     * implementation or events will not be correctly
     * emitted. It is provided to allow implementation
     * specific code when tracking begins.
     * @return true if tracking can be started, false on error
     */
    protected boolean onStart()
    {
        getGVRContext().getEventManager().sendEvent(this, TrackerEvents.class, "onTrackStart", this);
        return true;
    }

    /**
     * Called when image capture stops.
     * <p>
     * This function emits the <b>onTrackEnd</b> event.
     * Subclasses which override it must call the parent
     * implementation or events will not be correctly
     * emitted. It is provided to allow implementation
     * specific code when tracking ends.
     */
    protected void onStop()
    {
        getGVRContext().getEventManager().sendEvent(this, TrackerEvents.class, "onTrackEnd", this);
    }

    /**
     * Opens the camera and starts capturing images.
     * <p>
     * The default implementation uses <i>ImageFormat.NV21</i>
     * and tracks at 30fps. It captures 1280 x 960 images.
     * The width and height is obtained from mWidth and mHeight
     * so constructors may set this to change the image size.
     * @return
     * @throws CameraAccessException
     * @throws IOException
     */
    protected Camera openCamera() throws CameraAccessException, IOException
    {
        Camera camera = Camera.open();

        if (camera == null)
        {
            throw new CameraAccessException(CameraAccessException.CAMERA_ERROR);
        }
        Camera.Parameters params = camera.getParameters();

        params.setPreviewSize(mWidth, mHeight);
        params.setPreviewFormat(ImageFormat.NV21);
        params.setPreviewFpsRange(30000, 30000);
        camera.setParameters(params);
        camera.setPreviewCallback(new Camera.PreviewCallback()
          {
              public void onPreviewFrame(byte[] data, Camera camera)
              {
                  setImageData(data);
              }
          });
        return camera;
    }
}

class NativeBodyTracker
{
    static native long getComponentType();
}
