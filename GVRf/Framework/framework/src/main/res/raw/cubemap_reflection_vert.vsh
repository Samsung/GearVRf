
precision highp float;
in vec3 a_position;
in vec3 a_normal;
layout (std140) uniform Transform_ubo{
 #ifdef HAS_MULTIVIEW
     mat4 u_view_[2];
     mat4 u_mvp_[2];
     mat4 u_mv_[2];
     mat4 u_mv_it_[2];
 #else
     mat4 u_view;
     mat4 u_mvp;
     mat4 u_mv;
     mat4 u_mv_it;
 #endif
     mat4 u_model;
     mat4 u_view_i;
     vec4 u_right;
};

out vec3 v_viewspace_position;
out vec3 v_viewspace_normal;
void main() {
  vec4 v_viewspace_position_vec4 = u_mv * a_position;
  v_viewspace_position = v_viewspace_position_vec4.xyz / v_viewspace_position_vec4.w;
  v_viewspace_normal = (u_mv_it * vec4(a_normal, 1.0)).xyz;
  gl_Position = u_mvp * vec4(a_position, 1.0);
 }