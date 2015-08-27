#include <stdio.h>
#include <string.h>
#include <math.h>

#include "VRCamera.h"
//#include "Matrix.h"

#include <utils/Log.h>

// b.mathew
// 5 - Dec - 2013

using namespace GVR;


#define DEG2RAD (0.0174f)

VRCamera :: VRCamera() {

	 mEye[0] = 0.0f; 
	 mEye[1] = 0.0f; 
	 mEye[2] = 0.0f;
	 
	 mLookAt[0] = 0.0f; 
	 mLookAt[1] = 0.0f; 
	 mLookAt[2] = -10.0f;
	 
	 mUp[0] = 0.0f; 
	 mUp[1] = 1.0f; 
	 mUp[2] = 0.0f;

	 mFov = 95.0f * DEG2RAD;
	 mAspect =  1080.0f /(1920.0f * 0.5f);
	 mNear = 0.1f;
	 mFar = 1000.0f;

	 updateViewMat();
	 updateProjMat();
}

VRCamera :: VRCamera(const VRCamera &cam) {
	//memcpy(mEye, cam.mEye, 3 * sizeof(float));
	//memcpy(mLookAt, cam.mLookAt, 3 * sizeof(float));
	//memcpy(mUp, cam.mUp, 3 * sizeof(float));
	mEye[0] = cam.mEye[0];
	mEye[1] = cam.mEye[1];
	mEye[2] = cam.mEye[2];
    
	mLookAt[0] = cam.mLookAt[0];
	mLookAt[1] = cam.mLookAt[1];
	mLookAt[2] = cam.mLookAt[2];

	mUp[0] = cam.mUp[0];
	mUp[1] = cam.mUp[1];
	mUp[2] = cam.mUp[2];
	
	mFov = cam.mFov;
	mAspect = cam.mAspect;
	mNear = cam.mNear;
	mFar = cam.mFar;

	//mViewMat = cam.getViewMat();
	//mProjMat = cam.getProjMat();
	updateViewMat();
	updateProjMat();
}

void VRCamera :: setViewParams(float *eye, float *lookAt, float *up) {
	memcpy(mEye, eye, 3 * sizeof(float));
	memcpy(mLookAt, lookAt, 3 * sizeof(float));
	memcpy(mUp, up, 3 * sizeof(float));

	updateViewMat();
}

void VRCamera :: setProjParams(float fovdegree, float aspect, float near, float far) {
	mFov = fovdegree * DEG2RAD;
	mAspect = aspect;
	mNear = near;
	mFar = far;

	updateProjMat();
}

void VRCamera :: translate(float x, float y, float z) {
	mViewMat.w.x = x;
	mViewMat.w.y = y;
	mViewMat.w.z = z;
}

void VRCamera :: setTransformation(mat4& rot_mat, mat4& trans_mat) {
	mViewMat =  trans_mat * rot_mat;
}

mat4& VRCamera :: getViewMat() {

	return mViewMat;
}

mat4& VRCamera :: getProjMat() {
	return mProjMat;
}

void VRCamera :: updateViewMat() {
	vec3 eye(mEye[0], mEye[1], mEye[2]);
	vec3 look(mLookAt[0], mLookAt[1], mLookAt[2]);
	vec3 up(mUp[0], mUp[1], mUp[2]);
	mViewMat = mat4::LookAt(eye, look, up);
}

void VRCamera :: updateProjMat() {
	mProjMat = mat4::Perspective(mFov, mAspect, mNear, mFar);
}

void VRCamera :: setOrientation(float *rotMat) {
	
	 mEye[0] = 0.0f; 
	 mEye[1] = 0.0f; 
	 mEye[2] = 0.0f;
	 
	 mLookAt[0] = 0.0f; 
	 mLookAt[1] = 0.0f; 
	 mLookAt[2] = -10.0f;
	 
	 mUp[0] = 0.0f; 
	 mUp[1] = 1.0f; 
	 mUp[2] = 0.0f;

	updateViewMat();

	//inv @ref http://3dgep.com/?p=1700
	//Matrix4<float> inv_model = VisualObject::UpdateModelMatrix().Inverse();
	//float tmpRotMat[16];
	//memcpy(tmpRotMat, rotMat, 16 * 4);
	//matrixInverse(rotMat);
	//memcpy(rotMat, tmpRotMat, 16 * 4);

	float newViewMat[16];
	//mat4 newViewMat4;
	//mat4 invRotMat;
	//invRotMat = mat4((const float*)rotMat);
	//newViewMat4 = invRotMat * mViewMat;
	//matrixInverse((float*)newViewMat4.asArray());
	//mViewMat = newViewMat4;


	//	memcpy( newViewMat, newViewMat4, 16 * 4);

	/*	matrixMultiply(newViewMat, rotMat, (float*)mViewMat.asArray());
	matrixInverse(newViewMat);


	memcpy((float*)mViewMat.asArray(), newViewMat, sizeof(float) * 16);*/
}

VRCamera :: ~VRCamera() {
}

