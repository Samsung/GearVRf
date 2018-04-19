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

package org.gearvrf.mixedreality.arcore;

import android.app.Activity;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.Surface;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.MRCommon;
import org.gearvrf.mixedreality.CameraPermissionHelper;
import org.gearvrf.mixedreality.R;
import org.gearvrf.utility.Log;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ARCoreSession extends MRCommon {

    private static float PASSTHROUGH_DISTANCE = 100.0f;
    private static float AR2VR_SCALE = 100;

    private Session mSession;
    private boolean mInstallRequested;

    private GVRScene mVRScene;
    private GVRSceneObject mARPassThroughObject;
    private Frame mLastARFrame;
    private ARCoreHandler mARCoreHandler;
    private GVRSceneObject mCursor;
    private GVRCursorController mCursorController;

    /* From AR to GVR space matrices */
    private float[] mGVRModelMatrix = new float[16];
    private float[] mARViewMatrix = new float[16];
    private float[] mGVRCamMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];

    private Vector3f mDisplayGeometry;
    private int mDisplayRotation;
    private int mWidthPx;
    private int mHeightPx;

    private ArrayList<ARCoreAnchor> mAnchorsObject = new ArrayList<>();
    private int mVirtObjCount;

    // Map to track planes
    private Map<Plane, ARCorePlane> planesMap = new HashMap<>();

    public ARCoreSession(GVRContext gvrContext) {
        super(gvrContext);
        mSession = null;
        mLastARFrame = null;
        mVRScene = gvrContext.getMainScene();
        setDisplayGeometry(Surface.ROTATION_90, 160, 90);
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResumeAR");

        if (mSession == null) {
            Activity activity = mGvrContext.getActivity();
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(activity, !mInstallRequested)) {
                    case INSTALL_REQUESTED:
                        mInstallRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // ARCore requires camera permissions to operate. If we did not yet obtain runtime
                // permission on Android M and above, now is a good time to ask the user for it.
                if (!CameraPermissionHelper.hasCameraPermission(activity)) {
                    CameraPermissionHelper.requestCameraPermission(activity);
                    return;
                }

                mSession = new Session(/* context= */ activity);
            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (Exception e) {
                message = "This device does not support AR";
                exception = e;
            }

            if (message != null) {
                showSnackbarMessage(message, true);
                android.util.Log.e(TAG, "Exception creating session", exception);
                return;
            }

            // Create default config and check if supported.
            Config config = new Config(mSession);
            config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            if (!mSession.isSupported(config)) {
                showSnackbarMessage("This device does not support AR", true);
                // FIXME: Finish app
            }
            mSession.configure(config);
        }

        showLoadingMessage();
        // Note that order matters - see the note in onPause(), the reverse applies here.
        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        //mDisplayRotationHelper.onResume();

        //mSession.setDisplayGeometry(mDisplayRotation , mWidthPx, mHeightPx);

        mGvrContext.registerDrawFrameListener(new GVRDrawFrameListener() {
            @Override
            public void onDrawFrame(float v) {
                mGvrContext.unregisterDrawFrameListener(this);
                try {
                    onInitARCoreSession(mGvrContext);
                     /* Cursor controller */
                    initCursorController(mGvrContext);
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        if (mSession != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            //mDisplayRotationHelper.onPause();
            mSession.pause();
        }
    }

    @Override
    protected void onSetDisplayGeometry(int displayRotation, int widthPx, int heightPx) {
        mDisplayRotation = displayRotation;
        mWidthPx = widthPx;
        mHeightPx = heightPx;
    }

    private boolean performRuntimeChecks() {
        return true;
    }

    private boolean checkWhetherIsInstalled(Activity activity) {
        switch (ArCoreApk.getInstance().requestInstall(activity, !mInstallRequested)) {
            case INSTALL_REQUESTED:
                mInstallRequested = true;
                return false;
            case INSTALLED:
                break;
        }

        return true;
    }

    private boolean checkWhetherHasCameraPermission(Activity activity) {
        if (!CameraPermissionHelper.hasCameraPermission(activity)) {
            CameraPermissionHelper.requestCameraPermission(activity);
            return false;
        }

        return true;
    }

    private boolean checkWhetherIsSupported(Session session) {
        // Create default config and check if supported.
        Config config = new Config(session);
        if (!session.isSupported(config)) {
            showSnackbarMessage("This device does not support AR", true);
            return false;
        }
        session.configure(config);
        return true;
    }

    private void showSnackbarMessage(String message, boolean finishOnDismiss) {
        Log.d(TAG, message);
        if (finishOnDismiss) {
            //FIXME: finish();
        }
    }

    private void showLoadingMessage() {
        showSnackbarMessage("Searching for surfaces...", false);
    }

    protected Session getARSession() {
        return this.mSession;
    }

    @Override
    public ArrayList<GVRPlane> onGetAllPlanes() {
        ArrayList<GVRPlane> planes = new ArrayList<>();

        try {
            mSession.update();
            for (Plane p : mSession.getAllTrackables(Plane.class)) {
                ARCorePlane gvrPlane = new ARCorePlane(mGvrContext, 0);
                float poseMatrix[] = new float[16];
                p.getCenterPose().toMatrix(poseMatrix, 0);
//                gvrPlane.setCenter(poseMatrix);
                //gvrPlane.setWidth(p.getExtentX());
                //gvrPlane.setHeight(p.getExtentZ());
//            gvrPlane.setTrackingState(p.getTrackingState());
                planes.add(gvrPlane);
            }
        } catch (Throwable t) {
            // Avoid crashing the application due to unhandled exceptions.
            Log.e(TAG, "Exception on the OpenGL thread", t);
        }
        return planes;
    }

    @Override
    protected GVRLightEstimate onGetLightEstimate() {
        ARLightEstimate lightEstimate = new ARLightEstimate(mGvrContext, this);
        return lightEstimate;
    }

    @Override
    protected void onUpdateFrame() {
        //TODO: Update Frame, anchors and planes

    }

    @Override
    public GVRAnchor onCreateAnchor(Matrix4f poseMatrix, GVRSceneObject sceneObject) {
        float[] vectorTranslation = new float[3];
        Vector3f translation = new Vector3f();
        float[] vectorRotaion = new float[4];
        Quaternionf rotation = new Quaternionf();
        float[] modelMatrix = new float[16];


        poseMatrix.getTranslation(translation);
        vectorTranslation[0] = translation.x;
        vectorTranslation[1] = translation.y;
        vectorTranslation[2] = translation.z;

        poseMatrix.getNormalizedRotation(rotation);
        vectorRotaion[0] = rotation.x;
        vectorRotaion[1] = rotation.y;
        vectorRotaion[2] = rotation.z;
        vectorRotaion[3] = rotation.w;

        Pose p = new Pose(vectorTranslation, vectorRotaion);

        Anchor anchor = mSession.createAnchor(p);
        anchor.getPose().toMatrix(modelMatrix, 0);
        ARCoreAnchor gvrAnchor = new ARCoreAnchor(mGvrContext, this, sceneObject);
        gvrAnchor.setAnchorAR(anchor);

        mAnchorsObject.add(gvrAnchor);

        return gvrAnchor;
    }

    @Override
    protected GVRAnchor onCreateAnchor(GVRHitResult hit, GVRSceneObject sceneObject) {

        ARCoreAnchor gvrAnchor = new ARCoreAnchor(mGvrContext, this, sceneObject);
        mAnchorsObject.add(gvrAnchor);
        return gvrAnchor;
    }

    @Override
    protected ArrayList<GVRHitResult> onHitTest(float[] hitLocation) {
        ArrayList<GVRHitResult> hits = new ArrayList<>();
        float[] modelMatrix = new float[16];
        float hitX = (hitLocation[0] + 0.5f) * mWidthPx;
        float hitY = (1 - hitLocation[1] + 0.5f) * mHeightPx;

        for (HitResult hit : mSession.update().hitTest(hitX, hitY)) {
            Trackable trackable = hit.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())) {
                GVRHitResult gvrHitResult = new GVRHitResult();
                ARCorePlane plane = new ARCorePlane(mGvrContext, 0);
//                plane.setPlaneAR((Plane)trackable);
                gvrHitResult.setPlane(plane);
                hit.getHitPose().toMatrix(modelMatrix, 0);
                gvrHitResult.setPose(modelMatrix);
                hits.add(gvrHitResult);
            }
        }

        return hits;
    }

    private void initCursorController(GVRContext gvrContext) {
        mVRScene.getEventReceiver().addListener(mARCoreHandler);
        GVRInputManager inputManager = gvrContext.getInputManager();
        mCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(0.2f * PASSTHROUGH_DISTANCE,
                        0.2f * PASSTHROUGH_DISTANCE),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mARCoreHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mARCoreHandler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-PASSTHROUGH_DISTANCE);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

    private void onInitARCoreSession(GVRContext gvrContext) throws CameraNotAvailableException {
        GVRTexture passThroughTexture = new GVRExternalTexture(gvrContext);

        mSession.setCameraTextureName(passThroughTexture.getId());

        // FIXME: detect VR screen aspect ratio.
        mSession.setDisplayGeometry(Surface.ROTATION_90, 160, 90);
        mLastARFrame = mSession.update();
        mDisplayGeometry = configDisplayGeometry(mLastARFrame.getCamera());

        mSession.setDisplayGeometry(Surface.ROTATION_90,
                (int) mDisplayGeometry.x, (int) mDisplayGeometry.y);

        /* To render texture from phone's camera */
        mARPassThroughObject = new GVRSceneObject(gvrContext, mDisplayGeometry.x, mDisplayGeometry.y,
                passThroughTexture, GVRMaterial.GVRShaderType.OES.ID);

        mARPassThroughObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        mARPassThroughObject.getRenderData().setDepthTest(false);
        mARPassThroughObject.getTransform().setPosition(0, 0, mDisplayGeometry.z);
        mARPassThroughObject.attachComponent(new GVRMeshCollider(gvrContext, true));

        mVRScene.addSceneObject(mARPassThroughObject);

        /* AR main loop */
        mARCoreHandler = new ARCoreHandler();
        gvrContext.registerDrawFrameListener(mARCoreHandler);

        mGVRCamMatrix = mVRScene.getMainCameraRig().getHeadTransform().getModelMatrix();

        updateAR2GVRMatrices(mLastARFrame.getCamera(), mVRScene.getMainCameraRig());

    }

    private static Vector3f configDisplayGeometry(Camera arCamera) {
        float near = 0.1f;
        float far = 100.0f;

        // Get phones' cam projection matrix.
        float[] m = new float[16];
        arCamera.getProjectionMatrix(m, 0, near, far);
        Matrix4f projmtx = new Matrix4f();
        projmtx.set(m);

        float aspectRatio = projmtx.m11()/projmtx.m00();
        float arCamFOV = projmtx.perspectiveFov();

        /*
        float widthY = 2 * (float) Math.tan(arCamFOV * 0.5f);
        float widthX = widthY * aspectRatio;
        */

        float quadDistance = PASSTHROUGH_DISTANCE;
        float quadHeight = new Float(2 * quadDistance * Math.tan(arCamFOV * 0.5f));
        float quadWidth = quadHeight * aspectRatio;

        android.util.Log.d(TAG, "ARCore configured to: passthrough[w: "
                + quadWidth + ", h: " + quadHeight +", z: " + quadDistance
                + "], cam fov: " +Math.toDegrees(arCamFOV) + ", aspect ratio: " + aspectRatio);

        return new Vector3f(quadWidth, quadHeight, -PASSTHROUGH_DISTANCE);
    }

    private void updateAR2GVRMatrices(Camera arCamera, GVRCameraRig cameraRig) {
        arCamera.getViewMatrix(mARViewMatrix, 0);
        mGVRCamMatrix = cameraRig.getHeadTransform().getModelMatrix();
    }

    public class ARCoreHandler extends GVREventListeners.TouchEvents implements GVRDrawFrameListener {
        private MotionEvent mCursorOverEvent = null;
        private MotionEvent mSingleTapEvent = null;

        @Override
        public void onDrawFrame(float v) {
            Frame arFrame = null;
            try {
                arFrame = mSession.update();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
                mGvrContext.unregisterDrawFrameListener(this);
                return;
            }

            Camera arCamera = arFrame.getCamera();

            if (arFrame.getTimestamp() == mLastARFrame.getTimestamp()) {
                return;
            }

            if (arCamera.getTrackingState() != TrackingState.TRACKING) {
                // Put passthrough object in from of current VR cam at paused states.
                updateAR2GVRMatrices(arCamera, mVRScene.getMainCameraRig());
                updatePassThroughObject(mARPassThroughObject);

                return;
            }

            // Update current AR cam's view matrix.
            arCamera.getViewMatrix(mARViewMatrix, 0);

            // Update passthrough object with last VR cam matrix
            updatePassThroughObject(mARPassThroughObject);

            List<HitResult> hitResult = null;

            if (mSingleTapEvent != null) {
                hitResult = arFrame.hitTest(mSingleTapEvent);
                addVirtualObject(createAnchor(hitResult));
                mSingleTapEvent.recycle();
                mSingleTapEvent = null;
            } else if (mCursorOverEvent != null) {
                // update planes only if cursor is inside arcore quad
            } else {
                // disable planes otherwise
            }

            updatePlanes();
            updateVirtualObjects();

            mLastARFrame = arFrame;

            // Update current VR cam's matrix to next update of passtrhough and virtual objects.
            // AR/30fps vs VR/60fps
            mGVRCamMatrix = mVRScene.getMainCameraRig().getHeadTransform().getModelMatrix();
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onInside(sceneObj, collision);

            if (sceneObj != mARPassThroughObject)
                return;

            if (mCursorOverEvent != null)
                mCursorOverEvent.recycle();

            mCursorOverEvent = convertToTouchEvent(collision.getHitLocation());
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onExit(sceneObj, collision);

            if (sceneObj != mARPassThroughObject)
                return;

            if (mCursorOverEvent != null) {
                mCursorOverEvent.recycle();
                mCursorOverEvent = null;
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onTouchEnd(sceneObj, collision);

            if (sceneObj != mARPassThroughObject)
                return;

            if (mSingleTapEvent != null)
                mSingleTapEvent.recycle();

            mSingleTapEvent = convertToTouchEvent(collision.getHitLocation());
        }

        public MotionEvent convertToTouchEvent(float[] hitPoint) {
            final float hitX = hitPoint[0] + 0.5f * mDisplayGeometry.x;
            final float hitY = mDisplayGeometry.y - hitPoint[1] - 0.5f * mDisplayGeometry.y;

            return MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, hitX, hitY, 0);
        }
    }

    private void addVirtualObject(Anchor anchor) {
        if (anchor == null)
            return;

        mVirtObjCount++;

        ARCoreAnchor obj = mAnchorsObject.get(mVirtObjCount % mAnchorsObject.size());
        obj.setAnchorAR(anchor);
        obj.setName("id: " + mVirtObjCount);

        android.util.Log.d(TAG, "New virtual object " + obj.getName());
    }

    private void updateVirtualObjects() {
        for (ARCoreAnchor obj: mAnchorsObject) {
            obj.update(mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);
        }
    }

    private void updatePlanes() {
        for (Plane plane: mSession.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() != TrackingState.TRACKING || plane.getSubsumedBy() != null) {
                ARCorePlane arCorePlane = planesMap.get(plane);
                if (arCorePlane != null) {
                    arCorePlane.setEnable(false);
                }
                continue;
            }

            ARCorePlane arCorePlane = planesMap.get(plane);
            if (arCorePlane == null) {
                arCorePlane = new ARCorePlane(mGvrContext, planesMap.size());
                mVRScene.addSceneObject(arCorePlane);
                planesMap.put(plane, arCorePlane);
            }
            arCorePlane.setEnable(true);
            arCorePlane.update(plane, mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);
        }
    }

    private Anchor createAnchor(List<HitResult> hitResult) {
        for (HitResult hit : hitResult) {
            // Check if any plane was hit, and if it was hit inside the plane polygon
            Trackable trackable = hit.getTrackable();
            // Creates an anchor if a plane or an oriented point was hit.
            if ((trackable instanceof Plane
                    && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                    && ((Plane) trackable).getSubsumedBy() == null) {
                return hit.createAnchor();
            }
        }
        return null;
    }

    private void updatePassThroughObject(GVRSceneObject object) {
        Matrix.setIdentityM(mModelViewMatrix, 0);
        Matrix.translateM(mModelViewMatrix, 0, 0, 0, mDisplayGeometry.z);

        Matrix.multiplyMM(mGVRModelMatrix, 0, mGVRCamMatrix, 0, mModelViewMatrix, 0);

        object.getTransform().setModelMatrix(mGVRModelMatrix);
    }

}