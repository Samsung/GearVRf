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

#include <gl/gl_index_buffer.h>
#include <gl/gl_vertex_buffer.h>
#include <gl/gl_render_target.h>
#include "glm/gtc/matrix_inverse.hpp"
#include "gl/gl_material.h"
#include "gl/gl_render_data.h"
#include "gl/gl_bitmap_image.h"
#include "gl/gl_cubemap_image.h"
#include "gl/gl_render_texture.h"
#include "gl/gl_render_image.h"
#include "gl/gl_external_image.h"
#include "gl/gl_float_image.h"
#include "gl/gl_imagetex.h"
#include "gl_renderer.h"
#include "objects/scene.h"

namespace gvr
{
    ShaderData *GLRenderer::createMaterial(const char* uniform_desc, const char* texture_desc)
    {
        return new GLMaterial(uniform_desc, texture_desc);
    }

    RenderData *GLRenderer::createRenderData()
    {
        return new GLRenderData();
    }

    RenderData *GLRenderer::createRenderData(RenderData* copy)
    {
        return new GLRenderData(*copy);
    }

    RenderPass* GLRenderer::createRenderPass()
    {
        return new RenderPass();
    }
    RenderTarget* GLRenderer::createRenderTarget(Scene* scene) {
        return new GLRenderTarget(scene);
    }
    RenderTarget* GLRenderer::createRenderTarget(RenderTexture* renderTexture, bool isMultiview){
        return new GLRenderTarget(renderTexture, isMultiview);
    }
    RenderTarget* GLRenderer::createRenderTarget(RenderTexture* renderTexture, const RenderTarget* renderTarget){
        return new GLRenderTarget(renderTexture, renderTarget);
    }
    RenderTexture* GLRenderer::createRenderTexture(const RenderTextureInfo* renderTextureInfo){

        if(renderTextureInfo->useMultiview)
            return  new GLMultiviewRenderTexture(renderTextureInfo->fdboWidth,renderTextureInfo->fboHeight,renderTextureInfo->multisamples,2, renderTextureInfo->fboId, renderTextureInfo->texId);

        return new GLNonMultiviewRenderTexture(renderTextureInfo->fdboWidth,renderTextureInfo->fboHeight,renderTextureInfo->multisamples,renderTextureInfo->fboId, renderTextureInfo->texId);
    }
    void GLRenderer::clearBuffers(const Camera &camera) const
    {
        GLbitfield mask = GL_DEPTH_BUFFER_BIT;

        if (-1 != camera.background_color_r())
        {
            glClearColor(camera.background_color_r(), camera.background_color_g(),
                         camera.background_color_b(), camera.background_color_a());
            mask |= GL_COLOR_BUFFER_BIT;
        }
        if (useStencilBuffer_)
        {
            mask |= GL_STENCIL_BUFFER_BIT;
            glStencilMask(~0);
        }
        glClear(mask);
    }

    GLUniformBlock *GLRenderer::createUniformBlock(const char* desc, int binding,
                                                   const char* name, int maxelems)
    {
        if (maxelems <= 1)
        {
            return new GLUniformBlock(desc, binding, name);
        }
        return new GLUniformBlock(desc, binding, name, maxelems);
    }

    Image *GLRenderer::createImage(int type, int format)
    {
        switch (type)
        {
            case Image::ImageType::BITMAP: return new GLBitmapImage(format);
            case Image::ImageType::CUBEMAP: return new GLCubemapImage(format);
            case Image::ImageType::FLOAT_BITMAP: return new GLFloatImage();
        }
        return NULL;
    }

    Texture *GLRenderer::createTexture(int type)
    {
        Texture *tex = new Texture(type);
        Image *gltex = NULL;

        switch (type)
        {
            case Texture::TextureType::TEXTURE_2D: gltex = new GLImageTex(GL_TEXTURE_2D);
                break;
            case Texture::TextureType::TEXTURE_ARRAY: gltex = new GLImageTex(GL_TEXTURE_2D_ARRAY);
                break;
            case Texture::TextureType::TEXTURE_EXTERNAL: gltex = new GLExternalImage();
                break;
            case Texture::TextureType::TEXTURE_EXTERNAL_RENDERER: gltex = new GLExternalImage();
                break;
        }
        if (gltex)
        {
            tex->setImage(gltex);
        }
        return tex;
    }

