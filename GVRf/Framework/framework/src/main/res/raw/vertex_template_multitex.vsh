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

out vec3 view_direction;
out vec3 viewspace_position;
out vec3 viewspace_normal;
out vec4 local_position;

out vec2 diffuse_coord;
out vec2 opacity_coord;
out vec2 ambient_coord;
out vec2 specular_coord;
out vec2 emissive_coord;
out vec2 normal_coord;
out vec2 lightmap_coord;
out vec2 diffuse_coord1;
out vec2 ambient_coord1;
out vec2 specular_coord1;
out vec2 emissive_coord1;
out vec2 normal_coord1;

//
// The Phong vertex shader supports up to 4 sets of texture coordinates.
// It also supports blending of two textures to compose ambient,
// diffuse, specular, emissive or normal components.
//
#ifdef HAS_a_texcoord1
in vec2 a_texcoord1;
#endif

#ifdef HAS_a_texcoord2
in vec2 a_texcoord2;
#endif

#ifdef HAS_a_texcoord3
in vec2 a_texcoord3;
#endif


struct Vertex
{
	vec4 local_position;
	vec4 local_normal;
	vec3 viewspace_position;
	vec3 viewspace_normal;
	vec3 view_direction;
};

#ifdef HAS_LIGHTSOURCES
//
// This section contains code to compute
// vertex contributions to lighting.
//
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
//
// This section contains code to compute
// vertex contributions to lighting.
//
	LightVertex(vertex);
#endif
#ifdef HAS_TEXCOORDS
//
// This section contains assignment statements from
// input vertex attributes to output shader variables
// generate by GVRShaderTemplate during shader construction.
//
	@TEXCOORDS
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