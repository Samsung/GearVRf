#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable
#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

precision highp float;
layout(location = 0) in vec3 a_position;
layout(location = 1) in vec3 a_normal;

@MATRIX_UNIFORMS

layout(location = 1) out vec3 viewspace_position;
layout(location = 2) out vec3 viewspace_normal;
void main()
{
  mat4 mv, mvp, mv_it;
#ifdef HAS_MULTIVIEW
    bool render_mask = (u_render_mask & (gl_ViewID_OVR + uint(1))) > uint(0) ? true : false;
    mvp = u_mvp_[gl_ViewID_OVR];
    mv  = u_mv_[gl_ViewID_OVR];
    mv_it = u_mv_it_[gl_ViewID_OVR];
    if(!render_mask){
        mvp = mat4(0.0);  //  if render_mask is not set for particular eye, dont render that object
    }
#else
   mvp = u_mvp;
   mv  = u_mv;
   mv_it = u_mv_it;
#endif
  vec4 v_viewspace_position_vec4 = mv * vec4(a_position,1.0);
  viewspace_position = v_viewspace_position_vec4.xyz / v_viewspace_position_vec4.w;
  viewspace_normal = (mv_it * vec4(a_normal, 1.0)).xyz;
  gl_Position = mvp * vec4(a_position, 1.0);
 }