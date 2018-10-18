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
#include "glm/gtc/matrix_inverse.hpp"

#include "gl/gl_index_buffer.h"
#include "gl/gl_vertex_buffer.h"
#include "gl/gl_material.h"
#include "gl/gl_render_data.h"
#include "gl/gl_bitmap_image.h"
#include "gl/gl_cubemap_image.h"
#include "gl/gl_render_texture.h"
#include "gl/gl_render_image.h"
#include "gl/gl_external_image.h"
#include "gl/gl_float_image.h"
#include "gl/gl_imagetex.h"
#include "gl/gl_light.h"
#include "gl_renderer.h"
#include "main_sorter.h"
#include "shadow_sorter.h"
#include "objects/scene.h"
#include "objects/components/render_target.h"

namespace gvr
{

    GLRenderer::GLRenderer() : Renderer()
    {
        mMatrixUniforms = createUniformBlock("uint u_matrix_offset; uint u_right; uint u_render_mask; float u_proj_offset; mat4 u_matrices[1]", MATRIX_UBO_INDEX, "MatrixUniforms", 0);
        mMatrixUniforms->useGPUBuffer(false);
        glGetIntegerv(GL_MAX_UNIFORM_BLOCK_SIZE, &mMaxUniformBlockSize);
        glGetIntegerv(GL_MAX_VERTEX_UNIFORM_COMPONENTS, &mMaxArrayFloats);
    }

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

    RenderTarget* GLRenderer::createRenderTarget(Scene* scene, bool isStereo)
    {
        RenderTarget* renderTarget = new RenderTarget(scene, isStereo);
        RenderSorter* sorter = new MainSceneSorter(*this);
        renderTarget->setRenderSorter(sorter);
        return renderTarget;
    }

    RenderTarget* GLRenderer::createRenderTarget(RenderTexture* renderTexture, bool isMultiview, bool isStereo)
    {
        RenderTarget* renderTarget = new RenderTarget(renderTexture, isMultiview, isStereo);
        RenderSorter* sorter = new MainSceneSorter(*this);
        renderTarget->setRenderSorter(sorter);
        return renderTarget;
    }

    RenderTarget* GLRenderer::createRenderTarget(RenderTexture* renderTexture, const RenderTarget* renderTarget)
    {
        RenderTarget* glTarget = new RenderTarget(renderTexture, renderTarget);
        glTarget->setRenderSorter(renderTarget->getRenderSorter());
        return glTarget;
    }

    RenderTexture* GLRenderer::createRenderTexture(const RenderTextureInfo& renderTextureInfo)
    {
        if (renderTextureInfo.useMultiview)
        {
            return new GLMultiviewRenderTexture(renderTextureInfo.fboWidth,
                                                renderTextureInfo.fboHeight,
                                                renderTextureInfo.multisamples, 2,
                                                renderTextureInfo.fboId,
                                                renderTextureInfo.texId,
                                                renderTextureInfo.viewport);
        }
        return new GLNonMultiviewRenderTexture(renderTextureInfo.fboWidth,
                                               renderTextureInfo.fboHeight,
                                               renderTextureInfo.multisamples,
                                               renderTextureInfo.fboId,
                                               renderTextureInfo.texId,
                                               renderTextureInfo.viewport);
    }

    ShadowMap* GLRenderer::createShadowMap(ShaderData* material)
    {
        ShadowMap* shadowMap = new ShadowMap();
        if (mShadowSorter == nullptr)
        {
            mShadowSorter = new ShadowRenderSorter(*material, *this);
        }
        shadowMap->setRenderSorter(mShadowSorter);
        return shadowMap;
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
        GLUniformBlock* block;

        if (maxelems <= 1)
        {
            block = new GLUniformBlock(desc, binding, name);
        }
        else
        {
            block = new GLUniformBlock(desc, binding, name, maxelems);
        }
        if (block->getTotalSize() > mMaxUniformBlockSize)
        {
            LOGE("ERROR: uniform block of %d bytes exceeds maximum allowed size of %d bytes",
                 block->getTotalSize(), mMaxUniformBlockSize);
        }
        return block;
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
                                                   const TextureParameters *texparams,
                                                   int number_views)
    {
        // Default viewport
        int viewport[4] = {0, 0, width, height};
        if(number_views == 1)
            return new GLNonMultiviewRenderTexture(width, height, sample_count, jcolor_format, jdepth_format,
                                                 resolve_depth, texparams, viewport);

         return new GLMultiviewRenderTexture(width, height, sample_count, jcolor_format, jdepth_format, resolve_depth, texparams, number_views, viewport);
    }

