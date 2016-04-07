#if defined(HAS_a_tangent) && defined(HAS_normalTexture) && defined(HAS_a_normal)
   mat3 tbnmtx = mat3(a_tangent, a_bitangent, vertex.local_normal.xyz);
   mat3 wtts = tbnmtx * mat3(u_mv_it);
   vec3 d = wtts * -vertex.viewspace_position;
   vertex.view_direction = normalize(d);
#endif