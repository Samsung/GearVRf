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
import android.view.Surface;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.mixedreality.MRCommon;
import org.gearvrf.mixedreality.CameraPermissionHelper;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;

public class ARCoreSession extends MRCommon {
    private Session mSession;
    private boolean mInstallRequested;
    private int mPassThroughTexureId = -1;
    private GVRSceneObject mPhysicCameraObject = null;

    public ARCoreSession(GVRContext gvrContext, GVRSceneObject owner) {
        super(gvrContext);
        mSession = null;
        mPhysicCameraObject = owner;
    }

    @Override
    protected void onResume() {

        Log.d(TAG, "onResume");

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
            if (!mSession.isSupported(config)) {
                showSnackbarMessage("This device does not support AR", true);
                // FIXME: Finish app
            }
            mSession.configure(config);
        }

        showLoadingMessage();
        // Note that order matters - see the note in onPause(), the reverse applies here.
        mSession.resume();

        //mDisplayRotationHelper.onResume();
        initARScene();
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

    private void initARScene() {

        if (mSession == null) {
            return;
        }

        GVRActivity activity = mGvrContext.getActivity();
        mSession.setDisplayGeometry(Surface.ROTATION_90 , activity.getFullScreenView().getWidth(),
                activity.getFullScreenView().getHeight());

        GVRScene scene = mGvrContext.getMainScene();
        GVRPerspectiveCamera cam = scene.getMainCameraRig().getCenterCamera();
        configCamera(cam.getNearClippingDistance(), cam.getFarClippingDistance());
    }

    private void configCamera(float near, float far) {

        GVRExternalTexture texture = new GVRExternalTexture(mGvrContext);
        float aspectRatio = 1.0f;

        mPassThroughTexureId = texture.getId();

        mSession.setCameraTextureName(mPassThroughTexureId);

        Frame frame = mSession.update();
        Camera camera = frame.getCamera();

        // Get projection matrix.
        float[] m = new float[16];
        camera.getProjectionMatrix(m, 0, near, far);
        Matrix4f projmtx = new Matrix4f();
        projmtx.set(m);

        aspectRatio = projmtx.m11()/projmtx.m00();

        float camFOV = projmtx.perspectiveFov();

        float fov_y = camFOV;
        float widthY = 2 * (float) Math.tan(fov_y / 2);
        float widthX = widthY * aspectRatio;

        float quadDistance = 100;
        float quadHeight = new Float(2 * quadDistance * Math.tan(fov_y / 2));
        float quadWidth = quadHeight * aspectRatio;

        GVRSceneObject passThroughObject = null;

        passThroughObject = createPassThroughObject(mGvrContext, texture, -quadDistance,
                quadWidth, quadHeight);
        mPhysicCameraObject.addChildObject(passThroughObject);

        android.util.Log.d(TAG, "RATION: " + aspectRatio
                + ", FOV: " + Math.toDegrees(camFOV) + ", wxh: " + quadWidth + "x" + quadHeight
                + ", FarXNear: " + near + "x" + far);
    }

    private static GVRSceneObject createPassThroughObject(GVRContext gvrContext, GVRExternalTexture texture,
                                                          float z, float width, float height) {

        GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.OES.ID);
        GVRSceneObject object = new GVRSceneObject(gvrContext,
                GVRMesh.createQuad(gvrContext, "float3 a_position float2 a_texcoord",
                        width, height));

        material.setMainTexture(texture);
        object.getRenderData().setMaterial(material);

        object.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
        object.getRenderData().setDepthTest(false);
        object.getTransform().setPosition(0.0f, 0.0f, z);

        return object;
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
}
