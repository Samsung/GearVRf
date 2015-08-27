/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// OpenGL ES 2.0 code

#include <jni.h>
#include <android/log.h>

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "VR/VRRenderEngine.h"
#define  LOG_TAG    "libgl2jni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace GVR;

VRRenderEngine *mVREngine;
int gWidth, gHeight;


typedef unsigned char byte;

static byte* framebuffer;
static int framebuffer_size;
static GLuint texture_id[2];
static int view_width, view_height;
static void printGLString(const char *name, GLenum s) {
    const char *v = (const char *) glGetString(s);
    LOGI("GL %s = %s\n", name, v);
}

static void checkGlError(const char* op) {
    for (GLint error = glGetError(); error; error
            = glGetError()) {
        LOGI("after %s() glError (0x%x)\n", op, error);
    }
}

/*static const char gVertexShader[] =
    "attribute vec4 vPosition;\n"
	"attribute vec2 vtexPosition;\n"
	"varying vec2 ftexPosition;\n"
    "void main() {\n"

	"ftexPosition = vtexPosition;\n"
    "  gl_Position = vPosition;\n"
    "}\n";

static const char gFragmentShader[] = 
    "precision mediump float;\n"
	"varying vec2 ftexPosition;\n"
	"uniform sampler2D uTexture;\n"
    "void main() {\n"

	"gl_FragColor = texture2D(uTexture , ftexPosition);\n"
    "}\n";*/

static const char gVertexShader[] =
    "attribute vec4 vPosition;\n"
	"attribute vec2 vtexPosition;\n"
	"varying vec2 ftexPosition;\n"
	"uniform mat4 uViewMat; \n"
	"uniform mat4 uProjMat; \n"
	"void main() {\n"

	"ftexPosition = vtexPosition;\n"
	"  gl_Position = uProjMat * uViewMat * vec4(vPosition.xy, -2.0, 1.0);\n"

    "}\n";

static const char gFragmentShader[] =
    "precision mediump float;\n"
	"varying vec2 ftexPosition;\n"
	"uniform sampler2D uTexture;\n"
    "void main() {\n"

	"gl_FragColor = texture2D(uTexture , ftexPosition);\n"
    "}\n";

