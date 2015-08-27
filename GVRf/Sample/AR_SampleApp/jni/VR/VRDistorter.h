#ifndef VRDISTORTER_H_
#define VRDISTORTER_H_

#include "GLES2/gl2.h"
#include "VRDistortionGrid.h"

namespace GVR {

enum VRRenderMode {
	VR_RENDERMODE_CINEMA,
	VR_RENDERMODE_VR
};
	

class VRDistortionGrid;

class VRDistorter {
public:
	VRDistorter(VRSurfaceType surfaceType);
	~VRDistorter();

	void setDistortion(bool flag);
	void setVertexDistortion(bool flag);
	void setFragmentDistortion(bool flag);
	void render(GLuint texture_id, VRRenderMode mode, VRRenderPass pass);
	void render(const VRDistortionGrid& distortion_grid, GLuint texture_id, VRRenderMode mode);
	
private:
	int mProgram;
	int mProgramOES;
	GLuint program_;
	GLuint a_position_;
	GLuint a_uv_;
	GLuint u_texture_;

	VRDistortionGrid *mLDistGrid;
	VRDistortionGrid *mRDistGrid;

	bool mUseDistortion;
	bool mUseVertexDistortion;
	bool mUseFragmentDistortion;
};
}
#endif /* DISTORTER_H_ */

