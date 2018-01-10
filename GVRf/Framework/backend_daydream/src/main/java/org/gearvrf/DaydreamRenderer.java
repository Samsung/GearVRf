/* Copyright 2016 Samsung Electronics Co., LTD
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
package org.gearvrf;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * This class and its accompanying native components are designed using the daydream native ndk
 * sample as reference:
 * https://github.com/googlevr/gvr-android-sdk/tree/master/samples/ndk-treasurehunt
 */
class DaydreamRenderer implements GLSurfaceView.Renderer {
    static {
        System.loadLibrary("gvr");
        System.loadLibrary("gvrf-daydream");
    }

    private DaydreamViewManager mViewManager;
    private long nativeDaydreamRenderer;
    private GVRCameraRig cameraRig;
    private final boolean[] mBlendEnabled = new boolean[1];

    public DaydreamRenderer(DaydreamViewManager viewManager, long nativeGvrContext) {
        mViewManager = viewManager;
        nativeDaydreamRenderer = nativeCreateRenderer(nativeGvrContext);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        nativeInitializeGl(nativeDaydreamRenderer);
        mViewManager.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //Do nothing
    }
    public long getNativeDaydreamRenderer(){
        return nativeDaydreamRenderer;
    }
    @Override
    public void onDrawFrame(GL10 gl) {
        if (cameraRig == null) {
            return;
        }
        GLES30.glGetBooleanv(GLES30.GL_BLEND, mBlendEnabled, 0);
        GLES30.glDisable(GLES30.GL_BLEND);
        mViewManager.beforeDrawEyes();
        mViewManager.onDrawFrame();
        nativeDrawFrame(nativeDaydreamRenderer);

        mViewManager.afterDrawEyes();
        if (mBlendEnabled[0]) {
            GLES30.glEnable(GLES30.GL_BLEND);
        }
    }

    public void onViewChange()
    {
        mViewManager.getEventManager().sendEvent(mViewManager, ICameraEvents.class, "onViewChange", this.cameraRig);
    }

    public void setCameraRig(GVRCameraRig cameraRig) {
        this.cameraRig = cameraRig;
        nativeSetCameraRig(nativeDaydreamRenderer, cameraRig.getNative());
    }

    void onResume() {
        nativeOnResume(nativeDaydreamRenderer);
    }

    void onPause() {
        nativeOnPause(nativeDaydreamRenderer);
    }

    void onDestroy() {
        nativeDestroyRenderer(nativeDaydreamRenderer);
    }

    // called from the native side
    void onDrawEye(int eye) {
        mViewManager.onDrawEye(eye);
    }

    private native long nativeCreateRenderer(long nativeDaydreamRenderer);

    private native void nativeDestroyRenderer(long nativeDaydreamRenderer);

    private native void nativeInitializeGl(long nativeDaydreamRenderer);

    private native long nativeDrawFrame(long nativeDaydreamRenderer);

    private native void nativeOnPause(long nativeDaydreamRenderer);

    private native void nativeOnResume(long nativeDaydreamRenderer);

    private native void nativeSetCameraRig(long nativeDaydreamRenderer, long nativeCamera);
}