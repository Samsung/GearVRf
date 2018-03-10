#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

layout ( location = 0 )in vec3 a_position;
layout ( location = 1 )in vec2 a_texcoord;

@MATRIX_UNIFORMS

layout ( location = 0 ) out vec2 diffuse_coord;

void main()
{
    diffuse_coord = a_texcoord;
 #ifdef HAS_MULTIVIEW
     bool render_mask = (u_render_mask & (gl_ViewID_OVR + uint(1))) > uint(0) ? true : false;
     mat4 mvp = u_mvp_[gl_ViewID_OVR];
     if(!render_mask)
         mvp = mat4(0.0);  //  if render_mask is not set for particular eye, dont render that object
     gl_Position = mvp  * vec4(a_position, 1);
 #else
 	gl_Position = u_mvp * vec4(a_position, 1);
 #endif
}