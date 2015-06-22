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
static float drawTime;
static float avgDrawTime;
static bool monoscopic = false;
static bool disjointTimerSupported = false;
static int beginCurrentFrameQuery;
static int endCurrentFrameQuery;
static int beginLastFrameQuery;
static int endLastFrameQuery;
static GLuint queries[2][4];
static GLuint queriesAvailable;
static GLint queriesDisjointOccurred;
static GLuint64 timeElapsed;
static GLuint64 frameCount;

#ifndef GL_TIME_ELAPSED_EXT
#define GL_TIME_ELAPSED_EXT 0x88BF
#define GL_GPU_DISJOINT_EXT 0x8FBB
#define GL_TIMESTAMP_EXT 0x8E28
extern "C" {
typedef void (*PFNGLGETQUERYOBJECTUI64VEXTPROC) (GLuint id, GLenum pname, GLuint64 *params);
typedef void (*PFNGLQUERYCOUNTEREXTPROC)(GLuint id, GLenum target);
PFNGLGETQUERYOBJECTUI64VEXTPROC glGetQueryObjectui64vEXT__;
PFNGLQUERYCOUNTEREXTPROC glQueryCounterEXT__;
}
#endif

void Renderer::initializeStats(){
    beginCurrentFrameQuery = 0;
    endCurrentFrameQuery = 1;
    beginLastFrameQuery = 2;
    endLastFrameQuery = 3;
    timeElapsed = 0;
    frameCount = 0;
    drawTime = 0.0f;
    avgDrawTime = 0.0f;

    const GLubyte *extensions = glGetString(GL_EXTENSIONS);
    char *exist = strstr((char *)extensions, "GL_EXT_disjoint_timer_query");
    if(!strncmp(exist, "GL_EXT_disjoint_timer_query", 26)) {
        disjointTimerSupported = true;
    }

    if(disjointTimerSupported) {
        glGenQueries(4, queries[0]);
        glGenQueries(4, queries[1]);

        glGetQueryObjectui64vEXT__ = (PFNGLGETQUERYOBJECTUI64VEXTPROC) eglGetProcAddress("glGetQueryObjectui64vEXT");
        glQueryCounterEXT__ = (PFNGLQUERYCOUNTEREXTPROC) eglGetProcAddress("glQueryCounterEXT");
    }
}

void Renderer::resetStats(){
    numberDrawCalls = 0;
    numberTriangles = 0;
    queriesAvailable = 0;
    queriesDisjointOccurred = 0;
    if(disjointTimerSupported) {
        glGetIntegerv(GL_GPU_DISJOINT_EXT, &queriesDisjointOccurred);
    }
}

int Renderer::getNumberDrawCalls(){
    return numberDrawCalls;
}

int Renderer::getNumberTriangles(){
    return numberTriangles;
}

void swapFrameQueries() {
    int swapTemporaryForBegin = beginLastFrameQuery;
    int swapTemporaryForEnd = endLastFrameQuery;

    beginLastFrameQuery = beginCurrentFrameQuery;
    endLastFrameQuery = endCurrentFrameQuery;

    beginCurrentFrameQuery = swapTemporaryForBegin;
    endCurrentFrameQuery = swapTemporaryForEnd;
}

void Renderer::startGpuTimer(int eye) {
    if(!disjointTimerSupported) {
        return;
    }
    //glBeginQuery(GL_TIME_ELAPSED_EXT, queries[eye][currentFrameQuery]);
    glQueryCounterEXT__(queries[eye][beginCurrentFrameQuery], GL_TIMESTAMP_EXT);
    //LOGD("startGpuTimer: [%d][%d]\n", eye, beginCurrentFrameQuery);
}

void Renderer::stopGpuTimer(int eye) {
    if(!disjointTimerSupported) {
        return;
    }

    //glEndQuery(GL_TIME_ELAPSED_EXT);
    glQueryCounterEXT__(queries[eye][endCurrentFrameQuery], GL_TIMESTAMP_EXT);
    //LOGD("stopGpuTimer: [%d][%d]\n", eye, endCurrentFrameQuery);

    if(eye == 0 && !monoscopic) {
        return;
    }

    drawTime += getGpuTimerResult(0);
    if(!monoscopic) {
        drawTime += getGpuTimerResult(1);
    }

    swapFrameQueries();

    frameCount++;
    if((frameCount % 10) == 0) {
        avgDrawTime = drawTime / 10.0f;
        drawTime = 0.0f;
    }
}

