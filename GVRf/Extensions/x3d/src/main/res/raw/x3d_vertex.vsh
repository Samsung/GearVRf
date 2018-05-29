
#ifdef HAS_a_texcoord
#ifdef HAS_texture_matrix
    vec3 temp = vec3(diffuse_coord, 1);
    temp *= texture_matrix;
    diffuse_coord = temp.xy;
#endif
#endif
