#ifndef _VRRenderEngine_h
#define _VRRenderEngine_h

#include "kSensor/math/matrix.hpp"
#include "VRDistorter.h"
#include "VRQuad.h"
#include "VRCamera.h"
#include "VRParams.h"
#include "VRSceneManager.h"
#include "VRSensorThread.h"
#include "VRCurvedMesh.h"

// b.mathew, 22-04- 2014

namespace GVR {

	enum VRDeviceType {
		VR_DEVICETYPE_S5 =0,
		VR_DEVICETYPE_NOTE4,
		VR_DEVICETYPE_3DTV,
		VR_DEVICETYPE_MAX
	};

	class VRRenderEngine {

		public:
			VRRenderEngine(int width, int height);
			~VRRenderEngine();

			void begin(int width, int height);
			void end();

			void beginLeftFrame(float *outLeftCamViewMat, float *outLeftCamProjMat);
			void endLeft();

			void beginRightFrame(float *outRightCamViewMat, float *outRightCamProjMat);
			void endRight();

			bool renderScene(unsigned int texId, unsigned int texTarget, unsigned int texTransform);
			bool setAssetsPath(char *path);
			bool setCinemaMode(VRSceneType type);
			bool setVRMode(int z_offset);
			bool setSensorType(VRSensorType type);
			bool setDistortion(bool flag);
			GLuint getTargetFBO();
						
		private:
			void destroyScenes();
			float getEyeCamAspectRatio();
			void setRenderToFramebuffer(VRRenderPass pass);
			void flushFramebuffer();
			void renderInStereo(int texId);
			void setRenderPass(VRRenderPass pass);
		
		private:
			VRRenderMode mMode;
			int mNotificationZ;

			int mVpX, mVpY, mVpWidth, mVpHeight;
			
			VRDeviceType mDeviceType;
			VRSceneType mSceneType;
			VRRenderPass mPass;
			VRSurfaceType mSurfaceType;

			int				m2DWidth;
			int				m2DHeight;
			GLuint			m2DTex;
			GLuint			m2DFBO;
			GLuint 			m2DRBO;
			int				mEyeWidth;
			int				mEyeHeight;
			GLuint			mEyeTex;
			GLuint			mEyeFBO;
			GLuint			mEyeRBO;
			int 				mShaderGame;
			int 				mShaderCurvedDisplay;
			VRSceneRenderer* mScnRenderer;	
			VRQuad 			mScreenView;
			VRCurvedMesh      *mCurvedScreen;
			mat4 			mScreenTransfMat;
			Quaternion		mOrientationQ;
			mat4 			mOrientationMat;
			VRCamera 		mCamera;
			VRParams 		mVRParams;
			
			VRSensorThread* mSensorThread;
			bool mVRComposeMode;

			char *mAssetsPath;
			char *mDefaultAssetsPath;

			VRDistorter *mDistorter;
	};
}

#endif
