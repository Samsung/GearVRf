package org.gearvrf.vuforiasample;

import java.io.IOException;

import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.ImageTarget;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;

import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRRenderTexture;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRScript;
import org.gearvrf.GVRTexture;

public class VuforiaSampleScript extends GVRScript {

    private static final String TAG = "gvr-vuforia";

    private GVRContext mGVRContext = null;
    private GVRSceneObject teapot = null;
    private GVRSceneObject mPassThroughObject = null;

    static final int VUFORIA_CAMERA_WIDTH = 1280;
    static final int VUFORIA_CAMERA_HEIGHT = 720;
    
    private volatile boolean init = false;

    private GVRScene mMainScene;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mMainScene = gvrContext.getMainScene();

        createCameraPassThrough();

        createTeaPotObject();

        mVuforiaMVMatrix = new float[16];
        mConvertedMVMatrix = new float[16];
        mGVRMVMatrix = new float[16];
        mTotalMVMatrix = new float[16];
        
        init = true;
    }
    
    public boolean isInit() {
        return init;
    }

    private void createCameraPassThrough() {
        mPassThroughObject = new GVRSceneObject(mGVRContext, 2.0f, 1.0f);

        mPassThroughObject.getTransform().setPosition(0.0f, 0.0f, -1000.0f);
        mPassThroughObject.getTransform().setScaleX(1000f);
        mPassThroughObject.getTransform().setScaleY(1000f);

        GVRTexture passThroughTexture;

        passThroughTexture = new GVRRenderTexture(mGVRContext,
                VUFORIA_CAMERA_WIDTH, VUFORIA_CAMERA_HEIGHT);

        GVRRenderData renderData = mPassThroughObject.getRenderData();
        GVRMaterial material = new GVRMaterial(mGVRContext);
        renderData.setMaterial(material);
        material.setMainTexture(passThroughTexture);
        material.setShaderType(GVRShaderType.Texture.ID);

        // the following texture coordinate values are determined empirically
        // and do not match what we expect them to be. but still they work :)
        float[] texCoords = { 0.0f, 0.0f, 0.0f, 0.70f, 0.62f, 0.0f, 0.62f, 0.7f };
        GVRMesh mesh = renderData.getMesh();
        mesh.setTexCoords(texCoords);
        renderData.setMesh(mesh);

        Renderer.getInstance().setVideoBackgroundTextureID(
                passThroughTexture.getId());

        mMainScene.getMainCameraRig().addChildObject(mPassThroughObject);
    }

    private void createTeaPotObject() {
        try {
            teapot = new GVRSceneObject(mGVRContext,
                    mGVRContext.loadMesh(new GVRAndroidResource(mGVRContext
                            .getContext(), "teapot.obj")),
                    mGVRContext.loadTexture(new GVRAndroidResource(mGVRContext
                            .getContext(), "teapot_tex1.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        teapot.getTransform().setPosition(0f, 0f, -0.5f);
        mMainScene.addSceneObject(teapot);
    }

    private float[] convertMatrix = { 1f, 0f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 0f,
            -1f, 0f, 0f, 0f, 0f, 1f };

    @Override
    public void onStep() {
        if (VuforiaSampleActivity.isVuforiaActive()) {
            Renderer.getInstance().begin();
            Renderer.getInstance().bindVideoBackground(0);
            Renderer.getInstance().end();
        }
    }

    public void updateObjectPose(State state) {
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
                Matrix.scaleM(mConvertedMVMatrix, 0, scaleFactor, scaleFactor,
                        scaleFactor);

                mGVRMVMatrix = mGVRContext.getMainScene().getMainCameraRig()
                        .getTransform().getModelMatrix();

                Matrix.multiplyMM(mTotalMVMatrix, 0, mGVRMVMatrix, 0,
                        mConvertedMVMatrix, 0);
                teapot.getTransform().setModelMatrix(mTotalMVMatrix);
                
                break;
            }
        }
    }

    @SuppressWarnings("unused")
    private void showMatrix(String name, float[] matrix) {
        Log.d(TAG, name);
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[0],
                matrix[4], matrix[8], matrix[12]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[1],
                matrix[5], matrix[9], matrix[13]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[2],
                matrix[6], matrix[10], matrix[14]));
        Log.d(TAG, String.format("%5.2f %5.2f %5.2f %5.2f", matrix[3],
                matrix[7], matrix[11], matrix[15]));
        Log.d(TAG, "\n");
    }

    private float[] mVuforiaMVMatrix;
    private float[] mConvertedMVMatrix;
    private float[] mGVRMVMatrix;
    private float[] mTotalMVMatrix;
}
