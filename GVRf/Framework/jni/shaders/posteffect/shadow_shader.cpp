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
 * Add shadow effect // TODO
 ***************************************************************************/

#include "gl/gl_program.h"
#include "objects/post_effect_data.h"
#include "objects/textures/render_texture.h"
#include "util/gvr_gl.h"
#include "engine/memory/gl_delete.h"
#include "shadow_shader.h"

#include "objects/components/render_data.h"

#include "objects/mesh.h"
#include "objects/light.h"

namespace gvr {
static const char VERTEX_SHADER[] = ""
        "uniform vec3 u_light_position;\n"
        "uniform mat4 u_matrix_light_view_projection;\n"
        "uniform mat4 u_matrix_bias;\n"
        "uniform mat4 u_mvp;\n"
        "uniform mat4 u_mv;\n"
        "uniform mat4 u_mv_cam;\n"
        "uniform mat4 u_mv_model;\n"
        "uniform mat4 u_mv_it;\n"
        "attribute vec4 a_position;\n"
        "attribute vec4 a_tex_coord;\n"
        "attribute vec3 a_normal;\n"
        "varying vec2 v_tex_coord;\n"
        "varying vec4 v_position_world;\n "
        "varying vec4 v_position;\n "
        "varying vec4 v_light_biased_clip_position;\n"

        "varying vec3 v_viewspace_normal;\n"
        "varying vec3 v_viewspace_light_direction;\n"

        "void main() {\n"

        "  vec4 v_viewspace_position_vec4 = u_mv * a_position;\n"
        "  vec3 v_viewspace_position = v_viewspace_position_vec4.xyz / v_viewspace_position_vec4.w;\n"

        "  vec4 v_viewspace_light_directionv4 = u_mv_cam * vec4(u_light_position, 1.0);\n"
        "  v_viewspace_light_direction = (v_viewspace_light_directionv4.xyz / v_viewspace_light_directionv4.w) - v_viewspace_position;\n"

        // option 1
        "  vec4 v_viewspace_normal4 = u_mv_it * vec4(a_normal, 1.0);\n"
        "  v_viewspace_normal = v_viewspace_normal4.xyz / v_viewspace_normal4.w;\n"

        // option 2
        //"  v_viewspace_normal = mat3(u_mv_cam) * mat3(u_mv_model) * a_normal;\n"

        ////////////////////////////////////////////////////////////////////

        "  vec4 position = vec4(a_position.xyz / a_position.w, 1.0);\n"
        "  v_light_biased_clip_position = u_matrix_bias * u_matrix_light_view_projection * position ;\n"
        "  v_position_world = u_mvp * position ;\n"
        "  v_position = position ;\n"
        "  v_tex_coord = a_tex_coord.xy;\n"
        "  gl_Position = v_position_world;\n"
        "}\n";

//http://www.opengl-tutorial.org/intermediate-tutorials/tutorial-16-shadow-mapping/
static const char FRAGMENT_SHADER[] =
        "precision highp float;\n"
        "uniform mat4 u_mvp;\n"
        "uniform int u_mode;\n"
        "uniform int u_edges_mode_;\n"
        "uniform float u_edges_length;\n"
        "uniform float u_shadow_gradient_center;\n"
        "uniform float u_lighting_shade;\n"

        "uniform float u_shadow_smooth_size;\n" // blur number of samples = // pow((shadowSmoothSize*2)+1, 2)
        "uniform float u_shadow_smooth_distance;\n" // blur distance

        "uniform float u_ambient_shadow;\n"

        "uniform float u_stratified_sampling;\n"

        "uniform float u_depth_offset;\n" // bias => Resolve shadow acne => Care: Peter Panning
        "uniform sampler2D u_texture;\n" // main texture (color)
        "uniform sampler2D u_texture_shadow_map;\n" // shadow map
        "uniform sampler2D u_texture_camera_depth;\n" // TODO remove it: only to test
        "uniform sampler2D u_texture_light_color;\n" // TODO remove it: only to test
        "uniform vec3 u_light_position;\n"
        "uniform mat4 u_matrix_light_view_projection;\n"

        "varying vec3 v_viewspace_normal;\n"
        "varying vec3 v_viewspace_light_direction;\n"

        "varying vec2 v_tex_coord;\n"
        "varying vec4 v_position;\n"
        "varying vec4 v_position_world;\n"
        "varying vec4 v_light_biased_clip_position;\n"

        "void main() { \n"

