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

#include "renderer.h"
#include "glm/gtc/matrix_inverse.hpp"

#include "objects/post_effect_data.h"
#include "objects/scene.h"
#include "objects/textures/render_texture.h"
#include "shaders/shader_manager.h"
#include "shaders/post_effect_shader_manager.h"
#include "gl_renderer.h"

namespace gvr {

void GLRenderer::clearBuffers(const Camera& camera) const {
    glClearColor(camera.background_color_r(), camera.background_color_g(), camera.background_color_b(), camera.background_color_a());

    GLbitfield mask = GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT;
    if (useStencilBuffer_) {
        mask |= GL_STENCIL_BUFFER_BIT;
        glStencilMask(~0);
    }

    glClear(mask);
}

void GLRenderer::renderCamera(Scene* scene, Camera* camera, int framebufferId,
        int viewportX, int viewportY, int viewportWidth, int viewportHeight,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    resetStats();
    RenderState rstate;
    rstate.shadow_map = false;
    rstate.material_override = NULL;
    rstate.viewportX = viewportX;
    rstate.viewportY = viewportY;
    rstate.viewportWidth = viewportWidth;
    rstate.viewportHeight = viewportHeight;
    rstate.shader_manager = shader_manager;
    rstate.uniforms.u_view = camera->getViewMatrix();
    rstate.uniforms.u_proj = camera->getProjectionMatrix();
    rstate.shader_manager = shader_manager;
    rstate.scene = scene;
    rstate.render_mask = camera->render_mask();
    rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;

    std::vector<PostEffectData*> post_effects = camera->post_effect_data();

    GL(glEnable (GL_DEPTH_TEST));
    GL(glDepthFunc (GL_LEQUAL));
    GL(glEnable (GL_CULL_FACE));
    GL(glFrontFace (GL_CCW));
    GL(glCullFace (GL_BACK));
    GL(glEnable (GL_BLEND));
    GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));
    GL(glBlendEquation (GL_FUNC_ADD));
    GL(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA));
    GL(glDisable (GL_POLYGON_OFFSET_FILL));
    GL(glLineWidth(1.0f));
    if (post_effects.size() == 0) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        clearBuffers(*camera);
        renderRenderDataVector(rstate);
    } else {
        RenderTexture* texture_render_texture = post_effect_render_texture_a;

        GL(glBindFramebuffer(GL_FRAMEBUFFER,
                texture_render_texture->getFrameBufferId()));
        GL(glViewport(0, 0, texture_render_texture->width(),
                texture_render_texture->height()));

        clearBuffers(*camera);
        for (auto it = render_data_vector.begin();
                it != render_data_vector.end(); ++it) {
            GL(renderRenderData(rstate, *it));
        }

        GL(glDisable(GL_DEPTH_TEST));
        GL(glDisable(GL_CULL_FACE));

        for (int i = 0; i < post_effects.size() - 1; ++i) {
            if (i % 2 == 0) {
                texture_render_texture = post_effect_render_texture_a;
            } else {
                texture_render_texture = post_effect_render_texture_b;
            }
            GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
            GL(glViewport(viewportX, viewportY, viewportWidth, viewportHeight));

            GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
            GL(renderPostEffectData(camera, texture_render_texture,
                    post_effects[i], post_effect_shader_manager));
        }

        GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
        GL(glViewport(viewportX, viewportY, viewportWidth, viewportHeight));
        GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
        renderPostEffectData(camera, texture_render_texture, post_effects.back(), post_effect_shader_manager);
    }

    GL(glDisable(GL_DEPTH_TEST));
    GL(glDisable(GL_CULL_FACE));
    GL(glDisable(GL_BLEND));
}

/**
 * Set the render states for render data
 */
