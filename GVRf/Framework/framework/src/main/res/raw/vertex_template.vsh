precision highp float;

#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
flat out int view_id;
#endif

/*
#ifdef HAS_MULTIVIEW
uniform mat4 u_view_[2];
uniform mat4 u_mvp_[2];
uniform mat4 u_mv_[2];
uniform mat4 u_mv_it_[2];
#else
uniform mat4 u_view;
uniform mat4 u_mvp;
uniform mat4 u_mv;
uniform mat4 u_mv_it;
#endif	
uniform mat4 u_model;
*/

layout (std140) uniform Transform_ubo{
 #ifdef HAS_MULTIVIEW
     mat4 u_view_[2];
     mat4 u_mvp_[2];
     mat4 u_mv_[2];
     mat4 u_mv_it_[2];
 #else
     mat4 u_view;
     mat4 u_mvp;
     mat4 u_mv;
     mat4 u_mv_it;
 #endif
     mat4 u_model;
     mat4 u_view_i;
     vec4 u_right;
};

layout (std140) uniform Bones_ubo{
    mat4 u_bone_matrix[60];
};

in vec3 a_position;
in vec2 a_texcoord;

#if defined(HAS_a_normal) && defined(HAS_LIGHTSOURCES)
in vec3 a_normal;
#endif


#ifdef HAS_VertexSkinShader
/*
#ifdef HAS_SHADOWS
//
// shadow mapping uses more uniforms
// so we dont get as many bones
//
uniform mat4 u_bone_matrix[50];
#else
uniform mat4 u_bone_matrix[60];
#endif
*/

in vec4 a_bone_weights;
in ivec4 a_bone_indices;
#endif

#ifdef HAS_VertexNormalShader
in vec3 a_tangent;
in vec3 a_bitangent;
#endif

out vec3 view_direction;
out vec3 viewspace_position;
out vec3 viewspace_normal;
out vec4 local_position;
out vec2 diffuse_coord;
out vec2 ambient_coord;
out vec2 specular_coord;
out vec2 emissive_coord;
out vec2 lightmap_coord;

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
#ifdef HAS_TEXCOORDS
	@TEXCOORDS
#endif
	viewspace_position = vertex.viewspace_position;
	viewspace_normal = vertex.viewspace_normal;
	view_direction = vertex.view_direction;
#ifdef HAS_MULTIVIEW
	gl_Position = u_mvp_[gl_ViewID_OVR] * vertex.local_position;
#else
	gl_Position = u_mvp * vertex.local_position;	
#endif	
}
