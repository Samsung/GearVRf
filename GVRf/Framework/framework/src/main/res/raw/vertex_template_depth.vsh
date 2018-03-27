
#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable

#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif
precision highp float;

uniform mat4 shadow_matrix;

@MATRIX_UNIFORMS

#ifdef HAS_VertexSkinShader
#ifdef HAS_a_bone_weights
@BONES_UNIFORMS

layout(location = 6) in vec4 a_bone_weights;
layout(location = 7) in ivec4 a_bone_indices;
#endif
#endif

layout(location = 0) in vec3 a_position;
layout(location = 0) out vec4 proj_position;
struct Vertex
{
	vec4 local_position;
};

void main()
{
	Vertex vertex;

	vertex.local_position = vec4(a_position.xyz, 1.0);
#ifdef HAS_VertexSkinShader
    @VertexSkinShader
#endif
#ifdef HAS_MULTIVIEW
	proj_position = u_mvp_[gl_ViewID_OVR] * vertex.local_position;
#else
	proj_position = u_mvp * vertex.local_position;
#endif
	gl_Position = proj_position;
}