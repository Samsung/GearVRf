#include "VRRenderEngine.h"
#include "stdio.h"
#include "VRLogger.h"

//b.mathew, 22-04-2014

using namespace GVR;
using namespace android;

#define CURVED_DISPLAY 0

VRRenderEngine::VRRenderEngine(int width, int height) {
	
	mDeviceType = VR_DEVICETYPE_S5;
	mSceneType = VR_SCENE_CINEMAHALL;

	mNotificationZ = 0;

	int len = strlen("/system/media/vr_model/") + 1;
	mAssetsPath = new char[len];
	mDefaultAssetsPath = new char[len];
	strncpy(mAssetsPath, "/system/media/vr_model/", len);
	strncpy(mDefaultAssetsPath, "/system/media/vr_model/", len);

	mMode = VR_RENDERMODE_CINEMA;

	mVpX = 0;
	mVpY = 0;
	mVpWidth = 1080;
	mVpHeight = 1920;

	m2DWidth = 0;
	m2DHeight = 0;
	m2DTex = 0;
	m2DFBO = 0;
	mEyeWidth = 0;
	mEyeHeight = 0;
	mEyeTex = 0;
	mEyeFBO = 0;
	mEyeRBO = 0;

	mPass = VR_RENDERPASS_LEFT;

	mVRComposeMode = false;

	mShaderGame = -1;
	mShaderCurvedDisplay = -1;
	
	mSensorThread = VRSensorThread::getInstance();
	mDistorter = 0;
	mScnRenderer = new VRSceneRenderer(mAssetsPath);	

	VR_LOGI("Creating and compiling the shader to render the layer view in VR scene");
	mShaderGame = VRShaderUtil::instance().createProgram(gDefVShader, gDefFShader);
       mShaderCurvedDisplay = VRShaderUtil::instance().createProgram(gCurvedScreenVShader, gCurvedScreenFShader);  //
	   
	mVpX = 0;
	mVpY =0;
	mVpWidth = width;
	mVpHeight = height;

	if(mVpWidth > mVpHeight) {
		mSurfaceType = VR_SURFACE_LANDSCAPE;
	} else {
		mSurfaceType = VR_SURFACE_PORTRAIT;
	}

	mDistorter = new VRDistorter(mSurfaceType);

	mCurvedScreen = new VRCurvedMesh(10, 10);
	
	VR_LOGI("Created VRRenderEngine library Instance this = %p, mScnRenderer = %p, width = %d, height = %d, ", this, mScnRenderer, width, height);
}

VRRenderEngine::~VRRenderEngine() {
	if(mAssetsPath) {
		delete mAssetsPath;
		mAssetsPath = 0;
	}

	if(mDefaultAssetsPath) {
		delete mDefaultAssetsPath;
		mDefaultAssetsPath = 0;
	}

	if(mSensorThread != NULL) {
		mSensorThread->terminate();
		mSensorThread = 0;
	}	

	if(mDistorter != 0) {
		delete mDistorter;
		mDistorter = 0;
	}

	if(mCurvedScreen != 0) {
		delete mCurvedScreen;
		mCurvedScreen = 0;
	}

	glDeleteTextures(1, &mEyeTex);
	glDeleteFramebuffers(1, &mEyeFBO);
	glDeleteRenderbuffers(1, &mEyeRBO);
	glDeleteTextures(1, &m2DTex);
	glDeleteFramebuffers(1, &m2DFBO);
//	glDeleteRenderbuffers(1, &m2DRBO);

	destroyScenes();

	if(mShaderGame != -1) {
		glDeleteProgram(mShaderGame);
		mShaderGame = -1;
	}

	if(mShaderCurvedDisplay != -1) {
		glDeleteProgram(mShaderCurvedDisplay);
		mShaderCurvedDisplay = -1;
	}

	VR_LOGI("Deleting VRRenderEngine library Instance : completed");
}

void VRRenderEngine::begin(int width, int height) {
	if(mVRComposeMode)
		return;
	
	mVRComposeMode = true;
	VR_LOGI("Beginning the VR Compose mode!!width = %d, height = %d", width, height);

	if (m2DWidth != width || m2DHeight != height)
	{
		glDeleteTextures(1, &m2DTex);
		glDeleteFramebuffers(1, &m2DFBO);
		
		m2DTex = 0;
		m2DFBO = 0;
		
		m2DWidth = width;
		m2DHeight = height;
		glGenTextures(1, &m2DTex);
		glGenFramebuffers(1, &m2DFBO);


		glBindTexture(GL_TEXTURE_2D, m2DTex);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, m2DWidth, m2DHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

	
		glBindFramebuffer(GL_FRAMEBUFFER, m2DFBO);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, m2DTex, 0);

	}
	else
	{
		glBindFramebuffer(GL_FRAMEBUFFER, m2DFBO);
	}
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT);
    glViewport(4, 4, m2DWidth-20, m2DHeight-8);

	glDisable(GL_DEPTH_TEST);
}

