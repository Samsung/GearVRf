#version 300 es
precision highp float;
uniform sampler2D u_texture;

layout (std140) uniform Material_ubo
{
   vec3 u_color;
   float u_factor;
};

in vec2 diffuse_coord;
out vec4 outColor;

void main()
{
    vec4 tex = texture(u_texture, diffuse_coord);
    vec3 color = tex.rgb * (1.0 - u_factor) + vec3(u_color.x, u_color.y, u_color.z) * u_factor;
    float alpha = tex.a;
    outColor = vec4(color, alpha);
}