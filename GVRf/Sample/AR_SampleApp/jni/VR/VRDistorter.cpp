#include "VRShaderUtil.h"
#include "VRDistorter.h"
#include "VRLogger.h"

using namespace GVR;

VRDistorter::VRDistorter(VRSurfaceType type) {

	static const char fragment_shader_distortion[] =
		"precision mediump float;\n" //"#extension GL_OES_EGL_image_external : require\n"
		"uniform sampler2D uSampler0; \n"     //"uniform samplerExternalOES uTexCinema; \n"  
		"varying vec2 vTexCord; \n"
		"void main() \n"
		"{\n"
			 "vec2 vTexCord = outTexCoords;\n"
			 "ScreenCenter = vec2(0.5,0.5);\n"
			 "vec2 theta = (vTexCord - LensCenter) * 2.0 - 1.0;\n"
			 "float rSq = theta.x * theta.x + theta.y * theta.y;\n"
			"vec2 rvector = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq + HmdWarpParam.z * rSq * rSq + HmdWarpParam.w * rSq * rSq * rSq);\n"
			"vec2 NewPosition = ((rvector + LensCenter) + vec2(1.0)) / 2.0;\n"
			"vec2 ClampedPos = clamp(NewPosition, ScreenCenter - vec2(0.5, 0.5), ScreenCenter + vec2(0.5, 0.5)) - NewPosition;\n"
			"if(any(notEqual(ClampedPos, vec2(0.0))) ) \n"
			"gl_FragColor = vec4(0.0); \n"
			"else \n"
			"gl_FragColor =  texture2D(uSampler0, NewPosition) ;\n";
		"}\n";	

	static const char fragment_shader_distortion_oes[] =
		"#extension GL_OES_EGL_image_external : require\n"
		"precision mediump float;\n" 
	       "uniform samplerExternalOES uSampler0; \n"  
		"varying vec2 vTexCord; \n"
		"void main() \n"
		"{\n"
			 "vec2 vTexCord = outTexCoords;\n"
			 "ScreenCenter = vec2(0.5,0.5);\n"
			 "vec2 theta = (vTexCord - LensCenter) * 2.0 - 1.0;\n"
			 "float rSq = theta.x * theta.x + theta.y * theta.y;\n"
			"vec2 rvector = theta * (HmdWarpParam.x + HmdWarpParam.y * rSq + HmdWarpParam.z * rSq * rSq + HmdWarpParam.w * rSq * rSq * rSq);\n"
			"vec2 NewPosition = ((rvector + LensCenter) + vec2(1.0)) / 2.0;\n"
			"vec2 ClampedPos = clamp(NewPosition, ScreenCenter - vec2(0.5, 0.5), ScreenCenter + vec2(0.5, 0.5)) - NewPosition;\n"
			"if(any(notEqual(ClampedPos, vec2(0.0))) ) \n"
			"gl_FragColor = vec4(0.0); \n"
			"else \n"
			"gl_FragColor =  texture2D(uSampler0, NewPosition) ;\n";
		"}\n";	
	


	
	const char vertex_shader[] =
		"attribute vec4 a_position;\n"
		"attribute vec4 a_uv;\n"
		"varying vec2 v_uv;\n"
		"void main() {\n"
		"  v_uv = a_uv.xy;\n"
		"  gl_Position = a_position;\n"
		"}\n";

	const char fragment_shader_postdistortion[] =
		"precision mediump float;\n"
		"uniform sampler2D u_texture;\n"
		"varying vec2 v_uv;\n"
		"void main() {\n"
		"  if(v_uv.x < 0.0 || v_uv.x > 1.0 || v_uv.y < 0.0 || v_uv.y > 1.0)\n"
		"  {\n"
		"    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);\n"
		"  }\n"
		"  else\n"
		"  {\n"
		"    gl_FragColor = texture2D(u_texture, v_uv);\n"
		"  }\n"
		"}\n";

	const char vertex_shader_invtexcord[] =
		"attribute vec4 a_position;\n"
		"attribute vec4 a_uv;\n"
		"varying vec2 v_uv;\n"
		"void main() {\n"
		"  v_uv = vec2(a_uv.x, 1.0-a_uv.y);\n"
		"  gl_Position = a_position;\n"
		"}\n";

	const char fragment_shader_oes[] =
		"#extension GL_OES_EGL_image_external : require \n"
		"precision mediump float;\n"
		"uniform samplerExternalOES u_texture;\n"
		"varying vec2 v_uv;\n"
		"void main() {\n"
		"  if(v_uv.x < 0.0 || v_uv.x > 1.0 || v_uv.y < 0.0 || v_uv.y > 1.0)\n"
		"  {\n"
		"    gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);\n"
		"  }\n"
		"  else\n"
		"  {\n"
		"    gl_FragColor = texture2D(u_texture, v_uv);\n"
		"  }\n"
		"}\n";

	mProgram = -1;
	mProgramOES = -1;
	mProgram = VRShaderUtil::instance().createProgram(vertex_shader, fragment_shader_postdistortion);
	mProgramOES = VRShaderUtil::instance().createProgram(vertex_shader_invtexcord, fragment_shader_oes);
	program_ = mProgram;
	a_position_ = glGetAttribLocation(program_, "a_position");
	a_uv_ = glGetAttribLocation(program_, "a_uv");
	u_texture_ = glGetUniformLocation(program_, "u_texture");

	mUseDistortion  = true;
	mUseVertexDistortion = true;
	mUseFragmentDistortion = false;

	mLDistGrid = new VRDistortionGrid(40, 40, 2560 , 1440, type, VR_RENDERPASS_LEFT); // 1920 1080
	mRDistGrid = new VRDistortionGrid(40, 40, 2560 , 1440, type, VR_RENDERPASS_RIGHT); //1920 1080
}

