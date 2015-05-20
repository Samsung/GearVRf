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

package org.gearvrf.sample.sceneobjects;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.gearvrf.GVRActivity;

public class SceneObjectActivity extends GVRActivity {
    private static final String TAG = "SceneObjectActivity";
    private SampleViewManager mViewManager;
    private float lastY = 0;
    private float lastX = 0;
    private float lastYAngle = 0;
    private float lastXAngle = 0;
    private float yangle = 0;
    private float xangle = 0;
    private long lastDownTime = 0;
    private WebView webView;
    private Camera camera;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createWebView();
        createCameraView();

        mViewManager = new SampleViewManager(this);
        setScript(mViewManager, "gvr_note4.xml");
    }

    private void createWebView() {
        webView = new WebView(this);
        webView.setInitialScale(100);
        webView.measure(2000, 1000);
        webView.layout(0, 0, 2000, 1000);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl("http://gearvrf.org");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA);
    }

    private void createCameraView() {

        if (!checkCameraHardware(this)) {
            android.util.Log.d(TAG, "Camera hardware not available.");
            return;
        }

        camera = null;

        try {
            camera = Camera.open();
            if (camera != null) {
                Parameters params = camera.getParameters();
                params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                camera.setParameters(params);
                camera.startPreview();
            }
        } catch (Exception exception) {
            android.util.Log.d(TAG, "Camera not available or is in use");
        }
    }

    WebView getWebView() {
        return webView;
    }

    Camera getCamera() {
        return camera;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastY = event.getY();
            lastX = event.getX();
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // check if it was a quick tap
            if (event.getEventTime() - lastDownTime < 200) {
                // if things are rotating, stop the rotation.
                if (xangle != 0 || yangle != 0) {
                    xangle = 0.0f;
                    yangle = 0.0f;
                    mViewManager.setXAngle(0.0f);
                    mViewManager.setYAngle(0.0f);
                    return true;
                }

                // otherwise, pass it as a tap to the ViewManager
                mViewManager.setXAngle(0.0f);
                mViewManager.setYAngle(0.0f);
                mViewManager.onTap();
            }

            float xdifference = lastX - event.getX();
            if (Math.abs(xdifference) > 10) {
                xangle = lastXAngle + xdifference / 10;
                mViewManager.setXAngle(1.0f);
                lastX = event.getX();
                lastXAngle = xangle;
            }

            float ydifference = lastY - event.getY();
            if (Math.abs(ydifference) > 10) {
                yangle = lastYAngle + ydifference / 10;
                mViewManager.setYAngle(1.0f);
                lastY = event.getY();
                lastYAngle = yangle;
            }
        }

        return true;
    }
}
