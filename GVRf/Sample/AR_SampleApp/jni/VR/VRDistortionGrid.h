#ifndef _DISTORTION_GRID
#define _DISTORTION_GRID

#include "VRMesh.h"
#include "ui/vec2.h"

namespace GVR {

	enum VRSurfaceType {
		VR_SURFACE_PORTRAIT = 0,
		VR_SURFACE_LANDSCAPE
	};

	enum VRRenderPass {
		VR_RENDERPASS_LEFT,
		VR_RENDERPASS_RIGHT,
		VR_RENDERPASS_SINGLE
	};

// b.mathew - Ported from GVRF...
class VRDistortionGrid
{
public:
	VRDistortionGrid(int horizontal_count, int vertical_count, int screen_width, int screen_height, VRSurfaceType type, VRRenderPass pass);
	~VRDistortionGrid();
	const VRMesh& mesh() const
	{
		return mesh_;
	}	
	const VRMesh& quad() const 
	{
		return quad_;
	}
	void render(const GLuint& texture);

private:
	VRMesh mesh_;
	VRMesh quad_;
	};
};

#endif// _DISTORTION_GRID


