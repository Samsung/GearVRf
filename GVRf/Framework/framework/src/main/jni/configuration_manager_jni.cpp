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
 * JNI
 ***************************************************************************/

#include "configuration_manager.h"
#include "util/gvr_jni.h"

namespace gvr {
    extern "C" {

    JNIEXPORT jlong JNICALL Java_org_gearvrf_NativeConfigurationManager_ctor(JNIEnv *env,
                                                                             jobject obj);

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeConfigurationManager_configureRendering(JNIEnv *env, jobject obj,
                                                                           jlong jConfigurationManager, jboolean useStencil);

    JNIEXPORT int JNICALL
    Java_org_gearvrf_NativeConfigurationManager_getMaxLights(JNIEnv *env, jobject obj,
                                                                     jlong jConfigurationManager);

    JNIEXPORT void JNICALL Java_org_gearvrf_NativeConfigurationManager_delete(JNIEnv *env,
                                                                              jobject obj,
                                                                              jlong jConfigurationManager);

    JNIEXPORT jlong JNICALL Java_org_gearvrf_NativeConfigurationManager_ctor(JNIEnv *env,
                                                                             jobject obj) {
        return reinterpret_cast<jlong>(new ConfigurationManager());
    }

    JNIEXPORT void JNICALL
    Java_org_gearvrf_NativeConfigurationManager_configureRendering(JNIEnv *env, jobject obj,
                                                                   jlong jConfigurationManager, jboolean useStencil) {
        ConfigurationManager *configuration_manager = reinterpret_cast<ConfigurationManager *>(jConfigurationManager);
        configuration_manager->configureRendering(useStencil);
    }

    JNIEXPORT int JNICALL
    Java_org_gearvrf_NativeConfigurationManager_getMaxLights(JNIEnv *env, jobject obj,
                                                             jlong jConfigurationManager) {
        ConfigurationManager *configuration_manager = reinterpret_cast<ConfigurationManager *>(jConfigurationManager);
        return configuration_manager->getMaxLights();
    }

    JNIEXPORT void JNICALL Java_org_gearvrf_NativeConfigurationManager_delete(JNIEnv *env,
                                                                              jobject obj,
                                                                              jlong jConfigurationManager) {
        delete reinterpret_cast<ConfigurationManager *>(jConfigurationManager);
    }

    }
}
