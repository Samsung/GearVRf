#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

layout ( location = 0 )in vec3 a_position;
layout ( location = 1 )in vec2 a_texcoord;

@MATRIX_UNIFORMS

uniform highp float texelWidth;
uniform highp float texelHeight;

layout ( location = 0 ) out vec2 textureCoordinate;
layout ( location = 1 ) out vec2 leftTextureCoordinate;
layout ( location = 2 ) out vec2 rightTextureCoordinate;

layout ( location = 3 ) out vec2 topTextureCoordinate;
layout ( location = 4 ) out vec2 topLeftTextureCoordinate;
layout ( location = 5 ) out vec2 topRightTextureCoordinate;

layout ( location = 6 ) out vec2 bottomTextureCoordinate;
layout ( location = 7 ) out vec2 bottomLeftTextureCoordinate;
layout ( location = 8 ) out vec2 bottomRightTextureCoordinate;

void main()
{
 #ifdef HAS_MULTIVIEW
     bool render_mask = (u_render_mask & (gl_ViewID_OVR + uint(1))) > uint(0) ? true : false;
     mat4 mvp = u_mvp_[gl_ViewID_OVR];
     if(!render_mask)
         mvp = mat4(0.0);  //  if render_mask is not set for particular eye, dont render that object
     gl_Position = mvp  * vec4(a_position, 1);
 #else
 	gl_Position = u_mvp * vec4(a_position, 1);
 #endif

     vec2 widthStep = vec2(texelWidth, 0.0);
     vec2 heightStep = vec2(0.0, texelHeight);
     vec2 widthHeightStep = vec2(texelWidth, texelHeight);
     vec2 widthNegativeHeightStep = vec2(texelWidth, -texelHeight);

     textureCoordinate = a_texcoord.xy;
     leftTextureCoordinate = a_texcoord.xy - widthStep;
     rightTextureCoordinate = a_texcoord.xy + widthStep;

     topTextureCoordinate = a_texcoord.xy - heightStep;
     topLeftTextureCoordinate = a_texcoord.xy - widthHeightStep;
     topRightTextureCoordinate = a_texcoord.xy + widthNegativeHeightStep;

     bottomTextureCoordinate = a_texcoord.xy + heightStep;
     bottomLeftTextureCoordinate = a_texcoord.xy - widthNegativeHeightStep;
     bottomRightTextureCoordinate = a_texcoord.xy + widthHeightStep;
}