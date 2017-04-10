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
 *
 */
#include <string.h>
#include <jni.h>
#include <iostream>
#include <unistd.h>
#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>
#include "LeapC.h"
#include "ExampleConnection.h"

#include <math.h>

#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace std;

#define  LOG_TAG    "LeapController"

extern "C" {
JNIEXPORT void JNICALL
        Java_org_gearvrf_leap_LeapController_initialize(JNIEnv *env, jobject thiz, jobject
jreadback_buffer);
JNIEXPORT void JNICALL
        Java_org_gearvrf_leap_LeapController_destroy(JNIEnv *env, jobject instance);
};

class LeapController {
    JNIEnv *env;
    jobject thiz;
    int64_t lastFrameId;
    float *readbackBuffer;
public:
    volatile bool running;

public:
    LeapController(JNIEnv *env, jobject thiz, float *readbackBuffer) {
        running = true;
        this->readbackBuffer = readbackBuffer;
        this->env = env;
        this->thiz = thiz;
    }

    void stop() {
        LOGI("Stopping Thread");
        running = false;
    }


    void onFrame() {
        jclass clz = env->GetObjectClass(thiz);
        jmethodID method = env->GetMethodID(clz, "onFrame", "()V");
        env->CallVoidMethod(thiz, method);
        env->DeleteLocalRef(clz);
    }


    void setData(const LEAP_TRACKING_EVENT *frame) {
        int64_t current_frame = frame->tracking_frame_id;

        if (current_frame == lastFrameId) {
            return;
        }

        lastFrameId = current_frame;
        uint32_t h = 0;

        int count = 0;
        readbackBuffer[count++] = frame->nHands;

        for (h = 0; h < frame->nHands; h++) {
            LEAP_HAND *hand = &frame->pHands[h];
            readbackBuffer[count++] = (hand->type == eLeapHandType_Left ? 0.0f : 1.0f);

            readbackBuffer[count++] = hand->pinch_strength;

            readbackBuffer[count++] = hand->palm.direction.x;
            readbackBuffer[count++] = hand->palm.direction.y;
            readbackBuffer[count++] = hand->palm.direction.z;

            readbackBuffer[count++] = hand->palm.normal.x;
            readbackBuffer[count++] = hand->palm.normal.y;
            readbackBuffer[count++] = hand->palm.normal.z;

            //get the palm position
            readbackBuffer[count++] = hand->arm.next_joint.x;
            readbackBuffer[count++] = hand->arm.next_joint.y;
            readbackBuffer[count++] = hand->arm.next_joint.z;

            int fingerNum = 0;
            for (fingerNum = 0; fingerNum < 5; fingerNum++) {
                LEAP_DIGIT *finger = &hand->digits[fingerNum];
                int boneNum = 0;
                for (boneNum = 0; boneNum < 4; boneNum++) {
                    LEAP_BONE *bone = &finger->bones[boneNum];

                    readbackBuffer[count++] = bone->next_joint.x;
                    readbackBuffer[count++] = bone->next_joint.y;
                    readbackBuffer[count++] = bone->next_joint.z;

                    readbackBuffer[count++] = bone->rotation.w;
                    readbackBuffer[count++] = bone->rotation.x;
                    readbackBuffer[count++] = bone->rotation.y;
                    readbackBuffer[count++] = bone->rotation.z;
                }
            }
        }

        if (readbackBuffer[0] >= 0 && readbackBuffer[0] < 3) {
            onFrame();
        }
    }
};

LeapController *controller;
int64_t lastFrameID = 0; //The last frame received

JNIEXPORT void JNICALL
Java_org_gearvrf_leap_LeapController_initialize(JNIEnv *env, jobject thiz, jobject
jreadback_buffer) {
    float *readbackBuffer = (float *) env->GetDirectBufferAddress(jreadback_buffer);
    controller = new LeapController(env, thiz, readbackBuffer);

    if (readbackBuffer == NULL) {
        LOGI("readbackBuffer is null");
    }

    LOGI("Starting Thread %f", readbackBuffer[0]);

    OpenConnection();
    while (!IsConnected)
        millisleep(100); //wait a bit to let the connection complete

    printf("Connected.");
    LEAP_DEVICE_INFO *deviceProps = GetDeviceProperties();
    if (deviceProps)
        printf("Using device %s.\n", deviceProps->serial);

    while (controller->running) {
        LEAP_TRACKING_EVENT *frame = GetFrame();

        if (frame && (frame->tracking_frame_id > lastFrameID)) {
            lastFrameID = frame->tracking_frame_id;
            controller->setData(frame);
        }
    }
}


JNIEXPORT void JNICALL
Java_org_gearvrf_leap_LeapController_destroy(JNIEnv *env, jobject instance) {
    controller->stop();
}