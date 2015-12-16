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

#include "eglextension/tiledrendering/tiled_rendering_enhancer.h"
#include "objects/material.h"
#include "objects/post_effect_data.h"
#include "objects/scene.h"
#include "objects/scene_object.h"
#include "objects/components/camera.h"
#include "objects/components/eye_pointee_holder.h"
#include "objects/components/render_data.h"
#include "objects/textures/render_texture.h"
#include "shaders/shader_manager.h"
#include "shaders/post_effect_shader_manager.h"
#include "util/gvr_gl.h"
#include "util/gvr_log.h"

namespace gvr {

static int numberDrawCalls;
static int numberTriangles;

void Renderer::initializeStats() {
    // TODO: this function will be filled in once we add draw time stats
}

void Renderer::resetStats() {
    numberDrawCalls = 0;
    numberTriangles = 0;
}

int Renderer::getNumberDrawCalls() {
    return numberDrawCalls;
}

int Renderer::getNumberTriangles() {
    return numberTriangles;
}

static std::vector<RenderData*> render_data_vector;

void Renderer::cull(Scene *scene, Camera *camera, ShaderManager* shader_manager) {
    glm::mat4 view_matrix = camera->getViewMatrix();
    glm::mat4 projection_matrix = camera->getProjectionMatrix();
    glm::mat4 vp_matrix = glm::mat4(projection_matrix * view_matrix);

    render_data_vector.clear();
    std::vector<SceneObject*> scene_objects = scene->getWholeSceneObjects();

    // do occlusion culling, if enabled
    occlusion_cull(scene, scene_objects);

    // do frustum culling, if enabled
    frustum_cull(scene, camera, scene_objects, render_data_vector,
            vp_matrix, shader_manager);

    // do sorting based on render order
    std::sort(render_data_vector.begin(), render_data_vector.end(),
            compareRenderData);

}

void Renderer::renderCamera(Scene* scene, Camera* camera, int framebufferId,
        int viewportX, int viewportY, int viewportWidth, int viewportHeight,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    numberDrawCalls = 0;
    numberTriangles = 0;

    glm::mat4 view_matrix = camera->getViewMatrix();
    glm::mat4 projection_matrix = camera->getProjectionMatrix();
    glm::mat4 vp_matrix = glm::mat4(projection_matrix * view_matrix);

    std::vector<PostEffectData*> post_effects = camera->post_effect_data();

    glEnable (GL_DEPTH_TEST);
    glDepthFunc (GL_LEQUAL);
    glEnable (GL_CULL_FACE);
    glFrontFace (GL_CCW);
    glCullFace (GL_BACK);
    glEnable (GL_BLEND);
    glBlendEquation (GL_FUNC_ADD);
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    glDisable (GL_POLYGON_OFFSET_FILL);

    if (post_effects.size() == 0) {
        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        glClearColor(camera->background_color_r(),
                camera->background_color_g(), camera->background_color_b(),
                camera->background_color_a());
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        for (auto it = render_data_vector.begin();
                it != render_data_vector.end(); ++it) {
            renderRenderData(*it, view_matrix, projection_matrix,
                    camera->render_mask(), shader_manager);
        }
    } else {
        RenderTexture* texture_render_texture = post_effect_render_texture_a;
        RenderTexture* target_render_texture;

        glBindFramebuffer(GL_FRAMEBUFFER,
                texture_render_texture->getFrameBufferId());
        glViewport(0, 0, texture_render_texture->width(),
                texture_render_texture->height());

        glClearColor(camera->background_color_r(),
                camera->background_color_g(), camera->background_color_b(),
                camera->background_color_a());
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);

        for (auto it = render_data_vector.begin();
                it != render_data_vector.end(); ++it) {
            renderRenderData(*it, view_matrix, projection_matrix,
                    camera->render_mask(), shader_manager);
        }

        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        for (int i = 0; i < post_effects.size() - 1; ++i) {
            if (i % 2 == 0) {
                texture_render_texture = post_effect_render_texture_a;
                target_render_texture = post_effect_render_texture_b;
            } else {
                texture_render_texture = post_effect_render_texture_b;
                target_render_texture = post_effect_render_texture_a;
            }
            glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
            glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

            glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
            renderPostEffectData(camera, texture_render_texture,
                    post_effects[i], post_effect_shader_manager);
        }

        glBindFramebuffer(GL_FRAMEBUFFER, framebufferId);
        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        renderPostEffectData(camera, texture_render_texture,
                post_effects.back(), post_effect_shader_manager);
    }
}