void VRRenderEngine::end() {
	if(!mVRComposeMode)
		return;
	
	mVRComposeMode = false;
	VR_LOGI("Ending the VR Compose mode!!");
	
	glBindFramebuffer(GL_FRAMEBUFFER, 0);

	int texTransform = 0;

	if(mMode == 	VR_RENDERMODE_CINEMA){
		texTransform = (mSurfaceType == VR_SURFACE_LANDSCAPE)? 1 : 0;
		renderScene(m2DTex, GL_TEXTURE_2D, texTransform);

	} else {

		setRenderPass(VR_RENDERPASS_SINGLE);
       	renderInStereo(m2DTex);
	}
}

void VRRenderEngine::beginLeftFrame(float *outLeftCamViewMat, float *outLeftCamProjMat) {

	VR_LOGI("beginLeftFrame(leftcamviewmat = %d, leftcamprojmat = %d)!!", outLeftCamViewMat, outLeftCamProjMat);
	
	mSensorThread->start();

	mOrientationQ = mSensorThread->getQuaternion();

	mOrientationQ.ToMatrix4x4(mOrientationMat);
	GVR::mat4::AffineInverse(mOrientationMat);

	VRCamera cam;
	cam.setProjParams(95.0f, getEyeCamAspectRatio(), 0.1f, 1000.0f);

	VR_LOGI("Viewmat");
	for(int i = 0; i < 16; i++) 
		VR_LOGI("%f", mOrientationMat.Pointer()[i]); 

	mat4 lViewMat = cam.getViewMat();
	lViewMat = mOrientationMat;
	lViewMat.AddTranslation(GVR::vec3(mVRParams.mIPD * 0.5f, 0.0f, 0.0f));

	mat4 lProjMat = cam.getProjMat();
	lProjMat.AddTranslation(GVR::vec3(-mVRParams.getProjectionOffset(mVpHeight, mVpWidth), 0.0f, 0.0f));	
	
	setRenderToFramebuffer(VR_RENDERPASS_LEFT);

	memcpy(outLeftCamViewMat, lViewMat.Pointer(), 16 * sizeof(float));
	memcpy(outLeftCamProjMat, lProjMat.Pointer(), 16 * sizeof(float));
	
	mSensorThread->stop();

}
void VRRenderEngine::endLeft() {
	VR_LOGI("endLeft() !!");
	flushFramebuffer();
}

void VRRenderEngine::beginRightFrame(float *outRightCamViewMat, float *outRightCamProjMat) {
	VR_LOGI("beginRightFrame(rightcamviewmat = %d, rightcamviewmat = %d)!!", outRightCamViewMat, outRightCamProjMat);
	VRCamera cam;
	cam.setProjParams(95.0f, getEyeCamAspectRatio(), 0.1f, 1000.0f);

	mat4 rViewMat = cam.getViewMat();
	rViewMat = mOrientationMat;
	rViewMat.AddTranslation(GVR::vec3(-mVRParams.mIPD * 0.5f, 0.0f, 0.0f));

	mat4 rProjMat = cam.getProjMat();
	rProjMat.AddTranslation(GVR::vec3(mVRParams.getProjectionOffset(mVpHeight, mVpWidth), 0.0f, 0.0f));	
	
	setRenderToFramebuffer(VR_RENDERPASS_RIGHT);

	memcpy(outRightCamViewMat, rViewMat.Pointer(), 16 * sizeof(float));
	memcpy(outRightCamProjMat, rProjMat.Pointer(), 16 * sizeof(float));
}
void VRRenderEngine::endRight() {
	VR_LOGI("endRight() !!");
	flushFramebuffer();	
}

