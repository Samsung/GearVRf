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
#include <glm/glm.hpp>
#include <glm/gtc/type_ptr.hpp>
#include <glm/ext.hpp>

#include "util/jni_utils.h"
#include "objects/scene.h"
#include "shaders/shader.h"

namespace gvr {

RenderData::~RenderData() {
    if (nullptr == javaVm_) {
        return;
    }

    JNIEnv* env;

    const jint rc = javaVm_->GetEnv(reinterpret_cast<void**>(&env), SUPPORTED_JNI_VERSION);
    if (JNI_EDETACHED != rc && JNI_OK != rc) {
        FAIL("~RenderData: fatal GetEnv error");
    }
    if (rc == JNI_EDETACHED) {
        if (javaVm_->AttachCurrentThread(&env, nullptr) != JNI_OK) {
            FAIL("~RenderData: fatal AttachCurrentThread error");
        }
    }

    env->DeleteGlobalRef(bindShaderObject_);

    if (rc == JNI_EDETACHED) {
        if (JNI_OK != javaVm_->DetachCurrentThread()) {
            FAIL("~RenderData: fatal DetachCurrentThread error");
        }
    }
}

void RenderData::add_pass(RenderPass* render_pass) {
    markDirty();
    render_pass_list_.push_back(render_pass);
}

void RenderData::remove_pass(int pass)
{
    markDirty();
    render_pass_list_.erase(render_pass_list_.begin() + pass);
}

RenderPass* RenderData::pass(int pass) {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass];
    }
    return nullptr;
}

const RenderPass* RenderData::pass(int pass) const {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass];
    }
}

void RenderData::set_mesh(Mesh* mesh)
{
    if (mesh_ != mesh)
    {
        mesh_ = mesh;
        markDirty();
    }
}

int RenderData::cull_face(int pass) const {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass]->cull_face();
    }
}

ShaderData* RenderData::material(int pass) const {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass]->material();
    }
    return nullptr;
}

void RenderData::adjustRenderingOrderForTransparency(bool hasAlpha)
{
    if (hasAlpha)
    {
        // has transparency now, but was opaque before
        if (rendering_order_ < Transparent)
        {
            rendering_order_ = Transparent;
            return;
        }
    }
}

void RenderData::setCameraDistanceLambda(std::function<float()> func)
{
    cameraDistanceLambda_ = func;
}

void RenderData::setStencilFunc(int func, int ref, int mask) {
    stencilFuncFunc_= func;
    stencilFuncRef_ = ref;
    stencilFuncMask_ = mask;
}

void RenderData::setStencilOp(int sfail, int dpfail, int dppass) {
    stencilOpSfail_ = sfail;
    stencilOpDpfail_ = dpfail;
    stencilOpDppass_ = dppass;
}

void RenderData::setStencilMask(unsigned int mask) {
    stencilMaskMask_ = mask;
}

void RenderData::setStencilTest(bool flag) {
    render_data_flags.stencilTestFlag_ = flag;
}


/**
 * Called when the shader for a RenderData needs to be generated on the Java side.
 */
void RenderData::bindShader(JNIEnv* env, jobject localSceneObject, bool isMultiview)
{
    env->CallVoidMethod(bindShaderObject_, bindShaderMethod_, localSceneObject, isMultiview);
}

bool compareRenderDataByShader(RenderData *i, RenderData *j)
{
    // Compare renderData by their material's shader type
    // Note: multi-pass renderData is skipped for now and put to later position,
    // since each of the passes has a separate material (and shader as well).
    // An advanced sorting may be added later to take multi-pass into account
    if (j->pass_count() > 1) {
        return true;
    }

    if (i->pass_count() > 1) {
        return false;
    }

    return i->get_shader(0) < j->get_shader(0);
}

bool compareRenderDataByOrderShaderDistance(RenderData *i, RenderData *j) {
    //1. rendering order needs to be sorted first to guarantee specified correct order
    if (i->rendering_order() == j->rendering_order())
    {
        // if it is a transparent object, sort by camera distance from back to front
        if (i->rendering_order() >= RenderData::Transparent
            && i->rendering_order() < RenderData::Overlay)
        {
            return i->camera_distance() > j->camera_distance();
        }

        if (i->get_shader(0) == j->get_shader(0))
        {
            int no_passes1 = i->pass_count();
            int no_passes2 = j->pass_count();

            if (no_passes1 == no_passes2)
            {
                //@todo what about the other passes

                //this is pointer comparison; assumes batching is on; if the materials are not
                //the same then comparing the pointers further is an arbitrary decision; hence
                //falling back to camera distance.
                if (i->material(0) == j->material(0))
                {
                    if (i->cull_face(0) == j->cull_face(0))
                    {
                        if (i->getHashCode().compare(j->getHashCode()) == 0)
                        {
                            // otherwise sort from front to back
                            return i->camera_distance() < j->camera_distance();
                        }
                        return i->getHashCode() < j->getHashCode();
                    }
                    return i->cull_face(0) < j->cull_face(0);
                }
                return i->material(0) < j->material(0);
            }
            return no_passes1 < no_passes2;
        }
        return i->get_shader() < j->get_shader();
    }
    return i->rendering_order() < j->rendering_order();
}

