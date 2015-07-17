package org.gearvrf.vuforiasample;

import org.gearvrf.FutureWrapper;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData.GVRRenderingOrder;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;

import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;

public class VuforiaSampleScript extends GVRScript {

    private static final float CUBE_WIDTH = 20.0f;
    private static final float SCALE_FACTOR = 5.0f;
    private static final float BACKGROUND_OPACITY = 0.1f;
    private GVRContext mGVRContext = null;
    private GVRSceneObject bunny = null;

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;

        GVRScene scene = mGVRContext.getMainScene();

        scene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        scene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        FutureWrapper<GVRMesh> futureMesh = new FutureWrapper<GVRMesh>(
                gvrContext.createQuad(CUBE_WIDTH, CUBE_WIDTH));

        GVRSceneObject frontFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.front)));
        frontFace.setName("front");
        scene.addSceneObject(frontFace);
        frontFace.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);
        frontFace.getRenderData().getMaterial().setOpacity(BACKGROUND_OPACITY);
        frontFace.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT);

        GVRSceneObject backFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.back)));
        backFace.setName("back");
        scene.addSceneObject(backFace);
        backFace.getTransform().setPosition(0.0f, 0.0f, CUBE_WIDTH * 0.5f);
        backFace.getTransform().rotateByAxis(180.0f, 0.0f, 1.0f, 0.0f);
        backFace.getRenderData().getMaterial().setOpacity(BACKGROUND_OPACITY);
        backFace.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT);

        GVRSceneObject leftFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.left)));
        leftFace.setName("left");
        scene.addSceneObject(leftFace);
        leftFace.getTransform().setPosition(-CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        leftFace.getTransform().rotateByAxis(90.0f, 0.0f, 1.0f, 0.0f);
        leftFace.getRenderData().getMaterial().setOpacity(BACKGROUND_OPACITY);
        leftFace.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT);

        GVRSceneObject rightFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.right)));
        rightFace.setName("right");
        scene.addSceneObject(rightFace);
        rightFace.getTransform().setPosition(CUBE_WIDTH * 0.5f, 0.0f, 0.0f);
        rightFace.getTransform().rotateByAxis(-90.0f, 0.0f, 1.0f, 0.0f);
        rightFace.getRenderData().getMaterial().setOpacity(BACKGROUND_OPACITY);
        rightFace.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT);

        GVRSceneObject topFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.top)));
        topFace.setName("top");
        scene.addSceneObject(topFace);
        topFace.getTransform().setPosition(0.0f, CUBE_WIDTH * 0.5f, 0.0f);
        topFace.getTransform().rotateByAxis(90.0f, 1.0f, 0.0f, 0.0f);
        topFace.getRenderData().getMaterial().setOpacity(BACKGROUND_OPACITY);
        topFace.getRenderData()
                .setRenderingOrder(GVRRenderingOrder.TRANSPARENT);

        GVRSceneObject bottomFace = new GVRSceneObject(gvrContext, futureMesh,
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.bottom)));
        bottomFace.setName("bottom");
        scene.addSceneObject(bottomFace);
        bottomFace.getTransform().setPosition(0.0f, -CUBE_WIDTH * 0.5f, 0.0f);
        bottomFace.getTransform().rotateByAxis(-90.0f, 1.0f, 0.0f, 0.0f);
        bottomFace.getRenderData().getMaterial().setOpacity(BACKGROUND_OPACITY);
        bottomFace.getRenderData().setRenderingOrder(
                GVRRenderingOrder.TRANSPARENT);

        bunny = new GVRSceneObject(gvrContext,
                gvrContext.loadFutureMesh(new GVRAndroidResource(mGVRContext,
                        R.raw.bunny)),
                gvrContext.loadFutureTexture(new GVRAndroidResource(
                        mGVRContext, R.drawable.texture)));
        scene.addSceneObject(bunny);
        bunny.getTransform().setScale(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);
        bunny.getTransform().setPosition(0.0f, 0.0f, -CUBE_WIDTH * 0.5f);

        for (GVRSceneObject so : mGVRContext.getMainScene()
                .getWholeSceneObjects()) {
            Log.v("", "scene object name : " + so.getName());
        }

        mVuforiaMVMatrix = new float[16];
        mConvertedMVMatrix = new float[16];
        mGVRMVMatrix = new float[16];
        mTotalMVMatrix = new float[16];
    }

    private float[] convertMatrix = { 1f, 0f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 0f,
            -1f, 0f, 0f, 0f, 0f, 1f };

    @Override
    public void onStep() {
        FPSCounter.tick();

        if (VuforiaSampleActivity.isVuforiaActive) {

            vuforiaRederer = Renderer.getInstance();

            State state = vuforiaRederer.begin();

            // did we find any trackables this frame?
            for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
                TrackableResult result = state.getTrackableResult(tIdx);
                Trackable trackable = result.getTrackable();
                if (trackable.getId() == 1) {
                    Matrix44F modelViewMatrix_Vuforia = Tool
                            .convertPose2GLMatrix(result.getPose());
                    mVuforiaMVMatrix = modelViewMatrix_Vuforia.getData();

                    Matrix.multiplyMM(mConvertedMVMatrix, 0, convertMatrix, 0,
                            mVuforiaMVMatrix, 0);

                    float scaleFactor = ((ImageTarget) trackable).getSize()
                            .getData()[0];
                    Matrix.scaleM(mConvertedMVMatrix, 0, scaleFactor,
                            scaleFactor, scaleFactor);

                    mGVRMVMatrix = mGVRContext.getMainScene()
                            .getMainCameraRig().getOwnerObject().getTransform()
                            .getModelMatrix();

                    Matrix.multiplyMM(mTotalMVMatrix, 0, mGVRMVMatrix, 0,
                            mConvertedMVMatrix, 0);
                    bunny.getTransform().setModelMatrix(mTotalMVMatrix);
                    break;
                }
            }
        }
    }

    private Renderer vuforiaRederer;
    private float[] mVuforiaMVMatrix;
    private float[] mConvertedMVMatrix;
    private float[] mGVRMVMatrix;
    private float[] mTotalMVMatrix;
}