bool VRRenderEngine::setAssetsPath(char *path) {
	bool status = false;
	if(path) {
		if(mAssetsPath) {
			delete mAssetsPath;
			mAssetsPath = 0;
		}

		int len = strlen(path) +1;
		mAssetsPath = new char[len];
		strncpy(mAssetsPath, path, len);
		VR_LOGI("Setting the assets path for loading resources [%s]", mAssetsPath);
		
		status = true;
	} else {

		if(mAssetsPath) {
			delete mAssetsPath;
			mAssetsPath = 0;
		}
		
		int len = strlen(mDefaultAssetsPath) +1;
		mAssetsPath = new char[len];
		strncpy(mAssetsPath, mDefaultAssetsPath, len);
		VR_LOGE("Invalid Assets Path!! Using default asset path (%s)", mAssetsPath);
	}
	return status;
}
			
bool VRRenderEngine::setCinemaMode(VRSceneType type) {
	bool status = false;
	
	mMode = VR_RENDERMODE_CINEMA;

	if(type >= VR_SCENE_CINEMAHALL && type < VR_SCENE_MAX) {
		mSceneType = type;
		status = true;
	}

	if(status) {
		if(mScnRenderer) {
			mScnRenderer->init(mSceneType);
			VR_LOGI("Setting the VR cinema mode, scenetype = %d, [status = %d] ", type, status);
		} else {
			status = false;
			VR_LOGE("setCinemaMode failed : mScnRenderer instance is not created! mScnRenderer = %p", (void*)mScnRenderer);	
		}
	} else  {
		VR_LOGE("Error while setting the cinema mode.. scenetype = %d", type);
	}
	
	return status;
}

bool VRRenderEngine::setVRMode(int z_offset) {
	mMode = VR_RENDERMODE_VR;
	mNotificationZ = z_offset;
	VR_LOGI("Setting the VR notification mode");
	return true;
}

void VRRenderEngine::setRenderPass(VRRenderPass pass) {
	VR_LOGI("Setting the VR render pass, pass  =%d", pass);
	mPass = pass;
}

bool VRRenderEngine::renderScene(unsigned int texId, unsigned int texTarget, unsigned int texTransform) {

	VR_LOGI("Rendering the scene [texId = %d, texTarget = %d, texTransform = %d]", texId, texTarget, texTransform);

	mSensorThread->start();
	
	mScreenTransfMat = GVR::mat4::Identity();

	float ar = getEyeCamAspectRatio();	
	mCamera.setProjParams(95/*fov*/, ar /*Aspect Ratio*/, 0.1f/*far*/, 10000.0f/*near*/);

	mOrientationQ = mSensorThread->getQuaternion();

	GVR::mat4 orientation;
	mOrientationQ.ToMatrix4x4(orientation);
	GVR::mat4::AffineInverse(orientation);

	vec3 camPos;
	GVR::mat4 screen_quad_t;
	GVR::mat4 screen_quad_s;
	

	switch(mSceneType) {
		case VR_SCENE_CINEMAHALL:
			camPos = vec3 (0.0f, -5.0f, 10.0f); 
			screen_quad_t  = GVR::mat4::Translate(0.0f, 5.0f, -20.0f); 
			screen_quad_s = GVR::mat4::Scale(7.1f, 4.0f, 1.0f);
			mCurvedScreen->setPos(0.0f, 5.0f, -20.0f);
			mCurvedScreen->setScale(7.1f, 4.0f, 1.0f);
			break;
		case VR_SCENE_SPACESHIP:
			camPos = vec3(0.0f, -6.5f, -3.5f);   //-6.5
			screen_quad_t  = GVR::mat4::Translate(0.0f, 5.75f, -3.0f); // 5.0f
			screen_quad_s = GVR::mat4::Scale(4.0f, 3.0f, 1.0f); // 3.5 2.5
			mCurvedScreen->setPos(0.0f, 5.75f, -3.0f);
			mCurvedScreen->setScale(4.0f, 3.0f, 1.0f);
			break;
		case VR_SCENE_SPHERE:
			camPos = vec3(0.0f, 0.0f, 0.0f);   //-6.5
			screen_quad_t  = GVR::mat4::Translate(0.0f, 0.0f, -7.0f); // 5.0f
			screen_quad_s = GVR::mat4::Scale(4.0f, 3.0f, 1.0f); // 3.5 2.5
			mCurvedScreen->setPos(0.0f, 0.0f, -7.0f);
			mCurvedScreen->setScale(4.0f, 3.0f, 1.0f);
			break;			
		case VR_SCENE_MAX:
			camPos = vec3(0.0f, 0.0f, 0.0f);
			screen_quad_t  = GVR::mat4::Translate(0.0f, 0.0f, 0.0f);
			screen_quad_s = GVR::mat4::Scale(1.0f, 1.0f, 1.0f);
			mCurvedScreen->setPos(0.0f, 0.0f, 0.0f);
			mCurvedScreen->setScale(1.0f, 1.0f, 1.0f);
			break;
			
	}

	GVR::mat4 trans_mat = GVR::mat4::Translate(camPos.x, camPos.y, camPos.z); 
	mCamera.setTransformation(orientation, trans_mat);

	mVRParams.mShaderId= mShaderGame;	
	
	mScreenTransfMat = screen_quad_s * screen_quad_t;

	setRenderToFramebuffer(VR_RENDERPASS_LEFT);
	
	mCamera.getProjMat().AddTranslation(GVR::vec3(-mVRParams.getProjectionOffset(mVpHeight, mVpWidth), 0.0f, 0.0f));	
	mCamera.getViewMat().AddTranslation(GVR::vec3(mVRParams.mIPD * 0.5f, 0.0f,  2.0f));

	mScnRenderer->renderScene(0.0f, &mCamera);	
	if(CURVED_DISPLAY)
		mCurvedScreen->draw(mVRParams.mShaderId, texId, texTransform, mCamera);
	else
		mScreenView.draw(&mVRParams, &mCamera, mScreenTransfMat, texId, texTarget, texTransform);

	flushFramebuffer();

	

	setRenderToFramebuffer(VR_RENDERPASS_RIGHT);

	mCamera.getProjMat().AddTranslation(GVR::vec3(2.0f * mVRParams.getProjectionOffset(mVpHeight, mVpWidth), 0.0f,  0.0f));	
	mCamera.getViewMat().AddTranslation(GVR::vec3(-2.0f * mVRParams.mIPD * 0.5f, 0.0f, 0.0f));
	
	mScnRenderer->renderScene(0.0f, &mCamera);	

	if(CURVED_DISPLAY)
		mCurvedScreen->draw(mVRParams.mShaderId, texId,  texTransform, mCamera);
	else
		mScreenView.draw(&mVRParams, &mCamera,mScreenTransfMat, texId, texTarget, texTransform);

	flushFramebuffer();

	mSensorThread->stop();
	
	return true;	
}

