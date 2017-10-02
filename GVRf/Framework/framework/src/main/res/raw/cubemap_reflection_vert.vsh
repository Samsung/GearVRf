#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
precision highp float;
layout(location = 0) in vec3 a_position;
layout(location = 1) in vec3 a_normal;

@MATRIX_UNIFORMS

layout(location = 1) out vec3 viewspace_position;
layout(location = 2) out vec3 viewspace_normal;
void main()
{
  vec4 v_viewspace_position_vec4 = u_mv * vec4(a_position,1.0);
  viewspace_position = v_viewspace_position_vec4.xyz / v_viewspace_position_vec4.w;
  viewspace_normal = (u_mv_it * vec4(a_normal, 1.0)).xyz;
  gl_Position = u_mvp * vec4(a_position, 1.0);
 }