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
#include "shaders/material/custom_shader.h"
#include "shaders/material/error_shader.h"
#include "shaders/material/oes_horizontal_stereo_shader.h"
#include "shaders/material/oes_shader.h"
#include "shaders/material/oes_vertical_stereo_shader.h"
#include "shaders/material/unlit_horizontal_stereo_shader.h"
#include "shaders/material/unlit_shader.h"
#include "shaders/material/unlit_vertical_stereo_shader.h"
#include "util/gvr_log.h"

namespace gvr {
class ShaderManager: public HybridObject {
public:
    ShaderManager() :
            HybridObject(), unlit_shader_(), unlit_horizontal_stereo_shader_(), unlit_vertical_stereo_shader_(), oes_shader_(), oes_horizontal_stereo_shader_(), oes_vertical_stereo_shader_(), error_shader_(), latest_custom_shader_id_(
                    INITIAL_CUSTOM_SHADER_INDEX), custom_shaders_() {
    }
    ~ShaderManager() {
    }
    std::shared_ptr<UnlitShader> getUnlitShader() {
        if (!unlit_shader_) {
            unlit_shader_.reset(new UnlitShader());
        }
        return unlit_shader_;
    }
    std::shared_ptr<UnlitHorizontalStereoShader> getUnlitHorizontalStereoShader() {
        if (!unlit_horizontal_stereo_shader_) {
            unlit_horizontal_stereo_shader_.reset(
                    new UnlitHorizontalStereoShader());
        }
        return unlit_horizontal_stereo_shader_;
    }
    std::shared_ptr<UnlitVerticalStereoShader> getUnlitVerticalStereoShader() {
        if (!unlit_vertical_stereo_shader_) {
            unlit_vertical_stereo_shader_.reset(
                    new UnlitVerticalStereoShader());
        }
        return unlit_vertical_stereo_shader_;
    }
    std::shared_ptr<OESShader> getOESShader() {
        if (!oes_shader_) {
            oes_shader_.reset(new OESShader());
        }
        return oes_shader_;
    }
    std::shared_ptr<OESHorizontalStereoShader> getOESHorizontalStereoShader() {
        if (!oes_horizontal_stereo_shader_) {
            oes_horizontal_stereo_shader_.reset(
                    new OESHorizontalStereoShader());
        }
        return oes_horizontal_stereo_shader_;
    }
    std::shared_ptr<OESVerticalStereoShader> getOESVerticalStereoShader() {
        if (!oes_vertical_stereo_shader_) {
            oes_vertical_stereo_shader_.reset(new OESVerticalStereoShader());
        }
        return oes_vertical_stereo_shader_;
    }
    std::shared_ptr<ErrorShader> getErrorShader() {
        if (!error_shader_) {
            error_shader_.reset(new ErrorShader());
        }
        return error_shader_;
    }
    int addCustomShader(std::string vertex_shader,
            std::string fragment_shader) {
        int id = latest_custom_shader_id_++;
        std::shared_ptr<CustomShader> custom_shader(
                new CustomShader(vertex_shader, fragment_shader));
        custom_shaders_[id] = custom_shader;
        return id;
    }
    std::shared_ptr<CustomShader> getCustomShader(int id) {
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
    std::shared_ptr<UnlitShader> unlit_shader_;
    std::shared_ptr<UnlitHorizontalStereoShader> unlit_horizontal_stereo_shader_;
    std::shared_ptr<UnlitVerticalStereoShader> unlit_vertical_stereo_shader_;
    std::shared_ptr<OESShader> oes_shader_;
    std::shared_ptr<OESHorizontalStereoShader> oes_horizontal_stereo_shader_;
    std::shared_ptr<OESVerticalStereoShader> oes_vertical_stereo_shader_;
    std::shared_ptr<ErrorShader> error_shader_;
    int latest_custom_shader_id_;
    std::map<int, std::shared_ptr<CustomShader>> custom_shaders_;
};

}
#endif
