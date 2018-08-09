
#extension GL_ARB_separate_shader_objects : enable
#extension GL_ARB_shading_language_420pack : enable


#ifdef HAS_MULTIVIEW
#extension GL_OVR_multiview2 : enable
layout(num_views = 2) in;
#endif
precision highp float;
precision lowp int;

@MATRIX_UNIFORMS

@MATERIAL_UNIFORMS

layout(location = 0) in vec3 a_position;

#ifdef HAS_a_texcoord
layout(location = 1) in vec2 a_texcoord;
#endif

#if defined(HAS_a_normal) && defined(HAS_LIGHTSOURCES)
layout(location = 2) in vec3 a_normal;
#endif


#ifdef HAS_VertexSkinShader
#ifdef HAS_a_bone_weights
@BONES_UNIFORMS

layout(location = 6) in vec4 a_bone_weights;
layout(location = 7) in ivec4 a_bone_indices;
#endif
#endif

#ifdef HAS_VertexNormalShader
#ifdef HAS_a_tangent
layout(location = 8) in vec3 a_tangent;
layout(location = 9) in vec3 a_bitangent;
layout(location = 7) out mat3 tangent_matrix;
#endif
#endif

layout(location = 0) out vec3 view_direction;
layout(location = 1) out vec3 viewspace_position;
layout(location = 2) out vec3 viewspace_normal;

layout(location = 10) out vec2 diffuse_coord;
layout(location = 11) out vec2 ambient_coord;
layout(location = 12) out vec2 specular_coord;
layout(location = 13) out vec2 emissive_coord;
layout(location = 14) out vec2 lightmap_coord;
layout(location = 15) out vec2 opacity_coord;
layout(location = 16) out vec2 normal_coord;


#ifdef HAS_blendshapeTexture
layout (set = 0, binding = 17) uniform sampler2D blendshapeTexture;
#endif

struct Vertex
{
	vec4 local_position;
	vec4 local_normal;
	vec3 local_tangent;
	vec3 local_bitangent;
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
#ifdef HAS_a_normal
    vertex.local_normal = vec4(normalize(a_normal), 0.0);
#endif
#ifdef HAS_a_tangent
    vertex.local_tangent = a_tangent;
    vertex.local_bitangent = a_bitangent;
#endif
#ifdef HAS_VertexMorphShader
@VertexMorphShader
#endif

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
    bool render_mask = (u_render_mask & (gl_ViewID_OVR + uint(1))) > uint(0) ? true : false;
    mat4 mvp = u_mvp_[gl_ViewID_OVR];
    if (!render_mask)
        mvp = mat4(0.0);  //  if render_mask is not set for particular eye, dont render that object
    gl_Position = mvp  * vertex.local_position;
#else
	gl_Position = u_mvp * vertex.local_position;
#endif	
}
