#ifdef HAS_MULTIVIEW
vec4 pos = u_mv_[gl_ViewID_OVR] * vertex.local_position;
#else
vec4 pos = u_mv * vertex.local_position;
#endif

vertex.viewspace_position = pos.xyz / pos.w;
vertex.view_direction = normalize(-vertex.viewspace_position);
