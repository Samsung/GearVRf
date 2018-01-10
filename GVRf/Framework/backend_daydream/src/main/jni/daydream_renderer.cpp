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

#include "daydream_renderer.h"
#include "glm/gtc/matrix_inverse.hpp"

namespace {
    static const uint64_t kPredictionTimeWithoutVsyncNanos = 50000000;
    static const int kDefaultFboResolution = 1024;
    static const gvr::Rectf kDefaultUV = {0.0f, 1.0f, 0.0f, 1.0f};

    // Use the same default clipping distances as the perspective camera
    // TODO: Change this to read the values from the gvr.xml file
    static const float kZNear = 0.1f;
    static const float kZFar = 1000.0f;

    static glm::mat4 MatrixToGLMMatrix(const gvr::Mat4f &matrix) {
        glm::mat4 result;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                result[i][j] = matrix.m[i][j];
            }
        }
        return result;
    }

    static gvr::Rectf ModulateRect(const gvr::Rectf &rect, float width,
                                   float height) {
        gvr::Rectf result = {rect.left * width, rect.right * width,
                             rect.bottom * height, rect.top * height};
        return result;
    }

    static gvr::Recti CalculatePixelSpaceRect(const gvr::Sizei &texture_size,
                                              const gvr::Rectf &texture_rect) {
        const float width = static_cast<float>(texture_size.width);
        const float height = static_cast<float>(texture_size.height);
        const gvr::Rectf rect = ModulateRect(texture_rect, width, height);
        const gvr::Recti result = {
                static_cast<int>(rect.left), static_cast<int>(rect.right),
                static_cast<int>(rect.bottom), static_cast<int>(rect.top)};
        return result;
    }

}  // namespace

DaydreamRenderer::DaydreamRenderer(JNIEnv &env, jclass clazz,
                                   gvr_context *gvr_context1)
        : gvr_api_(gvr::GvrApi::WrapNonOwned(gvr_context1)), mUserPrefs(0),
          scratch_viewport_(gvr_api_->CreateBufferViewport()) {
    jclass rendererClass = env.GetObjectClass(clazz);
    rendererObject_ = env.NewGlobalRef(clazz);
    onDrawEyeMethodId_ = env.GetMethodID(rendererClass, "onDrawEye", "(I)V");
    onViewChangeMethodId_ = env.GetMethodID(rendererClass, "onViewChange", "()V");
    env.DeleteLocalRef(rendererClass);
}

DaydreamRenderer::~DaydreamRenderer() {
}

void DaydreamRenderer::InitializeGl() {
    gvr_api_->InitializeGl();

    //@todo read gvr.xml and obtain the values from EyeBufferParams
    render_size_.height = kDefaultFboResolution;
    render_size_.width = kDefaultFboResolution;
    std::vector <gvr::BufferSpec> specs;
    specs.push_back(gvr_api_->CreateBufferSpec());
    specs.push_back(gvr_api_->CreateBufferSpec());

    specs[0].SetColorFormat(GVR_COLOR_FORMAT_RGBA_8888);
    specs[0].SetDepthStencilFormat(GVR_DEPTH_STENCIL_FORMAT_DEPTH_24_STENCIL_8);
    specs[0].SetSize(render_size_);
    specs[0].SetSamples(2);

    specs[1].SetColorFormat(GVR_COLOR_FORMAT_RGBA_8888);
    specs[1].SetDepthStencilFormat(GVR_DEPTH_STENCIL_FORMAT_DEPTH_24_STENCIL_8);
    specs[1].SetSize(render_size_);
    specs[1].SetSamples(2);
    swapchain_.reset(new gvr::SwapChain(gvr_api_->CreateSwapChain(specs)));

    viewport_list_.reset(new gvr::BufferViewportList(
            gvr_api_->CreateEmptyBufferViewportList()));
    scratch_viewport_list_.reset(new gvr::BufferViewportList(
            gvr_api_->CreateEmptyBufferViewportList()));

}

