vec4 pos = u_mv * vertex.local_position;
vertex.viewspace_position = pos.xyz / pos.w;
#ifdef HAS_a_normal
   vertex.local_normal = vec4(normalize(a_normal), 0.0);
#endif
#ifdef HAS_a_texcoord
   diffuse_coord = a_texcoord.xy;
#endif
vertex.viewspace_normal = normalize((u_mv_it * vertex.local_normal).xyz);
vertex.view_direction = normalize(-vertex.viewspace_position);