    RenderTexture* GLRenderer::createRenderTexture(int width, int height, int sample_count, int layers, int jdepth_format)
    {
        // Default viewport
        int viewport[4] = {0, 0, width, height};
        RenderTexture* tex = new GLNonMultiviewRenderTexture(width, height, sample_count, layers, jdepth_format, viewport);
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
                                     const char* fragmentShader,
                                     const char* matrixCalc)
    {
        return new GLShader(id, signature, uniformDescriptor, textureDescriptor, vertexDescriptor,
                            vertexShader, fragmentShader, matrixCalc);
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

    Light* GLRenderer::createLight(const char* uniformDescriptor, const char* textureDescriptor)
    {
        return new GLLight(uniformDescriptor, textureDescriptor);
    }


    UniformBlock* GLRenderer::createTransformBlock(int numMatrices)
    {
        int maxMatrices = getMaxArraySize(sizeof(glm::mat4));
        if (numMatrices > maxMatrices)
        {
            LOGE("TRANSFORM: createTransformBlock %d matrices exceeds allowed size of %d", numMatrices, maxMatrices);
            numMatrices = maxMatrices;
        }
        UniformBlock* transBlock = GLRenderer::createUniformBlock("mat4 u_matrices", TRANSFORM_UBO_INDEX, "Transform_ubo", numMatrices);
        transBlock->useGPUBuffer(true);
        return transBlock;
    }

    void GLRenderer::validate(RenderSorter::Renderable& r)
    {
        r.material->updateGPU(this);
        r.renderData->updateGPU(this, r.shader);
    }

    void GLRenderer::renderRenderTarget(Scene* scene, jobject javaSceneObject, RenderTarget* renderTarget,
                            ShaderManager* shader_manager,
                            RenderTexture* post_effect_render_texture_a,
                            RenderTexture* post_effect_render_texture_b)
    {
        RenderState& rstate = renderTarget->getRenderState();
        Camera* camera = rstate.camera;
        RenderData* post_effects = camera->post_effect_data();

        resetStats();
        renderTarget->beginRendering();
        mCurrentState.reset();
        //@todo makes it clear this is a hack
        rstate.javaSceneObject = javaSceneObject;
        rstate.scene = scene;
        rstate.shader_manager = shader_manager;
        if (rstate.is_multiview)
        {
            rstate.u_render_mask = RenderData::RenderMaskBit::Right | RenderData::RenderMaskBit::Left;
            rstate.u_right = 1;
        }
        else
        {
            rstate.u_render_mask = camera->render_mask();
            rstate.u_right = 0;
            if (((rstate.u_render_mask & RenderData::RenderMaskBit::Right) != 0) && rstate.is_stereo)
            {
                rstate.u_right = 1;
            }
        }
        /*
         * Set GL state to match default RenderModes state
         * which restoreRenderStates restores to.
         */
        glDepthMask(GL_TRUE);
        GL(glEnable(GL_DEPTH_TEST));
        GL(glDepthFunc(GL_LEQUAL));
        GL(glEnable(GL_CULL_FACE));
        GL(glFrontFace(GL_CCW));
        GL(glCullFace(GL_BACK));
        GL(glDisable(GL_POLYGON_OFFSET_FILL));
        GL(glDisable(GL_BLEND));
        GL(glBlendEquation(GL_FUNC_ADD));
        GL(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA));
        GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));
#ifdef DEBUG_RENDER
        LOGV("RENDER: render_mask = %d u_right = %d", rstate.u_render_mask, rstate.u_right);