void DaydreamRenderer::DrawFrame(JNIEnv &env) {
    // use the scratch list to get the recommended viewports
    scratch_viewport_list_->SetToRecommendedBufferViewports();

    // construct FBO backed viewports
    gvr::BufferViewport fbo_viewport = gvr_api_->CreateBufferViewport();

    scratch_viewport_list_->GetBufferViewport(0, &scratch_viewport_);
    fbo_viewport.SetSourceBufferIndex(0);
    fbo_viewport.SetSourceFov(scratch_viewport_.GetSourceFov());
    fbo_viewport.SetReprojection(scratch_viewport_.GetReprojection());
    fbo_viewport.SetSourceUv(kDefaultUV);
    fbo_viewport.SetTargetEye(GVR_LEFT_EYE);
    viewport_list_->SetBufferViewport(0, fbo_viewport);

    scratch_viewport_list_->GetBufferViewport(1, &scratch_viewport_);
    fbo_viewport.SetSourceBufferIndex(1);
    fbo_viewport.SetSourceFov(scratch_viewport_.GetSourceFov());
    fbo_viewport.SetReprojection(scratch_viewport_.GetReprojection());
    fbo_viewport.SetSourceUv(kDefaultUV);
    fbo_viewport.SetTargetEye(GVR_RIGHT_EYE);
    viewport_list_->SetBufferViewport(1, fbo_viewport);

    gvr::Frame frame = swapchain_->AcquireFrame();

    gvr::ClockTimePoint target_time = gvr::GvrApi::GetTimePointNow();
    target_time.monotonic_system_time_nanos += kPredictionTimeWithoutVsyncNanos;

    head_view_ = gvr_api_->GetHeadSpaceFromStartSpaceRotation(target_time);

    cameraRig_->getHeadTransform()->setModelMatrix(MatrixToGLMMatrix(head_view_));

    env.CallVoidMethod(rendererObject_, onViewChangeMethodId_);
    // Render the eye images.
    for (int eye = 0; eye < 2; eye++) {
        frame.BindBuffer(eye);
        viewport_list_->GetBufferViewport(eye, &scratch_viewport_);
        SetViewport(scratch_viewport_);
        env.CallVoidMethod(rendererObject_, onDrawEyeMethodId_, eye);
        frame.Unbind();
    }

    // Submit frame.
    frame.Submit(*viewport_list_, head_view_);

    checkGLError("onDrawFrame");
}

void DaydreamRenderer::OnPause() {
    gvr_api_->PauseTracking();
}

void DaydreamRenderer::OnResume() {
    gvr_api_->RefreshViewerProfile();
    gvr_api_->ResumeTracking();
}

void DaydreamRenderer::OnDestroy(JNIEnv &env) {
    env.DeleteGlobalRef(rendererObject_);
    delete cameraRig_;
}

void DaydreamRenderer::SetViewport(const gvr::BufferViewport &viewport) {
    const gvr::Recti pixel_rect = CalculatePixelSpaceRect(render_size_, viewport.GetSourceUv());
    glViewport(pixel_rect.left, pixel_rect.bottom,
               pixel_rect.right - pixel_rect.left,
               pixel_rect.top - pixel_rect.bottom);
}

void DaydreamRenderer::SetCameraRig(jlong native_camera) {
    cameraRig_ = reinterpret_cast<gvr::CameraRig *>(native_camera);
    scratch_viewport_list_->SetToRecommendedBufferViewports();

    scratch_viewport_list_->GetBufferViewport(0, &scratch_viewport_);
    SetCameraProjectionMatrix((gvr::CustomCamera *) cameraRig_->left_camera(), scratch_viewport_
            .GetSourceFov(), kZNear, kZFar);

    scratch_viewport_list_->GetBufferViewport(1, &scratch_viewport_);
    SetCameraProjectionMatrix((gvr::CustomCamera *) cameraRig_->right_camera(), scratch_viewport_
            .GetSourceFov(), kZNear, kZFar);
}

void DaydreamRenderer::SetCameraProjectionMatrix(gvr::CustomCamera *camera, const gvr::Rectf &fov,
                                                 float z_near, float z_far) {
    float x_left = -std::tan(fov.left * M_PI / 180.0f) * z_near;
    float x_right = std::tan(fov.right * M_PI / 180.0f) * z_near;
    float y_bottom = -std::tan(fov.bottom * M_PI / 180.0f) * z_near;
    float y_top = std::tan(fov.top * M_PI / 180.0f) * z_near;
    glm::mat4 projection_matrix = glm::frustum(x_left, x_right, y_bottom, y_top, z_near, z_far);
    // set the camera projection matrix
    camera->set_projection_matrix(projection_matrix);
}