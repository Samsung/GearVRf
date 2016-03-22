#ifdef HAS_ambientTexture
   uniform sampler2D ambientTexture;
#endif
#ifdef HAS_diffuseTexture
   uniform sampler2D diffuseTexture;
#endif
#ifdef HAS_specularTexture
   uniform sampler2D specularTexture;
#endif
#ifdef HAS_opacityTexture
   uniform sampler2D opacityTexture;
#endif
#ifdef HAS_lightmapTexture
	uniform sampler2D lightmapTexture;
	uniform vec2 u_lightmap_offset;
	uniform vec2 u_lightmap_scale;
#endif
#ifdef HAS_emissiveTexture
	uniform sampler2D emissiveTexture;
#endif
#ifdef HAS_normalTexture
	uniform sampler2D normalTexture;
#endif

uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;
uniform vec4 emissiveColor;
uniform float specularExponent;

struct Surface
{
   vec4 ambient;
   vec4 diffuse;
   vec4 specular;
   vec4 emission;
};

Surface @ShaderName()
{
	vec4 diffuse = diffuseColor;
	vec4 emission = emissiveColor;
	vec4 specular = specularColor;
	vec4 ambient = ambientColor;

#ifdef HAS_ambientTexture
	ambient *= texture2D(ambientTexture, diffuse_coord.xy);
#endif
#ifdef HAS_diffuseTexture
	diffuse *= texture2D(diffuseTexture, diffuse_coord.xy);
#endif
#ifdef HAS_opacityTexture
	diffuse.w *= texture2D(opacityTexture, diffuse_coord.xy).a;
#endif
#ifdef HAS_specularTexture
	specular *= texture2D(specularTexture, diffuse_coord.xy);
#endif
#ifdef HAS_emissiveTexture
	emission = texture2D(emissiveTexture, diffuse_coord.xy);
#else
	emission = vec4(0.0, 0.0, 0.0, 0.0);
#endif
#ifdef HAS_normalTexture
	world_normal = texture2D(normalTexture, diffuse_coord.xy);
#endif
#ifdef HAS_lightMapTexture
	vec2 lmap_coord = (diffuse_coord * u_lightMap_scale) + u_lightMap_offset;
	diffuse *= texture2D(lightMapTexture, vec2(lmap_coord.x, 1 - lmap_coord.y);
	return Surface(ambient, vec4(0.0, 0.0, 0.0, 0.0), specular, diffuse);
#else
	return Surface(ambient, diffuse, specular, emission);
#endif
}