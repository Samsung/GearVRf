#extension GL_OES_EGL_image_external : enable
#extension GL_OES_EGL_image_external_essl3 : enable
precision highp float;
uniform samplerExternalOES u_texture;

uniform vec3 u_color;
uniform float u_opacity;

@MATRIX_UNIFORMS

in vec2 diffuse_coord;
void main()
{
    vec2 tex_coord = vec2(0.5 * (diffuse_coord.x + float(u_right.x)), diffuse_coord.y);
    vec4 color = texture(u_texture, tex_coord);
    gl_FragColor = vec4(color.r * u_color.r * u_opacity, color.g * u_color.g * u_opacity, color.b * u_color.b * u_opacity, color.a * u_opacity);
}