VRDistorter::~VRDistorter() {
	if(mProgram != -1) {
		VRShaderUtil::instance().deleteProgram(mProgram);
		mProgram = 0;
	}
	if(mProgramOES != -1) {
		VRShaderUtil::instance().deleteProgram(mProgramOES);
		mProgramOES = 0;
	}

	if(mLDistGrid) {
		delete mLDistGrid;
		mLDistGrid = 0;
	}

	if(mRDistGrid) {
		delete mRDistGrid;
		mRDistGrid = 0;
	}
}

void VRDistorter::setDistortion(bool flag) {
	mUseDistortion = flag;
}

void VRDistorter::setVertexDistortion(bool flag) {
	mUseVertexDistortion = flag;

	if(mUseVertexDistortion) {
		mUseFragmentDistortion = false;
	} else  {
		mUseFragmentDistortion = true;
	}

}

void VRDistorter::setFragmentDistortion(bool flag) {
	mUseFragmentDistortion = flag;
	
	if(mUseFragmentDistortion) {
		mUseVertexDistortion = false;
	} else {
		mUseVertexDistortion = true;
	}
}

void VRDistorter::render(GLuint texId, VRRenderMode mode, VRRenderPass pass) {
	// b.mathew : check for vertex or fragment shader distortion and no distortion.. 

	if(pass == VR_RENDERPASS_LEFT)
		render(*mLDistGrid, texId, mode);
	else 
		render(*mRDistGrid, texId, mode);
}

void VRDistorter::render(const VRDistortionGrid& distortion_grid, GLuint texture_id, VRRenderMode mode)
{
	if(mode == VR_RENDERMODE_CINEMA)
		program_ = mProgram;
	else if(mode == VR_RENDERMODE_VR)
		program_ = mProgram;
	
	glUseProgram(program_);

	mUseDistortion  = true;

	if(mUseDistortion) {
		glBindBuffer(GL_ARRAY_BUFFER, distortion_grid.mesh().vertexVBO());
		glEnableVertexAttribArray(a_position_);
		glVertexAttribPointer(a_position_, 3, GL_FLOAT, GL_FALSE, 0, 0);	
	}
	else 
		glVertexAttribPointer(a_position_, 3, GL_FLOAT, GL_FALSE, 0, distortion_grid.quad().vertices().array());

	
	if(mUseDistortion) {
		glBindBuffer(GL_ARRAY_BUFFER, distortion_grid.mesh().uvsVBO());
		glEnableVertexAttribArray(a_uv_);
		glVertexAttribPointer(a_uv_, 2, GL_FLOAT, GL_FALSE, 0, 0);
	}
	else { 
		glVertexAttribPointer(a_uv_, 2, GL_FLOAT, GL_FALSE, 0, distortion_grid.quad().uvs().array());
	}


	glActiveTexture(GL_TEXTURE0);
	glBindTexture(GL_TEXTURE_2D, texture_id);
	glUniform1i(u_texture_, 0);


	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, distortion_grid.mesh().trianglesVBO());
	
	if(mUseDistortion)
		glDrawElements(GL_TRIANGLES, distortion_grid.mesh().triangles().size(), GL_UNSIGNED_SHORT, 0);
	else
		glDrawElements(GL_TRIANGLE_STRIP, distortion_grid.quad().triangles().size(), GL_UNSIGNED_SHORT, /*0*/distortion_grid.quad().triangles().array());
	
	glBindBuffer( GL_ARRAY_BUFFER, 0);
	glBindBuffer( GL_ELEMENT_ARRAY_BUFFER, 0);

	
	/*glDisableVertexAttribArray(a_position_);
	glDisableVertexAttribArray(a_uv_);*/
}


