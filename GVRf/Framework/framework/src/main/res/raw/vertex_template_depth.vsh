uniform mat4 u_bone_matrix[60];
uniform mat4 u_model;
uniform mat4 shadow_matrix;
uniform mat4 u_mvp;
uniform mat4 u_view;
uniform mat4 u_mv_it;

in vec3 a_position;
in vec4 a_bone_weights;
in ivec4 a_bone_indices;
out vec4 local_position;
out vec4 proj_position;
struct Vertex
{
	vec4 local_position;
};

void main() {
	Vertex vertex;

	vertex.local_position = vec4(a_position.xyz, 1.0);
	proj_position = u_mvp * vertex.local_position;
	gl_Position = proj_position;
}