void Renderer::occlusion_cull(Scene* scene,
        std::vector<SceneObject*> scene_objects) {
#if _GVRF_USE_GLES3_
    if (!scene->get_occlusion_culling()) {
        return;
    }

    for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
        RenderData* render_data = (*it)->render_data();
        if (render_data == 0) {
            continue;
        }

        if (render_data->pass(0)->material() == 0) {
            continue;
        }

        //If a query was issued on an earlier or same frame and if results are
        //available, then update the same. If results are unavailable, do nothing
        if (!(*it)->is_query_issued()) {
            continue;
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
        }
    }
#endif
}

void Renderer::frustum_cull(Scene* scene, Camera *camera,
        std::vector<SceneObject*> scene_objects,
        std::vector<RenderData*>& render_data_vector, glm::mat4 vp_matrix,
        ShaderManager* shader_manager) {
    for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {
        SceneObject *scene_object = (*it);
        RenderData* render_data = scene_object->render_data();
        if (render_data == 0 || render_data->pass(0)->material() == 0) {
            continue;
        }

        // Check for frustum culling flag
        if (!scene->get_frustum_culling()) {
            //No occlusion or frustum tests enabled
            render_data_vector.push_back(render_data);
            continue;
        }

        // Frustum culling setup
        Mesh* currentMesh = render_data->mesh();
        if (currentMesh == NULL) {
            continue;
        }

        const BoundingVolume& bounding_volume = currentMesh->getBoundingVolume();

        Transform* t = render_data->owner_object()->transform();
        if (nullptr == t) {
            continue;
        }
        glm::mat4 model_matrix_tmp(t->getModelMatrix());
        glm::mat4 mvp_matrix_tmp(vp_matrix * model_matrix_tmp);

        // Frustum
        float frustum[6][4];

        // Matrix to array
        float mvp_matrix_array[16] = { 0.0 };
        const float *mat_to_array = (const float*) glm::value_ptr(
                mvp_matrix_tmp);
        memcpy(mvp_matrix_array, mat_to_array, sizeof(float) * 16);

        // Build the frustum
        build_frustum(frustum, mvp_matrix_array);

        // Check for being inside or outside frustum
        bool is_inside = is_cube_in_frustum(frustum, bounding_volume);

        // Only push those scene objects that are inside of the frustum
        if (!is_inside) {
            scene_object->set_in_frustum(false);
            continue;
        }

        // Transform the bounding sphere
        glm::vec4 sphere_center(bounding_volume.center(), 1.0f);
        glm::vec4 transformed_sphere_center = mvp_matrix_tmp * sphere_center;

        // Calculate distance from camera
        t = camera->owner_object()->transform();
        if (nullptr == t) {
            continue;
        }
        glm::vec3 camera_position = t->position();
        glm::vec4 position(camera_position, 1.0f);
        glm::vec4 difference = transformed_sphere_center - position;
        float distance = glm::dot(difference, difference);

        // this distance will be used when sorting transparent objects
        render_data->set_camera_distance(distance);

        // Check if this is the correct LOD level
        if (!scene_object->inLODRange(distance)) {
            // not in range, don't add it to the list
            continue;
        }

        scene_object->set_in_frustum();
        bool visible = scene_object->visible();

        //If visibility flag was set by an earlier occlusion query,
        //turn visibility on for the object
        if (visible) {
            render_data_vector.push_back(render_data);
        }

        if (render_data->pass(0)->material() == 0
                || !scene->get_occlusion_culling()) {
            continue;
        }

#if _GVRF_USE_GLES3_
        //If a previous query is active, do not issue a new query.
        //This avoids overloading the GPU with too many queries
        //Queries may span multiple frames

        bool is_query_issued = scene_object->is_query_issued();
        if (!is_query_issued) {
            //Setup basic bounding box and material
            RenderData* bounding_box_render_data(new RenderData());
            Mesh* bounding_box_mesh = render_data->mesh()->getBoundingBox();
            bounding_box_render_data->set_mesh(bounding_box_mesh);

            GLuint *query = scene_object->get_occlusion_array();

            glDepthFunc (GL_LEQUAL);
            glEnable (GL_DEPTH_TEST);
            glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);

            //Issue the query only with a bounding box
            glBeginQuery(GL_ANY_SAMPLES_PASSED, query[0]);
            shader_manager->getBoundingBoxShader()->render(mvp_matrix_tmp,
                    bounding_box_render_data,
                    bounding_box_render_data->pass(0)->material());
            glEndQuery (GL_ANY_SAMPLES_PASSED);
            scene_object->set_query_issued(true);

            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);

            //Delete the generated bounding box mesh
            bounding_box_mesh->cleanUp();
            delete bounding_box_render_data;
        }
