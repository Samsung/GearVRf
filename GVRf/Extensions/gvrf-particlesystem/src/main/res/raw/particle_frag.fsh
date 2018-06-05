
#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision highp float;
layout(set = 0, binding = 5) uniform sampler2D u_texture;

@MATERIAL_UNIFORMS

layout ( location = 0 ) in float deltaTime;
layout ( location = 0 ) out vec4 outColor;

void main() {

    float opacity = 1.0;

    if ( u_fade == 1.0 )
    {
        opacity = 1.0 - (deltaTime / u_particle_age);
    }

    vec4 color = texture(u_texture, gl_PointCoord);
    outColor = vec4(color.r * u_color.r * opacity * u_color.a, color.g * u_color.g * opacity * u_color.a,
    color.b * u_color.b * opacity * u_color.a, u_color.a * (color.a * opacity));

}
