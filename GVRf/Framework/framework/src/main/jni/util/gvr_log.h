/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/***************************************************************************
 * Logging macros.
 ***************************************************************************/

#ifndef LOG_H_
#define LOG_H_

#include <android/log.h>
#include <exception>

#include "GLES3/gl3.h"

#define  LOG_TAG    "gvrf"
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

//#define STOP_ON_ERROR
//#define GL( func )      func; checkGLError(#func);
#define GL( func )      func;

//#define clearGLError(msg)
#define clearGLError(msg) checkGLError(msg);

static bool DEBUG_RENDERER = false; //printf() or glGetError() per frame is expensive

static const char * GlErrorString( GLenum error )
{
    switch ( error )
    {
        case GL_NO_ERROR:                       return "GL_NO_ERROR";
        case GL_INVALID_ENUM:                   return "GL_INVALID_ENUM";
        case GL_INVALID_VALUE:                  return "GL_INVALID_VALUE";
        case GL_INVALID_OPERATION:              return "GL_INVALID_OPERATION";
        case GL_INVALID_FRAMEBUFFER_OPERATION:  return "GL_INVALID_FRAMEBUFFER_OPERATION";
        case GL_OUT_OF_MEMORY:                  return "GL_OUT_OF_MEMORY";
        default: return "unknown";
    }
}

static void checkGLError(const char* name)
{
#ifdef STOP_ON_ERROR
    bool error = false;
#endif
    for (int i = 0; i < 10; ++i) {
        const GLenum error = glGetError();
        if (GL_NO_ERROR == error) {
            break;
        }
        LOGE("%s error: %s", name, GlErrorString(error));
#ifdef STOP_ON_ERROR
        error = true;
#endif
    }

#ifdef STOP_ON_ERROR
    if (error) {
        std::terminate();
    }
#endif
}

static const char* frameBufferStatusString(GLenum status) {
    switch (status) {
    case GL_FRAMEBUFFER_UNDEFINED:
        return "GL_FRAMEBUFFER_UNDEFINED";
    case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
        return "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
    case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
        return "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
    case GL_FRAMEBUFFER_UNSUPPORTED:
        return "GL_FRAMEBUFFER_UNSUPPORTED";
    case GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE:
        return "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
    default:
        return "unknown";
    }
}

//#define FAIL(...)
#define FAIL(msg, ...) do { LOGE(msg, ##__VA_ARGS__); std::terminate(); } while(0)

#endif