#endif
    }
}

void Renderer::build_frustum(float frustum[6][4], float mvp_matrix[16]) {
    float t;

    /* Extract the numbers for the RIGHT plane */
    frustum[0][0] = mvp_matrix[3] - mvp_matrix[0];
    frustum[0][1] = mvp_matrix[7] - mvp_matrix[4];
    frustum[0][2] = mvp_matrix[11] - mvp_matrix[8];
    frustum[0][3] = mvp_matrix[15] - mvp_matrix[12];

    /* Normalize the result */
    t = sqrt(
            frustum[0][0] * frustum[0][0] + frustum[0][1] * frustum[0][1]
                    + frustum[0][2] * frustum[0][2]);
    frustum[0][0] /= t;
    frustum[0][1] /= t;
    frustum[0][2] /= t;
    frustum[0][3] /= t;

    /* Extract the numbers for the LEFT plane */
    frustum[1][0] = mvp_matrix[3] + mvp_matrix[0];
    frustum[1][1] = mvp_matrix[7] + mvp_matrix[4];
    frustum[1][2] = mvp_matrix[11] + mvp_matrix[8];
    frustum[1][3] = mvp_matrix[15] + mvp_matrix[12];

    /* Normalize the result */
    t = sqrt(
            frustum[1][0] * frustum[1][0] + frustum[1][1] * frustum[1][1]
                    + frustum[1][2] * frustum[1][2]);
    frustum[1][0] /= t;
    frustum[1][1] /= t;
    frustum[1][2] /= t;
    frustum[1][3] /= t;

    /* Extract the BOTTOM plane */
    frustum[2][0] = mvp_matrix[3] + mvp_matrix[1];
    frustum[2][1] = mvp_matrix[7] + mvp_matrix[5];
    frustum[2][2] = mvp_matrix[11] + mvp_matrix[9];
    frustum[2][3] = mvp_matrix[15] + mvp_matrix[13];

    /* Normalize the result */
    t = sqrt(
            frustum[2][0] * frustum[2][0] + frustum[2][1] * frustum[2][1]
                    + frustum[2][2] * frustum[2][2]);
    frustum[2][0] /= t;
    frustum[2][1] /= t;
    frustum[2][2] /= t;
    frustum[2][3] /= t;

    /* Extract the TOP plane */
    frustum[3][0] = mvp_matrix[3] - mvp_matrix[1];
    frustum[3][1] = mvp_matrix[7] - mvp_matrix[5];
    frustum[3][2] = mvp_matrix[11] - mvp_matrix[9];
    frustum[3][3] = mvp_matrix[15] - mvp_matrix[13];

    /* Normalize the result */
    t = sqrt(
            frustum[3][0] * frustum[3][0] + frustum[3][1] * frustum[3][1]
                    + frustum[3][2] * frustum[3][2]);
    frustum[3][0] /= t;
    frustum[3][1] /= t;
    frustum[3][2] /= t;
    frustum[3][3] /= t;

    /* Extract the FAR plane */
    frustum[4][0] = mvp_matrix[3] - mvp_matrix[2];
    frustum[4][1] = mvp_matrix[7] - mvp_matrix[6];
    frustum[4][2] = mvp_matrix[11] - mvp_matrix[10];
    frustum[4][3] = mvp_matrix[15] - mvp_matrix[14];

    /* Normalize the result */
    t = sqrt(
            frustum[4][0] * frustum[4][0] + frustum[4][1] * frustum[4][1]
                    + frustum[4][2] * frustum[4][2]);
    frustum[4][0] /= t;
    frustum[4][1] /= t;
    frustum[4][2] /= t;
    frustum[4][3] /= t;

    /* Extract the NEAR plane */
    frustum[5][0] = mvp_matrix[3] + mvp_matrix[2];
    frustum[5][1] = mvp_matrix[7] + mvp_matrix[6];
    frustum[5][2] = mvp_matrix[11] + mvp_matrix[10];
    frustum[5][3] = mvp_matrix[15] + mvp_matrix[14];

    /* Normalize the result */
    t = sqrt(
            frustum[5][0] * frustum[5][0] + frustum[5][1] * frustum[5][1]
                    + frustum[5][2] * frustum[5][2]);
    frustum[5][0] /= t;
    frustum[5][1] /= t;
    frustum[5][2] /= t;
    frustum[5][3] /= t;
}

