precision highp float;
uniform sampler2D u_texture;

layout (std140) uniform Material_ubo
{
    vec3 u_color;
    float u_opacity;
};

in vec2 diffuse_coord;
out vec4 outColor;

void main()
{
    vec4 color = texture(u_texture, diffuse_coord);
    outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
    outColor = vec4(1,0,0,1);
}