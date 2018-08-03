
#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision highp float;
layout ( set = 1, binding = 10 ) uniform samplerCube u_texture;
layout ( location = 0 ) in vec3 diffuse_coord;
layout ( location = 0 ) out vec4 fragColor;

@MATERIAL_UNIFORMS

void main()
{
  vec4 color = texture(u_texture, diffuse_coord);
  fragColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
}
