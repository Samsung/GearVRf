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
 * Renders a texture with light.
 ***************************************************************************/

#ifndef LIT_SHADER_H_
#define LIT_SHADER_H_

#include <memory>

#include "GLES3/gl3.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"

#include "objects/recyclable_object.h"

namespace gvr {
class GLProgram;
class RenderData;

class LitShader: public RecyclableObject {
public:
	LitShader();
	~LitShader();
	void recycle();
	void render(const glm::mat4& model_matrix, const glm::mat4& model_it_matrix,
			const glm::mat4& mvp_matrix, RenderData* render_data);

private:
	LitShader(const LitShader& lit_shader);
	LitShader(LitShader&& lit_shader);
	LitShader& operator=(const LitShader& lit_shader);
	LitShader& operator=(LitShader&& lit_shader);

private:
	GLProgram* program_;
	GLuint a_position_;
	GLuint a_tex_coord_;
	GLuint a_normal_;
	GLuint u_mv_;
	GLuint u_mv_it_;
	GLuint u_mvp_;
	GLuint u_light_pos_;
	GLuint u_texture_;
	GLuint u_color_;
	GLuint u_opacity_;
	GLuint u_material_ambient_color_;
	GLuint u_material_diffuse_color_;
	GLuint u_material_specular_color_;
	GLuint u_material_specular_exponent_;
	GLuint u_light_ambient_intensity_;
	GLuint u_light_diffuse_intensity_;
	GLuint u_light_specular_intensity_;
};

}

#endif
