#ifdef HAS_diffuseTexture
uniform sampler2D diffuseTexture;
#else
uniform sampler2D u_texture;
#endif

layout (std140) uniform Material_ubo
{
    vec4 ambient_color;
    vec4 diffuse_color;
    vec4 specular_color;
    vec4 emissive_color;
    vec3 u_color;
    float u_opacity;
    float specular_exponent;
    float line_width;
};

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