    RenderTexture* GLRenderer::createRenderTexture(int width, int height, int sample_count,
                                                   int jcolor_format, int jdepth_format,
                                                   bool resolve_depth,
                                                   const TextureParameters *texparams, int number_views)
    {
        if(number_views == 1)
            return new GLNonMultiviewRenderTexture(width, height, sample_count, jcolor_format, jdepth_format,
                                                 resolve_depth, texparams);

         return new GLMultiviewRenderTexture(width,height,sample_count,jcolor_format,jdepth_format, resolve_depth,texparams, number_views);
    }

    RenderTexture* GLRenderer::createRenderTexture(int width, int height, int sample_count,
                                                   int jcolor_format, int jdepth_format,
                                                   bool resolve_depth,
                                                   const TextureParameters *texparams, int number_views, bool monoscopic)
    {
        return createRenderTexture(width, height, sample_count, jcolor_format, jdepth_format, resolve_depth, texparams, number_views);
    }

    RenderTexture* GLRenderer::createRenderTexture(int width, int height, int sample_count, int layers, int depthformat)
    {
        RenderTexture* tex = new GLNonMultiviewRenderTexture(width, height, sample_count, layers, depthformat);
        return tex;
    }

    Texture *GLRenderer::createSharedTexture(int id)
    {
        Texture *tex = new Texture(GL_TEXTURE_2D);
        tex->setImage(new GLImageTex(GL_TEXTURE_2D, id));
        return tex;
    }