        //////////////////////////////////////////////////////////////////////////////////////////
        ///  Ignore render: get depth light mode
        //  FBO created with: getFBO => glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depth->id(), 0);
        "	if (u_mode < 0) return;\n"

        //////////////////////////////////////////////////////////////////////////////////////////
        /// Default texture // Optional mode: replace TextureShader
        "   if (u_mode == 1) { gl_FragColor = texture2D(u_texture, v_tex_coord); return; } //\n"

        //////////////////////////////////////////////////////////////////////////////////////////
        /// Test Mode
        "   if (u_mode == 2) { gl_FragColor = vec4( pow( texture2D(u_texture_light_color, (v_position_world.xy / v_position_world.w + 1.0) / 2.0).z, 10.0) ); return; } \n" //
        "   if (u_mode == 3) { gl_FragColor = vec4( pow( texture2D(u_texture_camera_depth, (v_position_world.xy / v_position_world.w + 1.0) / 2.0).z, 10.0) ); return; } \n" //


        //////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////////////////////////////////////////////////////
        /////////// >>  u_mode == 0 << ///////////  ||
        //////////////////////////////////////////  \/


        /////////////////////////////////////////////
        // 	Calculates diffuse => cosTheta is dot( n,l ), clamped between 0 and 1
        "	float cosTheta = dot(normalize(v_viewspace_light_direction), normalize(v_viewspace_normal));"
        " 	float diffuse = max(0.0, cosTheta) * (1.0 - u_lighting_shade) + u_lighting_shade;\n"

        /////////////////////////////////////////////
        // Calculates light NDC Position
        "	vec4 lightBiasedClipPositionBase = normalize(v_light_biased_clip_position);\n"
        "	vec4 lightNDCPosition = lightBiasedClipPositionBase / lightBiasedClipPositionBase.w;\n"

        /////////////////////////////////////////////
        // Get depth shadow map
        "	vec4 depthZ = texture2D(u_texture_shadow_map, lightNDCPosition.xy);\n"// from light
        "	float shadowCheck = depthZ.z - lightNDCPosition.z;\n"

        /////////////////////////////////////////////
        // 	Modify the bias according to the slope // TODO
        "	float biasFixes = 0.0002*tan(acos(cosTheta)); "// cosTheta is dot( n,l )
        "	biasFixes = clamp(biasFixes, 0.0, 0.01);"// clamped between 0 and 1

        /////////////////////////////////////////////
        /// Random with seed = position // TODO TO use with (Stratified Poisson Sampling)
        "	vec4 seed4 = v_position;\n"
        "	float dot_product = dot(seed4, vec4(12.9898,78.233,45.164,94.673));\n"
        "	float random = fract(sin(dot_product) * 43758.5453);\n"

        /////////////////////////////////////////////
        /// "Stratified Poisson Sampling"
        "	float distanceShadow = 1000.0 * (shadowCheck - (v_position_world.z / v_position_world.w)) / u_shadow_smooth_distance;"// Blur factor by distance on shadow
        "	float randomPositive = clamp(random, 0.001, 1.0) * u_stratified_sampling ;\n" // "Stratified Poisson Sampling" <<== input
        "	distanceShadow /= randomPositive != 0.0? randomPositive : 1.0;\n" // "Stratified Poisson Sampling" with blur

        /////////////////////////////////////////////
        /// Aliasing / "Anti aliasing" => Poisson sampling
        "	float shadowSmoothSize = u_shadow_smooth_size * 0.0 + 1.0;\n"// Default blur factor on shadow <<== input
        "	float visibility = 1.0;\n"// factor shadow
        "	visibility = 0.0; \n"
        "	for (float x = -shadowSmoothSize ; x <= shadowSmoothSize ; x++ )  \n"
        "	for (float y = -shadowSmoothSize ; y <= shadowSmoothSize ; y++ ) { \n"
        "		vec2 posSample = vec2(x, y);\n"
        "		posSample = distanceShadow < 10000.0 ? posSample / distanceShadow: vec2(0.0, 0.0);\n"
        "		float shadowCheckWithBlur = texture2D( u_texture_shadow_map, lightNDCPosition.xy + posSample ).z;\n" // calculates shadow sample
        "		if ( shadowCheckWithBlur - (lightNDCPosition.z - u_depth_offset  ) > 0.0  ){\n" // check is shadow
        "			visibility++;\n"// count visible by light
        "		}\n"
        "	}\n"
        "	float count = 4.0 * shadowSmoothSize * shadowSmoothSize + 4.0 * shadowSmoothSize + 1.0;  \n"// pow((shadowSmoothSize*2)+1, 2); // => total samples (blur)
        "	visibility = 1.0 - ((visibility/count) ) ; \n"// (average) : shadow intensity between [0.1 : 0.9]

