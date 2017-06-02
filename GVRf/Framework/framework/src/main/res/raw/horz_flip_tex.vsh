in vec3 a_position;
in vec2 a_texcoord;
out vec2 diffuse_coord;

layout (std140) uniform Transform_ubo
{
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

void main()
{
  vec4 pos = vec4(a_position, 1.0);
  diffuse_coord = vec2(a_texcoord.x, 1.0 - a_texcoord.y);
  gl_Position = u_mvp * pos;
}