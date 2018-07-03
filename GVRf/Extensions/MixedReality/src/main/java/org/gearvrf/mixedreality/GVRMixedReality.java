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
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.gearvrf.GVRBehavior;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IActivityEvents;
import org.gearvrf.mixedreality.arcore.ARCoreSession;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;

/**
 * Component to enable AR functionalities on GVRf.
 */
public class GVRMixedReality extends GVRBehavior implements IMRCommon {
    private final IActivityEvents mActivityEventsHandler;
    private final MRCommon mSession;
    private SessionState mState;

    /**
     * Create a instace of GVRMixedReality component.
     *
     * @param gvrContext
     */
    public GVRMixedReality(final GVRContext gvrContext) {
        this(gvrContext, false, null);
    }

    /**
     * Create a instace of GVRMixedReality component and specifies the use of cloud anchors.
     *
     * @param gvrContext
     * @param enableCloudAnchor
     */
    public GVRMixedReality(final GVRContext gvrContext, boolean enableCloudAnchor) {
        this(gvrContext, enableCloudAnchor, null);
    }

    /**
     * Create a instance of GVRMixedReality component and add it to the specified scene.
     *
     * @param gvrContext
     * @param scene
     */
    public GVRMixedReality(final GVRContext gvrContext, GVRScene scene) {
        this(gvrContext, false, scene);
    }

    /**
     * Default GVRMixedReality constructor. Create a instace of GVRMixedReality component, set
     * the use of cloud anchors and add it to the specified scened.
     *
     * @param gvrContext
     * @param enableCloudAnchor
     * @param scene
     */
    public GVRMixedReality(GVRContext gvrContext, boolean enableCloudAnchor, GVRScene scene) {
        super(gvrContext, 0);


        if (scene == null) {
            scene = gvrContext.getMainScene();
        }

        mActivityEventsHandler = new ActivityEventsHandler();
        mSession = new ARCoreSession(gvrContext, enableCloudAnchor);
        mState = SessionState.ON_PAUSE;

        scene.getMainCameraRig().getOwnerObject().attachComponent(this);
    }

    @Override
    public void resume() {
        if (mState == SessionState.ON_RESUME) {
            return;
        }
        mSession.resume();
        mState = SessionState.ON_RESUME;
    }

    @Override
    public void pause() {
        if (mState == SessionState.ON_PAUSE) {
            return;
        }
        mSession.pause();
        mState = SessionState.ON_PAUSE;
    }

    @Override
    public GVRSceneObject getPassThroughObject() {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        return mSession.getPassThroughObject();
    }

    @Override
    public void registerPlaneListener(IPlaneEventsListener listener) {
        mSession.registerPlaneListener(listener);
    }

    @Override
    public void registerAnchorListener(IAnchorEventsListener listener) {
        mSession.registerAnchorListener(listener);
    }

    @Override
    public void registerAugmentedImageListener(IAugmentedImageEventsListener listener) {
        mSession.registerAugmentedImageListener(listener);
    }

    @Override
    public ArrayList<GVRPlane> getAllPlanes() {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        return mSession.getAllPlanes();
    }


    @Override
    public GVRAnchor createAnchor(float[] pose) {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        return mSession.createAnchor(pose);
    }

    @Override
    public GVRAnchor createAnchor(float[] pose, GVRSceneObject sceneObject) {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        return mSession.createAnchor(pose, sceneObject);
    }

    @Override
    public void updateAnchorPose(GVRAnchor anchor, float[] pose) {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        mSession.updateAnchorPose(anchor, pose);
    }

    @Override
    public void removeAnchor(GVRAnchor anchor) {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        mSession.removeAnchor(anchor);
    }

    @Override
    public void hostAnchor(GVRAnchor anchor, ICloudAnchorListener listener) {
        mSession.hostAnchor(anchor, listener);
    }

    @Override
    public void resolveCloudAnchor(String anchorId, ICloudAnchorListener listener) {
        mSession.resolveCloudAnchor(anchorId, listener);
    }

    @Override
    public void setEnableCloudAnchor(boolean enableCloudAnchor) {
        mSession.setEnableCloudAnchor(enableCloudAnchor);
    }

    @Override
    public GVRHitResult hitTest(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        return mSession.hitTest(sceneObj, collision);
    }

    @Override
    public GVRLightEstimate getLightEstimate() {
        if (mState == SessionState.ON_PAUSE) {
            throw new UnsupportedOperationException("Session is not resumed");
        }
        return mSession.getLightEstimate();
    }

    @Override
    public void setAugmentedImage(Bitmap image) {
        mSession.setAugmentedImage(image);
    }

    @Override
    public void setAugmentedImages(ArrayList<Bitmap> imagesList) {
        mSession.setAugmentedImages(imagesList);
    }

    @Override
    public ArrayList<GVRAugmentedImage> getAllAugmentedImages() {
        return mSession.getAllAugmentedImages();
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
        public void onControllerEvent(Vector3f position, Quaternionf orientation, PointF touchpadPoint, boolean touched, Vector3f angularAcceleration, Vector3f angularVelocity) {}

        @Override
        public void dispatchKeyEvent(KeyEvent keyEvent) {}
    }


    private enum SessionState {
        ON_RESUME,
        ON_PAUSE
    };
}
