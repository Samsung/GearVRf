/* 
 * Single Player Game View Manager
 */

package org.gearvrf.ARVuforia;

import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.Vector;

import javax.security.auth.login.LoginException;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRBitmapTexture;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderTexture;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;
import org.gearvrf.animation.GVRAnimationEngine;
import org.gearvrf.utility.Log;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;

public class AppViewManager extends GVRScript {

    private GVRScene mMainScene = null;
    private static final String LOGTAG = "SolarViewManager";
    private GVRContext mGVRContext = null;
    private SampleApplicationSession vuforiaAppSession;
    private Renderer mRenderer;
    boolean mIsActive = false;
    GVRRenderTexture cameraTex;
    GVRTexture teaoptTex = null;
    GVRMesh camMesh = null, teapotMesh = null;
    GVRSceneObject cameraObj, teapotObj;
    public float[] matMVP = new float[16];
    float[] modelViewProjection = new float[16];
    float[] modelViewMatrix = new float[16];
    static final int VUFORIA_CAMERA_WIDTH = 1280;
    static final int VUFORIA_CAMERA_HEIGHT = 720;
//    FPSCounter fpscounter;
    @SuppressWarnings("unused")
    private GVRSceneObject asyncSceneObject(GVRContext context,
            GVRAndroidResource meshResource, String textureName)
            throws IOException {
        return new GVRSceneObject(context, meshResource,
                new GVRAndroidResource(context, textureName));
    }

    public AppViewManager(AppActivity activity,
            SampleApplicationSession session) {
        vuforiaAppSession = session;
    }

    @Override
    public void onInit(GVRContext gvrContext) throws IOException {
        mGVRContext = gvrContext;
        initRendering();
        Log.i("new","entered onInit");
        mMainScene = gvrContext.getNextMainScene(new Runnable() {

            @Override
            public void run() {
            }
        });

        mMainScene.getMainCameraRig().getLeftCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mMainScene.getMainCameraRig().getRightCamera()
                .setBackgroundColor(0.0f, 0.0f, 0.0f, 1.0f);
        mMainScene.getMainCameraRig().getTransform()
                .setPosition(0.0f, 0.0f, 0.0f);

//        cameraTex = new GVRRenderTexture(mGVRContext, 2560, 1440);
        cameraTex = new GVRRenderTexture(gvrContext,
                VUFORIA_CAMERA_WIDTH, VUFORIA_CAMERA_HEIGHT);

        try {
            camMesh = mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext,
                    "screen.obj"));
            float[] texCoords = { 0.0f, 0.0f, 0.0f, 0.70f, 0.62f, 0.0f, 0.62f, 0.7f };
//            float[] texCoords = { 0.0f, 0.0f, 0.0f,1.0f, 1.0f, 1.0f, 1.0f, 0.0f };
            camMesh.setTexCoords(texCoords);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        cameraObj = new GVRSceneObject(mGVRContext, camMesh, cameraTex);
        cameraObj.getRenderData().getMaterial().setAR(1);
        cameraObj.getRenderData().getMaterial().setAROffsets();
        mRenderer.setVideoBackgroundTextureID(
        		cameraTex.getId());
        
        mMainScene.getMainCameraRig().addChildObject(cameraObj);
        cameraObj.getTransform().rotateByAxis(90, 0, 0, -1);
        cameraObj.getTransform().setScale(9.2f, 9.2f, 1.0f);

        GVRAndroidResource meshResource_board = new GVRAndroidResource(
                gvrContext, "teapot.obj");
        teaoptTex = mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext,
                "teapot_tex1.jpg"));
        teapotMesh = mGVRContext.loadMesh(meshResource_board);
        teapotObj = new GVRSceneObject(mGVRContext, teapotMesh, teaoptTex); // AR object
        teapotObj.getRenderData().getMaterial().setAR(2);
        teapotObj.getRenderData().getMaterial().setAROffsets();
        
//        fpscounter = new FPSCounter();
    }

    private void initRendering() {
//        mTeapot = new Teapot();
        mRenderer = Renderer.getInstance();
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    private void printUserData(Trackable trackable) {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data \"" + userData + "\"");
    }

    @Override
    public void onStep() {
        Log.i("new","entered onStep");
//        fpscounter.logFrame();
        if (!mIsActive)
            return;
//        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, cameraTex.getFBOId());
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        renderFrame();
    }

    // The render function.
    private void renderFrame() {
        Log.i("new","entered renderFrame");
        mMainScene.getMainCameraRig().removeChildObject(teapotObj);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        State state = mRenderer.begin();
//        mRenderer.drawVideoBackground();
        mRenderer.bindVideoBackground(3);

        GLES20.glDisable(GLES20.GL_BLEND);
        GLES20.glDisable(GLES20.GL_CULL_FACE);

        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            modelViewMatrix = modelViewMatrix_Vuforia.getData();
            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0
                    : 1;
            textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
                    : textureIndex;

            // deal with the modelview and projection matrices
            Matrix.rotateM(modelViewMatrix, 0, 90, 1.0f, 0.0f, 0.0f);
            Matrix.scaleM(modelViewMatrix, 0, 120.0f, 120.0f, 120.0f);

            
            Matrix.multiplyMM(modelViewProjection, 0, vuforiaAppSession
                    .getProjectionMatrix().getData(), 0, modelViewMatrix, 0);
            Log.i("track", "TRACKING......");
//            float temp[]=new float[16];
//            float temp1[]=new float[16];
//            mGVRContext.getMainScene().getMainCameraRig().getRightCamera()
//            Matrix.multiplyMM(temp, 0, temp1, 0, modelViewMatrix, 0);
            
//            teapotObj.setMVMatrix(modelViewMatrix);
//            teapotObj.setMVPMatrix(modelViewProjection);
//            teapotObj.getRenderData().getMaterial().setMVmat(modelViewMatrix);
            teapotObj.getRenderData().getMaterial().setMVPmat(modelViewProjection);
//            teapotObj.getTransform().setModelMatrix(modelViewProjection);
            mMainScene.getMainCameraRig().addChildObject(teapotObj);
//            SampleUtils.checkGLError("Render Frame");
        }
        mRenderer.end();
    }
}
