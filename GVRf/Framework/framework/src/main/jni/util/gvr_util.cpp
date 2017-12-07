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
#include "util/gvr_util.h"
#include <sys/system_properties.h>
#include <cstring>
#include <jni.h>

namespace gvr {

extern "C"
JNIEXPORT jboolean JNICALL
Java_org_gearvrf_SystemPropertyUtil_isSystemPropertySet(JNIEnv *env, jclass type, jstring name_) {
    const char *name = env->GetStringUTFChars(name_, 0);
    bool result = isSystemPropertySet(name);
    env->ReleaseStringUTFChars(name_, name);

    return result;
}

bool isSystemPropertySet(const char *prop) {
    const prop_info *pi = __system_property_find(prop);

    if (pi) {
        char buffer[PROP_VALUE_MAX];
        if (0 < __system_property_read(pi, 0, buffer)) {
            if (0 == std::strcmp(buffer, "1")) {
                return true;
            }
        }
    }

    return false;
}

}