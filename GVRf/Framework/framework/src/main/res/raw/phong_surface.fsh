
uniform sampler2D ambientTexture;
uniform sampler2D diffuseTexture;
uniform sampler2D specularTexture;
uniform sampler2D opacityTexture;
uniform sampler2D lightmapTexture;
uniform sampler2D emissiveTexture;
uniform sampler2D normalTexture;

uniform vec4 ambient_color;
uniform vec4 diffuse_color;
uniform vec4 specular_color;
uniform vec4 emissive_color;
uniform float specular_exponent;
uniform vec2 u_lightmap_offset;
uniform vec2 u_lightmap_scale;

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
	ambient *= texture(ambientTexture, diffuse_coord.xy);
#endif
#ifdef HAS_diffuseTexture
	diffuse *= texture(diffuseTexture, diffuse_coord.xy);
#endif
#ifdef HAS_opacityTexture
	diffuse.w *= texture(opacityTexture, diffuse_coord.xy).a;
#endif
diffuse.xyz *= diffuse.w;
#ifdef HAS_specularTexture
	specular *= texture(specularTexture, diffuse_coord.xy);
#endif
#ifdef HAS_emissiveTexture
	emission = texture(emissiveTexture, diffuse_coord.xy);
#else
	emission = vec4(0.0, 0.0, 0.0, 0.0);
#endif
#ifdef HAS_normalTexture
	viewspaceNormal = texture(normalTexture, diffuse_coord.xy).xyz * 2.0 - 1.0;
#else
	viewspaceNormal = viewspace_normal;
#endif

#ifdef HAS_lightMapTexture
	vec2 lmap_coord = (diffuse_coord * u_lightMap_scale) + u_lightMap_offset;
	diffuse *= texture(lightMapTexture, vec2(lmap_coord.x, 1 - lmap_coord.y);
	return Surface(viewspaceNormal, ambient, vec4(0.0, 0.0, 0.0, 0.0), specular, diffuse);
#else
	return Surface(viewspaceNormal, ambient, diffuse, specular, emission);
#endif
}
