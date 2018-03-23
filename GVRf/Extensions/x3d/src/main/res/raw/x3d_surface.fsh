@MATERIAL_UNIFORMS

layout ( set = 0, binding = 10 ) uniform sampler2D diffuseTexture;

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

#ifndef HAS_LIGHTSOURCES
    diffuse = emission;
#endif
#ifdef HAS_diffuseTexture
    diffuse *= texture(diffuseTexture, diffuse_coord.xy);
#endif
    diffuse.xyz *= diffuse.w;
	viewspaceNormal = viewspace_normal;
	return Surface(viewspaceNormal, ambient, diffuse, specular, emission);
}
