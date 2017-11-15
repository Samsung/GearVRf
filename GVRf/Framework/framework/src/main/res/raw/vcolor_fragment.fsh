#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

precision mediump float;

layout ( location = 0 ) in vec4 v_color;

out vec4 fragColor;

void main()
{
  fragColor = v_color;
}
