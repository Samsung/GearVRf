#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision highp float;
layout(input_attachment_index=0, set = 0, binding = 4) uniform subpassInput u_texture;

layout( location = 0) in vec2 diffuse_coord;
layout( location = 0) out vec4 outColor;

void main()
{
    vec4 tex = subpassLoad(u_texture);
    outColor = tex;
    //outColor = vec4(1.0,0,0,1.0);
}