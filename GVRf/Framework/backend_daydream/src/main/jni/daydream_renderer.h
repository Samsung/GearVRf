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

#ifndef DAYDREAM_RENDERER_H_
#define DAYDREAM_RENDERER_H_

#include <jni.h>

#include "vr/gvr/capi/include/gvr.h"
#include "vr/gvr/capi/include/gvr_types.h"

#include "objects/components/camera.h"
#include "objects/components/custom_camera.h"
#include "objects/components/perspective_camera.h"
#include "objects/components/camera_rig.h"
#include "objects/scene_object.h"

class DaydreamRenderer {
public:
    DaydreamRenderer(JNIEnv &env, jclass clazz, gvr_context *gvr_context);

    ~DaydreamRenderer();

    void InitializeGl();

    void DrawFrame(JNIEnv &env);

    void OnPause();

    void OnResume();

    void OnDestroy(JNIEnv &env);

    void SetCameraRig(jlong camera);

    void SetCameraProjectionMatrix(gvr::CustomCamera *camera, const gvr::Rectf &fov, float z_near,
                         float z_far);
    void updateHandedness(){
        // Needs to be called when user changes its preferences, right now we don't any api to change preferences so we are calling it only once
        mUserPrefs = gvr_api_->GetUserPrefs();
        mBuffer[0] = mUserPrefs.GetControllerHandedness();
    }
    void setFloatBuffer(float* buffer){
        mBuffer = buffer;
    }
private:
    void PrepareFramebuffer();

    void SetViewport(const gvr::BufferViewport &viewport);

    std::unique_ptr <gvr::GvrApi> gvr_api_;

    std::unique_ptr <gvr::BufferViewportList> viewport_list_;
    std::unique_ptr <gvr::BufferViewportList> scratch_viewport_list_;
    std::unique_ptr <gvr::SwapChain> swapchain_;
    gvr::BufferViewport scratch_viewport_;
    gvr::UserPrefs mUserPrefs;
    gvr::Sizei render_size_;
    gvr::Mat4f head_view_;
    float* mBuffer;
    jmethodID onDrawEyeMethodId_ = nullptr;
    jmethodID onViewChangeMethodId_ = nullptr;
    jobject rendererObject_ = nullptr;

    gvr::CameraRig *cameraRig_;
};

#endif  // DAYDREAM_RENDERER_H_
