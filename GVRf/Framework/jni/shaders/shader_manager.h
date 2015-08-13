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
 * Manages instances of shaders.
 ***************************************************************************/

#ifndef SHADER_MANAGER_H_
#define SHADER_MANAGER_H_

#include "objects/hybrid_object.h"
#include "shaders/material/bounding_box_shader.h"
#include "shaders/material/custom_shader.h"
#include "shaders/material/error_shader.h"
#include "shaders/material/oes_horizontal_stereo_shader.h"
#include "shaders/material/oes_shader.h"
#include "shaders/material/oes_vertical_stereo_shader.h"
#include "shaders/material/unlit_horizontal_stereo_shader.h"
#include "shaders/material/unlit_vertical_stereo_shader.h"
#include "shaders/material/cubemap_shader.h"
#include "shaders/material/cubemap_reflection_shader.h"
#include "shaders/material/texture_shader.h"
#include "shaders/material/external_renderer_shader.h"
#include "shaders/material/assimp_shader.h"
#include "util/gvr_log.h"

namespace gvr {
class ShaderManager: public HybridObject {
public:
    ShaderManager() :
            HybridObject(), bounding_box_shader_(),
            unlit_horizontal_stereo_shader_(), unlit_vertical_stereo_shader_(),
            oes_shader_(), oes_horizontal_stereo_shader_(), oes_vertical_stereo_shader_(),
            cubemap_shader_(), cubemap_reflection_shader_(), texture_shader_(), assimp_shader_(),
            external_renderer_shader_(), error_shader_(), latest_custom_shader_id_(
                    INITIAL_CUSTOM_SHADER_INDEX), custom_shaders_() {
    }
    ~ShaderManager() {
        delete unlit_horizontal_stereo_shader_;
        delete unlit_vertical_stereo_shader_;
        delete oes_shader_;
        delete oes_horizontal_stereo_shader_;
        delete oes_vertical_stereo_shader_;
        delete cubemap_shader_;
        delete cubemap_reflection_shader_;
        delete texture_shader_;
        delete external_renderer_shader_;
        delete assimp_shader_;
        delete error_shader_;
        // We don't delete the custom shaders, as their Java owner-objects will do that for us.
    }
    BoundingBoxShader* getBoundingBoxShader() {
        if (!bounding_box_shader_) {
            bounding_box_shader_ = new BoundingBoxShader();
        }
        return bounding_box_shader_;
    }
    UnlitHorizontalStereoShader* getUnlitHorizontalStereoShader() {
        if (!unlit_horizontal_stereo_shader_) {
            unlit_horizontal_stereo_shader_ = new UnlitHorizontalStereoShader();
        }
        return unlit_horizontal_stereo_shader_;
    }
    UnlitVerticalStereoShader* getUnlitVerticalStereoShader() {
        if (!unlit_vertical_stereo_shader_) {
            unlit_vertical_stereo_shader_ = new UnlitVerticalStereoShader();
        }
        return unlit_vertical_stereo_shader_;
    }
    OESShader* getOESShader() {
        if (!oes_shader_) {
            oes_shader_ = new OESShader();
        }
        return oes_shader_;
    }
    OESHorizontalStereoShader* getOESHorizontalStereoShader() {
        if (!oes_horizontal_stereo_shader_) {
            oes_horizontal_stereo_shader_ = new OESHorizontalStereoShader();
        }
        return oes_horizontal_stereo_shader_;
    }
    OESVerticalStereoShader* getOESVerticalStereoShader() {
        if (!oes_vertical_stereo_shader_) {
            oes_vertical_stereo_shader_ = new OESVerticalStereoShader();
        }
        return oes_vertical_stereo_shader_;
    }
    CubemapShader* getCubemapShader() {
        if (!cubemap_shader_) {
            cubemap_shader_ = new CubemapShader();
        }
        return cubemap_shader_;
    }
    CubemapReflectionShader* getCubemapReflectionShader() {
        if (!cubemap_reflection_shader_) {
            cubemap_reflection_shader_ = new CubemapReflectionShader();
        }
        return cubemap_reflection_shader_;
    }
    TextureShader* getTextureShader() {
        if (!texture_shader_) {
            texture_shader_ = new TextureShader();
        }
        return texture_shader_;
    }
    ExternalRendererShader* getExternalRendererShader() {
        if (!external_renderer_shader_) {
            external_renderer_shader_ = new ExternalRendererShader();
        }
        return external_renderer_shader_;
    }
    AssimpShader* getAssimpShader() {
        if (!assimp_shader_) {
            assimp_shader_ = new AssimpShader();
        }
        return assimp_shader_;
    }
    ErrorShader* getErrorShader() {
        if (!error_shader_) {
            error_shader_ = new ErrorShader();
        }
        return error_shader_;
    }
    int addCustomShader(std::string vertex_shader,
            std::string fragment_shader) {
        int id = latest_custom_shader_id_++;
        CustomShader* custom_shader(
                new CustomShader(vertex_shader, fragment_shader));
        custom_shaders_[id] = custom_shader;
        return id;
    }
    CustomShader* getCustomShader(int id) {
        auto it = custom_shaders_.find(id);
        if (it != custom_shaders_.end()) {
            return it->second;
        } else {
            LOGE("ShaderManager::getCustomShader()");
            throw "ShaderManager::getCustomShader()";
        }
    }

private:
    ShaderManager(const ShaderManager& shader_manager);
    ShaderManager(ShaderManager&& shader_manager);
    ShaderManager& operator=(const ShaderManager& shader_manager);
    ShaderManager& operator=(ShaderManager&& shader_manager);

private:
    static const int INITIAL_CUSTOM_SHADER_INDEX = 1000;
    BoundingBoxShader* bounding_box_shader_;
    UnlitHorizontalStereoShader* unlit_horizontal_stereo_shader_;
    UnlitVerticalStereoShader* unlit_vertical_stereo_shader_;
    OESShader* oes_shader_;
    OESHorizontalStereoShader* oes_horizontal_stereo_shader_;
    OESVerticalStereoShader* oes_vertical_stereo_shader_;
    CubemapShader* cubemap_shader_;
    CubemapReflectionShader* cubemap_reflection_shader_;
    TextureShader* texture_shader_;
    ExternalRendererShader* external_renderer_shader_;
    AssimpShader* assimp_shader_;
    ErrorShader* error_shader_;
    int latest_custom_shader_id_;
    std::map<int, CustomShader*> custom_shaders_;
};

}
#endif
