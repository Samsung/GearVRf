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

package org.gearvrf.mixedreality;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.view.MotionEvent;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IActivityEvents;
import org.gearvrf.mixedreality.arcore.ARCoreSession;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

public class GVRMixedReality extends GVRBehavior implements IMRCommon {
    private final IActivityEvents mActivityEventsHandler;

    private MRCommon mSession;
    private DisplayRotationHelper mDisplayRotationHelper;

    public GVRMixedReality(final GVRContext gvrContext) {
        super(gvrContext, 0);

        mActivityEventsHandler = new ActivityEventsHandler();
        mSession = new ARCoreSession(gvrContext);
    }

    @Override
    public void onAttach(GVRSceneObject newOwner) {
        super.onAttach(newOwner);

        Log.d("AR_GVRF ","onAttach");
        this.resume();
    }

    @Override
    public void resume() {
        mSession.resume();
    }

    @Override
    public void pause() {
        mSession.pause();
    }

    @Override
    public void setDisplayGeometry(int displayRotation, int widthPx, int heightPx) {
        mSession.setDisplayGeometry(displayRotation, widthPx, heightPx);
    }

    @Override
    public ArrayList<GVRPlane> getAllPlanes() { return mSession.getAllPlanes(); }

    @Override
    public GVRLightEstimate getLightEstimate() { return mSession.getLightEstimate(); }

    @Override
    public GVRAnchor createAnchor(Matrix4f poseMatrix) {
        return mSession.createAnchor(poseMatrix);
    }

    @Override
    public ArrayList<GVRHitResult> hitTest(float[] hitLocation) {
        return mSession.hitTest(hitLocation);
    }

    @Override
    public void onDrawFrame(float frameTime) {
        super.onDrawFrame(frameTime);
        mSession.updateFrame();
    }

    private class ActivityEventsHandler implements IActivityEvents {
        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onResume() {
            resume();
        }

        @Override
        public void onDestroy() {}

        @Override
        public void onSetMain(GVRMain script) {}

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {}

        @Override
        public void onConfigurationChanged(Configuration config) {}

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {}

        @Override
        public void onTouchEvent(MotionEvent event) {}

        @Override
        public void dispatchTouchEvent(MotionEvent event) {}

        @Override
        public void onControllerEvent(Vector3f position, Quaternionf orientation, PointF touchpadPoint, boolean touched) {}
    }
}