bool Renderer::is_cube_in_frustum(float frustum[6][4],
        const BoundingVolume &bounding_volume) {
    int p;
    glm::vec3 min_corner = bounding_volume.min_corner();
    glm::vec3 max_corner = bounding_volume.max_corner();

    float Xmin = min_corner[0];
    float Ymin = min_corner[1];
    float Zmin = min_corner[2];
    float Xmax = max_corner[0];
    float Ymax = max_corner[1];
    float Zmax = max_corner[2];

    for (p = 0; p < 6; p++) {
        if (frustum[p][0] * (Xmin) + frustum[p][1] * (Ymin)
                + frustum[p][2] * (Zmin) + frustum[p][3] > 0)
            continue;
        if (frustum[p][0] * (Xmax) + frustum[p][1] * (Ymin)
                + frustum[p][2] * (Zmin) + frustum[p][3] > 0)
            continue;
        if (frustum[p][0] * (Xmin) + frustum[p][1] * (Ymax)
                + frustum[p][2] * (Zmin) + frustum[p][3] > 0)
            continue;
        if (frustum[p][0] * (Xmax) + frustum[p][1] * (Ymax)
                + frustum[p][2] * (Zmin) + frustum[p][3] > 0)
            continue;
        if (frustum[p][0] * (Xmin) + frustum[p][1] * (Ymin)
                + frustum[p][2] * (Zmax) + frustum[p][3] > 0)
            continue;
        if (frustum[p][0] * (Xmax) + frustum[p][1] * (Ymin)
                + frustum[p][2] * (Zmax) + frustum[p][3] > 0)
            continue;
        if (frustum[p][0] * (Xmin) + frustum[p][1] * (Ymax)
                + frustum[p][2] * (Zmax) + frustum[p][3] > 0)
            continue;
        if (frustum[p][0] * (Xmax) + frustum[p][1] * (Ymax)
                + frustum[p][2] * (Zmax) + frustum[p][3] > 0)
            continue;
        return false;
    }
    return true;
}

