vec4 pos = u_mv * vertex.local_position;
vertex.viewspace_position = pos.xyz / pos.w;
vertex.view_direction = normalize(-vertex.viewspace_position);
