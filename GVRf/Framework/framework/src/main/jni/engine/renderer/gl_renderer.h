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

#ifndef FRAMEWORK_GL_RENDERER_H
#define FRAMEWORK_GL_RENDERER_H

#include <vector>
#include <memory>

#define __gl2_h_
#include "EGL/egl.h"
#include "EGL/eglext.h"
#ifndef GL_ES_VERSION_3_0
#include "GLES3/gl3.h"
#include <GLES2/gl2ext.h>
#include "GLES3/gl3ext.h"
#endif

#include "glm/glm.hpp"
#include "batch.h"
#include "objects/eye_type.h"
#include "objects/mesh.h"
#include "objects/bounding_volume.h"
#include "gl/gl_program.h"
#include <unordered_map>
#include "renderer.h"

typedef unsigned long Long;
namespace gvr {
class Camera;
class Scene;
class SceneObject;
class PostEffectData;
class PostEffectShaderManager;
class RenderData;
class RenderTexture;
class ShaderManager;
class Light;

class GLRenderer: public Renderer {
    friend class Renderer;
protected:
    GLRenderer(){}
    virtual ~GLRenderer(){}
public:
    // pure virtual
     void renderCamera(Scene* scene, Camera* camera,
             ShaderManager* shader_manager,
             PostEffectShaderManager* post_effect_shader_manager,
             RenderTexture* post_effect_render_texture_a,
             RenderTexture* post_effect_render_texture_b);
     void renderCamera(Scene* scene, Camera* camera, int viewportX,
             int viewportY, int viewportWidth, int viewportHeight,
             ShaderManager* shader_manager,
             PostEffectShaderManager* post_effect_shader_manager,
             RenderTexture* post_effect_render_texture_a,
             RenderTexture* post_effect_render_texture_b);

    void restoreRenderStates(RenderData* render_data);
    void setRenderStates(RenderData* render_data, RenderState& rstate);
    void renderShadowMap(RenderState& rstate, Camera* camera, GLuint framebufferId, std::vector<SceneObject*>& scene_objects);
    void makeShadowMaps(Scene* scene, ShaderManager* shader_manager, int width, int height);


    // Specific to GL
     void renderCamera(Scene* scene, Camera* camera, int framebufferId,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

     void renderCamera(Scene* scene, Camera* camera,
            RenderTexture* render_texture, ShaderManager* shader_manager,
            PostEffectShaderManager* post_effect_shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

     void set_face_culling(int cull_face);

private:
    // this is specific to GL
    bool checkTextureReady(Material* material);

    // Pure Virtual
    virtual void renderMesh(RenderState& rstate, RenderData* render_data);
    virtual void renderMaterialShader(RenderState& rstate, RenderData* render_data, Material *material) ;
    void occlusion_cull(Scene* scene,
                    std::vector<SceneObject*>& scene_objects,
                    ShaderManager *shader_manager, glm::mat4 vp_matrix);

    void clearBuffers(const Camera& camera) const;
};

}
#endif

