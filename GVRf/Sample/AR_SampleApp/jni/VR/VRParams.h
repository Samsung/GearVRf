#ifndef _VRPARAMS_H
#define _VRPARAMS_H

#include "stdlib.h"
#include "VRDeviceInfo.h"

#define DEG2RAD (0.0174f)

namespace GVR {

class VRScrollParams {
	
	public:

	VRScrollParams() {
		mDispPercentage = 100.0f;
		calculateScrollFactor(mDispPercentage, 90.0f * DEG2RAD, 40.0f * DEG2RAD);
	}

	void calculateScrollFactor(float displayPercentage, float maxRotAngle, float currentYaw) {
		mDispPercentage = displayPercentage;
		float rmngDispAreaBy2 = (1.0f - mDispPercentage * 0.01f)/2.0f;
		float left = rmngDispAreaBy2;
		float right = 1.0f - rmngDispAreaBy2;
		mMultFactor = right - rmngDispAreaBy2;
		mAddFactor = left;
		mScrollFactor = currentYaw * rmngDispAreaBy2/ maxRotAngle;
	}

			
	float mMultFactor;
	float mAddFactor;
	float mScrollFactor;
	float mDispPercentage;

};

class VRParams {
	
	public:
		
	VRParams() {
		mDistParams[0] = 0.0f;
		mDistParams[1] = 0.0f;	
		mDistParams[2] = 0.0f;
		mDistParams[3] = 0.0f;	
	
		mLSD= 0.0f;
		mESD = 0.0f;
		mIPD = 0.0f;

	}

	float getProjectionOffset(int hRes,int vRes) {
		float view_center = VRDeviceInfo::h_screen_size() * 0.25f;
		float eye_projection_shift = view_center - VRDeviceInfo::lens_separation_distance() * 0.5f;
		float projection_center_offset = 4.0f * eye_projection_shift / VRDeviceInfo::h_screen_size();
		return projection_center_offset;
	}

	void setVRParams(float esd, float lsd, float ipd, float* distParams) {
		mESD = esd;
		mLSD = lsd;
		mIPD = ipd;
		memcpy(mDistParams, distParams, 4 * sizeof(float)); 		
	}
	
	float mDistParams[4];
	float mLSD;
	float mESD;
	float mIPD;

	unsigned int mShaderId;
};

};


#endif // _VRPARAMS_H

