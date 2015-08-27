#ifndef _VR_CURVED_MESH
#define _VR_CURVED_MESH

#include "VRMesh.h"
#include "ui/vec2.h"
#include "ui/vec3.h"
#include "VRCamera.h"
#include "kSensor/math/matrix.hpp"

// b.mathew 
// 13-06-2014
namespace GVR {

	class VRCurvedMesh
	{
	public:
		VRCurvedMesh(int horizontal_count, int vertical_count);
		~VRCurvedMesh();
		void setPos(float x, float y, float z);
		void setScale(float sx, float sy, float sz);
		void draw(unsigned int program_, unsigned int texture_id, int texTransform, VRCamera &cam);
		mat4& getModelMat();
		const VRMesh& mesh() const
		{
			return mesh_;
		}	
		void render(const GLuint& texture);

	private:
		VRMesh mesh_;
		android::vec3 mPos;
		android::vec3 mScale;
		mat4 mModelMat;
		bool mIsDirty;
	};
}

#endif// _VR_CURVED_MESH




