@MATERIAL_UNIFORMS

layout ( set = 0, binding = 10 ) uniform sampler2D diffuseTexture;
layout ( set = 0, binding = 11 ) uniform sampler2D ambientTexture;
layout ( set = 0, binding = 12 ) uniform sampler2D specularTexture;
layout ( set = 0, binding = 13 ) uniform sampler2D opacityTexture;
layout ( set = 0, binding = 14 ) uniform sampler2D lightmapTexture;
layout ( set = 0, binding = 15 ) uniform sampler2D normalTexture;
layout ( set = 0, binding = 16 ) uniform sampler2D emissiveTexture;

struct Surface
{
   vec3 viewspaceNormal;
   vec4 ambient;
   vec4 diffuse;
   vec4 specular;
   vec4 emission;
};

Surface @ShaderName()
{
	vec4 diffuse = diffuse_color;
	vec4 emission = emissive_color;
	vec4 specular = specular_color;
	vec4 ambient = ambient_color;
	vec3 viewspaceNormal;

#ifdef HAS_ambientTexture
	ambient *= texture(ambientTexture, ambient_coord.xy);
#endif
#ifdef HAS_diffuseTexture
	diffuse *= texture(diffuseTexture, diffuse_coord.xy);
#endif
#ifdef HAS_opacityTexture
	diffuse.w *= texture(opacityTexture, diffuse_coord.xy).a;
#endif
diffuse.xyz *= diffuse.w;
#ifdef HAS_specularTexture
	specular *= texture(specularTexture, specular_coord.xy);
#endif
#ifdef HAS_emissiveTexture
	emission = texture(emissiveTexture, emissive_coord.xy);
#endif
#ifdef HAS_normalTexture
	viewspaceNormal = texture(normalTexture, normal_coord.xy).xyz * 2.0 - 1.0;
#else
	viewspaceNormal = viewspace_normal;
#endif

#ifdef HAS_lightMapTexture

	vec2 lmap_coord = (lightmap_coord * u_lightMap_scale) + u_lightMap_offset;
	diffuse *= texture(lightMapTexture, vec2(lmap_coord.x, 1 - lmap_coord.y);
	return Surface(viewspaceNormal, ambient, vec4(0.0, 0.0, 0.0, 0.0), specular, diffuse);
#else
	return Surface(viewspaceNormal, ambient, diffuse, specular, emission);
#endif
}
