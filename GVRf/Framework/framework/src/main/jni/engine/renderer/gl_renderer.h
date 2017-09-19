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
#include "gl/gl_uniform_block.h"

typedef unsigned long Long;
namespace gvr {
class Camera;
class Scene;
class SceneObject;
class ShaderData;
class RenderTexture;
class RenderData;
class RenderTexture;
class Light;

class GLRenderer: public Renderer {
    friend class Renderer;
protected:
    GLRenderer();

    virtual ~GLRenderer()
    {
        if(transform_ubo_[0])
            delete transform_ubo_[0];
        if(transform_ubo_[1])
            delete transform_ubo_[1];

    }

public:
    // pure virtual
     void renderCamera(Scene* scene, Camera* camera,
             ShaderManager* shader_manager,
             RenderTexture* post_effect_render_texture_a,
             RenderTexture* post_effect_render_texture_b);
     void renderCamera(Scene* scene, Camera* camera, int viewportX,
             int viewportY, int viewportWidth, int viewportHeight,
             ShaderManager* shader_manager,
             RenderTexture* post_effect_render_texture_a,
             RenderTexture* post_effect_render_texture_b);

    void restoreRenderStates(RenderData* render_data);
    void setRenderStates(RenderData* render_data, RenderState& rstate);
    Texture* createSharedTexture(int id);
    virtual IndexBuffer* createIndexBuffer(int bytesPerIndex, int icount);
    virtual VertexBuffer* createVertexBuffer(const char* descriptor, int vcount);

    virtual void renderRenderTarget(Scene*, RenderTarget* renderTarget, ShaderManager* shader_manager,
            RenderTexture* post_effect_render_texture_a, RenderTexture* post_effect_render_texture_b);
    void makeShadowMaps(Scene* scene, ShaderManager* shader_manager);

    // Specific to GL
    void renderCamera(Scene* scene, Camera* camera, int framebufferId,
            int viewportX, int viewportY, int viewportWidth, int viewportHeight,
            ShaderManager* shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    void renderCamera(Scene* scene, Camera* camera,
            RenderTexture* render_texture, ShaderManager* shader_manager,
            RenderTexture* post_effect_render_texture_a,
            RenderTexture* post_effect_render_texture_b);

    void set_face_culling(int cull_face);
    virtual RenderPass* createRenderPass();
    virtual ShaderData* createMaterial(const char* uniform_desc, const char* texture_desc);
    virtual RenderData* createRenderData();
    virtual UniformBlock* createUniformBlock(const char* desc, int binding, const char* name, int maxelems);
    virtual Image* createImage(int type, int format);
    virtual Texture* createTexture(int target = GL_TEXTURE_2D);
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count, int layers);
    virtual RenderTexture* createRenderTexture(int width, int height, int sample_count,
                                               int jcolor_format, int jdepth_format, bool resolve_depth,
                                               const TextureParameters* texture_parameters, int number_views);
    virtual Shader* createShader(int id, const char* signature,
                                 const char* uniformDescriptor, const char* textureDescriptor,
                                 const char* vertexDescriptor, const char* vertexShader,
                                 const char* fragmentShader);
    GLUniformBlock* getTransformUbo(int index) { return transform_ubo_[index]; }
    virtual Mesh* getPostEffectMesh();
    virtual bool renderWithShader(RenderState& rstate, Shader* shader, RenderData* renderData, ShaderData* shaderData,  int);

private:
    void updateLights(RenderState &rstate, Shader* shader, int texIndex);
    virtual void renderMesh(RenderState& rstate, RenderData* render_data);
    virtual void renderMaterialShader(RenderState& rstate, RenderData* render_data, ShaderData *material, Shader* shader);
    virtual void occlusion_cull(RenderState& rstate, std::vector<SceneObject*>& scene_objects, std::vector<RenderData*>* render_data_vector);
    void clearBuffers(const Camera& camera) const;
    RenderData* post_effect_render_data();

    GLUniformBlock* transform_ubo_[2];
};

}
#endif

