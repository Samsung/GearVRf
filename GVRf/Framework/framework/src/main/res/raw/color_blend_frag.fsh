#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision highp float;
layout(set = 0, binding = 2) uniform sampler2D u_texture;

@MATERIAL_UNIFORMS

layout(set = 0, binding = 0) in vec2 diffuse_coord;
layout(set = 0, binding = 1) out vec4 outColor;

void main()
{
    vec4 tex = texture(u_texture, diffuse_coord);
    vec3 color = tex.rgb * (1.0 - u_factor) + vec3(u_color.xyz) * u_factor;
    float alpha = tex.a;
    outColor = vec4(color, alpha);
}