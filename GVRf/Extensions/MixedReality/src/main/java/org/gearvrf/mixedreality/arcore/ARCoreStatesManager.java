package org.gearvrf.mixedreality.arcore;

import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import com.google.ar.core.Anchor;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;

import org.gearvrf.GVRContext;
import org.gearvrf.GVRDrawFrameListener;
import org.gearvrf.GVRExternalTexture;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRPerspectiveCamera;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.mixedreality.GVRAnchor;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;


public class ARCoreStatesManager {
    public static String TAG = "AR_GVRF";/*ARCoreStatesManager.class.getSimpleName();*/
    private final ARCoreStatesManager.ARCoreState mStartState;
    private final ARCoreStatesManager.ARCoreState mResumeState;
    private final ARCoreStatesManager.ARCoreState mTrackingState;

    public ARCoreStatesManager(GVRContext gvrContext, Session arSession,  ArrayList<ARCoreAnchor> anchors) {
        mStartState = new ARCoreStatesManager.OnStartState(gvrContext, arSession, gvrContext.getMainScene());
        mResumeState = new ARCoreStatesManager.OnResumeState(gvrContext, arSession, gvrContext.getMainScene());
        mTrackingState = new ARCoreStatesManager.OnTrackingState(gvrContext, arSession,
                gvrContext.getMainScene(), anchors);

            /* [start] -> [resume] -> ([tracking] | [finish]) -> [resume] */

        mStartState.setNextState(mResumeState);
        mResumeState.setNextState(mTrackingState);
        mTrackingState.setNextState(mResumeState);
    }

    public void start() {
        mStartState.onEnter();
    }

    private static abstract class ARCoreState implements GVRDrawFrameListener {
        protected  final GVRContext mGvrContext;
        protected final Session mARSession;
        protected final GVRScene mScene;

        protected ARCoreState mNextState;
        protected static PassThroughObject mPassThroughObject = null;
        protected static Frame mLastFrame = null;
        protected static GVRFrameManager mFrameManager = null;


        ARCoreState(GVRContext gvrContext, Session arSession, GVRScene scene) {
            mGvrContext = gvrContext;
            mARSession = arSession;
            mScene = scene;

            if (mFrameManager == null) {
                mFrameManager = new GVRFrameManager(mScene);
                mGvrContext.registerDrawFrameListener(mFrameManager);
            }
        }

        public void setNextState(ARCoreState nextState) {
            mNextState = nextState;
        }

        public void onEnter() {
            mGvrContext.registerDrawFrameListener(this);
        }

        public void onExit() {
            mGvrContext.unregisterDrawFrameListener(this);

            if (mNextState != null) {
                mNextState.onEnter();
            }
        }

        public abstract void onRun();

        @Override
        public void onDrawFrame(float v) {
            onRun();
        }
    }

    private static class PassThroughObject extends GVRSceneObject {

        public PassThroughObject(GVRContext gvrContext, GVRTexture texture,
                                 float width, float height, float distance) {
            super(gvrContext, createMesh(gvrContext, width, height, distance));

            GVRMaterial material = new GVRMaterial(gvrContext, GVRMaterial.GVRShaderType.OES.ID);
            material.setMainTexture(texture);

            getRenderData().setMaterial(material);
            getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.BACKGROUND);
            getRenderData().setDepthTest(false);
        }

