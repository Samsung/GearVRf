#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

layout ( location = 0 ) in vec3 a_position;
layout ( location = 1 ) in vec2 a_texcoord;
layout ( location = 0 ) out vec2 diffuse_coord;

@MATRIX_UNIFORMS

void main()
{
  mat4 mvp = u_mvp;

  //generate right eye mvp from left
  mvp[3][0] = mvp[3][0] - (u_proj_offset * float(u_right));

  gl_Position = mvp * vec4(a_position, 1.0);
  diffuse_coord = vec2(a_texcoord.x, 1.0 - a_texcoord.y);
}
