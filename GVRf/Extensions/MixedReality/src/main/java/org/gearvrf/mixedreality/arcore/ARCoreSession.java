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

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import org.gearvrf.GVRContext;
import org.gearvrf.mixedreality.MRCommon;
import org.gearvrf.mixedreality.CameraPermissionHelper;
import org.gearvrf.utility.Log;

public class ARCoreSession extends MRCommon {
    private Session mSession;
    private boolean mInstallRequested;

    public ARCoreSession(GVRContext gvrContext) {
        super(gvrContext);
        mSession = null;
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
