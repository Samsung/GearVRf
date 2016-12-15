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
#include "shader_manager.h"
#include "objects/shader_data.h"
#include "util/gvr_log.h"

namespace gvr {
/**
 * Keeps track of a set of PostEffect shaders.
 * The only real difference between this class and the ShaderManager
 * is that it contains geometry for a full screen quad.
 * This class should not exist - the geometry can be kept elsewhere.
 */
class PostEffectShaderManager: public ShaderManager {
public:
    PostEffectShaderManager() :
            ShaderManager(), render_data(NULL) {
        std::vector<glm::vec3> quad_vertices;
        std::vector<glm::vec2> quad_uvs;
        std::vector<unsigned short> quad_triangles;

        quad_vertices.push_back(glm::vec3(-1.0f, -1.0f, 0.0f));
        quad_vertices.push_back(glm::vec3(-1.0f, 1.0f, 0.0f));
        quad_vertices.push_back(glm::vec3(1.0f, -1.0f, 0.0f));
        quad_vertices.push_back(glm::vec3(1.0f, 1.0f, 0.0f));

        quad_uvs.push_back(glm::vec2(0.0f, 0.0f));
        quad_uvs.push_back(glm::vec2(0.0f, 1.0f));
        quad_uvs.push_back(glm::vec2(1.0f, 0.0f));
        quad_uvs.push_back(glm::vec2(1.0f, 1.0f));

        quad_triangles.push_back(0);
        quad_triangles.push_back(1);
        quad_triangles.push_back(2);

        quad_triangles.push_back(1);
        quad_triangles.push_back(3);
        quad_triangles.push_back(2);
        Mesh* mesh = new Mesh(std::string("float3 a_position float2 a_texcoord"));
        mesh->set_vertices(quad_vertices);
        mesh->setVec2Vector("a_texcoord",quad_uvs);
        mesh->set_triangles(quad_triangles);
        RenderPass* pass = new RenderPass();
        render_data = new RenderData();
        render_data->set_mesh(mesh);
        render_data->add_pass(pass);
    }

    RenderData* get_render_data()
    {
        return render_data;
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
    RenderData* render_data;
    std::vector<glm::vec3> quad_vertices_;
    std::vector<glm::vec2> quad_uvs_;
    std::vector<unsigned short> quad_triangles_;
};

}
#endif