        /////////////////////////////////////////////
        /// Edges
        "	float shadowEdges = u_edges_length;\n"// <<== input
        "	bool isShadowOutside = lightNDCPosition.x <= shadowEdges || lightNDCPosition.x >= (1.0-shadowEdges) || lightNDCPosition.y <= shadowEdges || lightNDCPosition.y >= (1.0-shadowEdges); // isShadowEdge\n"
        "	if (isShadowOutside){ \n "
        "		if (u_edges_mode_ == 1) visibility = 1.0; \n "
        "		if (u_edges_mode_ == 2) visibility = 0.0; \n "
        "	}\n "

        /////////////////////////////////////////////
        /// Gradient by center
        "	float dx = (lightNDCPosition.x - .5);\n"
        "	float dy = (lightNDCPosition.y - .5);\n"
        "	float dist = sqrt(dx * dx + dy * dy) * u_shadow_gradient_center;\n"
        "	float shadowGradientCenter = max(0.0, min(1.0, 1.0 - dist));\n"
        "	float shadowGradientCenterFactor = shadowGradientCenter ; \n"

        /////////////////////////////////////////////
        /// Calculates and light x shadow

        "  	float ambientShadow = u_ambient_shadow; "// 0.1 <<== input
        "  	float ambientLight = max(ambientShadow, diffuse * shadowGradientCenterFactor); "

        "	float darkness = ambientShadow * visibility ; \n"// shadow intensity
        "	float brightness = ambientLight * (1.0 - visibility); \n"// light intensity
        "  	float lightAndShadow = (darkness + brightness ); "

        /////////////////////////////////////////////
        /// Color all others shader
        "	vec4 colorShader = texture2D(u_texture, (v_position_world.xy / v_position_world.w + 1.0) / 2.0);\n"

        /////////////////////////////////////////////
        /// Combination
        "   gl_FragColor = colorShader * clamp(lightAndShadow + ambientShadow, 0.0, 1.0);\n"

        "}";

static const float BIAS_MATRIX[16] = {
    0.5f, 0.0f, 0.0f, 0.0f,
    0.0f, 0.5f, 0.0f, 0.0f,
    0.0f, 0.0f, 0.5f, 0.0f,
    0.5f, 0.5f, 0.5f, 1.0f
};

static const float QUALITY = 1;

GLuint ShadowShader::getFBO(GLTexture* color, GLTexture* depth, int width, int height) {

    checkGlError("ShadowShader::getFBO");
    const int realWidth = width*QUALITY;
    const int realHeight = height*QUALITY;

    glBindTexture(GL_TEXTURE_2D, color->id());
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, realWidth, realHeight, 0, GL_RGB,
            GL_UNSIGNED_BYTE, NULL);
    glBindTexture(GL_TEXTURE_2D, 0);

    glBindTexture(GL_TEXTURE_2D, depth->id());
    glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, realWidth, realHeight, 0,
            GL_DEPTH_COMPONENT, GL_UNSIGNED_SHORT, NULL);
    glBindTexture(GL_TEXTURE_2D, 0);

    GLuint fbo = 0;
    glGenFramebuffers(1, &fbo);
    glBindFramebuffer(GL_FRAMEBUFFER, fbo);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
            color->id(), 0);
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,
            depth->id(), 0);

    ////////// Check FrameBuffer was created with success ///
    int fboStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if (fboStatus != GL_FRAMEBUFFER_COMPLETE) {
        LOGE("Could not create FBO: %d", fboStatus);
        throw std::exception(); // "Could not create FBO: " + fboStatus
    }

    ////////// Release bind for texture and FrameBuffer /////
    glBindFramebuffer(GL_FRAMEBUFFER, 0);

    checkGlError("ShadowShader::getFBO");

    return fbo;
}

