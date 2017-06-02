precision highp float;
uniform sampler2D u_texture;

layout (std140) uniform Material_ubo
{
    vec3 u_color;
    float u_opacity;
};

layout (std140) uniform Transform_ubo
{
    mat4 u_mvp;
    vec4 u_right;
};

in vec2 diffuse_coord;
out vec4 outColor;

void main()
{
    vec2 tex_coord = vec2(0.5 * (diffuse_coord.x + float(u_right.x)), diffuse_coord.y);
    vec4 color = texture(u_texture, tex_coord);
    outColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
}
