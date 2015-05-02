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

#ifndef RENDERER_H_
#define RENDERER_H_

#include <vector>
#include <memory>

#define __gl2_h_
#include "EGL/egl.h"
#include "EGL/eglext.h"
#include "GLES3/gl3.h"
#include <GLES2/gl2ext.h>
#include "GLES3/gl3ext.h"

#include "glm/glm.hpp"

#include "objects/eye_type.h"

namespace gvr {
class Camera;
class Scene;
class SceneObject;
class PostEffectData;
class PostEffectShaderManager;
class RenderData;
class RenderTexture;
class ShaderManager;

class Renderer {
private:
    Renderer();

public:
    static void renderCamera(std::shared_ptr<Scene> scene,
            std::shared_ptr<Camera> camera,
            std::shared_ptr<RenderTexture> render_texture,
            std::shared_ptr<ShaderManager> shader_manager,
            std::shared_ptr<PostEffectShaderManager> post_effect_shader_manager,
            std::shared_ptr<RenderTexture> post_effect_render_texture_a,
            std::shared_ptr<RenderTexture> post_effect_render_texture_b);

    static void renderCamera(std::shared_ptr<Scene> scene,
            std::shared_ptr<Camera> camera,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            std::shared_ptr<ShaderManager> shader_manager,
            std::shared_ptr<PostEffectShaderManager> post_effect_shader_manager,
            std::shared_ptr<RenderTexture> post_effect_render_texture_a,
            std::shared_ptr<RenderTexture> post_effect_render_texture_b);

    static void renderCamera(std::shared_ptr<Scene> scene,
            std::shared_ptr<Camera> camera,
            std::shared_ptr<RenderTexture> render_texture,
            std::shared_ptr<ShaderManager> shader_manager,
            std::shared_ptr<PostEffectShaderManager> post_effect_shader_manager,
            std::shared_ptr<RenderTexture> post_effect_render_texture_a,
            std::shared_ptr<RenderTexture> post_effect_render_texture_b,
            glm::mat4 vp_matrix);

private:
    static void renderRenderData(std::shared_ptr<RenderData> render_data,
            const glm::mat4& vp_matrix, int render_mask,
            std::shared_ptr<ShaderManager> shader_manager);
    static void renderPostEffectData(
            std::shared_ptr<RenderTexture> render_texture,
            std::shared_ptr<PostEffectData> post_effect_data,
            std::shared_ptr<PostEffectShaderManager> post_effect_shader_manager);

    Renderer(const Renderer& render_engine);
    Renderer(Renderer&& render_engine);
    Renderer& operator=(const Renderer& render_engine);
    Renderer& operator=(Renderer&& render_engine);
};

}
#endif
