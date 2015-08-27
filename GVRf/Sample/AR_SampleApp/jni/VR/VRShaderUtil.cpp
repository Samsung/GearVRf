#include "VRShaderUtil.h"
#include "VRLogger.h"

using namespace GVR;


VRShaderUtil VRShaderUtil::mInstance;

VRShaderUtil::VRShaderUtil() {
	
}
 
VRShaderUtil& VRShaderUtil::instance(){
	return mInstance;
}

int VRShaderUtil::compileShader(const char shaderSrc[], int glShaderType) {
	int shader = -1;
	int isShaderCompiled = 0;

	shader = glCreateShader(glShaderType);
	glShaderSource(shader, 1, (const char**) &shaderSrc, NULL);
	glCompileShader (shader);
	glGetShaderiv(shader, GL_COMPILE_STATUS, &isShaderCompiled);
	
	if (!isShaderCompiled)
	{
		int logLength = 0, charsWritten = 0;
		glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &logLength);
		char* infoLog = new char[logLength];
		glGetShaderInfoLog(shader, logLength, &charsWritten, infoLog);
		
		if(glShaderType == GL_VERTEX_SHADER)
			VR_LOGE("Vertex Shader Compilation Failed : %s", infoLog);
		else 
			VR_LOGE("Fragment Shader Compilation Failed : %s", infoLog);
		
		delete[] infoLog;
		glDeleteShader(shader);
		return -1;
	}
	VR_LOGI("Shader Created Successfully !! ShaderId = %d", shader);
	return shader;
}

void VRShaderUtil::deleteShader(int shaderId) {
	if(shaderId != -1) {
		glDeleteShader(shaderId);
		VR_LOGI("Shader Deleted !! ShaderId = %d", shaderId);
		shaderId = -1;
	}
}

int VRShaderUtil::createProgram(int vs, int fs) {
	int program = -1;
	
	program = glCreateProgram();
	glAttachShader(program, vs);
	glAttachShader(program, fs);
	glLinkProgram (program);

	GLint bLinked = 0;
	glGetProgramiv(program, GL_LINK_STATUS, &bLinked);

	if (!bLinked)
	{
		deleteShader(vs);
		deleteShader(fs);
		deleteProgram(program);
		
		VR_LOGE("Program Link Failed");
		return -1;
	}
	
	return program;
}

int VRShaderUtil::createProgram(const char vShaderSrc[], const char fShaderSrc[]) {
	
	if(vShaderSrc == 0 || fShaderSrc == 0) {
		VR_LOGE("Invalid Shader Src, vShaderSrc = %d, fShaderSrc = %d", vShaderSrc, fShaderSrc);
		return -1;
	}
	
	int vs = compileShader(vShaderSrc, GL_VERTEX_SHADER);
	int fs = compileShader(fShaderSrc, GL_FRAGMENT_SHADER);

	if(vs == -1 || fs == -1) {
		VR_LOGE("Error creating vertex shader or fragment shader, vs = %d, fs =%d", vs, fs);
		return -1;
	}
		
	int program = createProgram(vs, fs);
	VR_LOGI("Program created!!, porgramId = %d", program);
	return program;
}

void VRShaderUtil::deleteProgram(int programId) {
	if(programId != -1) {
		glDeleteProgram(programId);
		VR_LOGI("Program deleted !! programId=%d", programId);
		programId = -1;
	}
}

