#if defined(HAS_a_bone_indices) && defined(HAS_a_bone_weights)
	vec4 weights = a_bone_weights;
	ivec4 bone_idx = a_bone_indices;
	mat4 bone = u_bone_matrix[bone_idx[0]] * weights[0];
	bone += u_bone_matrix[bone_idx[1]] * weights[1];
	bone += u_bone_matrix[bone_idx[2]] * weights[2];
	bone += u_bone_matrix[bone_idx[3]] * weights[3];
	vertex.local_position = bone * vertex.local_position;
#endif