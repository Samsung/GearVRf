uniform mat4 u_mvp;
uniform mat4 u_mv;
uniform mat4 u_mv_it;
uniform vec3 u_eye_pos;

attribute vec3 a_position;
#ifdef HAS_a_normal
    attribute vec3 a_normal;
#endif
#ifdef HAS_a_texcoord
    attribute vec2 a_texcoord;
#endif
#ifdef HAS_a_tangent
	attribute vec3 a_tangent;
	attribute vec3 a_bitangent;
#endif

varying vec2 diffuse_coord;
varying vec3 view_direction;
varying vec3 viewspace_position;
varying vec3 viewspace_normal;

struct Vertex
{
	vec4 local_position;
	vec4 local_normal;
	vec3 viewspace_position;
	vec3 viewspace_normal;
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
	view_direction = normalize(-viewspace_position);
	gl_Position = u_mvp * vertex.local_position;
}