const std::string& RenderData::getHashCode()
{
    if (hash_code_dirty_)    {
        std::string render_data_string;
        render_data_string.append(to_string(getRenderDataFlagsHashCode()));
        render_data_string.append(to_string(getComponentType()));
        render_data_string.append(to_string(render_mask_));
        render_data_string.append(to_string(offset_factor_));
        render_data_string.append(to_string(offset_units_));
        render_data_string.append(to_string(sample_coverage_));
        render_data_string.append(to_string(stencilMaskMask_));
        render_data_string.append(to_string(stencilFuncFunc_));
        render_data_string.append(to_string(stencilFuncRef_));
        render_data_string.append(to_string(stencilFuncMask_));
        render_data_string.append(to_string(stencilOpSfail_));
        render_data_string.append(to_string(stencilOpDpfail_));
        render_data_string.append(to_string(stencilOpDppass_));
        render_data_string.append(to_string(mesh_->getVertexBuffer()->getDescriptor()));
        hash_code = render_data_string;
        hash_code_dirty_ = false;
    }
    return hash_code;
}

/**
 * Determine whether this RenderData can be rendered.
 * To be renderable, a RenderData must have a mesh with vertices,
 * and a material with a valid shader and all textures loaded.
 * After validation, this RenderData will have the correct
 * rendering order based on the texture transparency.
 * @param renderer  Renderer used to render this RenderData
 * @param scene     Scene this RenderData is rendered to
 * @return true if renderable, else false
 */
int RenderData::isValid(Renderer* renderer, const RenderState& rstate)
{
    Mesh* m = mesh();
    bool dirty = isDirty();

    if (m == NULL)
    {
        return -1;
    }
    if ((rstate.render_mask & render_mask()) == 0)
    {
        return -1;
    }
    dirty |= m->isDirty();
    for (int p = 0; p < pass_count(); ++p)
    {
        RenderPass* rpass = pass(p);

        switch (rpass->isValid(renderer, rstate, this))
        {
            case -1: return -1;
            case 0: dirty = true;
        }
    }
    /*
     * If any of the render passes are dirty, their shaders
     * may need rebuilding. bindShader calls a Java function
     * to regenerate shader sources if necessary. We check
     * all the render passes to make sure they have valid shaders.
     */
    if (dirty)
    {
        markDirty();

        //@todo implementation details leaked; unify common JNI reqs of Scene and RenderData
        JNIEnv* env = nullptr;
        int rc = rstate.scene->get_java_env(&env);
        bindShader(env, rstate.javaSceneObject, rstate.is_multiview);
        if (rc > 0) {
            rstate.scene->detach_java_env();
        }

        for (int p = 0; p < pass_count(); ++p)
        {
            RenderPass *rpass = pass(p);
            if (rpass->get_shader(rstate.is_multiview) <= 0)
            {
                LOGE("RenderData::isValid shader could not be created");
                return -1;
            }
        }
    }
    return dirty ? 0 : 1;
}

bool RenderData::updateGPU(Renderer* renderer, Shader* shader)
{
    VertexBuffer* vbuf = mesh_->getVertexBuffer();

    if (mesh_->hasBones() && shader->hasBones())
    {
        VertexBoneData& vbd = mesh_->getVertexBoneData();
        std::vector<glm::mat4>& bone_matrices = vbd.getBoneMatrices();
        int numBones = bone_matrices.size();

        if (numBones > 0)
        {
            if (bones_ubo_ == NULL)
            {
                bones_ubo_ = renderer->createUniformBlock("mat4 u_bone_matrix", BONES_UBO_INDEX,
                                                          "Bones_ubo", MAX_BONES);
                bones_ubo_->setNumElems(numBones);
            }
            bones_ubo_->setRange(0, bone_matrices.data(), numBones);
            bones_ubo_->updateGPU(renderer);
        }
    }
    vbuf->updateGPU(renderer, mesh_->getIndexBuffer(), shader);
    return true;
}

void RenderData::setBindShaderObject(JNIEnv* env, jobject bindShaderObject) {
    static const jclass clazz = env->GetObjectClass(bindShaderObject);
    static const jmethodID method = env->GetMethodID(clazz, "call", "(Lorg/gearvrf/GVRScene;Z)V");
    if (method == 0)
    {
        FAIL("RenderData::setBindShaderObject: ERROR cannot find 'BindShaderObject.call' Java method");
    }

    bindShaderMethod_ = method;
    bindShaderObject_ = env->NewGlobalRef(bindShaderObject);
    env->GetJavaVM(&javaVm_);
}

}