float Renderer::getGpuTimerResult(int eye) {
    float gpuTime = 1000.0f;
    LOGD("getGpuTimerResult: checking [%d][%d]\n", eye, endLastFrameQuery);

    /*
    glGetQueryObjectuiv(queries[eye][endLastFrameQuery], GL_QUERY_RESULT_AVAILABLE, &queriesAvailable);
    if(queriesAvailable == 0) {
        LOGD("getGpuTimerResult: [%d][%d] query not available at this time\n", eye, endLastFrameQuery);

        return gpuTime;
    }
    */
    if(frameCount < 2) {
        return 0.0f;
    }
    queriesAvailable = 0;
    while(!queriesAvailable) {
        glGetQueryObjectuiv(queries[eye][endLastFrameQuery], GL_QUERY_RESULT_AVAILABLE, &queriesAvailable);
    }

    glGetIntegerv(GL_GPU_DISJOINT_EXT, &queriesDisjointOccurred);
    if(!queriesDisjointOccurred) {
        GLuint64 beginTimestamp;
        GLuint64 endTimestamp;
        glGetQueryObjectui64vEXT__(queries[eye][beginLastFrameQuery], GL_QUERY_RESULT, &beginTimestamp);
        glGetQueryObjectui64vEXT__(queries[eye][endLastFrameQuery], GL_QUERY_RESULT, &endTimestamp);
        LOGD("getGpuTimerResult: [%d][%d] begin timestamp = %lld\n", eye, beginLastFrameQuery, beginTimestamp);
        LOGD("getGpuTimerResult: [%d][%d]   end timestamp = %lld\n", eye, endLastFrameQuery, endTimestamp);


        //gpuTime = (float) ((double)timeElapsed / 1000000.0);
        gpuTime = (endTimestamp - beginTimestamp) * 0.000001;
        if(endTimestamp == 0 || beginTimestamp == 0) {
            LOGD("getGpuTimerResult: either begin or end was 0\n");

            gpuTime = 250.0f;
        }

    }
    return  gpuTime;
}


float Renderer::getDrawTime() {
    return avgDrawTime;
}



