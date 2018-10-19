@MATERIAL_UNIFORMS

layout(location = 3) in vec4 vertex_color;

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
	vec4 diffuse = diffuse_color * vertex_color;
	vec4 emission = emissive_color;
	vec4 specular = specular_color;
	vec4 ambient = ambient_color;
	vec3 viewspaceNormal;

    diffuse.xyz *= diffuse.w;
	viewspaceNormal = viewspace_normal;
	return Surface(viewspaceNormal, ambient, diffuse, specular, emission);

}
