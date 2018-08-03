#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
#endif
precision highp float;
layout( set = 1, binding = 10 ) uniform mediump samplerCube u_texture;

@MATERIAL_UNIFORMS

@MATRIX_UNIFORMS

layout(location = 1) in vec3 viewspace_position;
layout(location = 2) in vec3 viewspace_normal;
layout(location = 0) out vec4 outColor;

void main()
{
  vec3 v_reflected_position = reflect(viewspace_position, normalize(viewspace_normal));
  mat4 view_i;
#ifdef HAS_MULTIVIEW
  view_i = u_view_i_[gl_ViewID_OVR];
#else
  view_i = u_view_i;
#endif

  vec3 v_tex_coord = (view_i * vec4(v_reflected_position, 1.0)).xyz;
  v_tex_coord.z = -v_tex_coord.z;
  vec4 color = texture(u_texture, v_tex_coord.xyz);
  outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
}