#endif
        if ((post_effects == NULL) ||
            (post_effect_render_texture_a == nullptr) ||
            (post_effects->pass_count() == 0))
        {
            GL(clearBuffers(*camera));
            renderTarget->render();
        }
        else
        {
            static GLint viewport[4];
            GLint drawFboId = 0;
            int npost = post_effects->pass_count() - 1;
            RenderTexture* renderTexture = post_effect_render_texture_a;
            RenderTexture* input_texture = renderTexture;

            GL(glGetIntegerv(GL_VIEWPORT,viewport));
            GL(glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &drawFboId));
            GL(glBindFramebuffer(GL_FRAMEBUFFER, renderTexture->getFrameBufferId()));
            GL(glViewport(0, 0, renderTexture->width(), renderTexture->height()));
            GL(clearBuffers(*camera));
            renderTarget->render();
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
        renderTarget->endRendering();
        rstate.javaSceneObject = nullptr;
        checkGLError("GLRenderer::renderRenderTarget after");
    }

    void GLRenderer::setRenderStates(const RenderModes& rmodes)
    {
        switch (rmodes.getCullFace())
        {
            case RenderModes::CullFront:
            glEnable(GL_CULL_FACE);
            glCullFace(GL_FRONT);
            break;

            case RenderModes::CullNone:
            glDisable(GL_CULL_FACE);
            break;

            // CullBack as Default
            default:
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            break;
        }
        if (rmodes.isOffsetEnabled())
        {
            GL(glEnable(GL_POLYGON_OFFSET_FILL));
            GL(glPolygonOffset(rmodes.getOffsetFactor(), rmodes.getOffsetUnits()));
        }
        if (!rmodes.isDepthTestEnabled())
        {
            GL(glDisable(GL_DEPTH_TEST));
        }
        if (!rmodes.isDepthMaskEnabled())
        {
            GL(glDepthMask(GL_FALSE));
        }
        if (rmodes.isStencilTestEnabled())
        {
            int func = rmodes.getStencilFunc();
            int fmask = rmodes.getStencilFuncMask();
            int ref = rmodes.getStencilRef();
            int sfail = rmodes.getStencilFail();
            int dpfail = rmodes.getDepthFail();
            int dppass = rmodes.getStencilPass();
            GL(glEnable(GL_STENCIL_TEST));
            GL(glStencilFunc(func, ref, fmask));
            if (0 != sfail && 0 != dpfail && 0 != dppass)
            {
                GL(glStencilOp(sfail, dpfail, dppass));
            }
            GL(glStencilMask(rmodes.getStencilMask()));
            if (RenderModes::RenderOrder::Stencil == rmodes.getRenderOrder())
            {
                GL(glDepthMask(GL_FALSE));
                GL(glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE));
            }
        }
        if (rmodes.isAlphaBlendEnabled())
        {
            GL(glEnable(GL_BLEND));
            glBlendFunc(rmodes.getSourceBlendFunc(), rmodes.getDestBlendFunc());
        }
        if (rmodes.isAlphaToCoverageEnabled())
        {
            GL(glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE));
            GL(glSampleCoverage(rmodes.getSampleCoverage(),
                                rmodes.invertCoverageMask()));
        }
    }

    void GLRenderer::restoreRenderStates(const RenderModes& rmodes)
    {
        if (rmodes.getCullFace() != RenderModes::CullBack)
        {
            GL(glEnable(GL_CULL_FACE));
            GL(glCullFace(GL_BACK));
        }
        if (rmodes.isOffsetEnabled())
        {
            GL(glDisable(GL_POLYGON_OFFSET_FILL));
        }
        if (!rmodes.isDepthTestEnabled())
        {
            GL(glEnable(GL_DEPTH_TEST));
        }
        if (!rmodes.isDepthMaskEnabled())
        {
            GL(glDepthMask(GL_TRUE));
        }
        if (rmodes.isStencilTestEnabled())
        {
            GL(glDisable(GL_STENCIL_TEST));
            if (RenderData::Queue::Stencil == rmodes.getRenderOrder())
            {
                GL(glDepthMask(GL_TRUE));
                GL(glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE));
            }
        }
        if (rmodes.isAlphaBlendEnabled())
        {
            GL(glDisable(GL_BLEND));
        }
        if (rmodes.isAlphaToCoverageEnabled())
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
        GLint drawFB, readFB;

        glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &drawFB);
        glGetIntegerv(GL_READ_FRAMEBUFFER_BINDING, &readFB);
        scene->getLights().makeShadowMaps(scene, javaSceneObject, shader_manager);
        glBindFramebuffer(GL_READ_FRAMEBUFFER, readFB);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, drawFB);
    }

    bool GLRenderer::updateMatrix(const RenderState& rstate, const RenderSorter::Renderable& r)
    {
        float offset = rstate.camera->getProjectionMatrix()[0][0] * CameraRig::default_camera_separation_distance();
        mMatrixUniforms->setFloat("u_proj_offset", offset);

        if (r.transformBlock)
        {
            mMatrixUniforms->setInt("u_matrix_offset", r.matrixOffset);
            mMatrixUniforms->updateGPU(this, 0, 3 * sizeof(int) + sizeof(float));
        }
        else
        {
            mMatrixUniforms->setFloatVec("u_matrices", glm::value_ptr(r.mvp),
                                         sizeof(glm::mat4) / sizeof(float));
            mMatrixUniforms->updateGPU(this, 0, 3 * sizeof(int) + sizeof(float) + sizeof(glm::mat4));
        }
        mMatrixUniforms->bindBuffer(r.shader, this);
        return true;
    }

    bool GLRenderer::selectShader(const RenderState& rstate, Shader* shader)
    {
        try
        {
            shader->useShader(rstate.is_multiview);
        }
        catch (const std::string &error)
        {
            LOGE("ERROR: Renderer::selectShader %s", error.c_str());
            shader = rstate.shader_manager->findShader("GVRErrorShader");
            shader->useShader(rstate.is_multiview);
            return false;
        }
        if (shader->usesMatrixUniforms())
        {
            ((GLShader*) shader)->findUniforms(*mMatrixUniforms, MATRIX_UBO_INDEX);
            mMatrixUniforms->setInt("u_right", rstate.u_right);
            mMatrixUniforms->setInt("u_render_mask", rstate.u_render_mask);
        }
        if (shader->useLights())
        {
            rstate.scene->getLights().useLights(this, shader);
            if (rstate.shadow_map)
            {
                GLShader* glshader = static_cast<GLShader*>(shader);
                int loc = glGetUniformLocation(glshader->getProgramId(), "u_shadow_maps");

                if (loc >= 0)
                {
                    GLRenderTexture* rtex = static_cast<GLRenderTexture*>(rstate.shadow_map->getTexture());
                    if (rtex)
                    {
                        int texIndex = glshader->getNumTextures();
                        rtex->bindTexture(loc, texIndex);
#ifdef DEBUG_LIGHT
                        LOGV("LIGHT: binding shadow map loc=%d texIndex = %d", loc, texIndex);
#endif
                    }
                }
            }
        }
        return true;
    }

    bool GLRenderer::selectMaterial(const RenderSorter::Renderable& r)
    {
        GLMaterial* glmtl = static_cast<GLMaterial*>(r.material);
        return glmtl->bindToShader(r.shader, this) >= 0;
    }

    void GLRenderer::updateState(const RenderState& rstate, const RenderSorter::Renderable& r)
    {
        Shader* shader = r.shader;
        const RenderModes& rmodes = r.renderModes;

        if (mCurrentState.shader != r.shader)
        {
#ifdef DEBUG_RENDER
            LOGV("RENDER: selectShader %d", r.shader->getShaderID());
#endif
            mCurrentState.material = nullptr;
            mCurrentState.mesh = nullptr;
            mCurrentState.transformBlock = nullptr;
            mCurrentState.shader = shader;
            selectShader(rstate, shader);
        }
        if (r.shader->usesMatrixUniforms())
        {
            if (r.transformBlock != mCurrentState.transformBlock)
            {
                r.transformBlock->bindBuffer(r.shader, this);
                mCurrentState.transformBlock = r.transformBlock;
            }
            updateMatrix(rstate, r);
        }
        if (mCurrentState.material != r.material)
        {
#ifdef DEBUG_RENDER
            LOGV("RENDER: selectMaterial %p", r.material);
#endif
            selectMaterial(r);
            mCurrentState.material = r.material;
        }
        if (rmodes != mCurrentState.renderModes)
        {
            restoreRenderStates(mCurrentState.renderModes);
            mCurrentState.renderModes = rmodes;
            setRenderStates(rmodes);
        }
    }

    void GLRenderer::render(const RenderState&rstate, const RenderSorter::Renderable& r)
    {
        updateState(rstate, r);
        selectMesh(rstate, r);
    }

    void GLRenderer::selectMesh(const RenderState& rstate, const RenderSorter::Renderable& r)
    {
        int indexCount = r.mesh->getIndexCount();
        int vertexCount = r.mesh->getVertexCount();
        int drawMode = r.renderModes.getDrawMode();
        GLRenderData* rdata = static_cast<GLRenderData*>(r.renderData);

        if (mCurrentState.mesh != r.mesh)
        {
            if ((drawMode == GL_LINE_STRIP) ||
                (drawMode == GL_LINES) ||
                (drawMode == GL_LINE_LOOP))
            {
                float lineWidth;
                if (r.material->getFloat("line_width", lineWidth))
                {
                    glLineWidth(lineWidth);
                }
                else
                {
                    glLineWidth(1.0f);
                }
            }
            mCurrentState.mesh = r.mesh;
            rdata->bindToShader(r.shader, this);
        }
        incrementTriangles(indexCount);
        incrementDrawCalls();
        switch (r.mesh->getIndexSize())
        {
            case 2:
            glDrawElements(drawMode, indexCount, GL_UNSIGNED_SHORT, 0);
            break;

            case 4:
            glDrawElements(drawMode, indexCount, GL_UNSIGNED_INT, 0);
            break;

            default:
            glDrawArrays(drawMode, 0, vertexCount);
            break;
        }
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

