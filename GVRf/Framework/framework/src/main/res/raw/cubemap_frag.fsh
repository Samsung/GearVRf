
precision highp float;
uniform samplerCube u_texture;
in vec3 diffuse_coord;
out vec4 fragColor;

layout (std140) uniform Material_ubo
{
    vec4 u_opacity;
    vec4 u_color;
};

void main()
{
  vec4 color = texture(u_texture, diffuse_coord);
  fragColor = vec4(color.r * u_color.r * u_opacity.x, color.g * u_color.g * u_opacity.x, color.b * u_color.b * u_opacity.x, color.a * u_opacity.x);
}
