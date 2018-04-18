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

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRCameraRig;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVREventListeners;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.mixedreality.R;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ARCoreStatesManager {
    private static String TAG = "GVR_ARCORE";

    private static int MAX_VIRTUAL_OBJ = 20;
    private static float PASSTHROUGH_DISTANCE = 100.0f;
    private static float AR2VR_SCALE = 100;

    private GVRActivity mActivity;
    private Vector3f mDisplayGeometry;

    private GVRScene mVRScene;
    private Session mARCoreSession;
    private GVRSceneObject mARPassThroughObject;
    private Frame mLastARFrame;
    private ARCoreHandler mARCoreHandler;
    private GVRSceneObject mCursor;
    private GVRCursorController mCursorController;

    /* From AR to GVR space matrices */
    private float[] mGVRModelMatrix = new float[16];
    private float[] mARViewMatrix = new float[16];
    private float[] mGVRCamMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];


    // Pool of virtual objects
    private List<VirtualObject>  mVirtualObjects;
    private int mVirtObjCount;

    // Map to track planes
    private Map<Plane, ARCorePlane> planesMap = new HashMap<>();

    public ARCoreStatesManager(GVRActivity activity, final GVRContext gvrContext, Session session) {
        mActivity = activity;
        mVRScene = gvrContext.getMainScene();
        mARCoreSession = session;
        mARPassThroughObject = null;
        mLastARFrame = null;

        if (mARCoreSession == null) {
            Log.e(TAG, "Invalid ARCore session!");
            return;
        }

        initVirtualModels();

        mActivity.getGVRContext().registerDrawFrameListener(new GVRDrawFrameListener() {
            @Override
            public void onDrawFrame(float v) {
                mActivity.getGVRContext().unregisterDrawFrameListener(this);
                try {
                    onInitARCoreSession(gvrContext);
                     /* Cursor controller */
                     initCursorController(gvrContext);
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void onInitARCoreSession(GVRContext gvrContext) throws CameraNotAvailableException {
        GVRTexture passThroughTexture = new GVRExternalTexture(gvrContext);

        mARCoreSession.setCameraTextureName(passThroughTexture.getId());

        // FIXME: detect VR screen aspect ratio.
        mARCoreSession.setDisplayGeometry(Surface.ROTATION_90 , 160, 90);
        mLastARFrame = mARCoreSession.update();
        mDisplayGeometry = configDisplayGeometry(mLastARFrame.getCamera());

        mARCoreSession.setDisplayGeometry(Surface.ROTATION_90 ,
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

    private void initVirtualModels() {
            mVirtualObjects = new ArrayList<>();
            mVirtObjCount = 0;
    }

    private void initCursorController(GVRContext gvrContext) {
        mVRScene.getEventReceiver().addListener(mARCoreHandler);
        GVRInputManager inputManager = gvrContext.getInputManager();
        mCursor = new GVRSceneObject(gvrContext,
                gvrContext.createQuad(0.2f * PASSTHROUGH_DISTANCE,
                        0.2f * PASSTHROUGH_DISTANCE),
                gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                        R.raw.cursor)));
        mCursor.getRenderData().setDepthTest(false);
        mCursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener()
        {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController)
            {
                if (oldController != null)
                {
                    oldController.removePickEventListener(mARCoreHandler);
                }
                mCursorController = newController;
                newController.addPickEventListener(mARCoreHandler);
                newController.setCursor(mCursor);
                newController.setCursorDepth(-PASSTHROUGH_DISTANCE);
                newController.setCursorControl(GVRCursorController.CursorControl.CURSOR_CONSTANT_DEPTH);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });
    }

    public void setARCoreSession(Session session) {
        mARCoreSession = session;
    }

    public class ARCoreHandler extends GVREventListeners.TouchEvents implements GVRDrawFrameListener {
        private MotionEvent mCursorOverEvent = null;
        private MotionEvent mSingleTapEvent = null;

        @Override
        public void onDrawFrame(float v) {
            Frame arFrame = null;
            try {
                arFrame = mARCoreSession.update();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
                mActivity.getGVRContext().unregisterDrawFrameListener(this);
                return;
            }

            Camera arCamera = arFrame.getCamera();

            if (arFrame.getTimestamp() == mLastARFrame.getTimestamp()) {
                return;
            }

            if (arCamera.getTrackingState() != TrackingState.TRACKING) {
                // Put passthrough object in from of current VR cam at paused states.
                updateAR2GVRMatrices(arCamera, mVRScene.getMainCameraRig());
                updatePassThroughObject(mARPassThroughObject);

                if (mVirtObjCount != 0) {
                    disableVirtualObjects();
                }
                return;
            }

            // Update current AR cam's view matrix.
            arCamera.getViewMatrix(mARViewMatrix, 0);

            // Update passthrough object with last VR cam matrix
            updatePassThroughObject(mARPassThroughObject);

            List<HitResult> hitResult = null;

            if (mSingleTapEvent != null) {
                hitResult = arFrame.hitTest(mSingleTapEvent);
                addVirtualObject(createAnchor(hitResult));
                mSingleTapEvent.recycle();
                mSingleTapEvent = null;
            } else if (mCursorOverEvent != null) {
                // update planes only if cursor is inside arcore quad
            } else {
                // disable planes otherwise
            }

            updatePlanes();
            updateVirtualObjects();

            mLastARFrame = arFrame;

            // Update current VR cam's matrix to next update of passtrhough and virtual objects.
            // AR/30fps vs VR/60fps
            mGVRCamMatrix = mVRScene.getMainCameraRig().getHeadTransform().getModelMatrix();
        }

        @Override
        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onInside(sceneObj, collision);

            if (sceneObj != mARPassThroughObject)
                return;

            if (mCursorOverEvent != null)
                mCursorOverEvent.recycle();

            mCursorOverEvent = convertToTouchEvent(collision.getHitLocation());
        }

        @Override
        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onExit(sceneObj, collision);

            if (sceneObj != mARPassThroughObject)
                return;

            if (mCursorOverEvent != null) {
                mCursorOverEvent.recycle();
                mCursorOverEvent = null;
            }
        }

        @Override
        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
            super.onTouchEnd(sceneObj, collision);

            if (sceneObj != mARPassThroughObject)
                return;

            if (mSingleTapEvent != null)
                mSingleTapEvent.recycle();

            mSingleTapEvent = convertToTouchEvent(collision.getHitLocation());
        }

        public MotionEvent convertToTouchEvent(float[] hitPoint) {
            final float hitX = hitPoint[0] + 0.5f * mDisplayGeometry.x;
            final float hitY = mDisplayGeometry.y - hitPoint[1] - 0.5f * mDisplayGeometry.y;

            return MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, hitX, hitY, 0);
        }
    }

    private void updatePlanes() {
        for (Plane plane: mARCoreSession.getAllTrackables(Plane.class)) {
            if (plane.getTrackingState() != TrackingState.TRACKING || plane.getSubsumedBy() != null) {
                ARCorePlane arCorePlane = planesMap.get(plane);
                if (arCorePlane != null) {
                    arCorePlane.setEnable(false);
                }
                continue;
            }

            ARCorePlane arCorePlane = planesMap.get(plane);
            if (arCorePlane == null) {
                arCorePlane = new ARCorePlane(mActivity.getGVRContext(), planesMap.size());
                mVRScene.addSceneObject(arCorePlane);
                planesMap.put(plane, arCorePlane);
            }
            arCorePlane.setEnable(true);
            arCorePlane.update(plane, mARViewMatrix, mGVRCamMatrix, AR2VR_SCALE);

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

        /*
        float widthY = 2 * (float) Math.tan(arCamFOV * 0.5f);
        float widthX = widthY * aspectRatio;
        */

        float quadDistance = PASSTHROUGH_DISTANCE;
        float quadHeight = new Float(2 * quadDistance * Math.tan(arCamFOV * 0.5f));
        float quadWidth = quadHeight * aspectRatio;

        Log.d(TAG, "ARCore configured to: passthrough[w: "
                + quadWidth + ", h: " + quadHeight +", z: " + quadDistance
                + "], cam fov: " +Math.toDegrees(arCamFOV) + ", aspect ratio: " + aspectRatio);

        return new Vector3f(quadWidth, quadHeight, -PASSTHROUGH_DISTANCE);
    }

    private void addVirtualObject(Anchor anchor) {
        if (anchor == null)
            return;

        mVirtObjCount++;

        VirtualObject obj = mVirtualObjects.get(mVirtObjCount % mVirtualObjects.size());
        obj.setARAnchor(anchor);
        obj.setName("id: " + mVirtObjCount);

        Log.d(TAG, "New virtual object " + obj.getName());
    }

    private void updateVirtualObjects() {
        for (VirtualObject obj: mVirtualObjects) {
            obj.update();
        }
    }

    private void disableVirtualObjects() {
        for (VirtualObject obj: mVirtualObjects) {
            obj.setARAnchor(null);
        }

        mVirtObjCount = 0;
    }

    private Anchor createAnchor(List<HitResult> hitResult) {
        for (HitResult hit : hitResult) {
            // Check if any plane was hit, and if it was hit inside the plane polygon
            Trackable trackable = hit.getTrackable();
            // Creates an anchor if a plane or an oriented point was hit.
            if ((trackable instanceof Plane
                    && ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))
                    && ((Plane) trackable).getSubsumedBy() == null) {
                return hit.createAnchor();
            }
        }

        return null;
    }

    private class PoseObject extends GVRSceneObject {
        protected float[] mPoseMatrix = new float[16];
        protected GVRSceneObject mSceneObject = null;

        public PoseObject(GVRContext gvrContext, GVRSceneObject sceneObject) {
            super(gvrContext);

            mSceneObject = sceneObject;
            addChildObject(sceneObject);
        }

        public void update(Pose pose) {
            pose.toMatrix(mPoseMatrix, 0);

            ar2gvr();
        }

        public float[] getMatrix() {
            return mPoseMatrix;
        }

        private void ar2gvr() {
            Matrix.multiplyMM(mModelViewMatrix, 0, mARViewMatrix, 0, mPoseMatrix, 0);
            Matrix.multiplyMM(mGVRModelMatrix, 0, mGVRCamMatrix, 0, mModelViewMatrix, 0);

            Matrix.scaleM(mGVRModelMatrix, 0, AR2VR_SCALE, AR2VR_SCALE, AR2VR_SCALE);
            mGVRModelMatrix[12] = mGVRModelMatrix[12] * AR2VR_SCALE;
            mGVRModelMatrix[13] = mGVRModelMatrix[13] * AR2VR_SCALE;
            mGVRModelMatrix[14] = mGVRModelMatrix[14] * AR2VR_SCALE;

            getTransform().setModelMatrix(mGVRModelMatrix);
        }
    }

    private class VirtualObject extends PoseObject {
        private Anchor mARAnchor;

        public VirtualObject(GVRContext gvrContext, GVRSceneObject sceneObject) {
            super(gvrContext, sceneObject);

            setARAnchor(null);
        }

        public void update(Anchor anchor) {
            mARAnchor = anchor;

            update();
        }

        public void update() {
            if (mARAnchor == null)
                return;

            if (mARAnchor.getTrackingState() != TrackingState.TRACKING) {
                mARAnchor.detach();
                mARAnchor = null;
                setEnable(false);
            } else {
                super.update(mARAnchor.getPose());
                setEnable(true);
            }
        }

        public void setARAnchor(Anchor anchor) {
            if (mARAnchor != null) {
                mARAnchor.detach();
            }
            mARAnchor = anchor;
            setEnable(false);
        }

        public Anchor getARAnchor() {
            return mARAnchor;
        }
    }
}
