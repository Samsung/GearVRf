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
 * Renders a scene, a screen.
 ***************************************************************************/

#include "glm/gtc/type_ptr.hpp"
#include "glm/glm.hpp"
#include "glm/gtc/matrix_inverse.hpp"
#include "renderer.h"
#include "objects/scene.h"
#include "shaders/shader.h"
#include "render_sorter.h"

namespace gvr {

Renderer* gRenderer = nullptr;
bool use_multiview = false; // TODO: don't use a global variable for this

Renderer::Renderer() :
    mJavaVM(nullptr),
    numberDrawCalls(0),
    numberTriangles(0),
    numLights(0),
    mShadowSorter(nullptr),
    mMaxUniformBlockSize(4096),
    mMaxArrayFloats(512),
    mPostEffectSorter(nullptr),
    mLeftRenderTarget{nullptr, nullptr, nullptr},
    mRightRenderTarget{nullptr, nullptr, nullptr},
    mMultiviewRenderTarget{nullptr, nullptr, nullptr}
{

}

int Renderer::getMaxArraySize(int elemSize) const
{
    int vec4Size = sizeof(float) * 4;
    int numVec4 = elemSize / vec4Size;

    if (numVec4 * vec4Size == elemSize)
    {
        return (mMaxArrayFloats * 4) / elemSize;
    }
    if (elemSize < vec4Size)
        return mMaxArrayFloats;
    elemSize = (elemSize + 15) / vec4Size;
    return (mMaxArrayFloats * 4) / elemSize;
}


int Renderer::getJavaEnv(JNIEnv** envptr)
{
    if (mJavaVM == nullptr)
    {
        FAIL("Scene::get_java_env Could not attach to Java VM");\
        return -1;
    }
    int rc = mJavaVM->GetEnv((void **) envptr, JNI_VERSION_1_6);
    if (rc == JNI_EDETACHED)

    {
        if (mJavaVM->AttachCurrentThread(envptr, NULL) && *envptr)
        {
            return 1;
        }
        FAIL("Scene::get_java_env Could not attach to Java VM");
    }
    else if (rc == JNI_EVERSION)
    {
        FAIL("Scene::get_java_env JNI version not supported");
        return -1;
    }
    return 0;
}

void Renderer::detachJavaEnv()
{
   mJavaVM->DetachCurrentThread();
}


bool Renderer::renderPostEffectData(RenderState& rstate, RenderTexture* input_texture, RenderData* post_effect, int pass)
{
    RenderPass *rpass = post_effect->pass(pass);
    if (rpass == NULL)
    {
        return false;
    }

    ShaderData *material = rpass->material();
    if (material == NULL)
    {
        return false;
    }

    if (mPostEffectSorter == nullptr)
    {
        mPostEffectSorter = new RenderSorter(*this, "PostEffectSorter", 8);
    }

    RenderSorter::Renderable r;

    material->setTexture("u_texture", input_texture);
    r.material = material;
    r.renderData = post_effect;
    r.renderPass = rpass;
    mPostEffectSorter->init(rstate);
    RenderSorter::Renderable* added = mPostEffectSorter->add(rstate, r);
    added->renderModes.setUseLights(false);
    added->renderModes.setDepthTest(false);
    added->renderModes.setCullFace(RenderModes::CullNone);
    mPostEffectSorter->sort(rstate);
    mPostEffectSorter->render(rstate);
    return true;
}

}
