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
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.gearvrf.GVRActivity;
import org.gearvrf.scene_objects.view.GVRView;
import org.gearvrf.scene_objects.view.GVRWebView;

public class SceneObjectActivity extends GVRActivity {
    private static final String TAG = "SceneObjectActivity";
    private SampleViewManager mViewManager;
    private long lastDownTime = 0;
    private GVRWebView webView;
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
        webView = new GVRWebView(this);
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
    
    private long prevTime = 0;
    private int numFrames = 0;
    private PreviewCallback previewCallback = new PreviewCallback() {

        @Override
        /**
         * The byte data comes from the android camera in the yuv format. so we
         * need to convert it to rgba format.
         */
		public void onPreviewFrame(byte[] data, Camera camera) {
			numFrames++;
			if (numFrames == 100) {
				Parameters params = SceneObjectActivity.this.camera
						.getParameters();
				params.setAutoWhiteBalanceLock(true);
				params.setAutoExposureLock(true);
				SceneObjectActivity.this.camera.setParameters(params);
			}
			long currentTime = System.currentTimeMillis();
			Log.d(TAG,
					"Preview Frame rate "
							+ Math.round(1000 / (currentTime - prevTime)));
			prevTime = currentTime;
			camera.addCallbackBuffer(previewCallbackBuffer);
		}
	};

    private byte[] previewCallbackBuffer = null;
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
                
				// check if the device supports vr mode preview
				if ("true".equalsIgnoreCase(params.get("vrmode-supported"))) {
					Log.v(TAG, "VR Mode supported!");

					// set vr mode
					params.set("vrmode", 1);
					
					// true if the apps intend to record videos using MediaRecorder
				    params.setRecordingHint(true); 

					// set preview size
					//params.setPreviewSize(640, 480);

					// set fast-fps-mode: 0 for 30fps, 1 for 60 fps, 
                    // 2 for 120 fps
					params.set("fast-fps-mode", 1);
					params.setPreviewFpsRange(60000, 60000);

					// for auto focus
					params.set("focus-mode", "continuous-video");

					params.setVideoStabilization(false);
					if ("true".equalsIgnoreCase(params.get("ois-supported"))) {
						params.set("ois", "center");
					}
				}

				camera.setParameters(params);
				int bufferSize = params.getPreviewSize().height
						* params.getPreviewSize().width
						* ImageFormat
								.getBitsPerPixel(params.getPreviewFormat()) / 8;
				previewCallbackBuffer = new byte[bufferSize];
				camera.addCallbackBuffer(previewCallbackBuffer);
				camera.setPreviewCallbackWithBuffer(previewCallback);
				camera.startPreview();
			}
		} catch (Exception exception) {
			android.util.Log.d(TAG, "Camera not available or is in use");
		}
    }

    GVRView getWebView() {
        return webView;
    }

    Camera getCamera() {
        return camera;
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewManager.onPause();
        if (camera != null) {
        	camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastDownTime = event.getDownTime();
        }

        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            // check if it was a quick tap
            if (event.getEventTime() - lastDownTime < 200) {
                // pass it as a tap to the ViewManager
                mViewManager.onTap();
            }
        }

        return true;
    }
}