ShadowShader::ShadowShader() :
        program_(0), a_position_(0), a_tex_coord_(0), u_texture_(0) {

    LOGI(" Initialize ShadowShader ");
    checkGlError("ShadowShader::ShadowShader");

    deleter_ = getDeleterForThisThread();

    program_ = new GLProgram(VERTEX_SHADER, FRAGMENT_SHADER);
    glUseProgram(program_->id());

    for (GLint error = glGetError(); error; error = glGetError()) {
        throw std::invalid_argument("shadow program fail");
        //	    throw std::exception();  // "Could not create FBO: " + fboStatus
    }

    checkGlError("ShadowShader::GLProgram");

    a_position_ = glGetAttribLocation(program_->id(), "a_position");
    a_tex_coord_ = glGetAttribLocation(program_->id(), "a_tex_coord");
    u_texture_ = glGetUniformLocation(program_->id(), "u_texture");
    u_texture_shadow_map_ = glGetUniformLocation(program_->id(),
            "u_texture_shadow_map");

    checkGlError(
            "ShadowShader::glGetAttribLocation a_position a_tex_coord u_texture");

    u_mvp_ = glGetUniformLocation(program_->id(), "u_mvp");
    u_matrix_bias_ = glGetUniformLocation(program_->id(), "u_matrix_bias");
    u_matrix_light_view_projection_ = glGetUniformLocation(program_->id(),
            "u_matrix_light_view_projection");
    u_depth_offset_ = glGetUniformLocation(program_->id(), "u_depth_offset");

    checkGlError(
            "ShadowShader::glGetAttribLocation u_mvp u_matrix_bias u_matrix_light_view_projection u_depth_offset");

    glUniform1i(u_texture_, 0);
    glUniform1i(u_texture_shadow_map_, 1);
    glUniform1i(glGetUniformLocation(program_->id(), "u_texture_camera_depth"), 2);
    glUniform1i(glGetUniformLocation(program_->id(), "u_texture_light_color"), 3);

    checkGlError(
            "ShadowShader::glUniform1i u_texture_ u_texture_shadow_map_ u_texture_camera_depth u_texture_light_color");

    vaoID_ = 0;

    LOGI(" Initialize ShadowShader FBO light");

    texture_light_depth = new GLTexture(GL_TEXTURE_2D);
    texture_light_color = new GLTexture(GL_TEXTURE_2D); // no use...

    texture_camera_depth = new GLTexture(GL_TEXTURE_2D);
    texture_camera_color = new GLTexture(GL_TEXTURE_2D);

    checkGlError("ShadowShader::getFBO create");

    glUseProgram(0);
}

ShadowShader::~ShadowShader() {
    if (program_ != 0) {
        recycle();
    }

    if (vaoID_ != 0) {
        deleter_->queueVertexArray(vaoID_);
        vaoID_ = 0;
    }

    glDeleteFramebuffers(1, &fbo_light);
    delete texture_light_depth;
    delete texture_light_color;

    glDeleteFramebuffers(1, &fbo_camera);
    delete texture_camera_depth;
    delete texture_camera_color;

}

void ShadowShader::recycle() {
    delete program_;
    program_ = 0;
}

