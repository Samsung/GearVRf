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
 * Horizontally flips the scene
 ***************************************************************************/

#ifndef SHADOW_SHADER_H_
#define SHADOW_SHADER_H_

#include <memory>
#include <vector>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"

#include "glm/gtc/type_ptr.hpp"

//#include "objects/recyclable_object.h"
//#include "objects/components/render_data.h"
#include "gl/gl_texture.h"

#include "objects/components/camera.h"
#include "objects/components/directional_light.h"
#include "engine/memory/gl_delete.h"

namespace gvr {
class GLProgram;
class RenderTexture;
class PostEffectData;
class RenderData;
class Material;

class ShadowShader // : RecyclableObject
{
public:

    enum RENDER_MODE {
        RENDER_DEFAULT = 0,
        RENDER_FROM_LIGHT = 1,
        RENDER_FROM_CAMERA = 2,
        RENDER_WITH_SHADOW = 3
    };

    ShadowShader();
    ~ShadowShader();
    void recycle();

    void render(const glm::mat4& mvp_matrix_cam,
            const glm::mat4& mvp_matrix_light_cam, const glm::mat4& mv_matrix,
            const glm::mat4& mv_it_matrix, const glm::mat4& mv_it_cam,
            const glm::mat4& mv_it_model,
            //const glm::mat4& mvp_matrix_light,
            glm::vec3 light_position, RenderData* render_data,
            Material* material, int mode);

    GLuint getFBOFromLight() {
        return fbo_light;
    }

    GLuint getFBOFromCamera() {
        return fbo_camera;
    }

    void setCameraLight(DirectionalLight* camera) {
        cameraLight = camera;
    }

    DirectionalLight* getCameraLight() {
        return cameraLight;
    }

    void updateViewportInfo(int width, int height);

private:
    ShadowShader(const ShadowShader& shadow_shader);
    ShadowShader(ShadowShader&& shadow_shader);
    ShadowShader& operator=(
            const ShadowShader& shadow_shader);
    ShadowShader& operator=(
            ShadowShader&& shadow_shader);

    GLuint getFBO(GLTexture* color, GLTexture* depth, int width, int height);

private:
    GLProgram* program_;
    GLuint a_position_;
    GLuint a_tex_coord_;
    GLuint u_texture_;

    GLuint u_texture_shadow_map_;
    GLuint u_texture_depth_map_;
    GLuint u_mvp_;
    GLuint u_matrix_bias_;
    GLuint u_matrix_light_view_projection_;
    GLuint u_depth_offset_;

    // add vertex array object
    GLuint vaoID_;

    GLuint fbo_light = 0;
    GLuint fbo_camera = 0;

    GLTexture* texture_light_depth;
    GLTexture* texture_light_color;
    GLTexture* texture_camera_depth;
    GLTexture* texture_camera_color;
    DirectionalLight* cameraLight;
    GLsizei viewportWidth, viewportHeight;

    GlDelete* deleter_;
};

}

#endif
