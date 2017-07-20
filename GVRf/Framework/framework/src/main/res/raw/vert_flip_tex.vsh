#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

layout ( location = 0 ) in vec3 a_position;
layout ( location = 1 ) in vec2 a_texcoord;
layout ( location = 0 ) out vec2 diffuse_coord;


void main()
{
  gl_Position = vec4(a_position, 1.0);
  diffuse_coord = vec2(a_texcoord.x, 1.0 - a_texcoord.y);
}