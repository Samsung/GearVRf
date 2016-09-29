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


#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace std;

#define  LOG_TAG    "TemplateDevice"


extern "C" {
JNIEXPORT void JNICALL
        Java_com_sample_hand_template_HandTemplateDevice_initialize(JNIEnv *env, jobject thiz,
                                                                    jobject
                                                                    jreadback_buffer);
JNIEXPORT void JNICALL
        Java_com_sample_hand_template_HandTemplateDevice_destroy(JNIEnv *env,
                                                                 jobject instance);

};

static int action = 0;

//_VENDOR_TODO_ rename HandTemplateDevice here
class HandTemplateDevice {
    const int BUTTON_1 = 0;
    const int BUTTON_2 = 1;
    const int ACTION_DOWN = 0;
    const int ACTION_UP = 2;
    const int NUM_HANDS = 2;
    const float DEPTH = -20.0f;

    volatile bool running;
    int controller_action;
    bool flag = false;
    float x, y, z;
    int keyCode, keyAction;
    JNIEnv *env;
    jobject thiz;


    float *data;

public:
    HandTemplateDevice(JNIEnv *env, jobject thiz, int action, float *data) {
        controller_action = action;
        running = true;
        this->data = data;
        this->env = env;
        this->thiz = thiz;
    }

    void stop() {
        LOGI("Stopping Thread");
        running = false;
    }

    void setKeyEvent(int code, int action) {
        jclass clz = env->GetObjectClass(thiz);
        jmethodID method = env->GetMethodID(clz, "dispatchKeyEvent", "(II)V");
        env->CallVoidMethod(thiz, method, code, action);
        env->DeleteLocalRef(clz);
    }

    void processDataAtJava() {
        jclass clz = env->GetObjectClass(thiz);
        jmethodID method = env->GetMethodID(clz, "processData", "()V");
        env->CallVoidMethod(thiz, method);
        env->DeleteLocalRef(clz);
    }


    void start() {
        while (running) {
            processData();
        }
    }

    void processData() {
        int count = 0;
        // Set number of hands
        data[count] = NUM_HANDS;
        count++;
        float xOffset;
        int h;
        for (h = 0; h < NUM_HANDS; h++) {
            xOffset = 3.0f;

            if (h == 0.0f) {
                //Thumb
                // metacarpal x , y , z
                data[count] = xOffset;
                count++;
                data[count] = (-4.0f);
                count++;
                data[count] = DEPTH;
                count++;

                // proximal x , y , z
                data[count] = xOffset;
                count++;
                data[count] = (-4.0f);
                count++;
                data[count] = DEPTH;
                count++;

                // intermediate x , y , z
                data[count] = xOffset - 1.0f;
                count++;
                data[count] = (-2.0f);
                count++;
                data[count] = DEPTH - 2.0f;
                count++;

                // distal x , y , z
                data[count] = xOffset - 2.0f;
                count++;
                data[count] = (-0.5f);
                count++;
                data[count] = DEPTH - 3.0f;
                count++;
            }
            else {
                xOffset = -9.0f;
                //Thumb
                // metacarpal x , y , z
                data[count] = xOffset + 6.0f;
                count++;
                data[count] = (-4.0f);
                count++;
                data[count] = DEPTH;
                count++;

                // proximal x , y , z
                data[count] = xOffset + 6.0f;
                count++;
                data[count] = (-4.0f);
                count++;
                data[count] = DEPTH;
                count++;

                // intermediate x , y , z
                data[count] = xOffset + 6.0f + 1.0f;
                count++;
                data[count] = (-2.0f);
                count++;
                data[count] = DEPTH - 2.0f;
                count++;

                // distal x , y , z
                data[count] = xOffset + 6.0f + 2.0f;
                count++;
                data[count] = (-0.5f);
                count++;
                data[count] = DEPTH - 3.0f;
                count++;
            }

            int fingerNum = 0;
            for (fingerNum = 0; fingerNum < 4; fingerNum++) {
                int boneNum = 0;
                for (boneNum = 0; boneNum < 4; boneNum++) {
                    data[count] = xOffset + fingerNum * 2.0f;
                    count++;
                    data[count] = (2.0f * boneNum);
                    count++;
                    data[count] = DEPTH;
                    count++;
                }
            }

            //setPosition palm position
            data[count] = xOffset + 3.0f;
            count++;
            data[count] = -2.0f;
            count++;
            data[count] = DEPTH;
            count++;

            if (data[0] >= 0 && data[0] < 3) {
                processDataAtJava();
            }
            sleep(0.016);
        }
    }
};

HandTemplateDevice *templateDevice;

JNIEXPORT void JNICALL
Java_com_sample_hand_template_HandTemplateDevice_initialize(JNIEnv *env, jobject thiz, jobject
jreadback_buffer) {
    float *data = (float *) env->GetDirectBufferAddress(jreadback_buffer);
    templateDevice = new HandTemplateDevice(env, thiz, action, data);
    action++;
    if (data == NULL) {
        LOGI("Data is null");
    }
    LOGI("Starting Thread");
    templateDevice->start();
}


JNIEXPORT void JNICALL
Java_com_sample_hand_template_HandTemplateDevice_destroy(JNIEnv *env, jobject instance) {
    templateDevice->stop();
}