#ifndef _VRShaderUtil_h
#define _VRShaderUtil_h

#include <GLES2/gl2.h>

namespace GVR {

	static const char gDefVShader[] = 
		"attribute vec4 vPosition; \n"
		"attribute  vec2 aTexCord;	\n"
		"uniform mat4 uViewMat; \n"
		"uniform mat4 uProjMat; \n"
		"uniform mat4 uWorldMat;\n"
		"varying	vec2 vTexCord;		\n"
		"void main()                  \n"
		"{                            \n"
			"gl_Position =  uProjMat * uViewMat * uWorldMat * vPosition;  \n"
			"vTexCord	= aTexCord; \n"
		"}\n"; //

	static const char gCurvedScreenVShader[] = 
		"attribute vec4 vPosition; \n"
		"attribute  vec2 aTexCord;	\n"
		"uniform mat4 uViewMat; \n"
		"uniform mat4 uProjMat; \n"
		"uniform mat4 uWorldMat;\n"
		"varying	vec2 vTexCord;		\n"
		"void main()                  \n"
		"{                            \n"
		"	gl_Position =  uProjMat * uViewMat * uWorldMat * vPosition;  \n"
		"	vTexCord = aTexCord; \n"
		"}\n"; //
//				"      vec4 newPos = vPosition; \n"
		//	"  	newPos.z -= 1.2 * sin(3.14 * ( newPos.x + 1.0) * 0.5); \n"

	static const char gDefFShader[] =
		"precision mediump float;\n"
		"uniform sampler2D uTexCinema; \n"     
		"varying vec2 vTexCord; \n"
		"void main() \n"
		"{\n"
		"	gl_FragColor =  texture2D(uTexCinema, vTexCord); \n"
		"}\n";	

	static const char gCurvedScreenFShader[] =
		"precision mediump float;\n" //"#extension GL_OES_EGL_image_external : require\n"
		"uniform sampler2D uTexCinema; \n"     //"uniform samplerExternalOES uTexCinema; \n"  
		"varying vec2 vTexCord; \n"
		"void main() \n"
		"{\n"
		"	gl_FragColor = texture2D(uTexCinema, vTexCord);  \n"
		"}\n";	
		
	static const char gExtOESFShader[] =
		"#extension GL_OES_EGL_image_external : require\n" //"precision mediump float;\n" 
		"precision mediump float;\n"
		"uniform samplerExternalOES uTexCinema; \n"  //"uniform sampler2D uTexCinema; \n"     
		"varying vec2 vTexCord; \n"
		"void main() \n"
		"{\n"
		"	gl_FragColor =  texture2D(uTexCinema, vTexCord); \n"
		"}\n";	

	
	/*static const char gCurvedScreenFShader[] =
		"precision mediump float;\n" //"#extension GL_OES_EGL_image_external : require\n"
		"uniform sampler2D uTexCinema; \n"     //"uniform samplerExternalOES uTexCinema; \n"  
		"varying vec2 vTexCord; \n"
		"void main() \n"
		"{\n"
		"	gl_FragColor = texture2D(uTexCinema, vTexCord);  \n"
		"}\n";	*/

	static const char gSceneVShader[] =
		"attribute vec3 pos;\n"
		"attribute vec2 a_TexCoordinate;\n"
		"uniform mat4 u_MVPMatrix;\n"
		"varying vec2 v_TexCoordinate;\n"
		"void main() {\n"
		"	v_TexCoordinate = vec2(a_TexCoordinate.x, 1.0 - a_TexCoordinate.y);\n"
		"	gl_Position = u_MVPMatrix * vec4(pos, 1.0) ;\n"
		"}\n";

	static const char gSceneFShader[] =
		"precision mediump float;\n"
		"uniform sampler2D u_Texture;\n"
		"varying vec2 v_TexCoordinate;\n"
		"void main() {\n"
		"	vec4 tmp =  texture2D(u_Texture, v_TexCoordinate);\n"
		"	gl_FragColor = tmp;\n"
		"}\n";

	static const char gSceneBoneAnimVShader[] =

		"const int MAX_BONES = 60;\n"

		"uniform mat4 finalMat[MAX_BONES];\n"
		"uniform mat4 u_MVPMatrix;\n"

		"attribute vec4 boneId;\n"
		"attribute vec4 boneWt;\n"

		"attribute vec3 pos;\n"
		"attribute vec2 a_TexCoordinate;\n"
		"varying vec2 v_TexCoordinate;\n"

		"void main()\n"
		"{\n"
		"	vec4 posOrg = vec4(pos, 1.0);\n"
		"	vec4 posTmp = vec4(pos, 1.0);\n"
		"	mat4 finalTrans = mat4(1,0,0,0,0,1,0,0,0,0,1,0,0,0,0,1);\n"
		"	gl_Position = u_MVPMatrix * finalTrans * posTmp;\n"
		"	v_TexCoordinate = a_TexCoordinate;\n"
		"}\n";

	static const char gSceneBoneAnimFShader[] =
		"precision mediump float;\n"
		"uniform sampler2D u_Texture;\n"
		"varying vec2 v_TexCoordinate;\n"
		"void main() {\n"
		"	gl_FragColor =  texture2D(u_Texture, v_TexCoordinate);\n"
		"}\n";
/*
		"int indx = int(boneId.x);\n"
		"if(indx != -1 && indx < MAX_BONES)\n"
		"{\n"
		"finalTrans = finalMat[indx] * boneWt.x;\n"
		"}\n"

		"indx = int(boneId.y);\n"
		"if(indx != -1 && indx < MAX_BONES)\n"
		"{\n"
		"finalTrans += finalMat[indx] * boneWt.y ;\n"
		"}\n"

		"indx = int(boneId.z);\n"
		"if(indx != -1 && indx < MAX_BONES)\n"
		"{\n"
		"finalTrans += finalMat[indx] * boneWt.z;\n"
		"}\n"

		"indx = int(boneId.w);\n"
		"if(indx != -1 && indx < MAX_BONES)\n"
		"{\n"
		"finalTrans += finalMat[indx] * boneWt.w;\n"
		"}\n"
*/
	class VRShaderUtil {

		public:
			static VRShaderUtil& instance();
			static int compileShader(const char shaderSrc[], int glShaderType);
			static void deleteShader(int shaderId);
			static int createProgram(int vertexShader, int fragmentShader);
			static int createProgram(const char vShaderSrc[], const char fShaderSrc[]);
			static void deleteProgram(int programId);

		private:
			VRShaderUtil();

			static VRShaderUtil mInstance;
		
	};
};

#endif // _VRShaderManager_h
