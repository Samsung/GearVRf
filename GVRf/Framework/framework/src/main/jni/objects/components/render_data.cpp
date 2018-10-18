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
#include <glslang/Include/Common.h> //@todo remove; for to_string

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
    return nullptr;
}

void RenderData::set_mesh(Mesh* mesh)
{
    if (mesh_ != mesh)
    {
        mesh_ = mesh;
        markDirty();
        SceneObject* owner = owner_object();
        if (owner)
        {
            owner->dirtyHierarchicalBoundingVolume();
        }
    }
}

int RenderData::cull_face(int pass) const
{
    if (pass >= 0 && pass < render_pass_list_.size())
    {
        return render_pass_list_[pass]->cull_face();
    }
    return 0;
}

ShaderData* RenderData::material(int pass) const {
    if (pass >= 0 && pass < render_pass_list_.size()) {
        return render_pass_list_[pass]->material();
    }
    return nullptr;
}

/**
 * Called when the shader for a RenderData needs to be generated on the Java side.
 */
void RenderData::bindShader(JNIEnv* env, jobject localSceneObject, bool isMultiview)
{
    env->CallVoidMethod(bindShaderObject_, bindShaderMethod_, localSceneObject, isMultiview);
}

const std::string& RenderData::getHashCode()
{
    if (isHashCodeDirty())
    {
        std::string render_data_string;
        render_data_string.append(std::to_string(getRenderDataFlagsHashCode()));
        render_data_string.append(std::to_string(getComponentType()));
        render_data_string.append(std::to_string(mesh_->getVertexBuffer()->getDescriptor()));
        hash_code = render_data_string;
        pass(0)->render_modes().clearDirty();
    }
    return hash_code;
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
            bones_ubo_->updateGPU(renderer, 0, numBones * sizeof(glm::mat4));
        }
    }
    return vbuf->updateGPU(renderer, mesh_->getIndexBuffer(), shader);
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
