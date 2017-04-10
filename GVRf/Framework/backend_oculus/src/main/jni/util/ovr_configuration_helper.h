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


#ifndef CONFIGURATION_HELPER_H
#define CONFIGURATION_HELPER_H

#include "VrApi_Types.h"

namespace gvr {

class ConfigurationHelper
{
public:
    ConfigurationHelper(JNIEnv& env, jobject vrAppSettings);
    ~ConfigurationHelper();

    void getFramebufferConfiguration(JNIEnv& env, int& fbWidthOut, int& fbHeightOut,
            const int fbWidthDefault, const int fbHeightDefault, int& multiSamplesOut,
            ovrTextureFormat& colorFormatOut, bool& resolveDepth, ovrTextureFormat& depthTextureFormatOut);
    void getModeConfiguration(JNIEnv& env, bool& allowPowerSaveOut, bool& resetWindowFullscreenOut);
    void getPerformanceConfiguration(JNIEnv& env, ovrPerformanceParms& parmsOut);
    void getHeadModelConfiguration(JNIEnv& env, ovrHeadModelParms& parmsOut);
    void getSceneViewport(JNIEnv& env, int& viewport_x, int& viewport_y, int& viewport_width, int& viewport_height);
    void getMultiviewConfiguration(JNIEnv& env, bool& useMultiview);
private:
    JNIEnv& env_;
    jclass vrAppSettingsClass_;
    jobject vrAppSettings_;
};
}
#endif