void ShadowShader::render(const glm::mat4& mvp_matrix_cam,
        const glm::mat4& mvp_matrix_light_cam, const glm::mat4& mv_matrix,
        const glm::mat4& mv_it_matrix, const glm::mat4& mv_it_cam,
        const glm::mat4& mv_it_model, glm::vec3 light_position,
        RenderData* render_data, Material* material, int mode) {

    checkGlError("ShadowShader::render init");

    Mesh* mesh = render_data->mesh();

    checkGlError("ShadowShader::render render_data->mesh()");

    glEnable(GL_DEPTH_TEST);
    glDisable(GL_BLEND);

    /// TODO: Check
//	bool use_light = false;
//	Light* light;
//	if (render_data->light_enabled()) {
//		light = render_data->light();
//		if (light->enabled()) {
//			use_light = true;
//		}
//	}

    mesh->generateVAO();

    DirectionalLight* cameraLight = this->getCameraLight();

    glUseProgram(program_->id());
    checkGlError("ShadowShader::render program");

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, texture_camera_color->id());
    checkGlError("ShadowShader::render glBindTexture texture_camera_color");

    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, texture_light_depth->id());
    checkGlError("ShadowShader::render glBindTexture texture_light_depth");

    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, texture_camera_depth->id());
    checkGlError("ShadowShader::render glBindTexture texture_camera_depth");

    glActiveTexture(GL_TEXTURE3);
    glBindTexture(GL_TEXTURE_2D, texture_light_color->id());
    checkGlError("ShadowShader::render glBindTexture texture_light_color");

    glUniform3f(glGetUniformLocation(program_->id(), "u_light_position"),
            light_position.x, light_position.y, light_position.z);

    switch (mode) {
    case ShadowShader::RENDER_FROM_LIGHT:
        glViewport(0, 0, viewportWidth, viewportHeight);
//        glCullFace(GL_FRONT); // TODO
        glCullFace(GL_BACK); // TODO
        glUniform1i(glGetUniformLocation(program_->id(), "u_mode"), -1); // ignore all, less depth render;
        checkGlError("TextureShader::render RENDER_FROM_LIGHT");
        break;

    case ShadowShader::RENDER_FROM_CAMERA:
        // Optional: Replace main render: (Material::ShaderType::TEXTURE_SHADER) only test
        glViewport(0, 0, viewportWidth, viewportHeight);
        glCullFace(GL_BACK);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D,
                material->getTexture("main_texture")->getId());
        glUniform1i(glGetUniformLocation(program_->id(), "u_mode"), 1);
        checkGlError("TextureShader::render RENDER_FROM_CAMERA");
        break;

    case ShadowShader::RENDER_WITH_SHADOW: {
        glCullFace(GL_BACK);

        DirectionalLight::ShadowMapHandlerMode mode =
                cameraLight->getShadowMapHandlerMode();

        glUniform1i(glGetUniformLocation(program_->id(), "u_mode"), 0); // render shadow from original map;
        glUniform1i(glGetUniformLocation(program_->id(), "u_edges_mode_"),
                cameraLight->getShadowMapHandlerEdges());
        glUniform1f(glGetUniformLocation(program_->id(), "u_edges_length"),
                cameraLight->getShadowMapEdgesLength());
        glUniform1f(
                glGetUniformLocation(program_->id(), "u_shadow_smooth_size"),
                cameraLight->getShadowSmoothSize());
        glUniform1f(
                glGetUniformLocation(program_->id(),
                        "u_shadow_smooth_distance"),
                cameraLight->getShadowSmoothDistance());
        glUniform1f(glGetUniformLocation(program_->id(), "u_ambient_shadow"),
                cameraLight->getLightAmbientOnShadow());
        glUniform1f(
                glGetUniformLocation(program_->id(),
                        "u_shadow_gradient_center"),
                cameraLight->getShadowGradientCenter());
        glUniform1f(glGetUniformLocation(program_->id(), "u_lighting_shade"),
                cameraLight->getLightingShade());
        glUniform1f(
                glGetUniformLocation(program_->id(), "u_stratified_sampling"),
                cameraLight->getBoardStratifiedSampling());
        glUniform1f(u_depth_offset_, cameraLight->getBias());

        checkGlError("TextureShader::render RENDER_WITH_SHADOW");
        break;
    }

    default:
        break;
    }

    glUniformMatrix4fv(u_mvp_, 1, GL_FALSE, glm::value_ptr(mvp_matrix_cam));

    glm::mat4 biasMatrix = glm::make_mat4(BIAS_MATRIX);
    glUniformMatrix4fv(u_matrix_bias_, 1, GL_FALSE, glm::value_ptr(biasMatrix));
    glUniformMatrix4fv(u_matrix_light_view_projection_, 1, GL_FALSE,
            glm::value_ptr(mvp_matrix_light_cam));

    glUniformMatrix4fv(glGetUniformLocation(program_->id(), "u_mv"), 1,
            GL_FALSE, glm::value_ptr(mv_matrix));
    glUniformMatrix4fv(glGetUniformLocation(program_->id(), "u_mv_it"), 1,
            GL_FALSE, glm::value_ptr(mv_it_matrix));
    glUniformMatrix4fv(glGetUniformLocation(program_->id(), "u_mv_cam"), 1,
            GL_FALSE, glm::value_ptr(mv_it_cam));
    glUniformMatrix4fv(glGetUniformLocation(program_->id(), "u_mv_model"), 1,
            GL_FALSE, glm::value_ptr(mv_it_model));

    glBindVertexArray(mesh->getVAOId(Material::TEXTURE_SHADER));

    glDrawElements(GL_TRIANGLES, mesh->triangles().size(), GL_UNSIGNED_SHORT,
            0);

    glBindVertexArray(0);

    checkGlError("TextureShader::render");

}

void ShadowShader::updateViewportInfo(int width, int height) {
    if (0 == fbo_light && 0 == fbo_camera) {
        viewportWidth = width;
        viewportHeight = height;

        fbo_light = getFBO(texture_light_color, texture_light_depth, width, height);
        fbo_camera = getFBO(texture_camera_color, texture_camera_depth, width, height);
        checkGlError("ShadowShader::updateViewportInfo check");
    }
}

}