void VRRenderEngine::destroyScenes() {
	VR_LOGI("Destroying the VRSceneRenderer instance !! mScnRenderer = %p", (void*)mScnRenderer);
	if(mScnRenderer) {
		delete mScnRenderer;
		mScnRenderer = 0;
	}
}

float VRRenderEngine::getEyeCamAspectRatio() {
	float ar = 0.0f;
	int fb_vp_w = 0;
	int fb_vp_h = 0;
	
	if(mSurfaceType == VR_SURFACE_LANDSCAPE) {
		fb_vp_w = mVpWidth /2;
		fb_vp_h = mVpHeight;
		
	} else {
		fb_vp_w = mVpHeight/2; 
		fb_vp_h = mVpWidth;
	}

	ar = (float)fb_vp_w / fb_vp_h; 
	return ar;
}

void VRRenderEngine::setRenderToFramebuffer(VRRenderPass pass) {

	glDisable(GL_BLEND);
	glEnable(GL_CULL_FACE);
	glEnable(GL_DEPTH_TEST);

	VR_LOGI("Setting the Render target to Framebuffer for pass = %d", pass);

	int fb_vp_w = 0;
	int fb_vp_h = 0;

	if(mSurfaceType == VR_SURFACE_PORTRAIT){
		fb_vp_w = mVpHeight /2;
		fb_vp_h = mVpWidth;
	} else {
		fb_vp_w = mVpWidth/2;
		fb_vp_h = mVpHeight;
	}

	if (mEyeWidth != fb_vp_w || mEyeHeight != fb_vp_h)	
	{
		glDeleteTextures(1, &mEyeTex);
		glDeleteFramebuffers(1, &mEyeFBO);
		glDeleteRenderbuffers(1, &mEyeRBO);
		mEyeTex = 0;
		mEyeFBO = 0;
		mEyeRBO = 0;

		mEyeWidth = fb_vp_w;
		mEyeHeight = fb_vp_h;

		glGenTextures(1, &mEyeTex);
		glGenFramebuffers(1, &mEyeFBO);
		glGenRenderbuffers(1, &mEyeRBO);

		glBindTexture(GL_TEXTURE_2D, mEyeTex);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, mEyeWidth, mEyeHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0);
		glBindTexture(GL_TEXTURE_2D, 0);

		glBindRenderbuffer(GL_RENDERBUFFER, mEyeRBO);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, mEyeWidth, mEyeHeight);
		glBindRenderbuffer(GL_RENDERBUFFER, 0);

		glBindFramebuffer(GL_FRAMEBUFFER, mEyeFBO);
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mEyeTex, 0);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, mEyeRBO); 

		VR_LOGI("Width and height are not same so creating framebuffer mEyeWidth = %d, mEyeHeight = %d, mEyeTex = %d, mEyeFBO = %d, mEyeRBO = %d", mEyeWidth, mEyeHeight, mEyeTex, mEyeFBO, mEyeRBO);
	}
	else
	{
		glBindFramebuffer(GL_FRAMEBUFFER, mEyeFBO);
	}

	glViewport(0, 0, fb_vp_w, fb_vp_h); 
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);	
	
	setRenderPass(pass);

}