    Shader *GLRenderer::createShader(int id, const char* signature,
                                     const char* uniformDescriptor,
                                     const char* textureDescriptor,
                                     const char* vertexDescriptor,
                                     const char* vertexShader,
                                     const char* fragmentShader)
    {
        return new GLShader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor,
                            vertexShader, fragmentShader);
    }

    VertexBuffer* GLRenderer::createVertexBuffer(const char* desc, int vcount)
    {
        return new GLVertexBuffer(desc, vcount);
    }

    IndexBuffer* GLRenderer::createIndexBuffer(int bytesPerIndex, int icount)
    {
        IndexBuffer* ibuf = new GLIndexBuffer(bytesPerIndex, icount);
        LOGV("Renderer::createIndexBuffer(%d, %d) = %p", bytesPerIndex, icount, ibuf);
        return ibuf;
    }

    GLRenderer::GLRenderer() : transform_ubo_{nullptr, nullptr}
    {
        const char* desc;

        desc = " mat4 u_view_[2]; mat4 u_mvp_[2]; mat4 u_mv_[2]; mat4 u_mv_it_[2]; mat4 u_view_i_[2]; mat4 u_model; float u_right; uint u_render_mask; ";

        transform_ubo_[1] = GLRenderer::createUniformBlock(desc, TRANSFORM_UBO_INDEX, "Transform_ubo", 0);
        transform_ubo_[1]->useGPUBuffer(false);

        desc = " mat4 u_view; mat4 u_mvp; mat4 u_mv; mat4 u_mv_it; mat4 u_view_i; mat4 u_model; float u_right;";
        transform_ubo_[0] = GLRenderer::createUniformBlock(desc, TRANSFORM_UBO_INDEX, "Transform_ubo", 0);
        transform_ubo_[0]->useGPUBuffer(false);
    }


    void GLRenderer::renderRenderTarget(Scene* scene, jobject javaSceneObject, RenderTarget* renderTarget,
                            ShaderManager* shader_manager,
                            RenderTexture* post_effect_render_texture_a,
                            RenderTexture* post_effect_render_texture_b)
    {

        resetStats();

        glDepthMask(GL_TRUE);
        GL(glEnable(GL_DEPTH_TEST));
        GL(glDepthFunc(GL_LEQUAL));
        GL(glEnable(GL_CULL_FACE));
        GL(glFrontFace(GL_CCW));
        GL(glCullFace(GL_BACK));
        GL(glDisable(GL_POLYGON_OFFSET_FILL));
        Camera* camera = renderTarget->getCamera();
        RenderState rstate = renderTarget->getRenderState();
        //@todo makes it clear this is a hack
        rstate.javaSceneObject = javaSceneObject;
        RenderData* post_effects = camera->post_effect_data();
        rstate.scene = scene;
        rstate.shader_manager = shader_manager;
        rstate.uniforms.u_view = camera->getViewMatrix();
        rstate.uniforms.u_proj = camera->getProjectionMatrix();

        std::vector<RenderData*>* render_data_vector = renderTarget->getRenderDataVector();

        if (!rstate.shadow_map)
        {
            rstate.render_mask = camera->render_mask();
            if(rstate.is_multiview)
                rstate.render_mask = RenderData::RenderMaskBit::Right | RenderData::RenderMaskBit::Left;

            rstate.uniforms.u_right = ((camera->render_mask() & RenderData::RenderMaskBit::Right) != 0) ? 1 : 0;
            rstate.material_override = NULL;
            GL(glEnable (GL_BLEND));
            GL(glBlendEquation (GL_FUNC_ADD));
            GL(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA));
            GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));
        }
        if ((post_effects == NULL) ||
            (post_effect_render_texture_a == nullptr) ||
            (post_effects->pass_count() == 0))
        {

            clearBuffers(*camera);
            for (auto it = render_data_vector->begin();
                 it != render_data_vector->end();
                 ++it)
            {
                RenderData* rdata = *it;
                if (!rstate.shadow_map || rdata->cast_shadows())
                {
                    GL(renderRenderData(rstate, rdata));
                }
            }

        }
        else
        {
            static GLint viewport[4];
            glGetIntegerv(GL_VIEWPORT,viewport);
            GLint drawFboId = 0;
            glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &drawFboId);
            int npost = post_effects->pass_count() - 1;
            RenderTexture* renderTexture = post_effect_render_texture_a;
            RenderTexture* input_texture = renderTexture;
            GL(glBindFramebuffer(GL_FRAMEBUFFER, renderTexture->getFrameBufferId()));
            GL(glViewport(0, 0, renderTexture->width(), renderTexture->height()));
            GL(clearBuffers(*camera));
            for (auto it = render_data_vector->begin();
                 it != render_data_vector->end();
                 ++it)
            {
                RenderData* rdata = *it;
                if (!rstate.shadow_map || rdata->cast_shadows())
                {
                    GL(renderRenderData(rstate, rdata));
                }
            }
            GL(glDisable(GL_DEPTH_TEST));
            GL(glDisable(GL_CULL_FACE));
            for (int i = 0; i < npost; ++i)
            {
                if (i % 2 == 0)
                {
                    renderTexture = static_cast<GLRenderTexture*>(post_effect_render_texture_b);
                }
                else
                {
                    renderTexture = static_cast<GLRenderTexture*>(post_effect_render_texture_a);
                }
                GL(glBindFramebuffer(GL_FRAMEBUFFER, renderTexture->getFrameBufferId()));
                GL(glViewport(0, 0, renderTexture->width(), renderTexture->height()));
                GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
                renderPostEffectData(rstate, input_texture, post_effects, i);
                input_texture = renderTexture;
            }
            GL(glBindFramebuffer(GL_FRAMEBUFFER, drawFboId));
            GL(glViewport(viewport[0], viewport[1], viewport[2], viewport[3]));
            GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
            renderPostEffectData(rstate, input_texture, post_effects, npost);
        }
        GL(glDisable(GL_BLEND));

    }

/**
 * Set the render states for render data
 */
    void GLRenderer::setRenderStates(RenderData *render_data, RenderState &rstate)
    {

        if (!(rstate.render_mask & render_data->render_mask()))
            return;

        if (render_data->offset())
        {
            GL(glEnable(GL_POLYGON_OFFSET_FILL));
            GL(glPolygonOffset(render_data->offset_factor(), render_data->offset_units()));
        }
        if (!render_data->depth_test())
        {
            GL(glDisable(GL_DEPTH_TEST));
        }

        if (!render_data->depth_mask())
        {
            GL(glDepthMask(GL_FALSE););
        }

        if (render_data->stencil_test())
        {
            GL(glEnable(GL_STENCIL_TEST));

            GL(glStencilFunc(render_data->stencil_func_func(), render_data->stencil_func_ref(),
                             render_data->stencil_func_mask()));

            int sfail = render_data->stencil_op_sfail();
            int dpfail = render_data->stencil_op_dpfail();
            int dppass = render_data->stencil_op_dppass();
            if (0 != sfail && 0 != dpfail && 0 != dppass)
            {
                GL(glStencilOp(sfail, dpfail, dppass));
            }

            GL(glStencilMask(render_data->getStencilMask()));
            if (RenderData::Queue::Stencil == render_data->rendering_order())
            {
                GL(glDepthMask(GL_FALSE));
                GL(glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE));
            }
        }

        if (!render_data->alpha_blend())
        {
            GL(glDisable(GL_BLEND));
        }
        if (render_data->alpha_to_coverage())
        {
            GL(glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE));
            GL(glSampleCoverage(render_data->sample_coverage(),
                                render_data->invert_coverage_mask()));
        }
        glBlendFunc(render_data->source_alpha_blend_func(), render_data->dest_alpha_blend_func());
    }

