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

#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

using namespace std;

#define  LOG_TAG    "TemplateDevice"


extern "C" {
JNIEXPORT void JNICALL
        Java_com_sample_template_TemplateDevice_initialize(JNIEnv *env, jobject thiz);
JNIEXPORT void JNICALL
        Java_com_sample_template_TemplateDevice_destroy(JNIEnv *env,
                                                        jobject instance);
};

static int action = 0;

//_VENDOR_TODO_ rename TemplateDevice here
class TemplateDevice {
    const int BUTTON_1 = 0;
    const int BUTTON_2 = 1;
    const int ACTION_DOWN = 0;
    const int ACTION_UP = 2;

    volatile bool running;
    int controller_action;
    bool flag = false;
    float x, y, z;
    int keyCode, keyAction;
    JNIEnv *env;
    jobject thiz;

public:
    TemplateDevice(JNIEnv *env, jobject thiz, int action);

    void start();

    void stop();

    void setPosition(float x, float y, float z);

    void setKeyEvent(int code, int action);
};

TemplateDevice::TemplateDevice(JNIEnv *env, jobject thiz, int action) {
    controller_action = action;
    running = true;
    this->env = env;
    this->thiz = thiz;
}

void TemplateDevice::start() {
    while (running) {
        //Generate mock events
        if (controller_action == 0) {
            if (flag) {
                x = 0.0f;
                y = 0.0f;
                z = -0.5f;
                flag = false;
                keyCode = BUTTON_1;
                keyAction = ACTION_DOWN;
            } else {
                x = 0.0f;
                y = 0.0f;
                z = -0.7f;
                flag = true;
                keyCode = BUTTON_1;
                keyAction = ACTION_UP;
            }
        }
        else if (controller_action == 1) {
            if (flag) {
                x = -0.25f;
                y = -0.25f;
                z = -0.25f;
                flag = false;
                keyCode = BUTTON_2;
                keyAction = ACTION_DOWN;
            } else {
                x = 0.0f;
                y = -0.25f;
                z = -0.25f;
                flag = true;
                keyCode = BUTTON_2;
                keyAction = ACTION_UP;
            }
        }
        //_VENDOR_TODO_ forward x, y, z values to the library using this call
        setPosition(x, y, z);
        setKeyEvent(keyCode, keyAction);
        sleep(1);
    }
}

void TemplateDevice::setPosition(float x, float y, float z) {
    jclass clz = env->GetObjectClass(thiz);
    jmethodID method = env->GetMethodID(clz, "processPosition", "(FFF)V");
    env->CallVoidMethod(thiz, method, x, y, z);
    env->DeleteLocalRef(clz);
}


void TemplateDevice::setKeyEvent(int code, int action) {
    jclass clz = env->GetObjectClass(thiz);
    jmethodID method = env->GetMethodID(clz, "dispatchKeyEvent", "(II)V");
    env->CallVoidMethod(thiz, method, code, action);
    env->DeleteLocalRef(clz);
}

void TemplateDevice::stop() {
    LOGI("Stopping Thread");
    running = false;
}

TemplateDevice *controller;

JNIEXPORT void JNICALL
Java_com_sample_template_TemplateDevice_initialize(JNIEnv *env, jobject thiz) {
    controller = new TemplateDevice(env, thiz, action);
    action++;
    LOGI("Starting Thread");
    controller->start();

}

JNIEXPORT void JNICALL
Java_com_sample_template_TemplateDevice_destroy(JNIEnv *env, jobject instance) {
    controller->stop();
}