void GLRenderer::setRenderStates(RenderData* render_data, RenderState& rstate) {

    if (!(rstate.render_mask & render_data->render_mask()))
        return;

    if (render_data->offset()) {
        GL(glEnable (GL_POLYGON_OFFSET_FILL));
        GL(glPolygonOffset(render_data->offset_factor(),
                           render_data->offset_units()));
    }
    if (!render_data->depth_test()) {
        GL(glDisable (GL_DEPTH_TEST));
    }

    if (render_data->stencil_test()) {
        GL(glEnable(GL_STENCIL_TEST));

        GL(glStencilFunc(render_data->stencil_func_func(), render_data->stencil_func_ref(), render_data->stencil_func_mask()));

        int sfail = render_data->stencil_op_sfail();
        int dpfail = render_data->stencil_op_dpfail();
        int dppass = render_data->stencil_op_dppass();
        if (0 != sfail && 0 != dpfail && 0 != dppass) {
            GL(glStencilOp(sfail, dpfail, dppass));
        }

        GL(glStencilMask(render_data->stencil_mask_mask()));
        if (RenderData::Queue::Stencil == render_data->rendering_order()) {
            GL(glDepthMask(GL_FALSE));
            GL(glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE));
        }
    }

    if (!render_data->alpha_blend()) {
        GL(glDisable (GL_BLEND));
    }
    if (render_data->alpha_to_coverage()) {
        GL(glEnable(GL_SAMPLE_ALPHA_TO_COVERAGE));
        GL(glSampleCoverage(render_data->sample_coverage(),
                            render_data->invert_coverage_mask()));
    }
    glBlendFunc(render_data->source_alpha_blend_func(), render_data->dest_alpha_blend_func());
}
/**
 * Restore the render states for render data
 */
void GLRenderer::restoreRenderStates(RenderData* render_data) {
    if (render_data->cull_face() != RenderData::CullBack) {
        GL(glEnable (GL_CULL_FACE));
        GL(glCullFace (GL_BACK));
    }

    if (render_data->offset()) {
        GL(glDisable (GL_POLYGON_OFFSET_FILL));
    }
    if (!render_data->depth_test()) {
        GL(glEnable (GL_DEPTH_TEST));
    }

    if (render_data->stencil_test()) {
        GL(glDisable(GL_STENCIL_TEST));
        if (RenderData::Queue::Stencil == render_data->rendering_order()) {
            GL(glDepthMask(GL_TRUE));
            GL(glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE));
        }
    }

    if (!render_data->alpha_blend()) {
        GL(glEnable (GL_BLEND));
    }
    if (render_data->alpha_to_coverage()) {
        GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));
    }
}

/**
 * Generate shadow maps for all the lights that cast shadows.
 * The scene is rendered from the viewpoint of the light using a
 * special depth shader (GVRDepthShader) to create the shadow map.
 * @see Renderer::renderShadowMap Light::makeShadowMap
 */
void GLRenderer::makeShadowMaps(Scene* scene, ShaderManager* shader_manager, int width, int height)
{
    const std::vector<Light*> lights = scene->getLightList();
    GL(glEnable (GL_DEPTH_TEST));
    GL(glDepthFunc (GL_LEQUAL));
    GL(glEnable (GL_CULL_FACE));
    GL(glFrontFace (GL_CCW));
    GL(glCullFace (GL_BACK));
    GL(glDisable(GL_SAMPLE_ALPHA_TO_COVERAGE));

    int texIndex = 0;
    std::vector<SceneObject*> scene_objects;
    scene_objects.reserve(1024);
    for (auto it = lights.begin(); it != lights.end(); ++it) {
     	if ((*it)->castShadow() &&
     	    (*it)->makeShadowMap(scene, shader_manager, texIndex, scene_objects, width, height))
            ++texIndex;
    }
    GL(glDisable(GL_DEPTH_TEST));
    GL(glDisable(GL_CULL_FACE));

}

/**
 * Generates a shadow map into the specified framebuffer.
 * @param rstate        RenderState with rendering parameters
 * @param camera        camera with light viewpoint
 * @param framebufferId ID of framebuffer to render shadow map into
 * @param scene_objects temporary storage for culling
 * @see Light::makeShadowMap Renderer::makeShadowMaps
 */
