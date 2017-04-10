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
#include <jni.h>

#include "OVR_Platform.h"

#include "util/gvr_log.h"


extern "C" {

JNIEXPORT jint JNICALL Java_org_gearvrf_PlatformEntitlementCheck_create(JNIEnv* jni, jclass, jobject activity, jstring appId) {
    const ovrPlatformInitializeResult result = ovr_PlatformInitializeAndroid(
            jni->GetStringUTFChars(appId, 0), activity, jni);

    if (ovrPlatformInitialize_Success != result) {
        LOGE("ovr_PlatformInitializeAndroid failed with error %d", result);
    } else {
        LOGI("entitlement check enabled!");
        ovr_Entitlement_GetIsViewerEntitled();
    }

    return result;
}

/**
 * @return 0 if indeterminate (keep on checking), -1 on failure, 1 on success
 */
JNIEXPORT jint JNICALL Java_org_gearvrf_PlatformEntitlementCheck_processEntitlementCheckResponse(JNIEnv* jni, jclass) {

    ovrMessage *response = ovr_PopMessage();
    if (response) {
        int messageType = ovr_Message_GetType(response);
        if (messageType == ovrMessage_Entitlement_GetIsViewerEntitled) {
            if (ovr_Message_IsError(response) != 0) {
                LOGI("entitlement check for user failed");
                return -1;
            } else {
                LOGI("entitlement check for user succeeded");
                return 1;
            }
        }
    }

    return 0;
}

}

