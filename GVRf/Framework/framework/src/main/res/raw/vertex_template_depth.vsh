

uniform mat4 shadow_matrix;

#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

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
     float u_right;
};

layout (std140) uniform Bones_ubo
{
    mat4 u_bone_matrix[60];
};

in vec3 a_position;
out vec4 proj_position;
struct Vertex
{
	vec4 local_position;
};

void main()
{
	Vertex vertex;

	vertex.local_position = vec4(a_position.xyz, 1.0);
#ifdef HAS_MULTIVIEW
	proj_position = u_mvp_[gl_ViewID_OVR] * vertex.local_position;
#else
	proj_position = u_mvp * vertex.local_position;
#endif
	gl_Position = proj_position;
}