void GLRenderer::renderShadowMap(RenderState& rstate, Camera* camera, GLuint framebufferId, std::vector<SceneObject*>& scene_objects) {

	cullFromCamera(rstate.scene, camera, rstate.shader_manager, scene_objects);

    GLint drawFbo = 0, readFbo = 0;
    glGetIntegerv(GL_DRAW_FRAMEBUFFER_BINDING, &drawFbo);
    glGetIntegerv(GL_READ_FRAMEBUFFER_BINDING, &readFbo);
    const GLenum attachments[] = {GL_COLOR_ATTACHMENT0, GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT};

	GL(glBindFramebuffer(GL_FRAMEBUFFER, framebufferId));
    GL(glInvalidateFramebuffer(GL_FRAMEBUFFER, 3, attachments));
    GL(glViewport(rstate.viewportX, rstate.viewportY, rstate.viewportWidth, rstate.viewportHeight));
    glClearColor(0,0,0,1);
    GL(glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT));
    rstate.shadow_map = true;
    for (auto it = render_data_vector.begin();
         it != render_data_vector.end(); ++it) {
        RenderData* rdata = *it;
        if (rdata->cast_shadows()) {
            GL(renderRenderData(rstate, rdata));
        }
    }
    rstate.shadow_map = false;
    GL(glInvalidateFramebuffer(GL_FRAMEBUFFER, 2, &attachments[1]));
    glBindFramebuffer(GL_READ_FRAMEBUFFER, readFbo);
    glBindFramebuffer(GL_DRAW_FRAMEBUFFER, drawFbo);
}

void GLRenderer::renderCamera(Scene* scene, Camera* camera,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {
    GLint curFBO;
    GLint viewport[4];
    glGetIntegerv(GL_FRAMEBUFFER_BINDING, &curFBO);
    glGetIntegerv(GL_VIEWPORT, viewport);

    renderCamera(scene, camera, curFBO, viewport[0], viewport[1], viewport[2],
            viewport[3], shader_manager, post_effect_shader_manager,
            post_effect_render_texture_a, post_effect_render_texture_b);
}

void GLRenderer::renderCamera(Scene* scene, Camera* camera,
        RenderTexture* render_texture, ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    renderCamera(scene, camera, render_texture->getFrameBufferId(), 0, 0,
            render_texture->width(), render_texture->height(), shader_manager,
            post_effect_shader_manager, post_effect_render_texture_a,
            post_effect_render_texture_b);

}

void GLRenderer::renderCamera(Scene* scene, Camera* camera, int viewportX,
        int viewportY, int viewportWidth, int viewportHeight,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    renderCamera(scene, camera, 0, viewportX, viewportY, viewportWidth,
            viewportHeight, shader_manager, post_effect_shader_manager,
            post_effect_render_texture_a, post_effect_render_texture_b);
}
void GLRenderer::set_face_culling(int cull_face) {
    switch (cull_face) {
    case RenderData::CullFront:
        glEnable (GL_CULL_FACE);
        glCullFace (GL_FRONT);
        break;

    case RenderData::CullNone:
        glDisable(GL_CULL_FACE);
        break;

        // CullBack as Default
    default:
        glEnable(GL_CULL_FACE);
        glCullFace (GL_BACK);
        break;
    }
}
bool GLRenderer::checkTextureReady(Material* material) {
    int shaderType = material->shader_type();

    //Skip custom shader here since they are rendering multiple textures
    //Check the textures later inside the rendering pass inside the custom shader
    if (shaderType < 0 || shaderType >= Material::ShaderType::BUILTIN_SHADER_SIZE)
    {
        return true;
    }
    else
    {
        return material->isMainTextureReady();
    }
 }

void GLRenderer::occlusion_cull(Scene* scene,
        std::vector<SceneObject*>& scene_objects, ShaderManager *shader_manager,
        glm::mat4 vp_matrix) {

    if(!occlusion_cull_init(scene, scene_objects))
        return;

    for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
        SceneObject *scene_object = (*it);
        RenderData* render_data = scene_object->render_data();
        if (render_data == 0 || render_data->material(0) == 0) {
            continue;
        }

        //If a query was issued on an earlier or same frame and if results are
        //available, then update the same. If results are unavailable, do nothing
        if (!scene_object->is_query_issued()) {
            continue;
        }

        //If a previous query is active, do not issue a new query.
        //This avoids overloading the GPU with too many queries
        //Queries may span multiple frames

        bool is_query_issued = scene_object->is_query_issued();
        if (!is_query_issued) {
            //Setup basic bounding box and material
            RenderData* bounding_box_render_data(new RenderData());
            Mesh* bounding_box_mesh = render_data->mesh()->createBoundingBox();
            Material *bbox_material = new Material(
                    Material::BOUNDING_BOX_SHADER);
            RenderPass *pass = new RenderPass();
            pass->set_material(bbox_material);
            bounding_box_render_data->set_mesh(bounding_box_mesh);
            bounding_box_render_data->add_pass(pass);

            GLuint *query = scene_object->get_occlusion_array();

            glDepthFunc (GL_LEQUAL);
            glEnable (GL_DEPTH_TEST);
            glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);

            glm::mat4 model_matrix_tmp(
                    scene_object->transform()->getModelMatrix());
            glm::mat4 mvp_matrix_tmp(vp_matrix * model_matrix_tmp);

            //Issue the query only with a bounding box
            glBeginQuery(GL_ANY_SAMPLES_PASSED, query[0]);
            shader_manager->getBoundingBoxShader()->render(mvp_matrix_tmp,
                    bounding_box_render_data,
                    bounding_box_render_data->material(0));
            glEndQuery (GL_ANY_SAMPLES_PASSED);
            scene_object->set_query_issued(true);

            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);

            //Delete the generated bounding box mesh
            bounding_box_mesh->cleanUp();
            delete bbox_material;
            delete pass;
            delete bounding_box_render_data;
        }

        GLuint query_result = GL_FALSE;
        GLuint *query = (*it)->get_occlusion_array();
        glGetQueryObjectuiv(query[0], GL_QUERY_RESULT_AVAILABLE, &query_result);

        if (query_result) {
            GLuint pixel_count;
            glGetQueryObjectuiv(query[0], GL_QUERY_RESULT, &pixel_count);
            bool visibility = ((pixel_count & GL_TRUE) == GL_TRUE);

            (*it)->set_visible(visibility);
            (*it)->set_query_issued(false);
            addRenderData((*it)->render_data());
            scene->pick(scene_object);
        }
    }
    scene->unlockColliders();
}

