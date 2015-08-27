#ifndef _QUAD_H
#define _QUAD_H

#include "VRParams.h"
#include <GLES/gl.h>
#include <GLES/glext.h>
#include "VRCamera.h"
#include <GLES2/gl2.h>
#include "kSensor/math/matrix.hpp"

namespace GVR  {

class VRQuad {

	public:
		VRQuad();
		void draw(VRParams *vrparams, VRCamera *camRef, GVR::mat4 &transMatrix,unsigned int texId, unsigned int target, const uint32_t texTransform);
		~VRQuad();
		
	private:
		float *mVertices;
		float *mTexCords;
};
};


#endif // QUAD_H

