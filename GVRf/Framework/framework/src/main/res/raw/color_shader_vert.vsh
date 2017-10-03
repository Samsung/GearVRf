#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

precision mediump float;
layout ( location = 0 ) in vec3 a_position;
@MATRIX_UNIFORMS

void main()
{
   #ifdef HAS_MULTIVIEW
       bool render_mask = (u_render_mask & (gl_ViewID_OVR + uint(1))) > uint(0) ? true : false;
       mat4 mvp = u_mvp_[gl_ViewID_OVR];
       gl_Position = mvp  * vec4(a_position, 1);
   #else
   	    gl_Position =  u_mvp * vec4(a_position, 1);
   #endif
}