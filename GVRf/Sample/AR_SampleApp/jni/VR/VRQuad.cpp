#include "VRQuad.h"

// b.mathew
// 04 - dec - 2013

using namespace GVR;

VRQuad :: VRQuad() {
	mVertices  =  new float[16] 
			{
			-1.0f, -1.0f, -1.0f, 1.0f,
			1.0f, -1.0f, -1.0f,  1.0f,
			-1.0f, 1.0f, -1.0f, 1.0f,
			 1.0f, 1.0f, -1.0f, 1.0f
			};


	mTexCords  = new float[8] { 
				0.0f, 1.0f, 
				1.0f, 1.0f,
				0.0f, 0.0f,
				1.0f, 0.0f
			};

}


VRQuad :: ~VRQuad() {

	if(mVertices) {
		delete[] mVertices;
		mVertices = 0;
	}

	if(mTexCords) {
		delete[] mTexCords;
		mTexCords = 0;
	}
}

void VRQuad :: draw(VRParams *vrparams, VRCamera *camRef,  GVR::mat4 &transMatrix,unsigned int texId, unsigned int target, const uint32_t texTransform) {

	glUseProgram(vrparams->mShaderId);	

	glDisable(GL_DEPTH_TEST);

	unsigned  int bgTexId = 0;

	if(texTransform == 0) { // Rotate 90 degs
		mTexCords[0] =  0.0f; mTexCords[1] = 1.0f; 
		mTexCords[2] =  0.0f; mTexCords[3] = 0.0f;
		mTexCords[4] =  1.0f; mTexCords[5] = 1.0f;
		mTexCords[6] =  1.0f; mTexCords[7] = 0.0f;	
	} else {
		mTexCords[0] =  0.0f; mTexCords[1] = 0.0f; 
		mTexCords[2] =  1.0f; mTexCords[3] = 0.0f;
		mTexCords[4] =  0.0f; mTexCords[5] = 1.0f;
		mTexCords[6] =  1.0f; mTexCords[7] = 1.0f;	
	}
	

	glVertexAttribPointer(glGetAttribLocation(vrparams->mShaderId, "vPosition"), 4, GL_FLOAT, GL_FALSE, 0, mVertices);
	glEnableVertexAttribArray(glGetAttribLocation(vrparams->mShaderId, "vPosition"));


	glVertexAttribPointer(glGetAttribLocation(vrparams->mShaderId, "aTexCord"), 2, GL_FLOAT, GL_FALSE, 0, mTexCords);
	glEnableVertexAttribArray(glGetAttribLocation(vrparams->mShaderId, "aTexCord"));

	glUniformMatrix4fv(glGetUniformLocation(vrparams->mShaderId, "uViewMat"), 1, false, camRef->getViewMat().Pointer());
	glUniformMatrix4fv(glGetUniformLocation(vrparams->mShaderId, "uProjMat"), 1, false, camRef->getProjMat().Pointer());
	
	glUniformMatrix4fv(glGetUniformLocation(vrparams->mShaderId, "uWorldMat"), 1, false, transMatrix.Pointer());
	glActiveTexture (GL_TEXTURE0);
	glBindTexture(target, texId);

	glUniform1i(glGetUniformLocation(vrparams->mShaderId,"uTexCinema"), 0);
	glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	glEnable(GL_DEPTH_TEST);
}