/**
 * Restore the render states for render data
 */
    void GLRenderer::restoreRenderStates(RenderData *render_data)
    {
        if (render_data->cull_face() != RenderData::CullBack)
        {
            GL(glEnable(GL_CULL_FACE));
            GL(glCullFace(GL_BACK));
        }

        if (render_data->offset())
        {
            GL(glDisable(GL_POLYGON_OFFSET_FILL));
        }
        if (!render_data->depth_test())
        {
            GL(glEnable(GL_DEPTH_TEST));
        }

        if (!render_data->depth_mask())
        {
            GL(glDepthMask(GL_TRUE));
        }

        if (render_data->stencil_test())
        {
            GL(glDisable(GL_STENCIL_TEST));
            if (RenderData::Queue::Stencil == render_data->rendering_order())
            {
                GL(glDepthMask(GL_TRUE));
                GL(glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE));
            }
        }

        if (!render_data->alpha_blend())
        {
            GL(glEnable(GL_BLEND));
        }
        if (render_data->alpha_to_coverage())
        {
            GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));
        }
    }

    /**
     * Generate shadow maps for all the lights that cast shadows.
     * The scene is rendered from the viewpoint of the light using a
     * special depth shader (GVRDepthShader) to create the shadow map.
     * @see Renderer::renderShadowMap Light::makeShadowMap
     */
    void GLRenderer::makeShadowMaps(Scene* scene, jobject javaSceneObject, ShaderManager* shader_manager)
    {
        checkGLError("makeShadowMaps");
        const std::vector<Light*> lights = scene->getLightList();
        GLint drawFB, readFB;
        int texIndex = 0;

        glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &drawFB);
        glGetIntegerv(GL_READ_FRAMEBUFFER_BINDING, &readFB);
        for (auto it = lights.begin(); it != lights.end(); ++it) {
            (*it)->makeShadowMap(scene, javaSceneObject, shader_manager, texIndex);
            ++texIndex;
        }
        glBindFramebuffer(GL_READ_FRAMEBUFFER, readFB);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, drawFB);
    }

    void GLRenderer::set_face_culling(int cull_face)
    {
        switch (cull_face)
        {
            case RenderData::CullFront:glEnable(GL_CULL_FACE);
                glCullFace(GL_FRONT);
                break;

            case RenderData::CullNone:glDisable(GL_CULL_FACE);
                break;

                // CullBack as Default
            default:glEnable(GL_CULL_FACE);
                glCullFace(GL_BACK);
                break;
        }
    }

    void GLRenderer::occlusion_cull(RenderState &rstate, std::vector<SceneObject *> &scene_objects, std::vector<RenderData*>* render_data_vector)
    {

        if (!occlusion_cull_init(rstate, scene_objects, render_data_vector))
            return;

        for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it)
        {
            SceneObject *scene_object = (*it);
            RenderData *render_data = scene_object->render_data();
            if (render_data == 0 || render_data->material(0) == 0)
            {
                continue;
            }

            //If a query was issued on an earlier or same frame and if results are
            //available, then update the same. If results are unavailable, do nothing
            if (!scene_object->is_query_issued())
            {
                continue;
            }

            //If a previous query is active, do not issue a new query.
            //This avoids overloading the GPU with too many queries
            //Queries may span multiple frames

            bool is_query_issued = scene_object->is_query_issued();
            if (!is_query_issued)
            {
                //Setup basic bounding box and material
                RenderData *bounding_box_render_data(createRenderData());
                Mesh *bounding_box_mesh = render_data->mesh()->createBoundingBox();
                ShaderData *bbox_material = new GLMaterial("", "");
                RenderPass *pass = Renderer::getInstance()->createRenderPass();
                GLShader *bboxShader = static_cast<GLShader *>(rstate.shader_manager
                        ->findShader("GVRBoundingBoxShader"));
                pass->set_shader(bboxShader->getProgramId(), false);
                pass->set_material(bbox_material);
                bounding_box_render_data->set_mesh(bounding_box_mesh);
                bounding_box_render_data->add_pass(pass);
                if (bounding_box_render_data->isValid(this, rstate) >= 0)
                {
                    GLuint* query = scene_object->get_occlusion_array();

                    glDepthFunc(GL_LEQUAL);
                    glEnable(GL_DEPTH_TEST);
                    glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);

                    rstate.uniforms.u_model = scene_object->transform()->getModelMatrix();
                    rstate.uniforms.u_mv = rstate.uniforms.u_view * rstate.uniforms.u_model;
                    rstate.uniforms.u_mv_it = glm::inverseTranspose(rstate.uniforms.u_mv);
                    rstate.uniforms.u_mvp = rstate.uniforms.u_proj * rstate.uniforms.u_mv;

                    //Issue the query only with a bounding box
                    glBeginQuery(GL_ANY_SAMPLES_PASSED, query[0]);
                    renderWithShader(rstate, bboxShader, bounding_box_render_data,
                                     bounding_box_render_data->material(0), 0);
                    glEndQuery(GL_ANY_SAMPLES_PASSED);
                    scene_object->set_query_issued(true);

                    glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);

                    //Delete the generated bounding box mesh
                    delete bounding_box_mesh;
                    delete bbox_material;
                    delete pass;
                    delete bounding_box_render_data;
                }
            }

            GLuint query_result = GL_FALSE;
            GLuint *query = (*it)->get_occlusion_array();
            glGetQueryObjectuiv(query[0], GL_QUERY_RESULT_AVAILABLE, &query_result);

            if (query_result)
            {
                GLuint pixel_count;
                glGetQueryObjectuiv(query[0], GL_QUERY_RESULT, &pixel_count);
                bool visibility = ((pixel_count & GL_TRUE) == GL_TRUE);

                (*it)->set_visible(visibility);
                (*it)->set_query_issued(false);
                addRenderData((*it)->render_data(), rstate, *render_data_vector);
                rstate.scene->pick(scene_object);
            }
        }
        rstate.scene->unlockColliders();
    }

    void GLRenderer::renderMesh(RenderState &rstate, RenderData *render_data)
    {
        Mesh* mesh = render_data->mesh();
        int indexCount = mesh->getIndexCount();
        ShaderData* curr_material = rstate.material_override;
        Shader* shader = nullptr;
        /*
         * If a material override is provided, render the mesh
         * once with the designated material.
         * If updateGPU returns -1, some textures are not ready
         * yet and we do not render this mesh.
         */
        if (rstate.shadow_map && curr_material)
        {
            const char* depthShaderName = mesh->hasBones() ? "GVRDepthShader$a_bone_weights$a_bone_indices" : "GVRDepthShader";
            shader = rstate.shader_manager->findShader(depthShaderName);

            if (shader == nullptr)
            {
                LOGE("Renderer::renderMesh cannot find depth shader %s", depthShaderName);
                return;
            }
            if (curr_material->updateGPU(this,render_data) >= 0)
            {
                numberTriangles += indexCount;
                numberDrawCalls++;
                set_face_culling(render_data->pass(0)->cull_face());
                render_data->updateGPU(this, shader);
                GL(renderMaterialShader(rstate, render_data, curr_material, shader));
            }
            return;
        }
        /*
         * No material override, render the mesh once for each pass
         * using a different shader each time.
         */
        for (int curr_pass = 0; curr_pass < render_data->pass_count(); ++curr_pass)
        {
            numberTriangles += indexCount;
            numberDrawCalls++;
            set_face_culling(render_data->pass(curr_pass)->cull_face());
            curr_material = render_data->pass(curr_pass)->material();
            int shader_id = render_data->get_shader(rstate.is_multiview, curr_pass);
            shader = rstate.shader_manager->getShader(shader_id);
            renderWithShader(rstate, shader, render_data, curr_material, curr_pass);
        }
        render_data->clearDirty();
    }

    void GLRenderer::renderMaterialShader(RenderState& rstate, RenderData* render_data,
                                          ShaderData* curr_material, Shader* shader)
    {
        GLMaterial* material = static_cast<GLMaterial*>(curr_material);
        GLRenderData* rdata = static_cast<GLRenderData*>(render_data);
        int drawMode = render_data->draw_mode();

        try
        {
            shader->useShader(rstate.is_multiview);
        }
        catch (const std::string &error)
        {
            LOGE("Error detected in Renderer::renderRenderData; name : %s, error : %s",
                 render_data->owner_object()->name().c_str(), error.c_str());
            shader = rstate.shader_manager->findShader("GVRErrorShader");
            shader->useShader(rstate.is_multiview);
        }
        if ((drawMode == GL_LINE_STRIP) ||
            (drawMode == GL_LINES) ||
            (drawMode == GL_LINE_LOOP))
        {
            float lineWidth;
            if (curr_material->getFloat("line_width", lineWidth))
            {
                glLineWidth(lineWidth);
            }
            else
            {
                glLineWidth(1.0f);
            }
        }
        int texIndex = material->bindToShader(shader, this);
        if (texIndex >= 0)
        {
            if (shader->usesMatrixUniforms())
            {
                UniformBlock* transformBlock = getTransformUbo(rstate.is_multiview ? 1 : 0);
                updateTransforms(rstate, transformBlock, rdata);
                if (!transformBlock->usesGPUBuffer())
                {
                    static_cast<GLShader*>(shader)->findUniforms(*transformBlock, TRANSFORM_UBO_INDEX);
                }
                transformBlock->bindBuffer(shader, this);
            }
            if (shader->useLights())
            {
                updateLights(rstate, shader, texIndex);
            }
            checkGLError("renderMesh:before render");
            rdata->render(shader, this);
        }
        checkGLError("renderMesh::renderMaterialShader");
    }

    bool GLRenderer::renderWithShader(RenderState& rstate, Shader* shader, RenderData* renderData, ShaderData* shaderData, int renderPass)
    {
        if (shader == NULL)
        {
            LOGE("SHADER: shader not found");
            return false;
        }
        if (shaderData->updateGPU(this, renderData) >= 0)
        {
            renderData->updateGPU(this,shader);
            renderMaterialShader(rstate, renderData, shaderData, shader);
            return true;
        }
        return  false;
    }

    void GLRenderer::updateLights(RenderState& rstate, Shader* shader, int texIndex)
    {
        const std::vector<Light*>& lightlist = rstate.scene->getLightList();
        ShadowMap* shadowMap = nullptr;

        for (auto it = lightlist.begin();
             it != lightlist.end();
             ++it)
        {
            Light* light = (*it);
            if (light != NULL)
            {
                light->render(shader);
                ShadowMap* sm = light->getShadowMap();
                if (sm != nullptr)
                {
                    shadowMap = sm;
                }
            }
        }
        if (shadowMap)
        {
            GLShader* glshader = static_cast<GLShader*>(shader);
            int loc = glGetUniformLocation(glshader->getProgramId(), "u_shadow_maps");
            if (loc >= 0)
            {
                shadowMap->bindTexture(loc, texIndex);
            }
        }
        checkGLError("GLRenderer::updateLights");
    }
void GLRenderer::updatePostEffectMesh(Mesh* copy_mesh)
{
    float positions[] = { -1.0f, -1.0f, 0.0f, -1.0f, 1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f };
    float uvs[] = { 0.0f, 0.0, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f };
    unsigned short faces[] = { 0, 2, 1, 1, 2, 3 };

    const int position_size = sizeof(positions)/ sizeof(positions[0]);
    const int uv_size = sizeof(uvs)/ sizeof(uvs[0]);
    const int faces_size = sizeof(faces)/ sizeof(faces[0]);

    copy_mesh->setVertices(positions, position_size);
    copy_mesh->setFloatVec("a_texcoord", uvs, uv_size);
    copy_mesh->setTriangles(faces, faces_size);
}

}

