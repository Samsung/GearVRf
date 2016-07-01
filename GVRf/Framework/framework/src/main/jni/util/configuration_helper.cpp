/* Copyright 2016 Samsung Electronics Co., LTD
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

#include "configuration_helper.h"
#include "jni_utils.h"
#include "util/gvr_log.h"

static const char* app_settings_name = "org/gearvrf/utility/VrAppSettings";

namespace gvr {

ConfigurationHelper::ConfigurationHelper(JNIEnv& env, jobject vrAppSettings)
    : env_(env), vrAppSettings_(vrAppSettings)
{
    vrAppSettings_ = env.NewGlobalRef(vrAppSettings);
    vrAppSettingsClass_ = GetGlobalClassReference(env, app_settings_name);
}

ConfigurationHelper::~ConfigurationHelper() {
    env_.DeleteGlobalRef(vrAppSettingsClass_);
    env_.DeleteGlobalRef(vrAppSettings_);
}

void ConfigurationHelper::getFramebufferConfiguration(JNIEnv& env, int& fbWidthOut, int& fbHeightOut,
        const int fbWidthDefault, const int fbHeightDefault, int& multiSamplesOut,
        ovrTextureFormat& colorTextureFormatOut, bool& resolveDepthOut, ovrTextureFormat& depthTextureFormatOut)
{
    LOGV("ConfigurationHelper: --- framebuffer configuration ---");

    jfieldID fid = env.GetFieldID(vrAppSettingsClass_, "eyeBufferParms", "Lorg/gearvrf/utility/VrAppSettings$EyeBufferParms;");
    const jobject parms = env.GetObjectField(vrAppSettings_, fid);
    const jclass parmsClass = env.GetObjectClass(parms);

    fid = env.GetFieldID(parmsClass, "resolutionWidth", "I");
    fbWidthOut = env.GetIntField(parms, fid);
    if (-1 == fbWidthOut) {
        env.SetIntField(parms, fid, fbWidthDefault);
        fbWidthOut = fbWidthDefault;
    }
    LOGV("ConfigurationHelper: --- width %d", fbWidthOut);

    fid = env.GetFieldID(parmsClass, "resolutionHeight", "I");
    fbHeightOut = env.GetIntField(parms, fid);
    if (-1 == fbHeightOut) {
        env.SetIntField(parms, fid, fbHeightDefault);
        fbHeightOut = fbHeightDefault;
    }
    LOGV("ConfigurationHelper: --- height: %d", fbHeightOut);

    fid = env.GetFieldID(parmsClass, "multiSamples", "I");
    multiSamplesOut = env.GetIntField(parms, fid);
    LOGV("ConfigurationHelper: --- multisamples: %d", multiSamplesOut);

    fid = env.GetFieldID(parmsClass, "colorFormat", "Lorg/gearvrf/utility/VrAppSettings$EyeBufferParms$ColorFormat;");
    jobject textureFormat = env.GetObjectField(parms, fid);
    jmethodID mid = env.GetMethodID(env.GetObjectClass(textureFormat),"getValue","()I");
    int textureFormatValue = env.CallIntMethod(textureFormat, mid);
    switch (textureFormatValue){
    case 0:
        colorTextureFormatOut = VRAPI_TEXTURE_FORMAT_565;
        break;
    case 1:
        colorTextureFormatOut = VRAPI_TEXTURE_FORMAT_5551;
        break;
    case 2:
        colorTextureFormatOut = VRAPI_TEXTURE_FORMAT_4444;
        break;
    case 3:
        colorTextureFormatOut = VRAPI_TEXTURE_FORMAT_8888;
        break;
    case 4:
        colorTextureFormatOut = VRAPI_TEXTURE_FORMAT_8888_sRGB;
        break;
    case 5:
        colorTextureFormatOut = VRAPI_TEXTURE_FORMAT_RGBA16F;
        break;
    default:
        LOGE("fatal error: unknown color texture format");
        std::terminate();
    }
    LOGV("ConfigurationHelper: --- color texture format: %d", colorTextureFormatOut);

    fid = env.GetFieldID(parmsClass, "resolveDepth", "Z");
    resolveDepthOut = env.GetBooleanField(parms, fid);
    LOGV("ConfigurationHelper: --- resolve depth: %d", resolveDepthOut);

    fid = env.GetFieldID(parmsClass, "depthFormat",
            "Lorg/gearvrf/utility/VrAppSettings$EyeBufferParms$DepthFormat;");
    jobject depthFormat = env.GetObjectField(parms, fid);
    mid = env.GetMethodID(env.GetObjectClass(depthFormat), "getValue", "()I");
    int depthFormatValue = env.CallIntMethod(depthFormat, mid);
    switch (depthFormatValue) {
    case 0:
        depthTextureFormatOut = VRAPI_TEXTURE_FORMAT_NONE;
        break;
    case 1:
        depthTextureFormatOut = VRAPI_TEXTURE_FORMAT_DEPTH_16;
        break;
    case 2:
        depthTextureFormatOut = VRAPI_TEXTURE_FORMAT_DEPTH_24;
        break;
    case 3:
        depthTextureFormatOut = VRAPI_TEXTURE_FORMAT_DEPTH_24_STENCIL_8;
        break;
    default:
        LOGE("fatal error: unknown depth texture format");
        std::terminate();
    }

    LOGV("ConfigurationHelper: --- depth texture format: %d", depthTextureFormatOut);
    LOGV("ConfigurationHelper: ---------------------------------");
}

void ConfigurationHelper::getModeConfiguration(JNIEnv& env, bool& allowPowerSaveOut, bool& resetWindowFullscreenOut) {
    LOGV("ConfigurationHelper: --- mode configuration ---");

    jfieldID fid = env.GetFieldID(vrAppSettingsClass_, "modeParms", "Lorg/gearvrf/utility/VrAppSettings$ModeParms;");
    jobject modeParms = env.GetObjectField(vrAppSettings_, fid);
    jclass modeParmsClass = env.GetObjectClass(modeParms);

    allowPowerSaveOut = env.GetBooleanField(modeParms, env.GetFieldID(modeParmsClass, "allowPowerSave", "Z"));
    LOGV("ConfigurationHelper: --- allowPowerSave: %d", allowPowerSaveOut);
    resetWindowFullscreenOut = env.GetBooleanField(modeParms, env.GetFieldID(modeParmsClass, "resetWindowFullScreen","Z"));
    LOGV("ConfigurationHelper: --- resetWindowFullscreen: %d", resetWindowFullscreenOut);

    LOGV("ConfigurationHelper: --------------------------");
}

void ConfigurationHelper::getPerformanceConfiguration(JNIEnv& env, ovrPerformanceParms& parmsOut) {
    LOGV("ConfigurationHelper: --- performance configuration ---");

    jfieldID fid = env.GetFieldID(vrAppSettingsClass_, "performanceParms", "Lorg/gearvrf/utility/VrAppSettings$PerformanceParms;");
    jobject parms = env.GetObjectField(vrAppSettings_, fid);
    jclass parmsClass = env.GetObjectClass(parms);

    parmsOut.GpuLevel = env.GetIntField(parms, env.GetFieldID(parmsClass, "gpuLevel", "I"));
    LOGV("ConfigurationHelper: --- gpuLevel: %d", parmsOut.GpuLevel);
    parmsOut.CpuLevel = env.GetIntField(parms, env.GetFieldID(parmsClass, "cpuLevel", "I"));
    LOGV("ConfigurationHelper: --- cpuLevel: %d", parmsOut.CpuLevel);

    LOGV("ConfigurationHelper: --------------------------");
}

void ConfigurationHelper::getHeadModelConfiguration(JNIEnv& env, ovrHeadModelParms& parmsInOut) {
    LOGV("ConfigurationHelper: --- head model configuration ---");

    jfieldID fid = env.GetFieldID(vrAppSettingsClass_, "headModelParms", "Lorg/gearvrf/utility/VrAppSettings$HeadModelParms;");
    jobject parms = env.GetObjectField(vrAppSettings_, fid);
    jclass parmsClass = env.GetObjectClass(parms);

    fid = env.GetFieldID(parmsClass, "interpupillaryDistance", "F");
    float interpupillaryDistance = env.GetFloatField(parms, fid);
    if (interpupillaryDistance != interpupillaryDistance) {
        //Value not set in Java side, current Value is NaN
        //Need to copy the system settings to java side.
        env.SetFloatField(parms, fid, parmsInOut.InterpupillaryDistance);
    } else {
        parmsInOut.InterpupillaryDistance = interpupillaryDistance;
    }
    LOGV("ConfigurationHelper: --- interpupillaryDistance: %f", parmsInOut.InterpupillaryDistance);

    fid = env.GetFieldID(parmsClass, "eyeHeight", "F");
    float eyeHeight = env.GetFloatField(parms, fid);
    if (eyeHeight != eyeHeight) {
        //same as interpupilaryDistance
        env.SetFloatField(parms, fid, parmsInOut.EyeHeight);
    }else{
        parmsInOut.EyeHeight = eyeHeight;
    }
    LOGV("ConfigurationHelper: --- eyeHeight: %f", parmsInOut.EyeHeight);

    fid = env.GetFieldID(parmsClass, "headModelDepth", "F");
    float headModelDepth = env.GetFloatField(parms, fid);
    if (headModelDepth != headModelDepth) {
        //same as interpupilaryDistance
        env.SetFloatField(parms, fid, parmsInOut.HeadModelDepth);
    } else {
        parmsInOut.HeadModelDepth = headModelDepth;
    }
    LOGV("ConfigurationHelper: --- headModelDepth: %f", parmsInOut.HeadModelDepth);

    fid = env.GetFieldID(parmsClass, "headModelHeight", "F");
    float headModelHeight = env.GetFloatField(parms, fid);
    if (headModelHeight != headModelHeight) {
        //same as interpupilaryDistance
        env.SetFloatField(parms, fid, parmsInOut.HeadModelHeight);
    } else {
        parmsInOut.HeadModelHeight = headModelHeight;
    }
    LOGV("ConfigurationHelper: --- headModelHeight: %f", parmsInOut.HeadModelHeight);

    LOGV("ConfigurationHelper: --------------------------------");
}

void ConfigurationHelper::getSceneViewport(JNIEnv& env, int& viewport_x, int& viewport_y, int& viewport_width, int& viewport_height) {

    LOGV("ConfigurationHelper: --- viewport configuration ---");
    int x, y, width, height;

    jfieldID fid = env.GetFieldID(vrAppSettingsClass_, "sceneParms", "Lorg/gearvrf/utility/VrAppSettings$SceneParms;");
    jobject parms = env.GetObjectField(vrAppSettings_, fid);
    jclass parmsClass = env.GetObjectClass(parms);

    x = env.GetIntField(parms, env.GetFieldID(parmsClass, "viewportX", "I"));
    y = env.GetIntField(parms, env.GetFieldID(parmsClass, "viewportY", "I"));
    width = env.GetIntField(parms, env.GetFieldID(parmsClass, "viewportWidth", "I"));
    height = env.GetIntField(parms, env.GetFieldID(parmsClass, "viewportHeight", "I"));

    if (width != 0 && height != 0) {

        // otherwise default viewport
        viewport_x = x;
        viewport_y = y;
        viewport_width = width;
        viewport_height = height;
    }
}

}
