#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision highp float;
layout(set = 0, binding = 2) uniform sampler2D u_texture;

layout(set = 0, binding = 0)  uniform vec3 u_color;
layout(set = 0, binding = 1)  uniform float u_opacity;

layout ( location = 0 ) in vec2 diffuse_coord;
layout ( location = 0 ) out vec4 outColor;

void main()
{
    vec4 color = texture(u_texture, diffuse_coord);
    outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
    outColor = vec4(1,0,0,1);
}