world_position = u_mvp * vec4(a_position, 1.0);
#ifdef HAS_a_normal
   world_normal = (u_mv_it * vec4(a_normal, 1.0)).xyz;
#else
   world_normal = vec3(0, 0, 1);
#endif
gl_Position = u_mvp * vec4(a_position.xyz, 1.0);
#ifdef HAS_a_texcoord
   diffuse_coord = a_texcoord.xy;
#else
   diffuse_coord = vec2(0, 0);
#endif