void Renderer::renderCamera(Scene* scene, Camera* camera,
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

void Renderer::renderCamera(Scene* scene, Camera* camera,
        RenderTexture* render_texture, ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    renderCamera(scene, camera, render_texture->getFrameBufferId(), 0, 0,
            render_texture->width(), render_texture->height(), shader_manager,
            post_effect_shader_manager, post_effect_render_texture_a,
            post_effect_render_texture_b);

}

void Renderer::renderCamera(Scene* scene, Camera* camera, int viewportX,
        int viewportY, int viewportWidth, int viewportHeight,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {

    renderCamera(scene, camera, 0, viewportX, viewportY, viewportWidth,
            viewportHeight, shader_manager, post_effect_shader_manager,
            post_effect_render_texture_a, post_effect_render_texture_b);
}

void Renderer::renderRenderData(RenderData* render_data,
        const glm::mat4& view_matrix, const glm::mat4& projection_matrix,
        int render_mask, ShaderManager* shader_manager) {
    if (render_mask & render_data->render_mask()) {

        if (render_data->offset()) {
            glEnable (GL_POLYGON_OFFSET_FILL);
            glPolygonOffset(render_data->offset_factor(),
                    render_data->offset_units());
        }
        if (!render_data->depth_test()) {
            glDisable (GL_DEPTH_TEST);
        }
        if (!render_data->alpha_blend()) {
            glDisable (GL_BLEND);
        }
        if (render_data->mesh() != 0) {
            for (int curr_pass = 0; curr_pass < render_data->pass_count();
                    ++curr_pass) {
                numberTriangles += render_data->mesh()->getNumTriangles();
                numberDrawCalls++;

                set_face_culling(render_data->pass(curr_pass)->cull_face());
                Material* curr_material =
                        render_data->pass(curr_pass)->material();
                Transform* const t = render_data->owner_object()->transform();

                if (curr_material != nullptr && nullptr != t) {
                    glm::mat4 model_matrix(t->getModelMatrix());
                    glm::mat4 mv_matrix(view_matrix * model_matrix);
                    glm::mat4 mvp_matrix(projection_matrix * mv_matrix);
                    try {
                        bool right = render_mask
                                & RenderData::RenderMaskBit::Right;
                        switch (curr_material->shader_type()) {
                        case Material::ShaderType::UNLIT_HORIZONTAL_STEREO_SHADER:
                            shader_manager->getUnlitHorizontalStereoShader()->render(
                                    mvp_matrix, render_data, curr_material,
                                    right);
                            break;
                        case Material::ShaderType::UNLIT_VERTICAL_STEREO_SHADER:
                            shader_manager->getUnlitVerticalStereoShader()->render(
                                    mvp_matrix, render_data, curr_material,
                                    right);
                            break;
                        case Material::ShaderType::OES_SHADER:
                            shader_manager->getOESShader()->render(mvp_matrix,
                                    render_data, curr_material);
                            break;
                        case Material::ShaderType::OES_HORIZONTAL_STEREO_SHADER:
                            shader_manager->getOESHorizontalStereoShader()->render(
                                    mvp_matrix, render_data, curr_material,
                                    right);
                            break;
                        case Material::ShaderType::OES_VERTICAL_STEREO_SHADER:
                            shader_manager->getOESVerticalStereoShader()->render(
                                    mvp_matrix, render_data, curr_material,
                                    right);
                            break;
                        case Material::ShaderType::CUBEMAP_SHADER:
                            shader_manager->getCubemapShader()->render(
                                    model_matrix, mvp_matrix, render_data,
                                    curr_material);
                            break;
                        case Material::ShaderType::CUBEMAP_REFLECTION_SHADER:
                            shader_manager->getCubemapReflectionShader()->render(
                                    mv_matrix, glm::inverseTranspose(mv_matrix),
                                    glm::inverse(view_matrix), mvp_matrix,
                                    render_data, curr_material);
                            break;
                        case Material::ShaderType::TEXTURE_SHADER:
                            shader_manager->getTextureShader()->render(
                                    mv_matrix, glm::inverseTranspose(mv_matrix),
                                    mvp_matrix, render_data, curr_material);
                            break;
                        case Material::ShaderType::EXTERNAL_RENDERER_SHADER:
                            shader_manager->getExternalRendererShader()->render(
                                    mv_matrix, glm::inverseTranspose(mv_matrix),
                                    mvp_matrix, render_data);
                            break;
                        case Material::ShaderType::ASSIMP_SHADER:
                            shader_manager->getAssimpShader()->render(
                                    mv_matrix, glm::inverseTranspose(mv_matrix),
                                    mvp_matrix, render_data, curr_material);
                            break;
                        default:
                            shader_manager->getCustomShader(
                                    curr_material->shader_type())->render(
                                    mvp_matrix, render_data, curr_material,
                                    right);
                            break;
                        }
                    } catch (std::string error) {
                        LOGE(
                                "Error detected in Renderer::renderRenderData; name : %s, error : %s", render_data->owner_object()->name().c_str(), error.c_str());
                        shader_manager->getErrorShader()->render(mvp_matrix,
                                render_data);
                    }
                }
            }
        }

        // Restoring to Default.
        // TODO: There's a lot of redundant state changes. If on every render face culling is being set there's no need to
        // restore defaults. Possibly later we could add a OpenGL state wrapper to avoid redundant api calls.
        if (render_data->cull_face() != RenderData::CullBack) {
            glEnable (GL_CULL_FACE);
            glCullFace (GL_BACK);
        }

        if (render_data->offset()) {
            glDisable (GL_POLYGON_OFFSET_FILL);
        }
        if (!render_data->depth_test()) {
            glEnable (GL_DEPTH_TEST);
        }
        if (!render_data->alpha_blend()) {
            glEnable (GL_BLEND);
        }
    }
}

void Renderer::renderPostEffectData(Camera* camera,
        RenderTexture* render_texture, PostEffectData* post_effect_data,
        PostEffectShaderManager* post_effect_shader_manager) {
    try {
        switch (post_effect_data->shader_type()) {
        case PostEffectData::ShaderType::COLOR_BLEND_SHADER:
            post_effect_shader_manager->getColorBlendPostEffectShader()->render(
                    render_texture, post_effect_data,
                    post_effect_shader_manager->quad_vertices(),
                    post_effect_shader_manager->quad_uvs(),
                    post_effect_shader_manager->quad_triangles());
            break;
        case PostEffectData::ShaderType::HORIZONTAL_FLIP_SHADER:
            post_effect_shader_manager->getHorizontalFlipPostEffectShader()->render(
                    render_texture, post_effect_data,
                    post_effect_shader_manager->quad_vertices(),
                    post_effect_shader_manager->quad_uvs(),
                    post_effect_shader_manager->quad_triangles());
            break;
        default:
            post_effect_shader_manager->getCustomPostEffectShader(
                    post_effect_data->shader_type())->render(camera,
                    render_texture, post_effect_data,
                    post_effect_shader_manager->quad_vertices(),
                    post_effect_shader_manager->quad_uvs(),
                    post_effect_shader_manager->quad_triangles());
            break;
        }
    } catch (std::string error) {
        LOGE(
                "Error detected in Renderer::renderPostEffectData; error : %s", error.c_str());
    }
}

void Renderer::set_face_culling(int cull_face) {
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

}
