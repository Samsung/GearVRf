/***************************************************************************
 * Captures a rendered texture to a buffer.
 ***************************************************************************/

#include "glm/glm.hpp"
#include "glm/gtc/constants.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_inverse.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "objects/components/render_data.h"
#include "objects/components/texture_capturer.h"
#include "objects/material.h"
#include "objects/mesh.h"
#include "util/gvr_log.h"
#include "util/gvr_time.h"

#define TOL 1e-8

namespace gvr {

extern "C" {
void Java_org_gearvrf_NativeTextureCapturer_callbackFromNative(
        JNIEnv *env, jobject obj, jint index, char *info);
}

TextureCapturer::TextureCapturer(ShaderManager *shaderManager)
        : Component(TextureCapturer::getComponentType())
        , mShaderManager(shaderManager)
        , mRenderTexture(0)
        , mPendingCapture(false)
        , mHasNewCapture(false)
        , mCaptureIntervalNS(0)
        , mLastCaptureTimeNS(0)
        , mJNIEnv(0)
        , mCapturerObject(0)
{
}

TextureCapturer::~TextureCapturer() {
    if (mJNIEnv && mCapturerObject) {
        mJNIEnv->DeleteGlobalRef(mCapturerObject);
    }
}

void TextureCapturer::setCapturerObject(JNIEnv *env, jobject capturer) {
    mJNIEnv = env;

    if (mCapturerObject) {
        mJNIEnv->DeleteGlobalRef(mCapturerObject);
    }

    mCapturerObject = env->NewGlobalRef(capturer);
}

void TextureCapturer::setRenderTexture(RenderTexture *renderTexture) {
    mRenderTexture = renderTexture;
}

void TextureCapturer::setCapture(bool capture, float fps) {
    mPendingCapture = capture;
    if (capture && fabs(fps) > TOL) {
        mCaptureIntervalNS = (long long)(1000000000.f / fps);
    } else {
        mCaptureIntervalNS = 0;
    }
}

bool TextureCapturer::getAndClearPendingCapture() {
    // periodic capture
    if (mCaptureIntervalNS) {
        long long now = getNanoTime();
        if (now - mLastCaptureTimeNS >= mCaptureIntervalNS) {
            mPendingCapture = true;
        }
    }

    bool rv = mPendingCapture;
    mPendingCapture = false;
    return rv;
}

void TextureCapturer::beginCapture() {
    // Save states
    glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &mSavedFBO);
    glGetIntegerv(GL_VIEWPORT, mSavedViewport);
    glGetIntegerv(GL_SCISSOR_BOX, mSavedScissor);
    mIsCullFace = glIsEnabled(GL_CULL_FACE);
    mIsBlend = glIsEnabled(GL_BLEND);
    mIsPolygonOffsetFill = glIsEnabled(GL_POLYGON_OFFSET_FILL);

    // Setup FBO
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mRenderTexture->getFrameBufferId());

    glDisable(GL_CULL_FACE);
    glEnable (GL_BLEND);
    glBlendEquation (GL_FUNC_ADD);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    glDisable (GL_POLYGON_OFFSET_FILL);

    // Setup viewport
    glViewport(0, 0, mRenderTexture->width(), mRenderTexture->height());
    glClearColor(0, 0, 0, 0); // transparency is needed
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

    mLastCaptureTimeNS = getNanoTime();
}

void TextureCapturer::startReadBack() {
    mRenderTexture->startReadBack();
}

void TextureCapturer::endCapture() {
    // Restore GL settings
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, mSavedFBO);
    glViewport(mSavedViewport[0], mSavedViewport[1],
               mSavedViewport[2], mSavedViewport[3]);
    glScissor(mSavedScissor[0], mSavedScissor[1],
              mSavedScissor[2], mSavedScissor[3]);

    if (mIsCullFace)
        glEnable(GL_CULL_FACE);
    else
        glDisable(GL_CULL_FACE);

    if (mIsBlend)
        glEnable(GL_BLEND);
    else
        glDisable(GL_BLEND);

    if (mIsPolygonOffsetFill)
        glEnable(GL_POLYGON_OFFSET_FILL);
    else
        glDisable(GL_POLYGON_OFFSET_FILL);
}

void TextureCapturer::render(RenderState* rstate, RenderData* render_data) {

    Material* material = render_data->pass(0)->material();
    if (material == NULL) {
        LOGE("No material");
        return;
    }

    // Create the texture material
    Material textureMaterial(Material::TEXTURE_SHADER);
    textureMaterial.setTexture("main_texture", mRenderTexture);
    textureMaterial.setFloat("opacity", material->getFloat("opacity"));

    // OpenGL default
    textureMaterial.setVec4("ambient_color", glm::vec4(0.2f, 0.2f, 0.2f, 1.0f));
    textureMaterial.setVec4("diffuse_color", glm::vec4(0.8f, 0.8f, 0.8f, 1.0f));
    textureMaterial.setVec4("specular_color", glm::vec4(0.0f, 0.0f, 0.0f, 1.0f));
    textureMaterial.setFloat("specular_exponent", 0.0f);

    TextureShader *textureShader = mShaderManager->getTextureShader();

    textureShader->render(rstate, render_data, &textureMaterial);
}

glm::mat4 TextureCapturer::getModelViewMatrix() {
    // Apply rotation
    glm::quat rot_quat = glm::angleAxis(glm::radians(180.f), glm::vec3(1.f, 0.f, 0.f));
    glm::mat4 mv = glm::mat4_cast(rot_quat);
    return mv;
}

glm::mat4 TextureCapturer::getMvpMatrix(float half_width, float half_height) {
    // Orthographic projection
    glm::mat4 proj = glm::ortho(-half_width, half_width,
                                -half_height, half_height,
                                -1.f, 1.f);
    return glm::mat4(proj * getModelViewMatrix());
}

void TextureCapturer::callback(int msg, char *info) {
    Java_org_gearvrf_NativeTextureCapturer_callbackFromNative(
            mJNIEnv, mCapturerObject, msg, info);
}

}
