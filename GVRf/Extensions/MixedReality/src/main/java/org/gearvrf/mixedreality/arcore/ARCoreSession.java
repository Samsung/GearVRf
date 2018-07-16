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
import android.graphics.Bitmap;
import android.opengl.Matrix;
import android.view.Surface;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRAugmentedImage;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.IAnchorEventsListener;
import org.gearvrf.mixedreality.IAugmentedImageEventsListener;
import org.gearvrf.mixedreality.ICloudAnchorListener;
import org.gearvrf.mixedreality.IPlaneEventsListener;
import org.gearvrf.mixedreality.MRCommon;
import org.gearvrf.mixedreality.CameraPermissionHelper;
import org.gearvrf.utility.Log;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class ARCoreSession extends MRCommon {

    private static float PASSTHROUGH_DISTANCE = 100.0f;
    private static float AR2VR_SCALE = 100;

    private Session mSession;
    private boolean mInstallRequested;
    private Config mConfig;

    private GVRScene mVRScene;
    private GVRSceneObject mARPassThroughObject;
    private Frame mLastARFrame;
    private Frame arFrame;
    private ARCoreHandler mARCoreHandler;
    private boolean mEnableCloudAnchor;

    /* From AR to GVR space matrices */
    private float[] mGVRModelMatrix = new float[16];
    private float[] mARViewMatrix = new float[16];
    private float[] mGVRCamMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];

    private Vector3f mDisplayGeometry;

    private ARCoreHelper mArCoreHelper;

    private final HashMap<Anchor, ICloudAnchorListener> pendingAnchors = new HashMap<>();

    public ARCoreSession(GVRContext gvrContext, boolean enableCloudAnchor) {
        super(gvrContext);
        mSession = null;
        mLastARFrame = null;
        mVRScene = gvrContext.getMainScene();
        mArCoreHelper = new ARCoreHelper(gvrContext, mVRScene);
        mEnableCloudAnchor = enableCloudAnchor;
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResumeAR");

        if (mSession == null) {

            if (!checkARCoreAndCamera()) {
                return;
            }

            // Create default config and check if supported.
            mConfig = new Config(mSession);
            if (mEnableCloudAnchor) {
                mConfig.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
            }
            mConfig.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            if (!mSession.isSupported(mConfig)) {
                showSnackbarMessage("This device does not support AR", true);
            }
            mSession.configure(mConfig);
        }

        showLoadingMessage();

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mGvrContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                try {
                    onInitARCoreSession(mGvrContext);
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
            mSession.pause();
        }
    }

    private boolean checkARCoreAndCamera() {
        Activity activity = mGvrContext.getApplication().getActivity();
        Exception exception = null;
        String message = null;
        try {
            switch (ArCoreApk.getInstance().requestInstall(activity, !mInstallRequested)) {
                case INSTALL_REQUESTED:
                    mInstallRequested = true;
                    return false;
                case INSTALLED:
                    break;
            }

            // ARCore requires camera permissions to operate. If we did not yet obtain runtime
            // permission on Android M and above, now is a good time to ask the user for it.
            if (!CameraPermissionHelper.hasCameraPermission(activity)) {
                CameraPermissionHelper.requestCameraPermission(activity);
                return false;
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
            return false;
        }

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


    private void onInitARCoreSession(GVRContext gvrContext) throws CameraNotAvailableException {
        GVRTexture passThroughTexture = new GVRExternalTexture(gvrContext);

        mSession.setCameraTextureName(passThroughTexture.getId());

        // FIXME: detect VR screen aspect ratio. Using empirical 16:9 aspect ratio
        /* Try other aspect ration whether virtual objects looks jumping ou sliding
        during camera's rotation.
         */
        mSession.setDisplayGeometry(Surface.ROTATION_90 , 160, 90);

        mLastARFrame = mSession.update();
        mDisplayGeometry = configDisplayGeometry(mLastARFrame.getCamera());

        mSession.setDisplayGeometry(Surface.ROTATION_90 ,
                (int)mDisplayGeometry.x, (int)mDisplayGeometry.y);

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


    public class ARCoreHandler implements GVRDrawFrameListener {
        @Override
        public void onDrawFrame(float v) {
            try {
                arFrame = mSession.update();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
                mGvrContext.unregisterDrawFrameListener(this);
                return;
            }

            Camera arCamera = arFrame.getCamera();

            if (arFrame.getTimestamp() == mLastARFrame.getTimestamp()) {
                // FIXME: ARCore works at 30fps.
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

            mArCoreHelper.updatePlanes(mSession.getAllTrackables(Plane.class),
                    mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);

            mArCoreHelper.updateAugmentedImages(arFrame.getUpdatedTrackables(AugmentedImage.class));

            mArCoreHelper.updateAnchors(mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);

            updateCloudAnchors(arFrame.getUpdatedAnchors());

            mLastARFrame = arFrame;

            // Update current VR cam's matrix to next update of passtrhough and virtual objects.
            // AR/30fps vs VR/60fps
            mGVRCamMatrix = mVRScene.getMainCameraRig().getHeadTransform().getModelMatrix();
        }
    }

    private void updateAR2GVRMatrices(Camera arCamera, GVRCameraRig cameraRig) {
        arCamera.getViewMatrix(mARViewMatrix, 0);
        mGVRCamMatrix = cameraRig.getHeadTransform().getModelMatrix();
    }

    private void updatePassThroughObject(GVRSceneObject object) {
        Matrix.setIdentityM(mModelViewMatrix, 0);
        Matrix.translateM(mModelViewMatrix, 0, 0, 0, mDisplayGeometry.z);

        Matrix.multiplyMM(mGVRModelMatrix, 0, mGVRCamMatrix, 0, mModelViewMatrix, 0);

        object.getTransform().setModelMatrix(mGVRModelMatrix);
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

        float quadDistance = PASSTHROUGH_DISTANCE;
        float quadHeight = new Float(2 * quadDistance * Math.tan(arCamFOV * 0.5f));
        float quadWidth = quadHeight * aspectRatio;

        android.util.Log.d(TAG, "ARCore configured to: passthrough[w: "
                + quadWidth + ", h: " + quadHeight +", z: " + quadDistance
                + "], cam fov: " +Math.toDegrees(arCamFOV) + ", aspect ratio: " + aspectRatio);

        return new Vector3f(quadWidth, quadHeight, -PASSTHROUGH_DISTANCE);
    }

    @Override
    protected GVRSceneObject onGetPassThroughObject() {
        return mARPassThroughObject;
    }

    @Override
    protected void onRegisterPlaneListener(IPlaneEventsListener listener) {
        mArCoreHelper.registerPlaneListener(listener);
    }

    @Override
    protected void onRegisterAnchorListener(IAnchorEventsListener listener) {
        mArCoreHelper.registerAnchorListener(listener);
    }

    @Override
    protected void onRegisterAugmentedImageListener(IAugmentedImageEventsListener listener) {
        mArCoreHelper.registerAugmentedImageListener(listener);
    }

    @Override
    protected ArrayList<GVRPlane> onGetAllPlanes() {
        return mArCoreHelper.getAllPlanes();
    }

    @Override
    protected GVRAnchor onCreateAnchor(float[] pose, GVRSceneObject sceneObject) {
        float[] translation = new float[3];
        float[] rotation = new float[4];

        convertMatrixPoseToVector(pose, translation, rotation);

        Anchor anchor = mSession.createAnchor(new Pose(translation, rotation));
        return mArCoreHelper.createAnchor(anchor, sceneObject);
    }

    @Override
    protected void onUpdateAnchorPose(GVRAnchor anchor, float[] pose) {
        float[] translation = new float[3];
        float[] rotation = new float[4];

        convertMatrixPoseToVector(pose, translation, rotation);

        Anchor arAnchor = mSession.createAnchor(new Pose(translation, rotation));
        mArCoreHelper.updateAnchorPose((ARCoreAnchor)anchor, arAnchor);
    }

    @Override
    protected void onRemoveAnchor(GVRAnchor anchor) {
        mArCoreHelper.removeAnchor((ARCoreAnchor)anchor);
    }

    /**
     * This method hosts an anchor. The {@code listener} will be invoked when the results are
     * available.
     */
    @Override
    synchronized protected void onHostAnchor(GVRAnchor anchor, ICloudAnchorListener listener) {
        Anchor newAnchor = mSession.hostCloudAnchor(((ARCoreAnchor)anchor).getAnchorAR());
        pendingAnchors.put(newAnchor, listener);
    }

    /**
     * This method resolves an anchor. The {@code listener} will be invoked when the results are
     * available.
     */
    synchronized protected void onResolveCloudAnchor(String anchorId, ICloudAnchorListener listener) {
        Anchor newAnchor = mSession.resolveCloudAnchor(anchorId);
        pendingAnchors.put(newAnchor, listener);
    }

    /** Should be called with the updated anchors available after a {@link Session#update()} call. */
    synchronized void updateCloudAnchors(Collection<Anchor> updatedAnchors) {
        for (Anchor anchor : updatedAnchors) {
            if (pendingAnchors.containsKey(anchor)) {
                Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
                if (isReturnableState(cloudState)) {
                    ICloudAnchorListener listener = pendingAnchors.remove(anchor);
                    GVRAnchor newAnchor = mArCoreHelper.createAnchor(anchor, null);
                    listener.onTaskComplete(newAnchor);
                }
            }
        }
    }

    /** Used to clear any currently registered listeners, so they wont be called again. */
    synchronized void clearListeners() {
        pendingAnchors.clear();
    }

    private static boolean isReturnableState(Anchor.CloudAnchorState cloudState) {
        switch (cloudState) {
            case NONE:
            case TASK_IN_PROGRESS:
                return false;
            default:
                return true;
        }
    }

    @Override
    protected void onSetEnableCloudAnchor(boolean enableCloudAnchor) {
        mEnableCloudAnchor = enableCloudAnchor;
    }

    @Override
    protected GVRHitResult onHitTest(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        if (sceneObj != mARPassThroughObject)
            return null;

        Vector2f tapPosition = convertToDisplayGeometrySpace(collision.getHitLocation());
        List<HitResult> hitResult = arFrame.hitTest(tapPosition.x, tapPosition.y);

        return mArCoreHelper.hitTest(hitResult);
    }

    @Override
    protected GVRLightEstimate onGetLightEstimate() {
        return mArCoreHelper.getLightEstimate(arFrame.getLightEstimate());
    }

    @Override
    protected void onSetAugmentedImage(Bitmap image) {
        ArrayList<Bitmap> imagesList = new ArrayList<>();
        imagesList.add(image);
        onSetAugmentedImages(imagesList);
    }

    @Override
    protected void onSetAugmentedImages(ArrayList<Bitmap> imagesList) {
        AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(mSession);
        for (Bitmap image: imagesList) {
            augmentedImageDatabase.addImage("image_name", image);
        }

        mConfig.setAugmentedImageDatabase(augmentedImageDatabase);
        mSession.configure(mConfig);
    }

    @Override
    protected ArrayList<GVRAugmentedImage> onGetAllAugmentedImages() {
        return mArCoreHelper.getAllAugmentedImages();
    }

    private Vector2f convertToDisplayGeometrySpace(float[] hitPoint) {
        final float hitX = hitPoint[0] + 0.5f * mDisplayGeometry.x;
        final float hitY = mDisplayGeometry.y - hitPoint[1] - 0.5f * mDisplayGeometry.y;

        return new Vector2f(hitX, hitY);
    }

    private void convertMatrixPoseToVector(float[] pose, float[] translation, float[] rotation) {
        Vector3f vectorTranslation = new Vector3f();
        Quaternionf quaternionRotation = new Quaternionf();
        Matrix4f matrixPose = new Matrix4f();

        matrixPose.set(pose);


        matrixPose.getTranslation(vectorTranslation);
        translation[0] = vectorTranslation.x;
        translation[1] = vectorTranslation.y;
        translation[2] = vectorTranslation.z;

        matrixPose.getNormalizedRotation(quaternionRotation);
        rotation[0] = quaternionRotation.x;
        rotation[1] = quaternionRotation.y;
        rotation[2] = quaternionRotation.z;
        rotation[3] = quaternionRotation.w;
    }
}