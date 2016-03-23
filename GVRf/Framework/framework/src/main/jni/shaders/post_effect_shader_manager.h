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
 * Manages instances of post effect shaders.
 ***************************************************************************/

#ifndef POST_EFFECT_SHADER_MANAGER_H_
#define POST_EFFECT_SHADER_MANAGER_H_

#include "objects/hybrid_object.h"
#include "shaders/posteffect/color_blend_post_effect_shader.h"
#include "shaders/posteffect/horizontal_flip_post_effect_shader.h"
#include "shaders/posteffect/custom_post_effect_shader.h"
#include "util/gvr_log.h"

namespace gvr {
class PostEffectShaderManager: public HybridObject {
public:
    PostEffectShaderManager() :
            HybridObject(), color_blend_post_effect_shader_(), horizontal_flip_post_effect_shader_(), latest_custom_shader_id_(
                    INITIAL_CUSTOM_SHADER_INDEX), custom_post_effect_shaders_(), quad_vertices_(), quad_uvs_(), quad_triangles_() {
        quad_vertices_.push_back(glm::vec3(-1.0f, -1.0f, 0.0f));
        quad_vertices_.push_back(glm::vec3(-1.0f, 1.0f, 0.0f));
        quad_vertices_.push_back(glm::vec3(1.0f, -1.0f, 0.0f));
        quad_vertices_.push_back(glm::vec3(1.0f, 1.0f, 0.0f));

        quad_uvs_.push_back(glm::vec2(0.0f, 0.0f));
        quad_uvs_.push_back(glm::vec2(0.0f, 1.0f));
        quad_uvs_.push_back(glm::vec2(1.0f, 0.0f));
        quad_uvs_.push_back(glm::vec2(1.0f, 1.0f));

        quad_triangles_.push_back(0);
        quad_triangles_.push_back(1);
        quad_triangles_.push_back(2);

        quad_triangles_.push_back(1);
        quad_triangles_.push_back(3);
        quad_triangles_.push_back(2);
    }

    ~PostEffectShaderManager() {
        delete color_blend_post_effect_shader_;
        delete horizontal_flip_post_effect_shader_;
        // We don't delete the custom shaders, as their Java owner-objects will do that for us.
    }

    ColorBlendPostEffectShader* getColorBlendPostEffectShader() {
        if (!color_blend_post_effect_shader_) {
            color_blend_post_effect_shader_ = new ColorBlendPostEffectShader();
        }
        return color_blend_post_effect_shader_;
    }

    HorizontalFlipPostEffectShader* getHorizontalFlipPostEffectShader() {
        if (!horizontal_flip_post_effect_shader_) {
            horizontal_flip_post_effect_shader_ =
                    new HorizontalFlipPostEffectShader();
        }
        return horizontal_flip_post_effect_shader_;
    }

    int addCustomPostEffectShader(std::string vertex_shader,
            std::string fragment_shader) {
        int id = latest_custom_shader_id_++;
        CustomPostEffectShader* custom_post_effect_shader =
                new CustomPostEffectShader(vertex_shader, fragment_shader);
        custom_post_effect_shaders_[id] = custom_post_effect_shader;
        return id;
    }

    CustomPostEffectShader* getCustomPostEffectShader(int id) {
        auto it = custom_post_effect_shaders_.find(id);
        if (it != custom_post_effect_shaders_.end()) {
            return it->second;
        } else {
            LOGE("PostEffectShaderManager::getCustomPostEffectShader()");
            throw "PostEffectShaderManager::getCustomPostEffectShader()";
        }
    }

    std::vector<glm::vec3>& quad_vertices() {
        return quad_vertices_;
    }

    std::vector<glm::vec2>& quad_uvs() {
        return quad_uvs_;
    }

    std::vector<unsigned short>& quad_triangles() {
        return quad_triangles_;
    }

private:
    PostEffectShaderManager(
            const PostEffectShaderManager& post_effect_shader_manager);
    PostEffectShaderManager(
            PostEffectShaderManager&& post_effect_shader_manager);
    PostEffectShaderManager& operator=(
            const PostEffectShaderManager& post_effect_shader_manager);
    PostEffectShaderManager& operator=(
            PostEffectShaderManager&& post_effect_shader_manager);

private:
    static const int INITIAL_CUSTOM_SHADER_INDEX = 1000;
    ColorBlendPostEffectShader* color_blend_post_effect_shader_;
    HorizontalFlipPostEffectShader* horizontal_flip_post_effect_shader_;
    int latest_custom_shader_id_;
    std::map<int, CustomPostEffectShader*> custom_post_effect_shaders_;
    std::vector<glm::vec3> quad_vertices_;
    std::vector<glm::vec2> quad_uvs_;
    std::vector<unsigned short> quad_triangles_;
};

}
#endif