        private static GVRMesh createMesh(GVRContext gvrContext,
                                          float width, float height, float distance)
        {
            final float[] vertices = {
                    width * -0.5f, height * 0.5f, distance,
                    width * -0.5f, height * -0.5f, distance,
                    width * 0.5f, height * 0.5f, distance,
                    width * 0.5f, height * -0.5f, distance};

            final float[] normals = {
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f};

            final float[] texCoords = {0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f};

            char[] triangles = { 0, 1, 2, 1, 3, 2 };

            GVRMesh mesh = new GVRMesh(gvrContext,
                    "float3 a_position float2 a_texcoord");

            mesh.setVertices(vertices);
            mesh.setTexCoords(texCoords);
            mesh.setIndices(triangles);

            return mesh;
        }
    }

    private static class OnStartState extends ARCoreState {
        OnStartState(GVRContext gvrContext, Session arSession, GVRScene scene) {
            super(gvrContext, arSession, scene);
        }

        @Override
        public void onEnter() {
            android.util.Log.d(TAG, "OnStartState enter: Creating pass through quad...");

            super.onEnter();
        }

        @Override
        public void onExit() {
            android.util.Log.d(TAG, "OnStartState exit: Pass through quad done!");

            super.onExit();
        }

        @Override
        public void onRun() {
            GVRTransform t = mScene.getMainCameraRig().getHeadTransform();

            if (t.getRotationX() != 0.0f || t.getRotationY() != 0.0f
                    || t.getRotationY() != 0.0f || t.getRotationW() != 1.0f) {

                /* GVRf' camera updated so go ahead */

                if (mPassThroughObject == null) {
                    createPassThroughObject();
                }

                onExit();
            }
        }

        private void createPassThroughObject() {
            GVRTexture passThroughTexture = createPassThroughTexture(mGvrContext);

            mARSession.setCameraTextureName(passThroughTexture.getId());

            GVRPerspectiveCamera gvrCenterCamera = null;//mScene.getMainCameraRig().getCenterCamera();
            Camera arCamera = mARSession.update().getCamera();

            Vector3f sensor = configCameraSensor(arCamera, gvrCenterCamera);

            mPassThroughObject = new PassThroughObject(mGvrContext,
                    passThroughTexture, sensor.x, sensor.y, -sensor.z);

            mScene.addSceneObject(mPassThroughObject);
        }

        private static GVRTexture createPassThroughTexture(GVRContext gvrContext) {
            return new GVRExternalTexture(gvrContext);
        }

        private static Vector3f configCameraSensor(Camera arCamera, GVRPerspectiveCamera gvrCenterCamera) {
            float near = 0.1f; //gvrCenterCamera.getNearClippingDistance();
            float far = 1000.0f; //gvrCenterCamera.getFarClippingDistance();

            // Get phones' cam projection matrix.
            float[] m = new float[16];
            arCamera.getProjectionMatrix(m, 0, near, far);
            Matrix4f projmtx = new Matrix4f();
            projmtx.set(m);

            float aspectRatio = projmtx.m11()/projmtx.m00();
            float arCamFOV = projmtx.perspectiveFov();

            //float widthY = 2 * (float) Math.tan(arCamFOV / 2);
            //float widthX = widthY * aspectRatio;

            float quadDistance = 900.0f;
            float quadHeight = new Float(2 * quadDistance * Math.tan(arCamFOV / 2));
            float quadWidth = quadHeight * aspectRatio;
            android.util.Log.d(TAG, "QUAD HEIGHT:" + quadHeight + "  WIDTH: " + quadWidth);

            return new Vector3f(quadWidth, quadHeight, quadDistance);
        }
    }

    private static class OnResumeState extends ARCoreState {
        OnResumeState(GVRContext gvrContext, Session arSession, GVRScene scene) {
            super(gvrContext, arSession, scene);
        }

        @Override
        public void onEnter() {
            android.util.Log.d(TAG, "OnResumeState enter");

            super.onEnter();
        }

        @Override
        public void onExit() {
            android.util.Log.d(TAG, "OnResumeState exit");

            super.onExit();
        }

        @Override
        public void onRun() {
            final Frame arFrame = mARSession.update();

            mPassThroughObject.getTransform().setModelMatrix(mScene.getMainCameraRig().getHeadTransform().getModelMatrix());

            if (arFrame == mLastFrame)
                return;

            mLastFrame = arFrame;

            Camera arCamera = arFrame.getCamera();

            if (arCamera.getTrackingState() != TrackingState.PAUSED) {
                if (arCamera.getTrackingState() == TrackingState.STOPPED) {
                    mNextState = null;
                    android.util.Log.d(TAG, "AR session stopped!");
                }

                onExit();
            }
        }
    }

    private static class OnTrackingState extends ARCoreState {
        private ArrayList<ARCoreAnchor> mAnchors = new ArrayList<>();

        OnTrackingState(GVRContext gvrContext, Session arSession, GVRScene scene,
                        ArrayList<ARCoreAnchor> anchors) {
            super(gvrContext, arSession, scene);
            mAnchors = anchors;
        }

        @Override
        public void onEnter() {
            android.util.Log.d(TAG, "OnTrackingState enter: Tracking ...");

            super.onEnter();
        }

        @Override
        public void onExit() {
            android.util.Log.d(TAG, "OnTrackingState exit: Tracking paused!");

            super.onExit();
        }

        @Override
        public void onRun() {
            final Frame arFrame = mARSession.update();

            if (arFrame == mLastFrame)
                return;
            mLastFrame = arFrame;

            Camera arCamera = arFrame.getCamera();

            // If not tracking, don't draw 3d objects.
            if (arCamera.getTrackingState() != TrackingState.TRACKING) {
                onExit();
                return;
            }

            mFrameManager.updatePassThroughObject(arFrame.getTimestamp(), mPassThroughObject.getTransform());

            float[] viewmtx = new float[16];
            arCamera.getViewMatrix(viewmtx, 0);



            for (ARCoreAnchor anchor : mAnchors) {
                if (anchor.getAnchorAR().getTrackingState() == TrackingState.TRACKING) {
                    updateGVRAnchorObject(arFrame.getTimestamp(), viewmtx, anchor);
                }
            }


        }

        private void updateGVRAnchorObject(long nanotime, float[] viewmtx, ARCoreAnchor anchor){
            float[] modelmtx = new float[16];
            float[] modelview = new float[16];


            anchor.getAnchorAR().getPose().toMatrix(modelmtx, 0);

            Matrix.multiplyMM(modelview, 0, viewmtx, 0, modelmtx, 0);

            mFrameManager.updateVRObject(nanotime, modelview, anchor);
        }

    }

    private static class GVRFrameManager implements GVRDrawFrameListener {
        private final GVRFramePosePool mVRFramePosePool = new GVRFramePosePool();
        private final GVRScene mScene;

        public GVRFrameManager(GVRScene scene) {
            mScene = scene;
        }

        public void updatePassThroughObject(long nanotime, GVRTransform trans) {
            GVRFramePose framePose = mVRFramePosePool.get(nanotime);

            if (framePose != null) {
                trans.setModelMatrix(framePose.matrix);
            }
        }

        public void updateVRObject(long nanotime, float[] modelview, GVRAnchor gvrAnchor) {
            GVRFramePose framePose = mVRFramePosePool.get(nanotime);

            if (framePose != null) {
                float[] transm = new float[16];
                Matrix.multiplyMM(transm, 0, framePose.matrix, 0, modelview, 0);
                gvrAnchor.setModelMatrix(transm);
            }
        }

        @Override
        public void onDrawFrame(float v) {
            mVRFramePosePool.add(new GVRFramePose(SystemClock.elapsedRealtimeNanos(),
                    mScene.getMainCameraRig().getHeadTransform()));
        }
    }

    private static class GVRFramePosePool {
        private static final int CAM_POOL_SIZE = 120;
        private static final int FRAME_NANO_SIZE = (1000 * 1000000)/60; // 16nanotime
        private final GVRFramePose[] mPosePool = new GVRFramePose[CAM_POOL_SIZE];

        private static int nanotimeToIndex(long nanotime) {

            return (int) (nanotime / FRAME_NANO_SIZE) % CAM_POOL_SIZE;
        }

        public void add(GVRFramePose pose) {
            int i = nanotimeToIndex(pose.nanotime);
            //Log.d(TAG, "i " + i);
            mPosePool[i] = pose;
        }

        public GVRFramePose get(long nanotime) {
            int k = nanotimeToIndex(nanotime);
            GVRFramePose framePose;

            for (int i = 0; i < CAM_POOL_SIZE; i++) {
                framePose = mPosePool[ (k + i) % CAM_POOL_SIZE];

                if (framePose != null
                        && framePose.nanotime - nanotime > -FRAME_NANO_SIZE) {
                    return framePose;
                }
            }

            return null;
        }
    }

    private static class GVRFramePose {
        public final long nanotime;
        public final float[] matrix;

        public GVRFramePose() {
            nanotime = SystemClock.elapsedRealtimeNanos();
            matrix = new float[]{
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0,
                    0, 0, 0, 0};
        }

        public GVRFramePose(long nanotime, Pose arPose) {
            this.nanotime = nanotime;
            this.matrix = new float[16];

            arPose.toMatrix(matrix, 0);
        }

        public GVRFramePose(long nanotime, GVRTransform vrTransform) {
            this.nanotime = nanotime;
            this.matrix = vrTransform.getModelMatrix();
        }
    }

}
