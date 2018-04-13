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

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.gearvrf.GVRContext;
import org.gearvrf.mixedreality.GVRAnchor;
import org.gearvrf.mixedreality.GVRLightEstimate;
import org.gearvrf.mixedreality.GVRPlane;
import org.gearvrf.mixedreality.MRCommon;
import org.gearvrf.mixedreality.CameraPermissionHelper;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;


public class ARCoreSession extends MRCommon {
    private Session mSession;
    private boolean mInstallRequested;
    private ARCoreStatesManager mArManager;

    private int mDisplayRotation;
    private int mWidthPx;
    private int mHeightPx;

    private ArrayList<ARCoreAnchor> mAnchors = new ArrayList<>();

    public ARCoreSession(GVRContext gvrContext) {
        super(gvrContext);
        mSession = null;
        setDisplayGeometry(Surface.ROTATION_90, 1280, 720);
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

        mSession.setDisplayGeometry(mDisplayRotation , mWidthPx, mHeightPx);

        mArManager = new ARCoreStatesManager(mGvrContext, mSession, mAnchors);

        mArManager.start();

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

    protected Session getARSession() { return this.mSession; }

    @Override
    public ArrayList<GVRPlane> onGetAllPlanes() {
        ArrayList<GVRPlane> planes = new ArrayList<>();

        try {
            mSession.update();
            for (Plane p : mSession.getAllTrackables(Plane.class)) {
                GVRPlane gvrPlane = new GVRPlane();
                float poseMatrix[] = new float[16];
                p.getCenterPose().toMatrix(poseMatrix, 0);
                gvrPlane.setCenter(poseMatrix);
                //gvrPlane.setWidth(p.getExtentX());
                //gvrPlane.setHeight(p.getExtentZ());
//            gvrPlane.setTrackingState(p.getTrackingState());
                planes.add(gvrPlane);
            }
        }
        catch (Throwable t) {
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
    public GVRAnchor onCreateAnchor(Matrix4f poseMatrix) {
        float[] vectorTranslation = new float[3];
        Vector3f translation = new Vector3f();
        float[] vectorRotaion = new float[4];
        Quaternionf rotation = new Quaternionf();
        float[] modelMatrix = new float[16];
        ARCoreAnchor gvrAnchor = new ARCoreAnchor(mGvrContext, this);

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
        gvrAnchor.setModelMatrix(modelMatrix);
        gvrAnchor.setAnchorAR(anchor);

        mAnchors.add(gvrAnchor);

        return  gvrAnchor;
    }

    @Override
    protected void onDetachAnchor(GVRAnchor anchor) {
        for (ARCoreAnchor arCoreAnchor : mAnchors) {
            android.util.Log.e(TAG, "Anchor removed from session");
            if (arCoreAnchor.equals(anchor)) {
                mAnchors.remove(arCoreAnchor);
                break;
            }
        }
    }

}
