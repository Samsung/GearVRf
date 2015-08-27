#ifndef _VRCAMERA_H
#define _VRCAMERA_H

#include "kSensor/math/matrix.hpp"

// b.mathew
// 05 - dec - 2013
namespace GVR {

class VRCamera {
	public:
		VRCamera();
		VRCamera(const VRCamera &cam);

		void setViewParams(float *eye, float *lookAt, float *up);
		void setProjParams(float fov, float aspect, float near, float far);

		void setTransformation(GVR::mat4& rot_mat, GVR::mat4& trans_mat);
		void translate(float x , float y, float z) ;

		GVR::mat4& getViewMat();
		GVR::mat4& getProjMat();
	
		void setOrientation(float *rotMat);

		~VRCamera();
	private:

		void updateViewMat();
		void updateProjMat();

		
		float mEye[3];
		float mLookAt[3];
		float mUp[3];

		GVR::mat4 mViewMat;
		GVR::mat4 mProjMat;

		float mFov;
		float mAspect;
		float mNear;
		float mFar;
		
};
};


#endif // _VRCAMERA_H