void Renderer::renderCamera(Scene* scene, Camera* camera, int framebufferId,
        int viewportX, int viewportY, int viewportWidth, int viewportHeight,
        ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b) {
    // there is no need to flat and sort every frame.
    // however let's keep it as is and assume we are not changed
    // This is not right way to do data conversion. However since GVRF doesn't support
    // bone/weight/joint and other assimp data, we will put general model conversion
    // on hold and do this kind of conversion fist

    numberDrawCalls = 0;
    numberTriangles = 0;

    int currentEye = camera->render_mask() - 1;
    if(scene->get_stats_enabled()) {
        startGpuTimer(currentEye);
    }

    if (scene->getSceneDirtyFlag()) {

        glm::mat4 view_matrix = camera->getViewMatrix();
        glm::mat4 projection_matrix = camera->getProjectionMatrix();
        glm::mat4 vp_matrix = glm::mat4(projection_matrix * view_matrix);

        std::vector<SceneObject*> scene_objects = scene->getWholeSceneObjects();
        std::vector<RenderData*> render_data_vector;

        // do occlusion culling, if enabled
        occlusion_cull(scene, scene_objects);

        // do frustum culling, if enabled
        frustum_cull(scene, scene_objects, render_data_vector, vp_matrix,
                shader_manager);

        // do sorting based on render order
        std::sort(render_data_vector.begin(), render_data_vector.end(),
                compareRenderData);

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

    if(scene->get_stats_enabled()) {
        stopGpuTimer(currentEye);
    }

    } // flag checking

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

        if (render_data->material() == 0) {
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

void Renderer::frustum_cull(Scene* scene,
        std::vector<SceneObject*> scene_objects,
        std::vector<RenderData* >& render_data_vector,
        glm::mat4 vp_matrix, ShaderManager* shader_manager) {
    for (auto it = scene_objects.begin(); it != scene_objects.end(); ++it) {

        RenderData* render_data = (*it)->render_data();
        if (render_data == 0 || render_data->material() == 0) {
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
        if(currentMesh == NULL) {
            continue;
        }

        const float* bounding_box_info = currentMesh->getBoundingBoxInfo();
        if(bounding_box_info == NULL) {
            continue;
        }

        glm::mat4 model_matrix_tmp(
                render_data->owner_object()->transform()->getModelMatrix());
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
        bool is_inside = is_cube_in_frustum(frustum, bounding_box_info);

        // Only push those scene objects that are inside of the frustum
        if (!is_inside) {
            (*it)->set_in_frustum(false);
            continue;
        }

        (*it)->set_in_frustum();
        bool visible = (*it)->visible();

        //If visibility flag was set by an earlier occlusion query,
        //turn visibility on for the object
        if (visible) {
            render_data_vector.push_back(render_data);
        }

        if (render_data->material() == 0 || !scene->get_occlusion_culling()) {
            continue;
        }

#if _GVRF_USE_GLES3_
        //If a previous query is active, do not issue a new query.
        //This avoids overloading the GPU with too many queries
        //Queries may span multiple frames

        bool is_query_issued = (*it)->is_query_issued();
        if (!is_query_issued) {
            //Setup basic bounding box and material
            RenderData* bounding_box_render_data(
                    new RenderData());
            Mesh* bounding_box_mesh =
                    render_data->mesh()->getBoundingBox();
            bounding_box_render_data->set_mesh(bounding_box_mesh);

            GLuint *query = (*it)->get_occlusion_array();

            glDepthFunc (GL_LEQUAL);
            glEnable (GL_DEPTH_TEST);
            glColorMask(GL_FALSE, GL_FALSE, GL_FALSE, GL_FALSE);

            //Issue the query only with a bounding box
            glBeginQuery(GL_ANY_SAMPLES_PASSED, query[0]);
            shader_manager->getBoundingBoxShader()->render(mvp_matrix_tmp,
                    bounding_box_render_data);
            glEndQuery (GL_ANY_SAMPLES_PASSED);
            (*it)->set_query_issued(true);

            glColorMask(GL_TRUE, GL_TRUE, GL_TRUE, GL_TRUE);

            //Delete the generated bounding box mesh
            bounding_box_mesh->cleanUp();
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

bool Renderer::is_cube_in_frustum(float frustum[6][4], const float *vertex_limit) {
    int p;
    float Xmin = vertex_limit[0];
    float Ymin = vertex_limit[1];
    float Zmin = vertex_limit[2];
    float Xmax = vertex_limit[3];
    float Ymax = vertex_limit[4];
    float Zmax = vertex_limit[5];

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
        RenderTexture* render_texture, ShaderManager* shader_manager,
        PostEffectShaderManager* post_effect_shader_manager,
        RenderTexture* post_effect_render_texture_a,
        RenderTexture* post_effect_render_texture_b, glm::mat4 vp_matrix) {
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

    monoscopic = true;
    renderCamera(scene, camera, 0, viewportX, viewportY, viewportWidth,
            viewportHeight, shader_manager, post_effect_shader_manager,
            post_effect_render_texture_a, post_effect_render_texture_b);
}

void Renderer::renderRenderData(RenderData* render_data,
		const glm::mat4& view_matrix, const glm::mat4& projection_matrix, int render_mask,
        ShaderManager* shader_manager) {
    if (render_mask & render_data->render_mask()) {
        if (!render_data->cull_test()) {
            glDisable (GL_CULL_FACE);
        }
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
            numberTriangles += render_data->mesh()->getNumTriangles();
            numberDrawCalls++;
            glm::mat4 model_matrix(
                    render_data->owner_object()->transform()->getModelMatrix());
            glm::mat4 mv_matrix(view_matrix * model_matrix);
            glm::mat4 mvp_matrix(projection_matrix * mv_matrix);
            try {
                bool right = render_mask & RenderData::RenderMaskBit::Right;
                switch (render_data->material()->shader_type()) {
                case Material::ShaderType::UNLIT_SHADER:
                    shader_manager->getUnlitShader()->render(mvp_matrix,
                            render_data);
                    break;
                case Material::ShaderType::UNLIT_HORIZONTAL_STEREO_SHADER:
                    shader_manager->getUnlitHorizontalStereoShader()->render(
                            mvp_matrix, render_data, right);
                    break;
                case Material::ShaderType::UNLIT_VERTICAL_STEREO_SHADER:
                    shader_manager->getUnlitVerticalStereoShader()->render(
                            mvp_matrix, render_data, right);
                    break;
                case Material::ShaderType::OES_SHADER:
                    shader_manager->getOESShader()->render(mvp_matrix,
                            render_data);
                    break;
                case Material::ShaderType::OES_HORIZONTAL_STEREO_SHADER:
                    shader_manager->getOESHorizontalStereoShader()->render(
                            mvp_matrix, render_data, right);
                    break;
                case Material::ShaderType::OES_VERTICAL_STEREO_SHADER:
                    shader_manager->getOESVerticalStereoShader()->render(
                            mvp_matrix, render_data, right);
                    break;
                case Material::ShaderType::CUBEMAP_SHADER:
                    shader_manager->getCubemapShader()->render(model_matrix,
                            mvp_matrix, render_data);
                    break;
                case Material::ShaderType::CUBEMAP_REFLECTION_SHADER:
                    shader_manager->getCubemapReflectionShader()->render(
                            mv_matrix, glm::inverseTranspose(mv_matrix),
                            glm::inverse(view_matrix), mvp_matrix, render_data);
                    break;
                default:
                    shader_manager->getCustomShader(
                            render_data->material()->shader_type())->render(
                            mvp_matrix, render_data, right);
                    break;
                }
            } catch (std::string error) {
                LOGE(
                        "Error detected in Renderer::renderRenderData; name : %s, error : %s",
                        render_data->owner_object()->name().c_str(),
                        error.c_str());
                shader_manager->getErrorShader()->render(mvp_matrix,
                        render_data);
            }
        }
        if (!render_data->cull_test()) {
            glEnable (GL_CULL_FACE);
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
        RenderTexture* render_texture,
        PostEffectData* post_effect_data,
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
        LOGE("Error detected in Renderer::renderPostEffectData; error : %s",
                error.c_str());
    }
}

}
