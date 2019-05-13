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
import android.util.DisplayMetrics;
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
import org.gearvrf.GVRExternalImage;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.mixedreality.CameraPermissionHelper;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRHitResult;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRMarker;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.IAnchorEvents;
import org.gearvrf.mixedreality.IPlaneEvents;
import org.gearvrf.mixedreality.MRCommon;
import org.gearvrf.utility.Log;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ARCoreSession extends MRCommon {
    private static float AR2VR_SCALE = 100.0f;

    private Session mSession;
    private boolean mInstallRequested;
    private Config mConfig;

    private GVRScene mVRScene;
    private GVRSceneObject mARPassThroughObject;
    private Frame mLastARFrame;
    private Frame arFrame;
    private ARCoreHandler mARCoreHandler;
    private boolean mEnableCloudAnchor;
    private Vector2f mScreenToCamera = new Vector2f(1, 1);

    /* From AR to GVR space matrices */
    private float[] mGVRCamMatrix = new float[16];

    private Vector3f mDisplayGeometry;

    private float mScreenDepth;

    private ARCoreHelper mArCoreHelper;

    private final Map<Anchor, CloudAnchorCallback> pendingAnchors = new HashMap<>();

    public ARCoreSession(GVRScene scene, boolean enableCloudAnchor) {
        super(scene.getGVRContext());
        mSession = null;
        mLastARFrame = null;
        mVRScene = scene;
        mArCoreHelper = new ARCoreHelper(scene.getGVRContext(), this);
        mEnableCloudAnchor = enableCloudAnchor;
    }

    @Override
    public float getARToVRScale() { return AR2VR_SCALE; }

    @Override
    public float getScreenDepth()
    {
        return mScreenDepth;
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
            ArCoreApk arCoreApk = ArCoreApk.getInstance();
            ArCoreApk.Availability availability = arCoreApk.checkAvailability(mGVRContext.getContext());
            if (availability == ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE) {
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

        mGVRContext.runOnGlThread(new Runnable() {
            @Override
            public void run() {
                try {
                    onInitARCoreSession(mGVRContext);
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
        Activity activity = mGVRContext.getApplication().getActivity();
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
        final GVRExternalImage image = new GVRExternalImage(gvrContext);
        final GVRTexture passThroughTexture = new GVRTexture(image);

        mSession.setCameraTextureName(passThroughTexture.getId());

        configDisplayAspectRatio(mGVRContext.getActivity());

        mLastARFrame = mSession.update();
        final GVRCameraRig cameraRig = mVRScene.getMainCameraRig();

        mDisplayGeometry = configDisplayGeometry(mLastARFrame.getCamera(), cameraRig);
        mSession.setDisplayGeometry(Surface.ROTATION_90,
                (int) mDisplayGeometry.x, (int) mDisplayGeometry.y);

        final GVRMesh mesh = GVRMesh.createQuad(mGVRContext, "float3 a_position float2 a_texcoord",
                mDisplayGeometry.x, mDisplayGeometry.y);

        final FloatBuffer texCoords = mesh.getTexCoordsAsFloatBuffer();
        final int capacity = texCoords.capacity();
        final int FLOAT_SIZE = 4;

        ByteBuffer bbTexCoordsTransformed = ByteBuffer.allocateDirect(capacity * FLOAT_SIZE);
        bbTexCoordsTransformed.order(ByteOrder.nativeOrder());

        FloatBuffer quadTexCoordTransformed = bbTexCoordsTransformed.asFloatBuffer();

        mLastARFrame.transformDisplayUvCoords(texCoords, quadTexCoordTransformed);

        float[] uv = new float[capacity];
        quadTexCoordTransformed.get(uv);

        mesh.setTexCoords(uv);

        /* To render texture from phone's camera */
        mARPassThroughObject = new GVRSceneObject(gvrContext, mesh,
                passThroughTexture, GVRMaterial.GVRShaderType.OES.ID);

        mARPassThroughObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        mARPassThroughObject.getRenderData().setDepthTest(false);
        mARPassThroughObject.getTransform().setPosition(0, 0, mDisplayGeometry.z);
        mARPassThroughObject.attachComponent(new GVRMeshCollider(gvrContext, true));
        mARPassThroughObject.setName("ARPassThrough");
        mVRScene.getMainCameraRig().addChildObject(mARPassThroughObject);

        /* AR main loop */
        mARCoreHandler = new ARCoreHandler();
        gvrContext.registerDrawFrameListener(mARCoreHandler);
        syncARCamToVRCam(mLastARFrame.getCamera(), cameraRig);
        gvrContext.getEventManager().sendEvent(this,
                IPlaneEvents.class,
                "onStartPlaneDetection",
                this);
    }

    public class ARCoreHandler implements GVRDrawFrameListener {
        @Override
        public void onDrawFrame(float v) {
            try {
                arFrame = mSession.update();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
                mGVRContext.unregisterDrawFrameListener(this);
                mGVRContext.getEventManager().sendEvent(this,
                        IPlaneEvents.class,
                        "onStopPlaneDetection",
                        this);
                return;
            }

            Camera arCamera = arFrame.getCamera();

            if (arFrame.getTimestamp() == mLastARFrame.getTimestamp()) {
                // FIXME: ARCore works at 30fps.
                return;
            }

            mArCoreHelper.setCamera(arCamera);

            syncARCamToVRCam(arCamera, mVRScene.getMainCameraRig());

            if (arCamera.getTrackingState() != TrackingState.TRACKING) {
                return;
            }

            mArCoreHelper.updatePlanes(mSession.getAllTrackables(Plane.class), AR2VR_SCALE);

            mArCoreHelper.updateAugmentedImages(arFrame.getUpdatedTrackables(AugmentedImage.class));

            mArCoreHelper.updateAnchors(AR2VR_SCALE);

            updateCloudAnchors(arFrame.getUpdatedAnchors());

            mLastARFrame = arFrame;
        }
    }

    private void syncARCamToVRCam(Camera arCamera, GVRCameraRig cameraRig) {
        float x = mGVRCamMatrix[12];
        float y = mGVRCamMatrix[13];
        float z = mGVRCamMatrix[14];

        arCamera.getDisplayOrientedPose().toMatrix(mGVRCamMatrix, 0);

        // FIXME: This is a workaround because the AR camera's pose is changing its
        // position values even if it is stopped! To avoid the scene looks trembling
        mGVRCamMatrix[12] = (mGVRCamMatrix[12] * AR2VR_SCALE + x) * 0.5f;
        mGVRCamMatrix[13] = (mGVRCamMatrix[13] * AR2VR_SCALE + y) * 0.5f;
        mGVRCamMatrix[14] = (mGVRCamMatrix[14] * AR2VR_SCALE + z) * 0.5f;

        cameraRig.getTransform().setModelMatrix(mGVRCamMatrix);
    }

    private void configDisplayAspectRatio(Activity activity) {
        final DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        mScreenToCamera.x = metrics.widthPixels;
        mScreenToCamera.y = metrics.heightPixels;
        mSession.setDisplayGeometry(Surface.ROTATION_90, metrics.widthPixels, metrics.heightPixels);
    }

    private Vector3f configDisplayGeometry(Camera arCamera, GVRCameraRig cameraRig) {
        GVRPerspectiveCamera centerCamera = cameraRig.getCenterCamera();
        float near = centerCamera.getNearClippingDistance();
        float far = centerCamera.getFarClippingDistance();

        // Get phones' cam projection matrix.
        float[] m = new float[16];
        arCamera.getProjectionMatrix(m, 0, near, far);
        Matrix4f projmtx = new Matrix4f();
        projmtx.set(m);

        float aspectRatio = projmtx.m11() / projmtx.m00();
        float arCamFOV = projmtx.perspectiveFov();
        float tanfov =  (float) Math.tan(arCamFOV * 0.5f);
        float quadDistance = far - 1;
        float quadHeight = quadDistance * tanfov * 2;
        float quadWidth = quadHeight * aspectRatio;

        // Use the same fov from AR to VR Camera as default value.
        float vrFov = (float) Math.toDegrees(arCamFOV);
        setVRCameraFov(cameraRig, vrFov);

        // VR Camera will be updated by AR pose, not by internal sensors.
        cameraRig.getHeadTransform().setRotation(1, 0, 0, 0);
        cameraRig.setCameraRigType(GVRCameraRig.GVRCameraRigType.Freeze.ID);

        android.util.Log.d(TAG, "ARCore configured to: passthrough[w: "
                + quadWidth + ", h: " + quadHeight +", z: " + quadDistance
                + "], cam fov: " +vrFov + ", aspect ratio: " + aspectRatio);
        mScreenToCamera.x = quadWidth / mScreenToCamera.x;    // map [0, ScreenSize] to [-Display, +Display]
        mScreenToCamera.y = quadHeight / mScreenToCamera.y;
        mScreenDepth = quadHeight / tanfov;
        return new Vector3f(quadWidth, quadHeight, -quadDistance);
    }

    private static void setVRCameraFov(GVRCameraRig camRig, float degreesFov) {
        camRig.getCenterCamera().setFovY(degreesFov);
        ((GVRPerspectiveCamera)camRig.getLeftCamera()).setFovY(degreesFov);
        ((GVRPerspectiveCamera)camRig.getRightCamera()).setFovY(degreesFov);
    }

    @Override
    protected GVRSceneObject onGetPassThroughObject() {
        return mARPassThroughObject;
    }

    @Override
    protected ArrayList<GVRPlane> onGetAllPlanes() {
        return mArCoreHelper.getAllPlanes();
    }

    @Override
    protected GVRAnchor onCreateAnchor(float[] pose) {
        final float[] translation = new float[3];
        final float[] rotation = new float[4];
        final float[] arPose = pose.clone();

        gvr2ar(arPose);

        convertMatrixPoseToVector(arPose, translation, rotation);

        Anchor anchor = mSession.createAnchor(new Pose(translation, rotation));
        return mArCoreHelper.createAnchor(anchor, AR2VR_SCALE);
    }

    @Override
    protected void onUpdateAnchorPose(GVRAnchor anchor, float[] pose) {
        final float[] translation = new float[3];
        final float[] rotation = new float[4];
        final float[] arPose = pose.clone();

        gvr2ar(arPose);

        convertMatrixPoseToVector(arPose, translation, rotation);

        Anchor arAnchor = mSession.createAnchor(new Pose(translation, rotation));
        mArCoreHelper.updateAnchorPose((ARCoreAnchor) anchor, arAnchor);
    }

    @Override
    protected void onRemoveAnchor(GVRAnchor anchor) {
        mArCoreHelper.removeAnchor((ARCoreAnchor) anchor);
    }

    /**
     * This method hosts an anchor. The {@code listener} will be invoked when the results are
     * available.
     */
    @Override
    synchronized protected void onHostAnchor(GVRAnchor anchor, CloudAnchorCallback cb) {
        Anchor newAnchor = mSession.hostCloudAnchor(((ARCoreAnchor) anchor).getAnchorAR());
        pendingAnchors.put(newAnchor, cb);
    }

    /**
     * This method resolves an anchor. The {@link IAnchorEvents} will be invoked when the results are
     * available.
     */
    synchronized protected void onResolveCloudAnchor(String anchorId, CloudAnchorCallback cb) {
        Anchor newAnchor = mSession.resolveCloudAnchor(anchorId);
        pendingAnchors.put(newAnchor, cb);
    }

    /**
     * Should be called with the updated anchors available after a {@link Session#update()} call.
     */
    synchronized void updateCloudAnchors(Collection<Anchor> updatedAnchors) {
        for (Anchor anchor : updatedAnchors) {
            if (pendingAnchors.containsKey(anchor)) {
                Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
                if (isReturnableState(cloudState)) {
                    CloudAnchorCallback cb = pendingAnchors.get(anchor);
                    pendingAnchors.remove(anchor);
                    GVRAnchor newAnchor = mArCoreHelper.createAnchor(anchor, AR2VR_SCALE);
                    cb.onCloudUpdate(newAnchor);
                }
            }
        }
    }

    /**
     * Used to clear any currently registered listeners, so they wont be called again.
     */
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
    protected GVRHitResult onHitTest(GVRPicker.GVRPickedObject collision) {
        Vector2f tapPosition = convertToDisplayGeometrySpace(collision.hitLocation[0], collision.hitLocation[1]);
        List<HitResult> hitResult = arFrame.hitTest(tapPosition.x, tapPosition.y);

        return mArCoreHelper.hitTest(hitResult, AR2VR_SCALE);
    }

    @Override
    protected GVRHitResult onHitTest(float x, float y) {
        x *= mScreenToCamera.x;
        y *= mScreenToCamera.y;
        List<HitResult> hitResult = arFrame.hitTest(x, y);
        return mArCoreHelper.hitTest(hitResult, AR2VR_SCALE);
    }

    @Override
    protected GVRLightEstimate onGetLightEstimate() {
        return mArCoreHelper.getLightEstimate(arFrame.getLightEstimate());
    }

    @Override
    protected void onSetMarker(Bitmap image) {
        ArrayList<Bitmap> imagesList = new ArrayList<>();
        imagesList.add(image);
        onSetMarkers(imagesList);
    }

    @Override
    protected void onSetMarkers(ArrayList<Bitmap> imagesList) {
        AugmentedImageDatabase augmentedImageDatabase = new AugmentedImageDatabase(mSession);
        for (Bitmap image : imagesList) {
            augmentedImageDatabase.addImage("image_name", image);
        }

        mConfig.setAugmentedImageDatabase(augmentedImageDatabase);
        mSession.configure(mConfig);
    }

    @Override
    protected ArrayList<GVRMarker> onGetAllMarkers() {
        return mArCoreHelper.getAllMarkers();
    }

    @Override
    protected float[] onMakeInterpolated(float[] poseA, float[] poseB, float t) {
        float[] translation = new float[3];
        float[] rotation = new float[4];
        float[] newMatrixPose = new float[16];

        convertMatrixPoseToVector(poseA, translation, rotation);
        Pose ARPoseA = new Pose(translation, rotation);

        convertMatrixPoseToVector(poseB, translation, rotation);
        Pose ARPoseB = new Pose(translation, rotation);

        Pose newPose = Pose.makeInterpolated(ARPoseA, ARPoseB, t);
        newPose.toMatrix(newMatrixPose, 0);

        return newMatrixPose;
    }

    private Vector2f convertToDisplayGeometrySpace(float x, float y) {
        final float hitX = x + 0.5f * mDisplayGeometry.x;
        final float hitY = 0.5f * mDisplayGeometry.y - y;

        return new Vector2f(hitX, hitY);
    }

    static void gvr2ar(float[] transformModelMatrix) {
        Matrix.scaleM(transformModelMatrix, 0, 1/AR2VR_SCALE, 1/AR2VR_SCALE, 1/AR2VR_SCALE);

        transformModelMatrix[12] /= AR2VR_SCALE;
        transformModelMatrix[13] /= AR2VR_SCALE;
        transformModelMatrix[14] /= AR2VR_SCALE;
    }

    static void convertMatrixPoseToVector(float[] pose, float[] translation, float[] rotation) {
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
