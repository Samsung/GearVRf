#ifdef HAS_diffuseTexture
layout(set = 1, binding = 10) uniform sampler2D diffuseTexture;
#else
layout(set = 1, binding = 10) uniform sampler2D u_texture;
#endif

@MATERIAL_UNIFORMS

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
	vec4 diffuse = vec4(u_color.x, u_color.y, u_color.z, u_opacity);
#ifdef HAS_LIGHTSOURCES
    diffuse *= diffuse_color;
#endif

#ifdef HAS_diffuseTexture
	diffuse *= texture(diffuseTexture, diffuse_coord.xy);
#else
#ifdef HAS_u_texture
    diffuse *= texture(u_texture, diffuse_coord.xy);
#endif
#endif
    float opacity = diffuse.w;
    diffuse = vec4(diffuse.r * opacity, diffuse.g * opacity, diffuse.b * opacity, opacity);
    return Surface(viewspace_normal, ambient_color, diffuse, specular_color, emissive_color);
}
