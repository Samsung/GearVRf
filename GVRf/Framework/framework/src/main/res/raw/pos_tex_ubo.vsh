#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif

in vec3 a_position;
in vec2 a_texcoord;

@MATRIX_UNIFORMS

out vec2 diffuse_coord;

void main()
{
    diffuse_coord = a_texcoord;
 #ifdef HAS_MULTIVIEW
 	gl_Position = u_mvp_[gl_ViewID_OVR] * vec4(a_position,1.0);
 #else
 	gl_Position = u_mvp * vec4(a_position,1.0);
 #endif
}