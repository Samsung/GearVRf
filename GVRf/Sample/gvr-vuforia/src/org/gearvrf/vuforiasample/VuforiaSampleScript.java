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

    private GVRContext gvrContext = null;
    private GVRSceneObject teapot = null;
    private GVRSceneObject passThroughObject = null;

    static final int VUFORIA_CAMERA_WIDTH = 1280;
    static final int VUFORIA_CAMERA_HEIGHT = 720;
    
    private volatile boolean init = false;

    private GVRScene mainScene;
    
    private float[] vuforiaMVMatrix;
    private float[] convertedMVMatrix;
    private float[] gvrMVMatrix;
    private float[] totalMVMatrix;

    private boolean teapotVisible = false;
    
    @Override
    public void onInit(GVRContext gvrContext) {
        this.gvrContext = gvrContext;
        mainScene = gvrContext.getMainScene();

        createCameraPassThrough();

        createTeaPotObject();

        vuforiaMVMatrix = new float[16];
        convertedMVMatrix = new float[16];
        gvrMVMatrix = new float[16];
        totalMVMatrix = new float[16];
        
        init = true;
    }
    
    public boolean isInit() {
        return init;
    }

    private void createCameraPassThrough() {
        passThroughObject = new GVRSceneObject(gvrContext, 16.0f / 9.0f, 1.0f);

        passThroughObject.getTransform().setPosition(0.0f, 0.0f, -1000.0f);
        passThroughObject.getTransform().setScaleX(1000f);
        passThroughObject.getTransform().setScaleY(1000f);

        GVRTexture passThroughTexture;

        passThroughTexture = new GVRRenderTexture(gvrContext,
                VUFORIA_CAMERA_WIDTH, VUFORIA_CAMERA_HEIGHT);

        GVRRenderData renderData = passThroughObject.getRenderData();
        GVRMaterial material = new GVRMaterial(gvrContext);
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

        mainScene.getMainCameraRig().addChildObject(passThroughObject);
    }

    private void createTeaPotObject() {
        try {
            teapot = new GVRSceneObject(gvrContext,
                    gvrContext.loadMesh(new GVRAndroidResource(gvrContext
                            .getContext(), "teapot.obj")),
                    gvrContext.loadTexture(new GVRAndroidResource(gvrContext
                            .getContext(), "teapot_tex1.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        teapot.getTransform().setPosition(0f, 0f, -0.5f);
        mainScene.addSceneObject(teapot);
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

	private void showTeapot() {
		if (teapotVisible == false) {
			mainScene.addSceneObject(teapot);
			teapotVisible = true;
		}
	}

	private void hideTeapot() {
		if (teapotVisible) {
			mainScene.removeSceneObject(teapot);
			teapotVisible = false;
		}
	}

    public void updateObjectPose(State state) {
        // did we find any trackables this frame?
        int numDetectedMarkers = state.getNumTrackableResults();

		if (numDetectedMarkers == 0) {
			hideTeapot();
			return;
		}

        for (int tIdx = 0; tIdx < numDetectedMarkers; tIdx++) {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            if (trackable.getId() == 1 || trackable.getId() == 2) {
                Matrix44F modelViewMatrix_Vuforia = Tool
                        .convertPose2GLMatrix(result.getPose());
                vuforiaMVMatrix = modelViewMatrix_Vuforia.getData();

                Matrix.multiplyMM(convertedMVMatrix, 0, convertMatrix, 0,
                        vuforiaMVMatrix, 0);

                float scaleFactor = ((ImageTarget) trackable).getSize()
                        .getData()[0];
                Matrix.scaleM(convertedMVMatrix, 0, scaleFactor, scaleFactor,
                        scaleFactor);

                gvrMVMatrix = gvrContext.getMainScene().getMainCameraRig()
                        .getHeadTransform().getModelMatrix();

                Matrix.multiplyMM(totalMVMatrix, 0, gvrMVMatrix, 0,
                        convertedMVMatrix, 0);
                teapot.getTransform().setModelMatrix(totalMVMatrix);

                showTeapot();
                
                break;
            } else {
				hideTeapot();
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
}
