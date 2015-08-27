#include <jni.h>
#include <android/log.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "VR/VRRenderEngine.h"
using namespace GVR;

VRRenderEngine *mVREngine;
int gWidth, gHeight;
float viewMat[16], projMat[16];

extern "C" {
	JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_initEngine(JNIEnv * env, jobject obj , jint w , jint h );
    JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_leftbegin(JNIEnv * env, jobject obj);
    JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_leftend(JNIEnv * env, jobject obj );
    JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_rightbegin(JNIEnv * env, jobject obj);
    JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_rightend(JNIEnv * env, jobject obj );
    JNIEXPORT jfloatArray JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_viewmat(JNIEnv * env, jobject obj );
    JNIEXPORT jfloatArray JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_projmat(JNIEnv * env, jobject obj );
};

JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_initEngine(JNIEnv * env, jobject obj , jint w , jint h )
{
		mVREngine = new VRRenderEngine(w,h);
	        mVREngine->setCinemaMode(VR_SCENE_CINEMAHALL);
	        mVREngine->setSensorType(VR_SENSOR_K);
	       /* gWidth = w;
	        gHeight = h;*/
}

JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_leftbegin(JNIEnv * env, jobject obj)
{
	 mVREngine->beginLeftFrame(viewMat, projMat);
}

JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_leftend(JNIEnv * env, jobject obj)
{
	 mVREngine->endLeft();
}

JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_rightbegin(JNIEnv * env, jobject obj)
{
	mVREngine->beginRightFrame(viewMat, projMat);
}

JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_rightend(JNIEnv * env, jobject obj)
{
	mVREngine->endRight();
}

JNIEXPORT jfloatArray JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_viewmat(JNIEnv * env, jobject obj )
{
	jfloatArray viewreturn = env->NewFloatArray(16);
	env->SetFloatArrayRegion(viewreturn , 0 , 16 ,viewMat );

	return viewreturn;
}


JNIEXPORT jfloatArray JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_projmat(JNIEnv * env, jobject obj )
{
	jfloatArray projreturn = env->NewFloatArray(16);
	env->SetFloatArrayRegion(projreturn , 0 , 16 ,projMat );

	return projreturn;
}
