#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision highp float;
//layout(input_attachment_index=0, set = 0, binding = 4) uniform subpassInput u_texture;
layout(set = 0, binding = 4) uniform sampler2D u_texture;

layout (push_constant) uniform Material_ubo{

            vec3 u_ratio_r;
            vec3 u_ratio_g;
            vec3 u_ratio_b;
             };

layout( location = 0) in vec2 diffuse_coord;
layout( location = 0) out vec4 outColor;

void main()
{
    //vec4 tex = subpassLoad(u_texture);
    vec4 tex = texture(u_texture, diffuse_coord);

    float r = tex.r * u_ratio_r.r + tex.g * u_ratio_r.g + tex.b * u_ratio_r.b;
                  float g = tex.r * u_ratio_g.r + tex.g * u_ratio_g.g + tex.b * u_ratio_g.b;
                 float b = tex.r * u_ratio_b.r + tex.g * u_ratio_b.g + tex.b * u_ratio_b.b;


    outColor = vec4(r,g,b,1.0);
}