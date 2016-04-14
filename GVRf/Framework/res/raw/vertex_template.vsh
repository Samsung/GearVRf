
uniform mat4 u_mvp;
uniform mat4 u_mv;
uniform mat4 u_mv_it;
uniform mat4 u_bone_matrix[60];

layout(location = 0) in vec3 a_position;
layout(location = 1) in vec2 a_texcoord;
layout(location = 2) in vec3 a_normal;
in vec4 a_bone_weights;
in ivec4 a_bone_indices;
in vec3 a_tangent;
in vec3 a_bitangent;

out vec2 diffuse_coord;
out vec3 view_direction;
out vec3 viewspace_position;
out vec3 viewspace_normal;

struct Vertex
{
	vec4 local_position;
	vec4 local_normal;
	vec3 viewspace_position;
	vec3 viewspace_normal;
	vec3 view_direction;
};

void main() {
	Vertex vertex;

	vertex.local_position = vec4(a_position.xyz, 1.0);
	vertex.local_normal = vec4(0.0, 0.0, 1.0, 0.0);
	@VertexShader
#ifdef HAS_VertexSkinShader
	@VertexSkinShader
#endif
#ifdef HAS_VertexNormalShader
	@VertexNormalShader
#endif
	viewspace_position = vertex.viewspace_position;
	viewspace_normal = vertex.viewspace_normal;
	view_direction = vertex.view_direction;
	gl_Position = u_mvp * vertex.local_position;
}