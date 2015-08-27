#ifndef VRMESH_H_
#define VRMESH_H_

// b.mathew - ported from GVRF

#include "GLES2/gl2.h"
#include <vector>
#include "ui/vec2.h"
#include "ui/vec3.h"

namespace GVR {

class VRMesh {
public:
	VRMesh()
	: vertices_(std::vector<android::vec3>())
	, triangles_(std::vector<unsigned short>())
	{
		mVertexVBO = 0;
		mTexCordVBO = 0;
		mIndicesVBO = 0;
	}
	~VRMesh()
	{
		vertices_.clear();
		normals_.clear();
		uvs_.clear();
		triangles_.clear();

		if(mVertexVBO) {
			glDeleteBuffers(1, &mVertexVBO);
			mVertexVBO = 0;
		}

		if(mTexCordVBO) {
			glDeleteBuffers(1, &mTexCordVBO);
			mTexCordVBO = 0;
		}

		if(mIndicesVBO) {
			glDeleteBuffers(1, &mIndicesVBO);
			mIndicesVBO = 0;
		}			
		
				
		/*android::Vector<android::vec3> vertices;
		vertices.swap(vertices_);
		android::Vector<android::vec3> normals;
		normals.swap(normals_);
		android::Vector<android::vec2> uvs;
		uvs.swap(uvs_);
		android::Vector<unsigned short> triangles;
		triangles.swap(triangles_);*/
	}
	void set_vertices(const std::vector<android::vec3>& vertices)
	{
		vertices_ = vertices;

		/*if(mVertexVBO) {
			glDeleteBuffers( 1, &mVertexVBO);
		}*/
		glGenBuffers(1, &mVertexVBO);
		glBindBuffer(GL_ARRAY_BUFFER, mVertexVBO);
		glBufferData(GL_ARRAY_BUFFER, sizeof(float)*vertices.size() * 3, &(vertices.at(0)), GL_STATIC_DRAW);
			
		glBindBuffer(GL_ARRAY_BUFFER, 0);

	}
	const GLuint vertexVBO() const {
		return mVertexVBO;
	}
	
	const std::vector<android::vec3>& vertices() const
	{
		return vertices_;
	}
	void set_normals(const std::vector<android::vec3>& normals)
	{
		normals_ = normals;
	}
	const std::vector<android::vec3>& normals() const
	{
		return normals_;
	}
	void set_uvs(const std::vector<android::vec2>& uvs)
	{
		uvs_ = uvs;
		
		/*if(mTexCordVBO) {
			glDeleteBuffers( 1, &mTexCordVBO);
		}*/
		glGenBuffers(1, &mTexCordVBO);
		glBindBuffer(GL_ARRAY_BUFFER, mTexCordVBO);
		glBufferData(GL_ARRAY_BUFFER, sizeof(float)*uvs.size() * 2 , &(uvs.at(0)), GL_STATIC_DRAW);
			
		glBindBuffer(GL_ARRAY_BUFFER, 0);

	}
	const GLuint uvsVBO() const {
		return mTexCordVBO;
	}
	const std::vector<android::vec2>& uvs() const
	{
		return uvs_;
	}
	void set_triangles(const std::vector<unsigned short>& triangles)
	{
		triangles_ = triangles;

		/*if(mIndicesVBO) {
			glDeleteBuffers( 1, &mIndicesVBO);
		}*/
		
		glGenBuffers(1, &mIndicesVBO);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIndicesVBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(unsigned short)*triangles.size(), &(triangles.at(0)), GL_STATIC_DRAW);
			
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}

	const GLuint trianglesVBO() const {
		return mIndicesVBO;
	}
	const std::vector<unsigned short>& triangles() const
	{
		return triangles_;
	}

private:
	std::vector<android::vec3> vertices_;
	std::vector<android::vec3> normals_;
	std::vector<android::vec2> uvs_;
	std::vector<unsigned short> triangles_;

	GLuint mVertexVBO;
	GLuint mTexCordVBO;
	GLuint mIndicesVBO;
};
};
#endif /* VRMesh_H_ */