GLuint loadShader(GLenum shaderType, const char* pSource) {
    GLuint shader = glCreateShader(shaderType);
    if (shader) {
        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {
            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {
                char* buf = (char*) malloc(infoLen);
                if (buf) {
                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE("Could not compile shader %d:\n%s\n",
                            shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}

GLuint createProgram(const char* pVertexSource, const char* pFragmentSource) {
    GLuint vertexShader = loadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        return 0;
    }

    GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char* buf = (char*) malloc(bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOGE("Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}

GLuint gProgram;
GLuint gvPositionHandle;
GLuint gvTexPositionHandle;
GLuint gTextureHandle;
GLuint test;

bool setupGraphics(int w, int h) {
    printGLString("Version", GL_VERSION);
    printGLString("Vendor", GL_VENDOR);
    printGLString("Renderer", GL_RENDERER);
    printGLString("Extensions", GL_EXTENSIONS);
    view_width = w ;
    view_height = h;
    framebuffer_size = 4*view_width*view_height;
     framebuffer = (byte*)calloc(framebuffer_size, sizeof(byte));
     for (int i = 0; i < framebuffer_size; i++)
     {
    	 if(i%4 == 0 || (i+1)%4 == 0) framebuffer[i] = 255;
    	 else framebuffer[i] = 0;
     }

    LOGI("setupGraphics(%d, %d)", w, h);


    gProgram = createProgram(gVertexShader, gFragmentShader);
    if (!gProgram) {
        LOGE("Could not create program.");
        return false;
    }

    gTextureHandle = glGetUniformLocation(gProgram , "uTexture");
    glUniform1i(gTextureHandle, 0);

    gvPositionHandle = glGetAttribLocation(gProgram, "vPosition");
    checkGlError("glGetAttribLocation");
    LOGI("glGetAttribLocation(\"vPosition\") = %d\n",
            gvPositionHandle);

    gvTexPositionHandle = glGetAttribLocation(gProgram , "vtexPosition");
    checkGlError("gvTexPositionHandle");
        LOGI("glGetAttribLocation(\"vtexPosition\") = %d\n",
        		gvTexPositionHandle);

        mVREngine = new VRRenderEngine(w,h);
        mVREngine->setCinemaMode(VR_SCENE_CINEMAHALL);
        mVREngine->setSensorType(VR_SENSOR_K);
        gWidth = w;
        gHeight = h;


    glViewport(0, 0, w, h);
    checkGlError("glViewport");
    return true;
}

const GLfloat gTriangleVertices[] = {
		1.0f , 1.0f ,
		-1.0f , 1.0f,
		-1.0f , -1.0f ,
		-1.0f , -1.0f ,
		1.0f , -1.0f,
		1.0f , 1.0f  };
/*const GLfloat gTextureCoordinates[] ={
		1 , 1,
		0 , 1 ,
		0 , 0 ,
		0 , 0,
		1 , 0 ,
		1 , 1
};*/
const GLfloat gTextureCoordinates[] ={
		1 , 0,
		1 , 1 ,
		0 ,1 ,
		0 , 1,
		0 , 0 ,
		1 , 0
};

void renderFrame(int texid) {
    static float grey = 0.01f;
    /*grey += 0.01f;
    if (grey > 1.0f) {
        grey = 0.0f;
    }*/
   /* glClearColor(1.0f, 1.0f, grey, 1.0f);
    checkGlError("glClearColor");
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    checkGlError("glClear");

    glUseProgram(gProgram);
    checkGlError("glUseProgram");
    //LOGI("progid =%d" , gProgram );
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texid);
    //LOGI("texid =%d" , texid );
   // glGenTextures(1, &texture_id[0]);
   //  glActiveTexture(GL_TEXTURE0);
    // glBindTexture(GL_TEXTURE_2D, texture_id[0]);
    // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    // glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    // glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, view_width, view_height, 0, GL_RGBA, GL_UNSIGNED_BYTE, framebuffer);

    glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
    checkGlError("glVertexAttribPointer");
    glEnableVertexAttribArray(gvPositionHandle);
    checkGlError("glEnableVertexAttribArray");

    glVertexAttribPointer(gvTexPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinates);
    glEnableVertexAttribArray(gvTexPositionHandle);

    glDrawArrays(GL_TRIANGLES, 0, 6);

    checkGlError("glDrawArrays");*/

    if (grey > 1.0f) {
        grey = 0.0f;
    }
    float viewMat[16], projMat[16];
    mVREngine->beginLeftFrame(viewMat, projMat);
    glClearColor(grey, grey, grey, 0.0f);
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    glUseProgram(gProgram);

    checkGlError("glUseProgram");
       //LOGI("progid =%d" , gProgram );
       glActiveTexture(GL_TEXTURE0);
       glBindTexture(GL_TEXTURE_2D, texid);
    int viewMatLoc = glGetUniformLocation(gProgram, "uViewMat");
    int projMatLoc = glGetUniformLocation(gProgram, "uProjMat");
    LOGI("RenderFrame : viewMatloc = %d, projMatLoc = %d", viewMatLoc, projMatLoc);
    glUniformMatrix4fv(viewMatLoc, 1, false, viewMat);
    glUniformMatrix4fv(projMatLoc, 1, false, projMat);
    glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
        checkGlError("glVertexAttribPointer");
        glEnableVertexAttribArray(gvPositionHandle);
        checkGlError("glEnableVertexAttribArray");

        glVertexAttribPointer(gvTexPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinates);
        glEnableVertexAttribArray(gvTexPositionHandle);

        glDrawArrays(GL_TRIANGLES, 0, 6);
    mVREngine->endLeft();


    mVREngine->beginRightFrame(viewMat, projMat);
    glClearColor(grey, grey, grey, 1.0f);
    glClear( GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    glUseProgram(gProgram);
    checkGlError("glUseProgram");
       //LOGI("progid =%d" , gProgram );
       glActiveTexture(GL_TEXTURE0);
       glBindTexture(GL_TEXTURE_2D, texid);
    LOGI("RenderFrame : viewMatloc = %d, projMatLoc = %d", viewMatLoc, projMatLoc);
    glUniformMatrix4fv(viewMatLoc, 1, false, viewMat);
    glUniformMatrix4fv(projMatLoc, 1, false, projMat);
    glVertexAttribPointer(gvPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTriangleVertices);
        checkGlError("glVertexAttribPointer");
        glEnableVertexAttribArray(gvPositionHandle);
        checkGlError("glEnableVertexAttribArray");

        glVertexAttribPointer(gvTexPositionHandle, 2, GL_FLOAT, GL_FALSE, 0, gTextureCoordinates);
        glEnableVertexAttribArray(gvTexPositionHandle);

        glDrawArrays(GL_TRIANGLES, 0, 6);
    mVREngine->endRight();


    //glDeleteTextures(1, &texture_id[0]);
}
//com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets
extern "C" {
    JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_init(JNIEnv * env, jobject obj,  jint width, jint height);
    JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_step(JNIEnv * env, jobject obj , jint texid);
};

JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_init(JNIEnv * env, jobject obj,  jint width, jint height)
{
    setupGraphics(width, height);
}

JNIEXPORT void JNICALL Java_com_qualcomm_vuforia_samples_VuforiaSamples_app_ImageTargets_GL2JNILib_step(JNIEnv * env, jobject obj , jint texid)
{
    renderFrame(texid);
}