void VRRenderEngine::flushFramebuffer() {
	glBindFramebuffer(GL_FRAMEBUFFER, 0);	
   	glBindRenderbuffer(GL_RENDERBUFFER, 0);

	glDisable(GL_BLEND);
	glDisable(GL_DEPTH_TEST);

	VR_LOGI("Rendering the FBO contents to display in stereoscopic L-R pair");
   
	renderInStereo(mEyeTex);

}

GLuint VRRenderEngine::getTargetFBO() {
	return m2DFBO;
}

void VRRenderEngine::renderInStereo(int texId) {

	VR_LOGI("Rendering in stereo, texId = %d, pass = %d", texId, mPass);
	
	if(mPass == VR_RENDERPASS_LEFT) {
		if(mSurfaceType == VR_SURFACE_PORTRAIT) {
			glViewport(0, mVpHeight/2, mVpWidth, mVpHeight/2);
			glScissor(0, mVpHeight/2, mVpWidth, mVpHeight/2);
		} else {
			glViewport(0, 0, mVpWidth/2, mVpHeight);
			glScissor(0, 0, mVpWidth/2, mVpHeight);
		}
		mDistorter->render(texId, mMode, mPass);
		
	} else if(mPass == VR_RENDERPASS_RIGHT){

		if(mSurfaceType == VR_SURFACE_PORTRAIT) {
			glViewport(0, 0, mVpWidth, mVpHeight/2);
			glScissor(0, 0, mVpWidth, mVpHeight/2);
		} else {
			glViewport(mVpWidth/2, 0, mVpWidth/2, mVpHeight);
			glScissor(mVpWidth/2, 0, mVpWidth/2, mVpHeight);
		}
		mDistorter->render( texId, mMode, mPass);

			
	} else {

		if(mSurfaceType == VR_SURFACE_PORTRAIT) {
			glViewport(0,mVpHeight/2, mVpWidth, mVpHeight/2);
			glScissor(0,mVpHeight/2, mVpWidth, mVpHeight/2);
			mDistorter->render(texId, mMode, VR_RENDERPASS_RIGHT);

			glViewport(0, 0, mVpWidth, mVpHeight/2);
			glScissor(0, 0, mVpWidth, mVpHeight/2);
			mDistorter->render(texId, mMode, VR_RENDERPASS_LEFT);
		} else {		
			glViewport(mVpWidth/2, 0, mVpWidth/2, mVpHeight);
			glScissor(mVpWidth/2, 0, mVpWidth/2, mVpHeight);
			mDistorter->render(texId, mMode, VR_RENDERPASS_RIGHT);

			glViewport(0, 0, mVpWidth/2, mVpHeight);
			glScissor(0, 0, mVpWidth/2, mVpHeight);
			mDistorter->render(texId, mMode, VR_RENDERPASS_LEFT);
		}		
	}
}

bool VRRenderEngine::setDistortion(bool flag) {
	VR_LOGI("Setting the distortion flag, flag = %d", flag);
	if(mDistorter) {
		mDistorter->setDistortion(flag);
		return true;
	}
	return false;
}
bool VRRenderEngine::setSensorType(VRSensorType type) {
	bool status = false;

	if(type == VR_SENSOR_INTERNAL || type == VR_SENSOR_K) {
		mSensorThread->setSensorType(type);
		status = true;
	}

	if(status) {
		VR_LOGI("Setting the SensorType, type= %d", type);
	} else {
		VR_LOGE("Error while setting the sensortype.. type= %d", type);
	}

	return status;
}
