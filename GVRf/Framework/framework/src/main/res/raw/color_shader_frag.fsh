#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision mediump float;

@MATERIAL_UNIFORMS

layout (location = 0) out vec4 outColor;

void main()
{
    outColor = vec4(u_color.x, u_color.y, u_color.z, 1);
}