
#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
uniform mat4 u_view_[2];
uniform mat4 u_mvp_[2];
uniform mat4 u_mv_[2];
uniform mat4 u_mv_it_[2];
flat out int view_id;
#else
uniform mat4 u_view;
uniform mat4 u_mvp;
uniform mat4 u_mv;
uniform mat4 u_mv_it;
#endif	

uniform mat4 u_model;
in vec3 a_position;
in vec2 a_texcoord;
in vec3 a_normal;

#ifdef HAS_VertexSkinShader
#ifdef HAS_SHADOWS
//
// shadow mapping uses more uniforms
// so we dont get as many bones
//
uniform mat4 u_bone_matrix[50];
#else
uniform mat4 u_bone_matrix[60];
#endif
in vec4 a_bone_weights;
in ivec4 a_bone_indices;
#endif

#ifdef HAS_VertexNormalShader
in vec3 a_tangent;
in vec3 a_bitangent;
#endif

out vec2 diffuse_coord;
out vec3 view_direction;
out vec3 viewspace_position;
out vec3 viewspace_normal;
out vec4 local_position;

struct Vertex
{
	vec4 local_position;
	vec4 local_normal;
	vec3 viewspace_position;
	vec3 viewspace_normal;
	vec3 view_direction;
};

#ifdef HAS_LIGHTSOURCES
	@LIGHTSOURCES
#endif
	
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
#ifdef HAS_LIGHTSOURCES
	LightVertex(vertex);
#endif

	viewspace_position = vertex.viewspace_position;
	viewspace_normal = vertex.viewspace_normal;
	view_direction = vertex.view_direction;
#ifdef HAS_MULTIVIEW
	view_id = int(gl_ViewID_OVR);
	gl_Position = u_mvp_[gl_ViewID_OVR] * vertex.local_position;
#else
	gl_Position = u_mvp * vertex.local_position;	
#endif	
}