void GLRenderer::renderMesh(RenderState& rstate, RenderData* render_data) {

    for (int curr_pass = 0; curr_pass < render_data->pass_count();
            ++curr_pass) {
        numberTriangles += render_data->mesh()->getNumTriangles();
        numberDrawCalls++;

        set_face_culling(render_data->pass(curr_pass)->cull_face());
        Material* curr_material = rstate.material_override;

        if (curr_material == nullptr)
            curr_material = render_data->pass(curr_pass)->material();
        if (curr_material != nullptr) {
            GL(renderMaterialShader(rstate, render_data, curr_material));
        }
    }
}

void GLRenderer::renderMaterialShader(RenderState& rstate, RenderData* render_data, Material *curr_material) {

    if (Material::ShaderType::BEING_GENERATED == curr_material->shader_type()) {
        return;
    }

    //Skip the material whose texture is not ready with some exceptions
    if (!checkTextureReady(curr_material))
        return;
    ShaderManager* shader_manager = rstate.shader_manager;
    Transform* const t = render_data->owner_object()->transform();

    if (t == nullptr)
        return;

    rstate.uniforms.u_model = t->getModelMatrix();
	rstate.uniforms.u_mv = rstate.uniforms.u_view * rstate.uniforms.u_model;
	rstate.uniforms.u_mv_it = glm::inverseTranspose(rstate.uniforms.u_mv);
	rstate.uniforms.u_mvp = rstate.uniforms.u_proj * rstate.uniforms.u_mv;
    rstate.uniforms.u_right = rstate.render_mask & RenderData::RenderMaskBit::Right;


    if(use_multiview && !rstate.shadow_map){
        rstate.uniforms.u_view_[0] = rstate.scene->main_camera_rig()->left_camera()->getViewMatrix();
        rstate.uniforms.u_view_[1] = rstate.scene->main_camera_rig()->right_camera()->getViewMatrix();
        rstate.uniforms.u_mv_[0] = rstate.uniforms.u_view_[0] * rstate.uniforms.u_model;
        rstate.uniforms.u_mv_[1] = rstate.uniforms.u_view_[1] * rstate.uniforms.u_model;
        rstate.uniforms.u_mv_it_[0] = glm::inverseTranspose(rstate.uniforms.u_mv_[0]);
        rstate.uniforms.u_mv_it_[1] = glm::inverseTranspose(rstate.uniforms.u_mv_[1]);
        rstate.uniforms.u_mvp_[0] = rstate.uniforms.u_proj * rstate.uniforms.u_mv_[0];
        rstate.uniforms.u_mvp_[1] = rstate.uniforms.u_proj * rstate.uniforms.u_mv_[1];
    }
    Mesh* mesh = render_data->mesh();

    GLuint programId = -1;
    ShaderBase* shader = NULL;

    try {
         //TODO: Improve this logic to avoid a big "switch case"
        if (rstate.material_override != nullptr)
            curr_material = rstate.material_override;
         switch (curr_material->shader_type()) {
            case Material::ShaderType::UNLIT_HORIZONTAL_STEREO_SHADER:
            	shader = shader_manager->getUnlitHorizontalStereoShader();
                break;
            case Material::ShaderType::UNLIT_VERTICAL_STEREO_SHADER:
                shader = shader_manager->getUnlitVerticalStereoShader();
                break;
            case Material::ShaderType::OES_SHADER:
                shader = shader_manager->getOESShader();
                break;
            case Material::ShaderType::OES_HORIZONTAL_STEREO_SHADER:
                shader = shader_manager->getOESHorizontalStereoShader();
                break;
            case Material::ShaderType::OES_VERTICAL_STEREO_SHADER:
                shader = shader_manager->getOESVerticalStereoShader();
                break;
            case Material::ShaderType::CUBEMAP_SHADER:
                shader = shader_manager->getCubemapShader();
                break;
            case Material::ShaderType::CUBEMAP_REFLECTION_SHADER:
                if(use_multiview){
                    rstate.uniforms.u_view_inv_[0] = glm::inverse(rstate.uniforms.u_view_[0]);
                    rstate.uniforms.u_view_inv_[1] = glm::inverse(rstate.uniforms.u_view_[1]);
                }
                else
                    rstate.uniforms.u_view_inv = glm::inverse(rstate.uniforms.u_view);
                shader = shader_manager->getCubemapReflectionShader();
                break;
            case Material::ShaderType::TEXTURE_SHADER:
                shader = shader_manager->getTextureShader();
                break;
            case Material::ShaderType::EXTERNAL_RENDERER_SHADER:
                shader = shader_manager->getExternalRendererShader();
                break;
            case Material::ShaderType::ASSIMP_SHADER:
                shader = shader_manager->getAssimpShader();
                break;
            case Material::ShaderType::LIGHTMAP_SHADER:
                shader = shader_manager->getLightMapShader();
                break;
			case Material::ShaderType::UNLIT_FBO_SHADER:
				shader = shader_manager->getUnlitFboShader();
                break;
            default:
                shader = shader_manager->getCustomShader(curr_material->shader_type());
                break;
        }
         if (shader == NULL) {
             LOGE("Rendering error: GVRRenderData shader cannot be determined\n");
             shader_manager->getErrorShader()->render(&rstate, render_data, curr_material);
             return;
         }
         if ((render_data->draw_mode() == GL_LINE_STRIP) ||
             (render_data->draw_mode() == GL_LINES) ||
             (render_data->draw_mode() == GL_LINE_LOOP)) {
             if (curr_material->hasUniform("line_width")) {
                 float lineWidth = curr_material->getFloat("line_width");
                 glLineWidth(lineWidth);
             }
             else {
                 glLineWidth(1.0f);
             }
         }
         shader->render(&rstate, render_data, curr_material);
    } catch (const std::string &error) {
        LOGE(
                "Error detected in Renderer::renderRenderData; name : %s, error : %s",
                render_data->owner_object()->name().c_str(),
                error.c_str());
        shader_manager->getErrorShader()->render(&rstate, render_data, curr_material);
    }

    programId = shader->getProgramId();
    //there is no program associated with EXTERNAL_RENDERER_SHADER
    if (-1 != programId) {
        glBindVertexArray(mesh->getVAOId(programId));
        if (mesh->indices().size() > 0) {
            glDrawElements(render_data->draw_mode(), mesh->indices().size(), GL_UNSIGNED_SHORT, 0);

        } else {
            glDrawArrays(render_data->draw_mode(), 0, mesh->vertices().size());
        }
        glBindVertexArray(0);
    }
    checkGlError("renderMesh::renderMaterialShader");
}
}


