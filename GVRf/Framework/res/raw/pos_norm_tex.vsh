vec4 pos = u_mv * vec4(a_position, 1.0);
viewspace_position = pos.xyz / pos.w;
vec4 normal; 
#ifdef HAS_a_normal
   normal = vec4(normalize(a_normal), 0.0);
#else
   normal = vec4(0, 0, 1, 0);
#endif
viewspace_normal = normalize((u_mv_it * normal).xyz);
gl_Position = u_mvp * vec4(a_position.xyz, 1.0);
#ifdef HAS_a_texcoord
   diffuse_coord = a_texcoord.xy;
#else
   diffuse_coord = vec2(0, 0);
#endif
view_direction = -viewspace_position;