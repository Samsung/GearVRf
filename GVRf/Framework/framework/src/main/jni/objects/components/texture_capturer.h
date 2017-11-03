/***************************************************************************
 * Captures a rendered texture to a buffer.
 ***************************************************************************/

#ifndef TEXTURE_CAPTURER_H_
#define TEXTURE_CAPTURER_H_

#include <chrono>
#include <memory>

#include "glm/glm.hpp"
#include "util/gvr_jni.h"
#include "util/jni_utils.h"
#include "objects/lazy.h"
#include "objects/components/component.h"
#include "objects/shader_data.h"
#include "objects/textures/render_texture.h"
#include "shaders/shader_manager.h"

#define TCCB_NEW_CAPTURE   1

namespace gvr {
class RenderData;
struct RenderState;

class TextureCapturer : public Component {
public:
    explicit TextureCapturer(ShaderManager *shaderManager);
    virtual ~TextureCapturer();

    void setCapturerObject(JNIEnv *env, jobject capturer);

    void setRenderTexture(RenderTexture *renderTexture);
    void setCapture(bool capture, float fps);
    bool getAndClearPendingCapture();

    void beginCapture();
    void endCapture();

    void startReadBack();

    void render(RenderState* rstate, RenderData* render_data);

    glm::mat4 getModelViewMatrix();
    glm::mat4 getMvpMatrix(float width, float height);

    void callback(int msg, char *info);

    static long long getComponentType() {
        return COMPONENT_TYPE_TEXTURE_CAPTURER;
    }

private:
    TextureCapturer(const TextureCapturer& capturer) = delete;
    TextureCapturer(TextureCapturer&& capturer) = delete;
    TextureCapturer& operator=(const TextureCapturer& capturer) = delete;
    TextureCapturer& operator=(TextureCapturer&& capturer) = delete;

private:
    ShaderManager *mShaderManager;
    RenderTexture *mRenderTexture;
    ShaderData *mMaterial;
    bool mPendingCapture;
    bool mHasNewCapture;
    long long mCaptureIntervalNS;
    long long mLastCaptureTimeNS;

    // Saved GL settings
    GLint mSavedFBO;
    GLint mSavedViewport[4];
    GLint mSavedScissor[4];
    bool  mIsCullFace;
    bool  mIsBlend;
    bool  mIsPolygonOffsetFill;

    // Data for callback
    JNIEnv  *mJNIEnv;
    jobject mCapturerObject;
};

}
#endif
