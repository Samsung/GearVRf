package org.gearvrf.mixedreality.arcore;

import android.opengl.Matrix;

import com.google.ar.core.Pose;


public class ARCorePose {
    private float[] mPoseMatrix = new float[16];
    private float[] mModelViewMatrix = new float[16];
    private float[] mGVRModelMatrix = new float[16];

    public float[] getPoseMatrix() {
        return mPoseMatrix;
    }

    public float[] getGVRModelMatrixMatrix() {
        return mGVRModelMatrix;
    }

    public void update(Pose pose, float[] ARViewMatrix, float[] GVRCamMatrix, float scale) {
        pose.toMatrix(mPoseMatrix, 0);

        ar2gvr(ARViewMatrix, GVRCamMatrix, scale);
    }

    private void ar2gvr(float[] ARViewMatrix, float[] GVRCamMatrix, float scale) {
        Matrix.multiplyMM(mModelViewMatrix, 0, ARViewMatrix, 0, mPoseMatrix, 0);
        Matrix.multiplyMM(mGVRModelMatrix, 0, GVRCamMatrix, 0, mModelViewMatrix, 0);

        Matrix.scaleM(mGVRModelMatrix, 0, scale, scale, scale);
        mGVRModelMatrix[12] = mGVRModelMatrix[12] * scale;
        mGVRModelMatrix[13] = mGVRModelMatrix[13] * scale;
        mGVRModelMatrix[14] = mGVRModelMatrix[14] * scale;
    }
}
