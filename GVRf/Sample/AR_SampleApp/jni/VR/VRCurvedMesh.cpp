#include "VRCurvedMesh.h"
#include<utils/Vector.h>
#include "VRDeviceInfo.h"

using namespace android;
using namespace GVR;

VRCurvedMesh::VRCurvedMesh(int horizontal_count, int vertical_count)
{
	int m = horizontal_count;
	int n = vertical_count;

	mModelMat = mat4::Identity();

	Vector<android::vec3> vertices;
	Vector<android::vec2> uvs;
	
	float u,v;

	for(int j = 0; j < n; ++j)
	{
		float y =  (float) j / (n - 1);
		for(int i = 0; i < m; ++i)
		{
			float x = (float) i / (m - 1);
			float curve_z_factor = 1.2f * sin(3.1415926f *  x);
			vertices.push_back(android::vec3(-1.0f + 2.0f * x, -1.0f + 2.0f * y,  -1.0f -curve_z_factor ));

			u = y ;
			v =  1.0f - x;
			android::vec2 val = android::vec2(u,v);
			uvs.push_back(val);
		}
	}
		

	mesh_.set_vertices(vertices);

	

	/*for(Vector<android::vec3>::iterator it = vertices.begin(); it != vertices.end(); ++it)
	{

		float x = (it->y + 1.0f) * 0.5f;
		float y = 1.0f - (it->x +1.0f) * 0.5f;

	//	x += (0.1f * sin( 3.14f * (y) ) * it->y);

		android::vec2 val = android::vec2(x, y) ;
		uvs.push_back(val);
	}*/

	mesh_.set_uvs(uvs);

	Vector<unsigned short> triangles;

	for(int j = 0; j < n - 1; ++j)
	{
		for(int i = 0; i < m - 1; ++i)
		{
			triangles.push_back( (i    ) + (j    ) * m);
			triangles.push_back( (i + 1) + (j    ) * m);
			triangles.push_back( (i    ) + (j + 1) * m);
			triangles.push_back( (i + 1) + (j    ) * m);
			triangles.push_back( (i + 1) + (j + 1) * m);
			triangles.push_back( (i    ) + (j + 1) * m);
		}
	}
	mesh_.set_triangles(triangles);

	mIsDirty = true;
}

void VRCurvedMesh::setPos(float x, float y, float z) {
	mPos = android::vec3(x, y, z);
	mIsDirty = true;
}

void VRCurvedMesh::setScale(float sx, float sy, float sz) {
	mScale = android::vec3(sx, sy, sz);
	mIsDirty = true;
}

mat4& VRCurvedMesh::getModelMat() {
	if(mIsDirty) {
		mModelMat = mat4::Scale(mScale.x, mScale.y, mScale.z) *  mat4::Translate(mPos.x, mPos.y, mPos.z) ;
		mIsDirty = false;
	}
	return mModelMat;
}

void VRCurvedMesh::draw(unsigned int program_, unsigned int texture_id, int texTransform, VRCamera &cam) {
	glUseProgram(program_);

	unsigned int a_position_ = glGetAttribLocation(program_, "vPosition");
	unsigned int a_uv_ = glGetAttribLocation(program_, "aTexCord");
	unsigned int u_texture_ = glGetUniformLocation(program_, "uTexCinema");
	unsigned int u_viewmat_ = glGetUniformLocation(program_, "uViewMat");
	unsigned int u_projmat_ = glGetUniformLocation(program_, "uProjMat");
	unsigned int u_modelmat_ = glGetUniformLocation(program_, "uWorldMat");

	ALOGE("a_position = %d, a_uv_ = %d, u_texture_ = %d, u_viewmat_ = %d, u_projmat_ - %d, u_modelmat_ = %d", a_position_,a_uv_, u_texture_,u_viewmat_, u_projmat_, u_modelmat_);

	glBindBuffer(GL_ARRAY_BUFFER, mesh_.vertexVBO());
	glEnableVertexAttribArray(a_position_);
	glVertexAttribPointer(a_position_, 3, GL_FLOAT, GL_FALSE, 0, 0);	

	glBindBuffer(GL_ARRAY_BUFFER, mesh_.uvsVBO());
	glEnableVertexAttribArray(a_uv_);
	glVertexAttribPointer(a_uv_, 2, GL_FLOAT, GL_FALSE, 0, 0);
	
	glActiveTexture(GL_TEXTURE0);
	glBindTexture(GL_TEXTURE_2D, texture_id);
	glUniform1i(u_texture_, 0);

	glUniformMatrix4fv(u_viewmat_, 1, false, cam.getViewMat().Pointer());
	glUniformMatrix4fv(u_projmat_, 1, false,  cam.getProjMat().Pointer());
	glUniformMatrix4fv(u_modelmat_, 1, false, getModelMat().Pointer());

	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mesh_.trianglesVBO());
	
	glDrawElements(GL_TRIANGLES, mesh_.triangles().size(), GL_UNSIGNED_SHORT, 0);
}
	
VRCurvedMesh::~VRCurvedMesh()
{

}

